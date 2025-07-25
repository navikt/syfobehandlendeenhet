package no.nav.syfo.infrastructure.cronjob

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.domain.EnhetId.Companion.ENHETNR_NAV_UTLAND
import no.nav.syfo.infrastructure.cache.ValkeyStore
import no.nav.syfo.infrastructure.client.azuread.AzureAdClient
import no.nav.syfo.infrastructure.client.norg.NorgClient
import no.nav.syfo.infrastructure.client.pdl.PdlClient
import no.nav.syfo.infrastructure.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.infrastructure.client.syfooversiktsrv.SyfooversiktsrvClient
import no.nav.syfo.infrastructure.database.repository.EnhetRepository
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ENHET_ID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.time.OffsetDateTime
import java.util.*

class ReturnToSenderIfNoVeilederCronjobTest {

    private val externalMockEnvironment = ExternalMockEnvironment.instance
    private val database = externalMockEnvironment.database
    private val enhetRepository = EnhetRepository(database)
    private val cache = mockk<ValkeyStore>(relaxed = true)
    private val skjermedePersonerPipClient = mockk<SkjermedePersonerPipClient>(relaxed = true)

    private val enhetService = EnhetService(
        norgClient = mockk<NorgClient>(relaxed = true),
        pdlClient = mockk<PdlClient>(relaxed = true),
        valkeyStore = cache,
        skjermedePersonerPipClient = skjermedePersonerPipClient,
        repository = enhetRepository,
        behandlendeEnhetProducer = mockk<BehandlendeEnhetProducer>(relaxed = true),
    )
    private val valkeyConfig = externalMockEnvironment.environment.valkeyConfig
    private val valkeyStore = ValkeyStore(
        JedisPool(
            JedisPoolConfig(),
            HostAndPort(valkeyConfig.host, valkeyConfig.port),
            DefaultJedisClientConfig.builder()
                .ssl(valkeyConfig.ssl)
                .password(valkeyConfig.valkeyPassword)
                .build()
        )
    )
    private val azureAdClient = AzureAdClient(
        azureAppClientId = externalMockEnvironment.environment.azureAppClientId,
        azureAppClientSecret = externalMockEnvironment.environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = externalMockEnvironment.environment.azureOpenidConfigTokenEndpoint,
        valkeyStore = valkeyStore,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    private val syfooversiktsrvClient = SyfooversiktsrvClient(
        azureAdClient = azureAdClient,
        baseUrl = externalMockEnvironment.environment.syfooversiktsrvUrl,
        clientId = externalMockEnvironment.environment.syfooversiktsrvClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    private val returnToSenderIfNoVeilederCronjob = ReturnToSenderIfNoVeilederCronjob(
        enhetService = enhetService,
        repository = enhetRepository,
        syfooversiktsrvClient = syfooversiktsrvClient,
    )

    @BeforeEach
    fun beforeEach() {
        database.dropData()
        clearAllMocks()
    }

    @Test
    fun `Cronjob fungerer på tom database`() {
        runBlocking {
            returnToSenderIfNoVeilederCronjob.run()
        }
    }

    @Test
    fun `Cronjob oppdaterer veileder_checked_ok_at for arbeidstaker som har veileder`() {
        every { cache.get(any()) } returns null
        val callId = UUID.randomUUID().toString()
        runBlocking {
            val oppfolgingsenhet = enhetService.updateOppfolgingsenhet(
                callId = callId,
                personIdent = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                enhetId = EnhetId(ENHET_ID),
            )!!

            returnToSenderIfNoVeilederCronjob.run()

            val storedEnhet = enhetService.arbeidstakersBehandlendeEnhet(
                callId = callId,
                personIdentNumber = UserConstants.ARBEIDSTAKER_PERSONIDENT,
            )
            assertEquals(ENHET_ID, storedEnhet.oppfolgingsenhet?.enhet?.enhetId?.value)
            assertNotNull(database.getVeilederCheckedOk(oppfolgingsenhet.uuid))
        }
    }

    @Test
    fun `Cronjob oppdaterer ikke veileder_checked_ok_at for arbeidstaker som mangler veileder`() {
        every { cache.get(any()) } returns null
        val callId = UUID.randomUUID().toString()
        runBlocking {
            val oppfolgingsenhet = enhetService.updateOppfolgingsenhet(
                callId = callId,
                personIdent = UserConstants.ARBEIDSTAKER_PERSONIDENT_2,
                enhetId = EnhetId(ENHET_ID),
            )!!

            returnToSenderIfNoVeilederCronjob.run()

            val storedEnhet = enhetService.arbeidstakersBehandlendeEnhet(
                callId = callId,
                personIdentNumber = UserConstants.ARBEIDSTAKER_PERSONIDENT_2,
            )
            assertEquals(ENHET_ID, storedEnhet.oppfolgingsenhet?.enhet?.enhetId?.value)
            assertNull(database.getVeilederCheckedOk(oppfolgingsenhet.uuid))
        }
    }

    @Test
    fun `Cronjob nuller ut oppfolgningsenhet for arbeidstaker som ikke har fått veileder etter 7 dager`() {
        every { cache.get(any()) } returns null
        val callId = UUID.randomUUID().toString()
        runBlocking {
            val oppfolgingsenhet = enhetService.updateOppfolgingsenhet(
                callId = callId,
                personIdent = UserConstants.ARBEIDSTAKER_PERSONIDENT_2,
                enhetId = EnhetId(ENHET_ID),
            )!!
            database.setCreatedAt(
                uuid = oppfolgingsenhet.uuid,
                datetime = OffsetDateTime.now().minusDays(8),
            )

            returnToSenderIfNoVeilederCronjob.run()

            assertNull(database.getVeilederCheckedOk(oppfolgingsenhet.uuid))
            val storedEnhet = enhetService.arbeidstakersBehandlendeEnhet(
                callId = callId,
                personIdentNumber = UserConstants.ARBEIDSTAKER_PERSONIDENT_2,
            )
            assertNull(storedEnhet.oppfolgingsenhet?.enhet?.enhetId?.value)
        }
    }

    @Test
    fun `Cronjob nuller ikke ut oppfolgningsenhet for arbeidstaker som ikke har fått veileder etter 7 dager hvis Nav Utland`() {
        every { cache.get(any()) } returns null
        val callId = UUID.randomUUID().toString()
        runBlocking {
            val oppfolgingsenhet = enhetService.updateOppfolgingsenhet(
                callId = callId,
                personIdent = UserConstants.ARBEIDSTAKER_PERSONIDENT_2,
                enhetId = EnhetId(ENHETNR_NAV_UTLAND),
            )!!
            database.setCreatedAt(
                uuid = oppfolgingsenhet.uuid,
                datetime = OffsetDateTime.now().minusDays(8),
            )

            returnToSenderIfNoVeilederCronjob.run()

            assertNull(database.getVeilederCheckedOk(oppfolgingsenhet.uuid))
            val storedEnhet = enhetService.arbeidstakersBehandlendeEnhet(
                callId = callId,
                personIdentNumber = UserConstants.ARBEIDSTAKER_PERSONIDENT_2,
            )
            assertEquals(ENHETNR_NAV_UTLAND, storedEnhet.oppfolgingsenhet?.enhet?.enhetId?.value)
        }
    }
}

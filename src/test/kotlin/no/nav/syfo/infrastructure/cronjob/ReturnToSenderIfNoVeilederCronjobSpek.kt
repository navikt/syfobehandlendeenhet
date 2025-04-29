package no.nav.syfo.infrastructure.cronjob

import io.mockk.*
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
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.UserConstants.ENHET_ID
import no.nav.syfo.testhelper.dropData
import no.nav.syfo.testhelper.getVeilederCheckedOk
import no.nav.syfo.testhelper.setCreatedAt
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.time.OffsetDateTime
import java.util.*

class ReturnToSenderIfNoVeilederCronjobSpek : Spek({

    describe(ReturnToSenderIfNoVeilederCronjobSpek::class.java.simpleName) {
        val externalMockEnvironment = ExternalMockEnvironment.instance
        val database = externalMockEnvironment.database
        val enhetRepository = EnhetRepository(database)
        val cache = mockk<ValkeyStore>(relaxed = true)
        val skjermedePersonerPipClient = mockk<SkjermedePersonerPipClient>(relaxed = true)

        val enhetService = EnhetService(
            norgClient = mockk<NorgClient>(relaxed = true),
            pdlClient = mockk<PdlClient>(relaxed = true),
            valkeyStore = cache,
            skjermedePersonerPipClient = skjermedePersonerPipClient,
            repository = enhetRepository,
            behandlendeEnhetProducer = mockk<BehandlendeEnhetProducer>(relaxed = true),
        )
        val valkeyConfig = externalMockEnvironment.environment.valkeyConfig
        val valkeyStore = ValkeyStore(
            JedisPool(
                JedisPoolConfig(),
                HostAndPort(valkeyConfig.host, valkeyConfig.port),
                DefaultJedisClientConfig.builder()
                    .ssl(valkeyConfig.ssl)
                    .password(valkeyConfig.valkeyPassword)
                    .build()
            )
        )
        val azureAdClient = AzureAdClient(
            azureAppClientId = externalMockEnvironment.environment.azureAppClientId,
            azureAppClientSecret = externalMockEnvironment.environment.azureAppClientSecret,
            azureOpenidConfigTokenEndpoint = externalMockEnvironment.environment.azureOpenidConfigTokenEndpoint,
            valkeyStore = valkeyStore,
            httpClient = externalMockEnvironment.mockHttpClient,
        )
        val syfooversiktsrvClient = SyfooversiktsrvClient(
            azureAdClient = azureAdClient,
            baseUrl = externalMockEnvironment.environment.syfooversiktsrvUrl,
            clientId = externalMockEnvironment.environment.syfooversiktsrvClientId,
            httpClient = externalMockEnvironment.mockHttpClient,
        )

        val returnToSenderIfNoVeilederCronjob = ReturnToSenderIfNoVeilederCronjob(
            enhetService = enhetService,
            repository = enhetRepository,
            syfooversiktsrvClient = syfooversiktsrvClient,
        )

        beforeEachTest {
            database.dropData()
            clearAllMocks()
        }

        describe("Cronjob nuller ut oppfolgningsenhet") {
            it("Cronjob fungerer på tom database") {
                runBlocking {
                    returnToSenderIfNoVeilederCronjob.run()
                }
            }
            it("Cronjob oppdaterer veileder_checked_ok_at for arbeidstaker som har veileder") {
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
                    storedEnhet.oppfolgingsenhet?.enhetId shouldBeEqualTo ENHET_ID
                    database.getVeilederCheckedOk(oppfolgingsenhet.uuid) shouldNotBe null
                }
            }
            it("Cronjob oppdaterer ikke veileder_checked_ok_at for arbeidstaker som mangler veileder") {
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
                    storedEnhet.oppfolgingsenhet?.enhetId shouldBeEqualTo ENHET_ID
                    database.getVeilederCheckedOk(oppfolgingsenhet.uuid) shouldBe null
                }
            }
            it("Cronjob nuller ut oppfolgningsenhet for arbeidstaker som ikke har fått veileder etter 7 dager") {
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

                    val storedEnhet = enhetService.arbeidstakersBehandlendeEnhet(
                        callId = callId,
                        personIdentNumber = UserConstants.ARBEIDSTAKER_PERSONIDENT_2,
                    )
                    storedEnhet.oppfolgingsenhet?.enhetId shouldBe null
                }
            }
            it("Cronjob nuller ikke ut oppfolgningsenhet for arbeidstaker som ikke har fått veileder etter 7 dager hvis Nav Utland") {
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

                    val storedEnhet = enhetService.arbeidstakersBehandlendeEnhet(
                        callId = callId,
                        personIdentNumber = UserConstants.ARBEIDSTAKER_PERSONIDENT_2,
                    )
                    storedEnhet.oppfolgingsenhet?.enhetId shouldBeEqualTo ENHETNR_NAV_UTLAND
                }
            }
        }
    }
})

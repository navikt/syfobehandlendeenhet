package no.nav.syfo.infrastructure.cronjob

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.infrastructure.cache.ValkeyStore
import no.nav.syfo.infrastructure.client.norg.NorgClient
import no.nav.syfo.infrastructure.client.pdl.PdlClient
import no.nav.syfo.infrastructure.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.infrastructure.database.repository.EnhetRepository
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.dropData
import no.nav.syfo.testhelper.setSkjermingCheckedAt
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

class RemoveOppfolgingsenhetCronjobTest {

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

    private val removeOppfolgingsenhetCronjob = RemoveOppfolgingsenhetCronjob(
        enhetService = enhetService,
        repository = enhetRepository,
    )

    @BeforeEach
    fun beforeEach() {
        database.dropData()
        clearAllMocks()
    }

    @Test
    fun `Cronjob fungerer på tom database`() {
        runBlocking {
            removeOppfolgingsenhetCronjob.run()
        }
    }

    @Test
    fun `Cronjob nuller ikke oppfolgingsenhet på person uten skjerming`() {
        every { cache.get(any()) } returns null
        val callId = UUID.randomUUID().toString()
        runBlocking {
            enhetService.updateOppfolgingsenhet(
                callId = callId,
                personIdent = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                enhetId = EnhetId(EnhetId.ENHETNR_NAV_UTLAND),
            )

            removeOppfolgingsenhetCronjob.run()

            val storedEnhet = enhetService.arbeidstakersBehandlendeEnhet(
                callId = callId,
                personIdentNumber = UserConstants.ARBEIDSTAKER_PERSONIDENT,
            )
            assertEquals(EnhetId.ENHETNR_NAV_UTLAND, storedEnhet.oppfolgingsenhet?.enhet?.enhetId?.value)
        }
    }

    @Test
    fun `Cronjob nuller oppfolgingsenhet på person med skjerming`() {
        every { cache.get(any()) } returns null
        val callId = UUID.randomUUID().toString()
        runBlocking {
            val oppfolgingsenhet = enhetService.updateOppfolgingsenhet(
                callId = callId,
                personIdent = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                enhetId = EnhetId(EnhetId.ENHETNR_NAV_UTLAND),
            )
            assertNotNull(oppfolgingsenhet)

            coEvery { skjermedePersonerPipClient.isSkjermet(any(), any(), any()) } returns true
            database.setSkjermingCheckedAt(oppfolgingsenhet!!.uuid, OffsetDateTime.now().minusDays(2))

            removeOppfolgingsenhetCronjob.run()

            val storedEnhet = enhetService.arbeidstakersBehandlendeEnhet(
                callId = callId,
                personIdentNumber = UserConstants.ARBEIDSTAKER_PERSONIDENT,
            )
            assertNotEquals(EnhetId.ENHETNR_NAV_UTLAND, storedEnhet.oppfolgingsenhet?.enhet?.enhetId?.value)
        }
    }
}

package no.nav.syfo.infrastructure.cronjob

import io.mockk.*
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
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.amshove.kluent.shouldNotBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.OffsetDateTime
import java.util.*

class RemoveOppfolgingsenhetCronjobSpek : Spek({

    describe(RemoveOppfolgingsenhetCronjobSpek::class.java.simpleName) {
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

        val removeOppfolgingsenhetCronjob = RemoveOppfolgingsenhetCronjob(
            enhetService = enhetService,
            repository = enhetRepository,
        )

        beforeEachTest {
            database.dropData()
            clearAllMocks()
        }

        describe("Cronjob nuller ut oppfolgningsenhet") {
            it("Cronjob fungerer på tom database") {
                runBlocking {
                    removeOppfolgingsenhetCronjob.run()
                }
            }
            it("Cronjob nuller ikke oppfolgingsenhet på person uten skjerming") {
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
                    storedEnhet.oppfolgingsenhet?.enhetId shouldBeEqualTo EnhetId.ENHETNR_NAV_UTLAND
                }
            }
            it("Cronjob nuller oppfolgingsenhet på person med skjerming") {
                every { cache.get(any()) } returns null
                val callId = UUID.randomUUID().toString()
                runBlocking {
                    val oppfolgingsenhet = enhetService.updateOppfolgingsenhet(
                        callId = callId,
                        personIdent = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                        enhetId = EnhetId(EnhetId.ENHETNR_NAV_UTLAND),
                    )
                    oppfolgingsenhet shouldNotBe null

                    coEvery { skjermedePersonerPipClient.isSkjermet(any(), any(), any()) } returns true
                    database.setSkjermingCheckedAt(oppfolgingsenhet!!.uuid, OffsetDateTime.now().minusDays(2))

                    removeOppfolgingsenhetCronjob.run()

                    val storedEnhet = enhetService.arbeidstakersBehandlendeEnhet(
                        callId = callId,
                        personIdentNumber = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                    )
                    storedEnhet.oppfolgingsenhet?.enhetId shouldNotBeEqualTo EnhetId.ENHETNR_NAV_UTLAND
                }
            }
        }
    }
})

package no.nav.syfo.infrastructure.cronjob

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.dropData
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDate

class RemoveOppfolgingsenhetCronjobSpek : Spek({

    describe(RemoveOppfolgingsenhetCronjobSpek::class.java.simpleName) {
        val externalMockEnvironment = ExternalMockEnvironment.instance
        val database = externalMockEnvironment.database
        val remoeveOppfolgingsenhetCronjob = RemoveOppfolgingsenhetCronjob(database)

        beforeEachTest {
            database.dropData()
            clearAllMocks()
        }

        describe("Cronjob nuller ut oppfolgningsenhet") {
            val fom = LocalDate.now()
            val tom = LocalDate.now().plusDays(30)

            it("Sender ikke vedtak lagret nå") {
                runBlocking {
                    remoeveOppfolgingsenhetCronjob.run()
                }
            }
        }
    }
})

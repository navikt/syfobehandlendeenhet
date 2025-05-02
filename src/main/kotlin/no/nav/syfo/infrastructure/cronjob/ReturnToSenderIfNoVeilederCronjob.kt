package no.nav.syfo.infrastructure.cronjob

import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.behandlendeenhet.IEnhetRepository
import no.nav.syfo.infrastructure.client.syfooversiktsrv.SyfooversiktsrvClient
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.*

class ReturnToSenderIfNoVeilederCronjob(
    val enhetService: EnhetService,
    val repository: IEnhetRepository,
    val syfooversiktsrvClient: SyfooversiktsrvClient,
) : Cronjob {
    override val initialDelayMinutes: Long = 4
    override val intervalDelayMinutes: Long = 60 * 24

    override suspend fun run(): List<Result<Any>> {
        val callId = UUID.randomUUID().toString()
        val personidenterWithOppfolgingsenhet = repository.getActiveOppfolgingsenheterWithoutVeileder()
        return personidenterWithOppfolgingsenhet.map { (uuid, createdAt, personident) ->
            try {
                if (syfooversiktsrvClient.tildeltVeileder(personident) != null) {
                    repository.updateVeilederCheckedOkAt(uuid)
                } else if (createdAt.isBefore(OffsetDateTime.now().minusDays(7))) {
                    log.info("Oppfolgingsenhet $uuid mangler veileder etter 7 dager, setter tilbake til geografisk enhet")
                    enhetService.updateOppfolgingsenhet(callId, personident, null)
                }
                Result.success(uuid)
            } catch (e: Exception) {
                log.error("Failed to check if veileder has been set", e)
                Result.failure(e)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ReturnToSenderIfNoVeilederCronjob::class.java)
    }
}

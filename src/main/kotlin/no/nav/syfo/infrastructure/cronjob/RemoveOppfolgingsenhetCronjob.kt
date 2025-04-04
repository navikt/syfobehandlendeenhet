package no.nav.syfo.infrastructure.cronjob

import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.behandlendeenhet.IEnhetRepository
import org.slf4j.LoggerFactory
import java.util.*

class RemoveOppfolgingsenhetCronjob(
    val enhetService: EnhetService,
    val repository: IEnhetRepository,
) : Cronjob {
    override val initialDelayMinutes: Long = 2
    override val intervalDelayMinutes: Long = 60

    override suspend fun run(): List<Result<Any>> {
        val callId = UUID.randomUUID().toString()
        val personidenterWithOppfolgingsenhet = repository.getActiveOppfolgingsenheter()
        return personidenterWithOppfolgingsenhet.map { (uuid, personident) ->
            try {
                if (!enhetService.validateForOppfolgingsenhet(callId, personident)) {
                    log.info("RemoveOppfolgingsenhetCronjob: Removing oppfolgingsenhet for person, uuid: $uuid")
                    enhetService.updateOppfolgingsenhet(callId, personident, null)
                }
                repository.updateSkjermingCheckedAt(uuid)
                Result.success(uuid)
            } catch (e: Exception) {
                log.error("Failed to remove oppfolgingsenhet", e)
                Result.failure(e)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RemoveOppfolgingsenhetCronjob::class.java)
    }
}

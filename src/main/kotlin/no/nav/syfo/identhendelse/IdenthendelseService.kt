package no.nav.syfo.identhendelse

import kotlinx.coroutines.runBlocking
import no.nav.syfo.behandlendeenhet.IEnhetRepository
import no.nav.syfo.infrastructure.client.pdl.PdlClient
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.identhendelse.kafka.COUNT_KAFKA_CONSUMER_PDL_AKTOR_UPDATES
import no.nav.syfo.identhendelse.kafka.KafkaIdenthendelseDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IdenthendelseService(
    private val repository: IEnhetRepository,
    private val pdlClient: PdlClient,
) {

    private val log: Logger = LoggerFactory.getLogger(IdenthendelseService::class.java)

    fun handleIdenthendelse(identhendelse: KafkaIdenthendelseDTO) {
        if (identhendelse.folkeregisterIdenter.size > 1) {
            val activeIdent = identhendelse.getActivePersonident()
            if (activeIdent != null) {
                val inactiveIdenter = identhendelse.getInactivePersonidenter()
                val oldPersonIdentList = inactiveIdenter.mapNotNull { personident ->
                    repository.getOppfolgingsenhetByPersonident(personident)?.personident?.let {
                        PersonIdentNumber(it)
                    }
                }

                if (oldPersonIdentList.isNotEmpty()) {
                    checkThatPdlIsUpdated(activeIdent)
                    val numberOfUpdatedIdenter = updatePersonOrDeleteOldVersions(activeIdent, oldPersonIdentList)
                    log.info("Identhendelse: Updated $numberOfUpdatedIdenter rows based on Identhendelse from PDL")
                    COUNT_KAFKA_CONSUMER_PDL_AKTOR_UPDATES.increment(numberOfUpdatedIdenter.toDouble())
                }
            } else {
                log.warn("Mangler gyldig ident fra PDL")
            }
        }
    }

    private fun updatePersonOrDeleteOldVersions(
        activeIdent: PersonIdentNumber,
        oldPersonIdentList: List<PersonIdentNumber>
    ): Int {
        var updatedRows = 0
        val personActiveIdent = repository.getOppfolgingsenhetByPersonident(activeIdent)
        if (personActiveIdent != null) {
            var deletedRows = 0
            oldPersonIdentList.forEach { deletedRows += repository.deletePerson(it) }
            log.info("Identhendelse: Deleted $deletedRows entries with an inactive personident from database.")
        } else {
            updatedRows += repository.updatePersonident(activeIdent, oldPersonIdentList.first())
        }
        return updatedRows
    }

    // Erfaringer fra andre team tilsier at vi burde dobbeltsjekke at ting har blitt oppdatert i PDL før vi gjør endringer
    private fun checkThatPdlIsUpdated(nyIdent: PersonIdentNumber) {
        runBlocking {
            val pdlIdenter = pdlClient.getPdlIdenter(nyIdent)?.hentIdenter ?: throw RuntimeException("Fant ingen identer fra PDL")
            if (nyIdent.value != pdlIdenter.aktivIdent && pdlIdenter.identhendelseIsNotHistorisk(nyIdent.value)) {
                throw IllegalStateException("Ny ident er ikke aktiv ident i PDL")
            }
        }
    }
}

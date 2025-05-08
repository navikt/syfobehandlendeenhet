package no.nav.syfo.behandlendeenhet

import no.nav.syfo.domain.EnhetId
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.infrastructure.database.repository.POppfolgingsenhet
import java.time.OffsetDateTime
import java.util.*

interface IEnhetRepository {
    fun createOppfolgingsenhet(
        personIdent: PersonIdentNumber,
        enhetId: EnhetId?,
        veilederident: String,
    ): POppfolgingsenhet

    fun getOppfolgingsenhetByPersonident(personIdent: PersonIdentNumber): POppfolgingsenhet?

    fun getEnhetUsageForVeileder(veilederident: String): List<EnhetId>

    fun getActiveOppfolgingsenheter(): List<Pair<UUID, PersonIdentNumber>>

    fun getActiveOppfolgingsenheterWithoutVeileder(): List<Triple<UUID, OffsetDateTime, PersonIdentNumber>>

    fun updateSkjermingCheckedAt(oppfolgingsenhetUUID: UUID): Int

    fun updateVeilederCheckedOkAt(oppfolgingsenhetUUID: UUID): Int

    fun updatePersonident(nyPersonident: PersonIdentNumber, oldIdent: PersonIdentNumber): Int

    fun deletePerson(personIdent: PersonIdentNumber): Int
}

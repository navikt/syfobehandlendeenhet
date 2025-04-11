package no.nav.syfo.behandlendeenhet

import no.nav.syfo.behandlendeenhet.domain.Oppfolgingsenhet
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.domain.PersonIdentNumber
import java.util.*

interface IEnhetRepository {
    fun createOppfolgingsenhet(
        personIdent: PersonIdentNumber,
        enhetId: EnhetId?,
        veilederident: String,
    ): Oppfolgingsenhet

    fun getOppfolgingsenhetByPersonident(personIdent: PersonIdentNumber): Oppfolgingsenhet?

    fun getEnhetUsageForVeileder(veilederident: String): List<EnhetId>

    fun getActiveOppfolgingsenheter(): List<Pair<UUID, PersonIdentNumber>>

    fun updateSkjermingCheckedAt(oppfolgingsenhetUUID: UUID): Int

    fun updatePersonident(nyPersonident: PersonIdentNumber, oldIdent: PersonIdentNumber): Int

    fun deletePerson(personIdent: PersonIdentNumber): Int
}

package no.nav.syfo.behandlendeenhet

import no.nav.syfo.behandlendeenhet.domain.Oppfolgingsenhet
import no.nav.syfo.domain.Enhet
import no.nav.syfo.domain.PersonIdentNumber

interface IEnhetRepository {
    fun createOppfolgingsenhet(
        personIdent: PersonIdentNumber,
        enhet: Enhet?,
        veilederident: String,
    ): Oppfolgingsenhet

    fun getOppfolgingsenhetByPersonident(personIdent: PersonIdentNumber): Oppfolgingsenhet?

    fun updatePersonident(nyPersonident: PersonIdentNumber, oldIdent: PersonIdentNumber): Int

    fun deletePerson(personIdent: PersonIdentNumber): Int
}

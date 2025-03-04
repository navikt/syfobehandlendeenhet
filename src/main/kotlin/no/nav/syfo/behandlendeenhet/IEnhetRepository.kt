package no.nav.syfo.behandlendeenhet

import no.nav.syfo.behandlendeenhet.domain.Person
import no.nav.syfo.domain.Enhet
import no.nav.syfo.domain.PersonIdentNumber

interface IEnhetRepository {
    fun createOrUpdatePerson(
        personIdent: PersonIdentNumber,
        enhet: Enhet?,
    ): Person?

    fun getPersonByIdent(personIdent: PersonIdentNumber): Person?

    fun updatePersonident(nyPersonident: PersonIdentNumber, oldIdent: PersonIdentNumber): Int

    fun deletePerson(personIdent: PersonIdentNumber): Int
}

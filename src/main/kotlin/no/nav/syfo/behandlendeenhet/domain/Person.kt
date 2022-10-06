package no.nav.syfo.behandlendeenhet.domain

import no.nav.syfo.behandlendeenhet.api.PersonDTO
import no.nav.syfo.domain.PersonIdentNumber
import java.util.*

data class Person(
    val uuid: UUID,
    val personident: PersonIdentNumber,
    val isNavUtland: Boolean
)

fun Person.toPersonDTO() = PersonDTO(
    personident = this.personident.value,
    isNavUtland = this.isNavUtland
)

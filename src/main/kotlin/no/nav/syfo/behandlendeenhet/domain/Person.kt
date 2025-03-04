package no.nav.syfo.behandlendeenhet.domain

import no.nav.syfo.behandlendeenhet.api.PersonDTO
import no.nav.syfo.domain.Enhet
import no.nav.syfo.domain.PersonIdentNumber
import java.time.OffsetDateTime
import java.util.*

data class Person(
    val uuid: UUID,
    val personident: PersonIdentNumber,
    val oppfolgingsenhet: Enhet?,
    val updatedAt: OffsetDateTime,
)

fun Person.isOppfolgingsenhetNavUtland() = oppfolgingsenhet?.isNavUtland() ?: false

fun Person.toPersonDTO() = PersonDTO(
    personident = this.personident.value,
    isNavUtland = this.isOppfolgingsenhetNavUtland(),
    oppfolgingsenhet = this.oppfolgingsenhet?.value,
)

package no.nav.syfo.infrastructure.database.repository

import no.nav.syfo.behandlendeenhet.domain.Person
import no.nav.syfo.domain.PersonIdentNumber
import java.time.OffsetDateTime
import java.util.*

data class PPerson(
    val id: Int,
    val uuid: UUID,
    val personident: String,
    val isNavUtland: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

fun PPerson.toPerson() = Person(
    uuid = this.uuid,
    personident = PersonIdentNumber(this.personident),
    isNavUtland = this.isNavUtland,
    updatedAt = this.updatedAt,
)

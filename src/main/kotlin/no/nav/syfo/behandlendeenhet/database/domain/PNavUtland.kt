package no.nav.syfo.behandlendeenhet.database.domain

import no.nav.syfo.behandlendeenhet.domain.NavUtland
import no.nav.syfo.domain.PersonIdentNumber
import java.time.OffsetDateTime
import java.util.*

data class PNavUtland(
    val id: Int,
    val uuid: UUID,
    val personident: String,
    val isNavUtland: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

fun PNavUtland.toNavUtland() = NavUtland(
    uuid = this.uuid,
    personident = PersonIdentNumber(this.personident),
    isNavUtland = this.isNavUtland
)

package no.nav.syfo.infrastructure.database.repository

import no.nav.syfo.behandlendeenhet.domain.Oppfolgingsenhet
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.domain.PersonIdentNumber
import java.time.OffsetDateTime
import java.util.*

data class POppfolgingsenhet(
    val id: Int,
    val uuid: UUID,
    val personident: String,
    val oppfolgingsenhet: String?,
    val veilederident: String,
    val createdAt: OffsetDateTime,
)

fun POppfolgingsenhet.toOppfolgingsenhet() = Oppfolgingsenhet(
    uuid = this.uuid,
    personident = PersonIdentNumber(this.personident),
    enhetId = this.oppfolgingsenhet?.let { EnhetId(it) },
    veilederident = veilederident,
    createdAt = this.createdAt,
)

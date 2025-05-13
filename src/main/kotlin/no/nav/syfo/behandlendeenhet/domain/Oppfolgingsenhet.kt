package no.nav.syfo.behandlendeenhet.domain

import no.nav.syfo.domain.EnhetId
import no.nav.syfo.domain.PersonIdentNumber
import java.time.OffsetDateTime
import java.util.*

data class Oppfolgingsenhet(
    val uuid: UUID,
    val personident: PersonIdentNumber,
    val enhetId: EnhetId?,
    val veilederident: String,
    val createdAt: OffsetDateTime,
)

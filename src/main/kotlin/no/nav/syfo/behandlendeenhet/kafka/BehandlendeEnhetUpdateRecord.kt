package no.nav.syfo.behandlendeenhet.kafka

import java.time.OffsetDateTime

data class BehandlendeEnhetUpdateRecord(
    val personident: String,
    val oppfolgingsenhet: String?,
    val updatedAt: OffsetDateTime,
)

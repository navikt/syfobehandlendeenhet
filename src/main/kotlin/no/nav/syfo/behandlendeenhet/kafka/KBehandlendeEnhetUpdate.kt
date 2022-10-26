package no.nav.syfo.behandlendeenhet.kafka

import java.time.OffsetDateTime

data class KBehandlendeEnhetUpdate(
    val personident: String,
    val updatedAt: OffsetDateTime,
)

package no.nav.syfo.infrastructure.kafka

import java.time.OffsetDateTime

data class KBehandlendeEnhetUpdate(
    val personident: String,
    val updatedAt: OffsetDateTime,
)

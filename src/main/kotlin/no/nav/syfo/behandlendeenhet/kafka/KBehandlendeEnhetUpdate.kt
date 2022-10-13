package no.nav.syfo.behandlendeenhet.kafka

import java.time.LocalDateTime

data class KBehandlendeEnhetUpdate(
    val personident: String,
    val updatedAt: LocalDateTime,
)

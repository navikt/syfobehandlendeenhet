package no.nav.syfo.domain.model

import lombok.Data
import lombok.experimental.Accessors

@Data
@Accessors(fluent = true)
data class Enhet (
    var enhetId: String,
    var navn: String
)

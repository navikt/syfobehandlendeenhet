package no.nav.syfo.behandlendeenhet.api

data class BehandlendeEnhetDTO(
    val personident: String,
    val isNavUtland: Boolean,
    val oppfolgingsenhet: String? = null,
)

data class TildelOppfolgingsenhetRequestDTO(
    val personidenter: List<String>,
    val oppfolgingsenhet: String,
)

data class TildelOppfolgingsenhetResponseDTO(
    val personident: String,
    val oppfolgingsenhet: String?,
)

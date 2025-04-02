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
    val tildelinger: List<TildelOppfolgingsenhetDTO>,
    val errors: List<ErrorDTO>,
)

data class TildelOppfolgingsenhetDTO(
    val personident: String,
    val oppfolgingsenhet: String?,
)

data class ErrorDTO(
    val personident: String,
    val errorMessage: String? = null,
    val errorCode: Int? = null,
)

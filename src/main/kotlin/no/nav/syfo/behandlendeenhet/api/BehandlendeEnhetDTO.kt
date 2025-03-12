package no.nav.syfo.behandlendeenhet.api

data class BehandlendeEnhetDTO(
    val personident: String,
    val isNavUtland: Boolean,
    val oppfolgingsenhet: String? = null,
)

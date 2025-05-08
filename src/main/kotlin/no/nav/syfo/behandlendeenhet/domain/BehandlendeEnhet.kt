package no.nav.syfo.behandlendeenhet.domain

data class BehandlendeEnhet(
    val geografiskEnhet: Enhet,
    val oppfolgingsenhet: Oppfolgingsenhet?,
)

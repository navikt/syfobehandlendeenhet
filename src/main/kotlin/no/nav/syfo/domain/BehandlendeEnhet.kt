package no.nav.syfo.domain

import no.nav.syfo.behandlendeenhet.Enhet

data class BehandlendeEnhet(
    val geografiskEnhet: Enhet,
    val oppfolgingsenhet: Enhet?,
)

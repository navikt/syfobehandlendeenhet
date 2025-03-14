package no.nav.syfo.domain

import no.nav.syfo.behandlendeenhet.BehandlendeEnhet

data class Stedtilknytning(
    val geografiskEnhet: BehandlendeEnhet?,
    val oppfolgingsenhet: BehandlendeEnhet?,
)

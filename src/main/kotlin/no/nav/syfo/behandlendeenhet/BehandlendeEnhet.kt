package no.nav.syfo.behandlendeenhet

import java.io.Serializable

data class BehandlendeEnhet(
    val enhetId: String,
    val navn: String,
) : Serializable

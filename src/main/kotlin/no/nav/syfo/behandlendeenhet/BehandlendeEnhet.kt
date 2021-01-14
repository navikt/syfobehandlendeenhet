package no.nav.syfo.behandlendeenhet

import java.io.Serializable

data class BehandlendeEnhet(
    var enhetId: String,
    var navn: String
) : Serializable

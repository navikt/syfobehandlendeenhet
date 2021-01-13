package no.nav.syfo.domain.model

import java.io.Serializable

data class BehandlendeEnhet(
    var enhetId: String,
    var navn: String
) : Serializable

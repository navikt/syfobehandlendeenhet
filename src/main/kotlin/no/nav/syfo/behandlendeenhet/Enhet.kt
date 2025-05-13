package no.nav.syfo.behandlendeenhet

import java.io.Serializable

data class Enhet(
    val enhetId: String,
    val navn: String,
) : Serializable

package no.nav.syfo.domain

import java.io.Serializable

data class Enhet(
    val enhetId: EnhetId,
    val navn: String,
) : Serializable

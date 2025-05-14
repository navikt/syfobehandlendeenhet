package no.nav.syfo.behandlendeenhet.domain

import no.nav.syfo.domain.EnhetId
import java.io.Serializable

data class Enhet(
    val enhetId: EnhetId,
    val navn: String,
) : Serializable

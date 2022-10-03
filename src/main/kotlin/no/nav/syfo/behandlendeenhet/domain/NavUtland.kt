package no.nav.syfo.behandlendeenhet.domain

import no.nav.syfo.domain.PersonIdentNumber
import java.util.*

data class NavUtland(
    val uuid: UUID,
    val personident: PersonIdentNumber,
    val isNavUtland: Boolean
)

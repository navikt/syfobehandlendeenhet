package no.nav.syfo.testhelper.generator

import no.nav.syfo.behandlendeenhet.api.PersonDTO
import no.nav.syfo.domain.Enhet.Companion.ENHETNR_NAV_UTLAND
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT

fun generatePersonDTO(
    personident: String = ARBEIDSTAKER_PERSONIDENT.value,
    isNavUtland: Boolean = true,
) = PersonDTO(
    personident = personident,
    isNavUtland = isNavUtland,
    oppfolgingsenhet = if (isNavUtland) ENHETNR_NAV_UTLAND else null,
)

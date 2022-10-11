package no.nav.syfo.testhelper.generator

import no.nav.syfo.behandlendeenhet.api.PersonDTO
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT

fun generatePersonDTO(
    personident: String = ARBEIDSTAKER_PERSONIDENT.value,
    isNavUtland: Boolean = true,
) = PersonDTO(
    personident = personident,
    isNavUtland = isNavUtland
)

package no.nav.syfo.testhelper.generator

import no.nav.syfo.behandlendeenhet.api.PersonDTO

fun generatePersonDTO(
    personident: String = "12345678910",
    isNavUtland: Boolean = true,
) = PersonDTO(
    personident = personident,
    isNavUtland = isNavUtland
)

package no.nav.syfo.testhelper

import no.nav.syfo.domain.PersonIdentNumber

object UserConstants {
    val ARBEIDSTAKER_PERSONIDENT = PersonIdentNumber("12345678912")
    val ARBEIDSTAKER_ADRESSEBESKYTTET = PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT.value.replace("2", "6"))

    const val VEILEDER_IDENT = "Z999999"
    val VEILEDER_IDENT_NO_ACCESS = VEILEDER_IDENT.replace("9", "1")
}

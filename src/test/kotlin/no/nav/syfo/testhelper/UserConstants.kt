package no.nav.syfo.testhelper

import no.nav.syfo.domain.PersonIdentNumber

object UserConstants {
    val ARBEIDSTAKER_PERSONIDENT = PersonIdentNumber("12345678912")
    val ARBEIDSTAKER_PERSONIDENT_2 = PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT.value.replace("2", "8"))
    val ARBEIDSTAKER_PERSONIDENT_3 = PersonIdentNumber("12345678913")
    val ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND = PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT.value.replace("2", "1"))
    val ARBEIDSTAKER_ADRESSEBESKYTTET = PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT.value.replace("2", "6"))
    val ARBEIDSTAKER_EGENANSATT = PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT.value.replace("2", "7"))

    const val VEILEDER_IDENT = "Z999999"
    val VEILEDER_IDENT_NO_ACCESS = VEILEDER_IDENT.replace("9", "1")

    const val ENHET_ID = "0314"
}

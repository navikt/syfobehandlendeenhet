package no.nav.syfo.util

fun isPersonNumberDnr(personIdentNummer: String): Boolean {
    val personIdentBornDay = personIdentNummer.substring(0, 2)
    return personIdentBornDay.toInt() in 41..71
}

fun isPersonNumberFnr(personIdentNummer: String): Boolean {
    val personIdentBornDay = personIdentNummer.substring(0, 2)
    return personIdentBornDay.toInt() in 1..31
}

enum class PersonIdentType {
    DNR,
    FNR,
    UNKNOWN
}

fun personIdentType(personIdentNummer: String): PersonIdentType {
    val isDnr = isPersonNumberDnr(personIdentNummer)
    return if (isDnr) {
        PersonIdentType.DNR
    } else {
        val isFnr = isPersonNumberFnr(personIdentNummer)
        if (isFnr) {
            PersonIdentType.FNR
        } else {
            PersonIdentType.UNKNOWN
        }
    }
}

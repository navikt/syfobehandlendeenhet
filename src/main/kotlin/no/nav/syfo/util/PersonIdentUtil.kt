package no.nav.syfo.util

fun isPersonNumberDnr(personIdent: String): Boolean {
    val personIdentBornDay = personIdent.substring(0, 2)
    return personIdentBornDay.toInt() in 41..71
}

fun isPersonNumberFnr(personIdent: String): Boolean {
    return personIdent.toInt() in 1..31
}

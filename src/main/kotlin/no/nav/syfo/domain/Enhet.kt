package no.nav.syfo.domain

data class Enhet(val value: String) {
    private val fourDigits = Regex("^\\d{4}\$")

    init {
        if (!fourDigits.matches(value)) {
            throw IllegalArgumentException("$value is not a valid enhetsnr")
        }
    }

    fun isNavUtland() = this.value == ENHETNR_NAV_UTLAND

    companion object {
        const val ENHETNR_NAV_UTLAND = "0393"
        const val ENHETNAVN_NAV_UTLAND = "Nav utland"
    }
}

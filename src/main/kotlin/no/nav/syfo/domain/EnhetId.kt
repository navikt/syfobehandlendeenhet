package no.nav.syfo.domain

data class EnhetId(val value: String) {
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
        const val VEST_VIKEN_ENHET_ID = "0600"
        const val VEST_VIKEN_ROE_ID = "0676"
    }
}

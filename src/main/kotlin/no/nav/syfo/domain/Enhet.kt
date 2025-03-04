package no.nav.syfo.domain

data class Enhet(val value: String) {
    private val fourDigits = Regex("^\\d{4}\$")

    init {
        if (!fourDigits.matches(value)) {
            throw IllegalArgumentException("$value is not a valid enhetsnr")
        }
    }

    fun isNavUtland() = this.value == enhetnrNAVUtland

    companion object {
        const val enhetnrNAVUtland = "0393"
        const val enhetnavnNAVUtland = "Nav Utland"
    }
}

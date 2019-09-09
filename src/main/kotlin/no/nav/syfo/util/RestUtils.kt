package no.nav.syfo.util

import java.lang.System.getenv

object RestUtils {

    fun baseUrl(): String {
        return "https://app" + miljo() + ".adeo.no"
    }

    private fun miljo(): String {
        val environmentName = getenv("FASIT_ENVIRONMENT_NAME")
        return if ("p" == environmentName) {
            ""
        } else "-$environmentName"
    }
}

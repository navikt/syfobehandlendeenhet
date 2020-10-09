package no.nav.syfo.config

object EnvironmentUtil {

    fun getEnvVar(varName: String, defaultValue: String? = null): String {
        return System.getenv(varName) ?: defaultValue
        ?: throw RuntimeException("Missing required variable \"$varName\"")
    }
}

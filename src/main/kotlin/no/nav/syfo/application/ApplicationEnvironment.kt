package no.nav.syfo.application

import io.ktor.application.*

data class Environment(
    val azureAppClientId: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val azureAppClientSecret: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val azureAppWellKnownUrl: String = getEnvVar("AZURE_APP_WELL_KNOWN_URL"),
    val azureAppPreAuthorizedApps: String = getEnvVar("AZURE_APP_PRE_AUTHORIZED_APPS"),
    val azureOpenidConfigTokenEndpoint: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),

    val redisHost: String = getEnvVar("REDIS_HOST"),
    val redisPort: Int = getEnvVar("REDIS_PORT", "6379").toInt(),
    val redisSecret: String = getEnvVar("REDIS_PASSWORD"),

    val isproxyClientId: String = getEnvVar("ISPROXY_CLIENT_ID"),
    val isproxyUrl: String = getEnvVar("ISPROXY_URL"),

    val pdlClientId: String = getEnvVar("PDL_CLIENT_ID"),
    val pdlUrl: String = getEnvVar("PDL_URL"),

    val skjermedePersonerPipClientId: String = getEnvVar("SKJERMEDEPERSONERPIP_CLIENT_ID"),
    val skjermedePersonerPipUrl: String = getEnvVar("SKJERMEDEPERSONERPIP_URL"),

    val syfotilgangskontrollClientId: String = getEnvVar("SYFOTILGANGSKONTROLL_CLIENT_ID"),
    val syfotilgangskontrollUrl: String = getEnvVar("SYFOTILGANGSKONTROLL_URL"),

    val ispersonoppgaveApplicationName: String = "ispersonoppgave",
    val syfomoteadminApplicationName: String = "syfomoteadmin",
    val syfomotebehovApplicationName: String = "syfomotebehov",
    val syfooversikthendelsetilfelleApplicationName: String = "syfooversikthendelsetilfelle",
    val syfooversiktsrvApplicationName: String = "syfooversiktsrv",
    val syfotilgangskontrollApplicationName: String = "syfo-tilgangskontroll",
    val systemAPIAuthorizedConsumerApplicationNameList: List<String> = listOf(
        ispersonoppgaveApplicationName,
        syfomoteadminApplicationName,
        syfomotebehovApplicationName,
        syfooversikthendelsetilfelleApplicationName,
        syfooversiktsrvApplicationName,
        syfotilgangskontrollApplicationName,
    ),
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")

val Application.envKind get() = environment.config.property("ktor.environment").getString()

fun Application.isDev(block: () -> Unit) {
    if (envKind == "dev") block()
}

fun Application.isProd(block: () -> Unit) {
    if (envKind == "production") block()
}

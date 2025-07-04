package no.nav.syfo.application

import io.ktor.server.application.*
import no.nav.syfo.infrastructure.cache.ValkeyConfig
import java.net.URI

data class Environment(
    val azureAppClientId: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val azureAppClientSecret: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val azureAppWellKnownUrl: String = getEnvVar("AZURE_APP_WELL_KNOWN_URL"),
    val azureAppPreAuthorizedApps: String = getEnvVar("AZURE_APP_PRE_AUTHORIZED_APPS"),
    val azureOpenidConfigTokenEndpoint: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),

    val kafka: ApplicationEnvironmentKafka = ApplicationEnvironmentKafka(
        aivenBootstrapServers = getEnvVar("KAFKA_BROKERS"),
        aivenSchemaRegistryUrl = getEnvVar("KAFKA_SCHEMA_REGISTRY"),
        aivenRegistryUser = getEnvVar("KAFKA_SCHEMA_REGISTRY_USER"),
        aivenRegistryPassword = getEnvVar("KAFKA_SCHEMA_REGISTRY_PASSWORD"),
        aivenSecurityProtocol = "SSL",
        aivenCredstorePassword = getEnvVar("KAFKA_CREDSTORE_PASSWORD"),
        aivenTruststoreLocation = getEnvVar("KAFKA_TRUSTSTORE_PATH"),
        aivenKeystoreLocation = getEnvVar("KAFKA_KEYSTORE_PATH"),
    ),
    val valkeyConfig: ValkeyConfig = ValkeyConfig(
        valkeyUri = URI(getEnvVar("VALKEY_URI_CACHE")),
        valkeyDB = 17, // se https://github.com/navikt/istilgangskontroll/blob/master/README.md
        valkeyUsername = getEnvVar("VALKEY_USERNAME_CACHE"),
        valkeyPassword = getEnvVar("VALKEY_PASSWORD_CACHE"),
    ),
    val electorPath: String = getEnvVar("ELECTOR_PATH"),

    val norg2Url: String = getEnvVar("NORG2_URL"),

    val pdlClientId: String = getEnvVar("PDL_CLIENT_ID"),
    val pdlUrl: String = getEnvVar("PDL_URL"),

    val skjermedePersonerPipClientId: String = getEnvVar("SKJERMEDEPERSONERPIP_CLIENT_ID"),
    val skjermedePersonerPipUrl: String = getEnvVar("SKJERMEDEPERSONERPIP_URL"),

    val istilgangskontrollClientId: String = getEnvVar("ISTILGANGSKONTROLL_CLIENT_ID"),
    val istilgangskontrollUrl: String = getEnvVar("ISTILGANGSKONTROLL_URL"),

    val syfooversiktsrvClientId: String = getEnvVar("SYFOOVERSIKTSRV_CLIENT_ID"),
    val syfooversiktsrvUrl: String = getEnvVar("SYFOOVERSIKTSRV_URL"),

    val syfobehandlendeenhetDbHost: String = getEnvVar("NAIS_DATABASE_SYFOBEHANDLENDEENHET_SYFOBEHANDLENDEENHET_DB_HOST"),
    val syfobehandlendeenhetDbPort: String = getEnvVar("NAIS_DATABASE_SYFOBEHANDLENDEENHET_SYFOBEHANDLENDEENHET_DB_PORT"),
    val syfobehandlendeenhetDbName: String = getEnvVar("NAIS_DATABASE_SYFOBEHANDLENDEENHET_SYFOBEHANDLENDEENHET_DB_DATABASE"),
    val syfobehandlendeenhetDbUsername: String = getEnvVar("NAIS_DATABASE_SYFOBEHANDLENDEENHET_SYFOBEHANDLENDEENHET_DB_USERNAME"),
    val syfobehandlendeenhetDbPassword: String = getEnvVar("NAIS_DATABASE_SYFOBEHANDLENDEENHET_SYFOBEHANDLENDEENHET_DB_PASSWORD"),

    val syfomotebehovApplicationName: String = "syfomotebehov",
    val syfooversiktsrvApplicationName: String = "syfooversiktsrv",
    val istilgangskontrollApplicationName: String = "istilgangskontroll",
    val ismeroppfolgingApplicationName: String = "ismeroppfolging",
    val systemAPIAuthorizedConsumerApplicationNameList: List<String> = listOf(
        syfomotebehovApplicationName,
        syfooversiktsrvApplicationName,
        istilgangskontrollApplicationName,
        ismeroppfolgingApplicationName,
    ),
) {
    fun jdbcUrl(): String {
        return "jdbc:postgresql://$syfobehandlendeenhetDbHost:$syfobehandlendeenhetDbPort/$syfobehandlendeenhetDbName"
    }
}

data class ApplicationEnvironmentKafka(
    val aivenBootstrapServers: String,
    val aivenSchemaRegistryUrl: String,
    val aivenRegistryUser: String,
    val aivenRegistryPassword: String,
    val aivenSecurityProtocol: String,
    val aivenCredstorePassword: String,
    val aivenTruststoreLocation: String,
    val aivenKeystoreLocation: String,
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

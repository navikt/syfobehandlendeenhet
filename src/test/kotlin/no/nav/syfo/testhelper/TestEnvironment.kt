package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationEnvironmentKafka
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.infrastructure.cache.ValkeyConfig
import no.nav.syfo.behandlendeenhet.api.access.PreAuthorizedClient
import no.nav.syfo.util.configuredJacksonMapper
import java.net.URI

fun testEnvironment(
    azureOpenIdTokenEndpoint: String = "azureTokenEndpoint",
    kafkaBootstrapServers: String = "boostrapserver",
    norg2Url: String = "norg2",
    pdlUrl: String = "pdl",
    skjermedePersonerPipUrl: String = "skjermedepersonerpip",
    istilgangskontrollUrl: String = "tilgangskontroll",
    syfooversiktsrvUrl: String = "syfooversiktsrv",
) = Environment(
    azureAppClientId = "syfoperson-client-id",
    azureAppClientSecret = "syfoperson-secret",
    azureAppWellKnownUrl = "wellknown",
    azureAppPreAuthorizedApps = configuredJacksonMapper().writeValueAsString(testAzureAppPreAuthorizedApps),
    azureOpenidConfigTokenEndpoint = azureOpenIdTokenEndpoint,
    kafka = ApplicationEnvironmentKafka(
        aivenBootstrapServers = kafkaBootstrapServers,
        aivenSchemaRegistryUrl = "http://kafka-schema-registry.tpa.svc.nais.local:8081",
        aivenRegistryUser = "registryuser",
        aivenRegistryPassword = "registrypassword",
        aivenSecurityProtocol = "SSL",
        aivenCredstorePassword = "credstorepassord",
        aivenTruststoreLocation = "truststore",
        aivenKeystoreLocation = "keystore",
    ),
    norg2Url = norg2Url,
    pdlClientId = "dev-fss.pdl.pdl-api",
    pdlUrl = pdlUrl,
    skjermedePersonerPipClientId = "dev-gcp.nom.skjermede-personer-pip",
    skjermedePersonerPipUrl = skjermedePersonerPipUrl,
    istilgangskontrollClientId = "dev-gcp.teamsykefravr.istilgangskontroll",
    istilgangskontrollUrl = istilgangskontrollUrl,
    syfooversiktsrvClientId = "dev-gcp.teamsykefravr.syfooversiktsrv",
    syfooversiktsrvUrl = syfooversiktsrvUrl,
    syfobehandlendeenhetDbHost = "localhost",
    syfobehandlendeenhetDbPort = "5432",
    syfobehandlendeenhetDbName = "syfobehandlendeenhet_dev",
    syfobehandlendeenhetDbUsername = "username",
    syfobehandlendeenhetDbPassword = "password",
    valkeyConfig = ValkeyConfig(
        valkeyUri = URI("http://localhost:6379"),
        valkeyDB = 0,
        valkeyUsername = "valkeyUser",
        valkeyPassword = "valkeyPassword",
        ssl = false,
    ),
    electorPath = "elector",
)

fun testAppState() = ApplicationState(
    alive = true,
    ready = true,
)

const val testIsdialogmoteClientId = "isdialogmote-client-id"
const val testSyfomodiapersonClientId = "syfomodiaperson-client-id"
const val testSyfomotebehovClientId = "syfomotebehov-client-id"
const val testSyfooversiktsrvClientId = "syfooversiktsrv-client-id"
const val testIstilgangskontrollClientId = "istilgangskontroll-client-id"
const val testIsMeroppfolgingClientId = "ismeroppfolging-client-id"

val testAzureAppPreAuthorizedApps = listOf(
    PreAuthorizedClient(
        name = "dev-gcp:teamsykefravr:isdialogmote",
        clientId = testIsdialogmoteClientId,
    ),
    PreAuthorizedClient(
        name = "dev-fss:teamsykefravr:syfomodiaperson",
        clientId = testSyfomodiapersonClientId,
    ),
    PreAuthorizedClient(
        name = "dev-fss:team-esyfo:syfomotebehov",
        clientId = testSyfomotebehovClientId,
    ),
    PreAuthorizedClient(
        name = "dev-gcp:teamsykefravr:syfooversiktsrv",
        clientId = testSyfooversiktsrvClientId,
    ),
    PreAuthorizedClient(
        name = "dev-gcp:teamsykefravr:istilgangskontroll",
        clientId = testIstilgangskontrollClientId,
    ),
    PreAuthorizedClient(
        name = "dev-gcp:teamsykefravr:ismeroppfolging",
        clientId = testIsMeroppfolgingClientId,
    ),
)

package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationEnvironmentKafka
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.behandlendeenhet.api.access.PreAuthorizedClient
import no.nav.syfo.util.configuredJacksonMapper

fun testEnvironment(
    azureOpenIdTokenEndpoint: String = "azureTokenEndpoint",
    kafkaBootstrapServers: String = "boostrapserver",
    norg2Url: String = "norg2",
    pdlUrl: String = "pdl",
    skjermedePersonerPipUrl: String = "skjermedepersonerpip",
    istilgangskontrollUrl: String = "tilgangskontroll",
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
    syfobehandlendeenhetDbHost = "localhost",
    syfobehandlendeenhetDbPort = "5432",
    syfobehandlendeenhetDbName = "syfobehandlendeenhet_dev",
    syfobehandlendeenhetDbUsername = "username",
    syfobehandlendeenhetDbPassword = "password",
    redisHost = "localhost",
    redisSecret = "password",
)

fun testAppState() = ApplicationState(
    alive = true,
    ready = true,
)

const val testIspersonoppgaveClientId = "ispersonoppgave-client-id"
const val testIsdialogmoteClientId = "isdialogmote-client-id"
const val testSyfomodiapersonClientId = "syfomodiaperson-client-id"
const val testSyfomotebehovClientId = "syfomotebehov-client-id"
const val testSyfooversiktsrvClientId = "syfooversiktsrv-client-id"
const val testSyfotilgangskontrollClientId = "syfo-tilgangskontroll-client-id"
const val testIstilgangskontrollClientId = "istilgangskontroll-client-id"
const val testMeroppfolgingClientId = "meroppfolging-backend-client-id"
const val testEsyfovarselClientId = "esyfovarsel-client-id"

val testAzureAppPreAuthorizedApps = listOf(
    PreAuthorizedClient(
        name = "dev-gcp:teamsykefravr:isdialogmote",
        clientId = testIsdialogmoteClientId,
    ),
    PreAuthorizedClient(
        name = "dev-gcp:teamsykefravr:ispersonoppgave",
        clientId = testIspersonoppgaveClientId,
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
        name = "dev-fss:teamsykefravr:syfo-tilgangskontroll",
        clientId = testSyfotilgangskontrollClientId,
    ),
    PreAuthorizedClient(
        name = "dev-gcp:teamsykefravr:istilgangskontroll",
        clientId = testIstilgangskontrollClientId,
    ),
    PreAuthorizedClient(
        name = "dev-gcp:team-esyfo:meroppfolging-backend",
        clientId = testMeroppfolgingClientId,
    ),
    PreAuthorizedClient(
        name = "dev-gcp:team-esyfo:esyfovarsel",
        clientId = testEsyfovarselClientId,
    ),
)

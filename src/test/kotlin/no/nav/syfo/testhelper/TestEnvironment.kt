package no.nav.syfo.testhelper

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.behandlendeenhet.api.access.PreAuthorizedClient
import no.nav.syfo.util.configuredJacksonMapper
import java.net.ServerSocket

fun testEnvironment(
    azureOpenIdTokenEndpoint: String = "azureTokenEndpoint",
    norg2Url: String = "norg2",
    pdlUrl: String = "pdl",
    skjermedePersonerPipUrl: String = "skjermedepersonerpip",
    syfotilgangskontrollUrl: String = "tilgangskontroll",
) = Environment(
    azureAppClientId = "syfoperson-client-id",
    azureAppClientSecret = "syfoperson-secret",
    azureAppWellKnownUrl = "wellknown",
    azureAppPreAuthorizedApps = configuredJacksonMapper().writeValueAsString(testAzureAppPreAuthorizedApps),
    azureOpenidConfigTokenEndpoint = azureOpenIdTokenEndpoint,
    norg2Url = norg2Url,
    pdlClientId = "dev-fss.pdl.pdl-api",
    pdlUrl = pdlUrl,
    skjermedePersonerPipClientId = "dev-gcp.nom.skjermede-personer-pip",
    skjermedePersonerPipUrl = skjermedePersonerPipUrl,
    syfotilgangskontrollClientId = "dev-fss.teamsykefravr.syfo-tilgangskontroll",
    syfotilgangskontrollUrl = syfotilgangskontrollUrl,
    redisHost = "localhost",
    redisSecret = "password",
)

fun testAppState() = ApplicationState(
    alive = true,
    ready = true,
)

fun getRandomPort() = ServerSocket(0).use {
    it.localPort
}

const val testIspersonoppgaveClientId = "ispersonoppgave-client-id"
const val testIsdialogmoteClientId = "isdialogmote-client-id"
const val testSyfomodiapersonClientId = "syfomodiaperson-client-id"
const val testSyfomotebehovClientId = "syfomotebehov-client-id"
const val testSyfooversikthendelsetilfelleClientId = "syfooversikthendelsetilfelle-client-id"
const val testSyfooversiktsrvClientId = "syfooversiktsrv-client-id"
const val testSyfotilgangskontrollClientId = "syfo-tilgangskontroll-client-id"

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
        name = "dev-fss:teamsykefravr:syfooversikthendelsetilfelle",
        clientId = testSyfooversikthendelsetilfelleClientId,
    ),
    PreAuthorizedClient(
        name = "dev-gcp:teamsykefravr:syfooversiktsrv",
        clientId = testSyfooversiktsrvClientId,
    ),
    PreAuthorizedClient(
        name = "dev-fss:teamsykefravr:syfo-tilgangskontroll",
        clientId = testSyfotilgangskontrollClientId,
    ),
)

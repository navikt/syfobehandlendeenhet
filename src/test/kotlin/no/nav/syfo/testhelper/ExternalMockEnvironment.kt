package no.nav.syfo.testhelper

import io.ktor.server.netty.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.testhelper.mock.*

class ExternalMockEnvironment private constructor() {
    val applicationState: ApplicationState = testAppState()
    val azureAdMock = AzureAdMock()
    val isproxyMock = IsproxyMock()
    val pdlMock = PdlMock()
    val skjermedPersonerPipMock = SkjermedePersonerPipMock()
    val veilederTilgangskontrollMock = VeilederTilgangskontrollMock()

    val externalApplicationMockMap = hashMapOf(
        azureAdMock.name to azureAdMock.server,
        isproxyMock.name to isproxyMock.server,
        pdlMock.name to pdlMock.server,
        skjermedPersonerPipMock.name to skjermedPersonerPipMock.server,
        veilederTilgangskontrollMock.name to veilederTilgangskontrollMock.server,
    )

    val environment = testEnvironment(
        azureOpenIdTokenEndpoint = azureAdMock.url,
        isproxyUrl = isproxyMock.url,
        pdlUrl = pdlMock.url,
        skjermedePersonerPipUrl = skjermedPersonerPipMock.url,
        syfotilgangskontrollUrl = veilederTilgangskontrollMock.url,
    )

    val redisServer = testRedis(
        port = environment.redisPort,
        secret = environment.redisSecret,
    )

    val wellKnownInternalAzureAD = wellKnownInternalAzureAD()

    companion object {
        val instance: ExternalMockEnvironment by lazy {
            ExternalMockEnvironment().also {
                it.startExternalMocks()
            }
        }
    }
}

fun ExternalMockEnvironment.startExternalMocks() {
    this.externalApplicationMockMap.start()
    this.redisServer.start()
}

fun HashMap<String, NettyApplicationEngine>.start() {
    this.forEach {
        it.value.start()
    }
}

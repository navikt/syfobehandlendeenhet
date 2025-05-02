package no.nav.syfo.testhelper.mock

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import no.nav.syfo.application.Environment
import no.nav.syfo.infrastructure.client.commonConfig

fun mockHttpClient(environment: Environment) = HttpClient(MockEngine) {
    commonConfig()
    engine {
        addHandler { request ->
            val requestUrl = request.url.encodedPath
            when {
                requestUrl == "/${environment.azureOpenidConfigTokenEndpoint}" -> getAzureAdResponse(request)
                requestUrl.startsWith("/${environment.skjermedePersonerPipUrl}") -> getSkjermedePersonerResponse(request)
                requestUrl.startsWith("/${environment.pdlUrl}") -> pdlMockResponse(request)
                requestUrl.startsWith("/${environment.istilgangskontrollUrl}") -> tilgangskontrollResponse(request)
                requestUrl.startsWith("/${environment.norg2Url}") -> getNorg2Response(request)
                requestUrl.startsWith("/${environment.syfooversiktsrvUrl}") -> getSyfooversiktsrvResponse(request)

                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
    }
}

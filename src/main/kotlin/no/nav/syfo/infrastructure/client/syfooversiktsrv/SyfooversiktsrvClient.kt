package no.nav.syfo.infrastructure.client.syfooversiktsrv

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.infrastructure.client.azuread.AzureAdClient
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.infrastructure.client.httpClientDefault
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class SyfooversiktsrvClient(
    private val azureAdClient: AzureAdClient,
    private val baseUrl: String,
    private val clientId: String,
    private val httpClient: HttpClient = httpClientDefault(),
) {

    suspend fun tildeltVeileder(
        personIdentNumber: PersonIdentNumber,
    ): String? {
        val token = azureAdClient.getSystemToken(scopeClientId = clientId)?.accessToken
            ?: throw RuntimeException("Failed to request access to Skjerming: Failed to get token")

        return try {
            val url = "$baseUrl/api/v1/system/persontildeling/personer/single"
            val tildelingResponse: VeilederBrukerKnytningDTO = httpClient.get(url) {
                header(HttpHeaders.Authorization, bearerHeader(token))
                header(NAV_CONSUMER_ID_HEADER, NAV_APP_CONSUMER_ID)
                header(NAV_PERSONIDENT_HEADER, personIdentNumber.value)
                accept(ContentType.Application.Json)
            }.body()
            tildelingResponse?.tildeltVeilederident
        } catch (e: ResponseException) {
            log.error(
                "Error while requesting tildelt veileder from syfooversiktsrv {}, {}",
                StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                StructuredArguments.keyValue("message", e.message),
            )
            throw e
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SyfooversiktsrvClient::class.java)
    }
}

package no.nav.syfo.infrastructure.client.veiledertilgang

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.application.api.ForbiddenAccessVeilederException
import no.nav.syfo.application.api.authentication.Token
import no.nav.syfo.infrastructure.client.azuread.AzureAdClient
import no.nav.syfo.infrastructure.client.httpClientDefault
import no.nav.syfo.util.bearerHeader
import no.nav.syfo.util.callIdArgument
import org.slf4j.LoggerFactory

class VeilederTilgangskontrollClient(
    private val azureAdClient: AzureAdClient,
    private val clientId: String,
    baseUrl: String,
    private val httpClient: HttpClient = httpClientDefault(),
) {

    private val tilgangskontrollSYFOUrl: String = "$baseUrl$ACCESS_TO_SYFO_PATH"

    suspend fun throwExceptionIfVeilederWithoutAccessToSYFOWithOBO(
        callId: String,
        token: Token,
    ) {
        val hasAccess = isVeilederGrantedAccessToSYFOWithOBO(
            callId = callId,
            token = token,
        )
        if (!hasAccess) {
            throw ForbiddenAccessVeilederException()
        }
    }

    private suspend fun isVeilederGrantedAccessToSYFOWithOBO(
        callId: String,
        token: Token,
    ): Boolean {
        val oboToken = azureAdClient.getOnBehalfOfToken(
            scopeClientId = clientId,
            token = token,
        )?.accessToken
            ?: throw RuntimeException("Failed to request access to SYFO from istilgangskontroll: Failed to get OBO token from AzureAD")

        return try {
            val response: HttpResponse = httpClient.get(tilgangskontrollSYFOUrl) {
                header(HttpHeaders.Authorization, bearerHeader(oboToken))
                accept(ContentType.Application.Json)
            }
            COUNT_CALL_TILGANGSKONTROLL_SYFO_SUCCESS.increment()
            response.body<TilgangDTO>().erGodkjent
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.Forbidden) {
                COUNT_CALL_TILGANGSKONTROLL_SYFO_FORBIDDEN.increment()
            } else {
                log.error(
                    "Error while requesting access to SYFO from istilgangskontroll with {}, {}",
                    StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                    callIdArgument(callId)
                )
                COUNT_CALL_TILGANGSKONTROLL_SYFO_FAIL.increment()
            }
            false
        } catch (e: ClosedReceiveChannelException) {
            log.error(
                "ClosedReceiveChannelException while requesting access to SYFO from istilgangskontroll, callId=$callId",
                e
            )
            COUNT_CALL_TILGANGSKONTROLL_SYFO_FAIL.increment()
            false
        }
    }

    companion object {
        const val ACCESS_TO_SYFO_PATH = "/api/tilgang/navident/syfo"

        private val log = LoggerFactory.getLogger(VeilederTilgangskontrollClient::class.java)
    }
}

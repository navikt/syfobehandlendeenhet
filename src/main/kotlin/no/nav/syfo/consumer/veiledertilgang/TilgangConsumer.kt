package no.nav.syfo.consumer.veiledertilgang

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.OIDCIssuer
import no.nav.syfo.api.auth.OIDCUtil.tokenFraOIDC
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl
import java.net.URI
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@Service
class TilgangConsumer @Inject constructor(
    @Value("\${syfotilgangskontroll.client.id}") private val syfotilgangskontrollClientId: String,
    @Value("\${tilgangskontrollapi.url}") private val tilgangskontrollUrl: String,
    private val azureAdV2TokenConsumer: AzureAdV2TokenConsumer,
    private val template: RestTemplate,
    private val contextHolder: TokenValidationContextHolder
) {
    private val accessToSYFOV2URI: URI

    init {
        accessToSYFOV2URI = fromHttpUrl(tilgangskontrollUrl)
            .path(ACCESS_TO_SYFO_WITH_AZURE_V2_PATH)
            .build()
            .toUri()
    }

    fun throwExceptionIfVeilederWithoutAccessToSYFOWithOBO() {
        if (!isVeilederGrantedAccessToSYFOWithOBO()) {
            throw ForbiddenException()
        }
    }

    fun isVeilederGrantedAccessToSYFOWithOBO(): Boolean {
        val token = tokenFraOIDC(contextHolder, OIDCIssuer.VEILEDER_AZURE_V2)
        val oboToken = azureAdV2TokenConsumer.getOnBehalfOfToken(
            scopeClientId = syfotilgangskontrollClientId,
            token = token
        )
        return callUriWithTemplate(
            token = oboToken,
            uri = accessToSYFOV2URI
        )
    }

    private fun callUriWithTemplate(
        token: String,
        uri: URI
    ): Boolean {
        return try {
            val response = template.exchange(
                uri,
                HttpMethod.GET,
                createEntity(token),
                String::class.java
            )
            return response.statusCode.is2xxSuccessful
        } catch (e: HttpClientErrorException) {
            if (e.rawStatusCode == 403) {
                false
            } else {
                LOG.error("HttpClientErrorException mot URI {}", uri.toString(), e)
                return false
            }
        }
    }

    private fun createEntity(token: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.setBearerAuth(token)
        return HttpEntity(headers)
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(TilgangConsumer::class.java)
        const val ACCESS_TO_SYFO_WITH_AZURE_V2_PATH = "/navident/syfo"
    }
}

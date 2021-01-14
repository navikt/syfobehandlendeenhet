package no.nav.syfo.consumer.veiledertilgang

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.auth.OIDCIssuer.AZURE
import no.nav.syfo.api.auth.OIDCUtil.tokenFraOIDC
import no.nav.syfo.util.bearerHeader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl
import java.net.URI
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@Service
class TilgangConsumer @Inject constructor(
    @Value("\${tilgangskontrollapi.url}") private val tilgangskontrollUrl: String,
    private val template: RestTemplate,
    private val contextHolder: TokenValidationContextHolder
) {
    private val accessToSYFOURI: URI

    init {
        accessToSYFOURI = fromHttpUrl(tilgangskontrollUrl)
            .path(ACCESS_TO_SYFO_WITH_AZURE_PATH)
            .build()
            .toUri()
    }

    fun throwExceptionIfVeilederWithoutAccessToSYFO() {
        if (!isVeilederGrantedAccessToSYFO()) {
            throw ForbiddenException()
        }
    }

    fun isVeilederGrantedAccessToSYFO(): Boolean {
        return callUriWithTemplate(accessToSYFOURI)
    }

    private fun callUriWithTemplate(uri: URI): Boolean {
        return try {
            val response = template.exchange(
                uri,
                HttpMethod.GET,
                createEntity(),
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

    private fun createEntity(): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.set(HttpHeaders.AUTHORIZATION, bearerHeader(tokenFraOIDC(contextHolder, AZURE)))
        return HttpEntity(headers)
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(TilgangConsumer::class.java)
        const val ACCESS_TO_SYFO_WITH_AZURE_PATH = "/syfo"
    }
}

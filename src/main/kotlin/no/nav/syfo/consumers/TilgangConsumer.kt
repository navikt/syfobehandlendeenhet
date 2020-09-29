package no.nav.syfo.consumers

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.config.EnvironmentUtil
import no.nav.syfo.oidc.OIDCIssuer.AZURE
import no.nav.syfo.oidc.OIDCUtil.tokenFraOIDC
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl
import java.net.URI
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@Service
class TilgangConsumer @Inject constructor(
        val template: RestTemplate,
        val contextHolder: TokenValidationContextHolder
) : InitializingBean {
    private var instance: TilgangConsumer? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    private val tilgangskontrollUrl = EnvironmentUtil.getEnvVar("TILGANGSKONTROLLAPI_URL", "http://syfo-tilgangskontroll")

    private val accessToSYFOUriTemplate: UriComponentsBuilder

    init {
        accessToSYFOUriTemplate = fromHttpUrl(tilgangskontrollUrl)
                .path(ACCESS_TO_SYFO_WITH_AZURE_PATH)
    }

    fun throwExceptionIfVeilederWithoutAccessToSYFO() {
        if (!isVeilederGrantedAccessToSYFO()) {
            throw ForbiddenException()
        }
    }

    fun isVeilederGrantedAccessToSYFO(): Boolean {
        val accessToSyfoUriWith = accessToSYFOUriTemplate.build().toUri()
        return callUriWithTemplate(accessToSyfoUriWith)
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
                LOG.error("HttpClientErrorException mot uri {}", uri, e)
                return false
            }
        }
    }

    private fun createEntity(): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer " + tokenFraOIDC(contextHolder, AZURE))
        return HttpEntity(headers)
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(TilgangConsumer::class.java)
        const val ACCESS_TO_SYFO_WITH_AZURE_PATH = "/syfo"
    }
}

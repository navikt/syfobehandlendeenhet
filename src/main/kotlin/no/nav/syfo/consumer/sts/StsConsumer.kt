package no.nav.syfo.consumer.sts

import no.nav.syfo.metric.Metric
import no.nav.syfo.util.basicHeader
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl
import java.time.LocalDateTime

@Service
class StsConsumer(
    private val metric: Metric,
    @Value("\${security.token.service.rest.url}") private val baseUrl: String,
    @Value("\${srv.username}") private val username: String,
    @Value("\${srv.password}") private val password: String,
    private val template: RestTemplate
) {
    private val getStsTokenUriTemplate: UriComponentsBuilder = fromHttpUrl(getStsTokenUrl())

    private var cachedOidcToken: StsToken? = null

    fun token(): String {
        if (StsToken.shouldRenew(cachedOidcToken)) {
            metric.countEvent(METRIC_CALL_STS)
            val request = HttpEntity<Any>(authorizationHeader())

            val stsTokenUri = getStsTokenUriTemplate.build().toUri()

            try {
                val response = template.exchange(
                    stsTokenUri,
                    HttpMethod.GET,
                    request,
                    StsToken::class.java
                )
                cachedOidcToken = response.body
                metric.countEvent(METRIC_CALL_STS_SUCCESS)
            } catch (e: HttpClientErrorException) {
                metric.countEvent(METRIC_CALL_STS_FAIL)
                throw e
            }
        }

        return cachedOidcToken!!.access_token
    }

    companion object {
        const val METRIC_CALL_STS = "call_sts"
        const val METRIC_CALL_STS_SUCCESS = "call_sts_success"
        const val METRIC_CALL_STS_FAIL = "call_sts_fail"
    }

    private fun getStsTokenUrl(): String {
        return "$baseUrl/rest/v1/sts/token?grant_type=client_credentials&scope=openid"
    }

    private fun authorizationHeader(): HttpHeaders {
        val credentials = basicHeader(username, password)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, credentials)
        return headers
    }
}

data class StsToken(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
) {
    val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(expires_in - 10L)

    companion object {
        fun shouldRenew(token: StsToken?): Boolean {
            if (token == null) {
                return true
            }

            return isExpired(token)
        }

        private fun isExpired(token: StsToken): Boolean {
            return token.expirationTime.isBefore(LocalDateTime.now())
        }
    }
}

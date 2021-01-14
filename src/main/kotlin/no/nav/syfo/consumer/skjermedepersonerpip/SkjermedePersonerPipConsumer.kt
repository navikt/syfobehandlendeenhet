package no.nav.syfo.consumer.skjermedepersonerpip

import no.nav.syfo.config.CacheConfig.Companion.CACHENAME_EGENANSATT
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@Service
class SkjermedePersonerPipConsumer @Inject constructor(
    private val metric: Metric,
    private val restTemplate: RestTemplate
) {
    private val log = LoggerFactory.getLogger(SkjermedePersonerPipConsumer::class.java)

    @Cacheable(cacheNames = [CACHENAME_EGENANSATT], key = "#personIdent", condition = "#personIdent != null")
    fun erSkjermet(
        callId: String,
        personIdent: String
    ): Boolean {
        try {
            val response = restTemplate.exchange(
                getSkjermedePersonerPipUrl(personIdent),
                HttpMethod.GET,
                entity(),
                String::class.java
            )
            val skjermedePersonerResponse = response.body!!
            metric.countOutgoingRequests("erSkjermet")
            return skjermedePersonerResponse.toBoolean()
        } catch (e: RestClientResponseException) {
            metric.countOutgoingRequestsFailed("erSkjermet", e.rawStatusCode.toString())
            log.error(
                "Call to get response from Skjermede Person failed with status: {} and message: {}. {}",
                e.rawStatusCode,
                e.responseBodyAsString,
                callIdArgument(callId)
            )
            throw e
        }
    }

    private fun entity(): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        headers[NAV_CALL_ID_HEADER] = createCallId()
        return HttpEntity(headers)
    }
}

fun getSkjermedePersonerPipUrl(personIdent: String): String {
    return "http://skjermede-personer-pip.nom.svc.nais.local/skjermet?personident=$personIdent"
}

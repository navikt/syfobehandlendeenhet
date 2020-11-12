package no.nav.syfo.consumers

import no.nav.syfo.domain.model.*
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.callIdArgument
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.util.*
import javax.inject.Inject

@Service
class NorgConsumer @Inject
constructor(
    @Value("\${norg2.url}") private val norg2BaseUrl: String,
    private val metric: Metric,
    private val restTemplate: RestTemplate
) {
    fun getArbeidsfordelingEnhet(
        callId: String,
        diskresjonskode: ArbeidsfordelingCriteriaDiskresjonskode?,
        geografiskTilknytning: String?,
        isEgenAnsatt: Boolean
    ): BehandlendeEnhet? {
        val enheter = getArbeidsfordelingEnheter(
            callId,
            diskresjonskode,
            geografiskTilknytning,
            isEgenAnsatt
        )
        if (enheter.isEmpty()) {
            return null
        }
        return enheter
            .filter { it.status == Enhetsstatus.AKTIV.formattedName }
            .map {
                BehandlendeEnhet(
                    it.enhetNr,
                    it.navn
                )
            }
            .first()
    }

    fun getArbeidsfordelingEnheter(
        callId: String,
        diskresjonskode: ArbeidsfordelingCriteriaDiskresjonskode?,
        geografiskTilknytning: String?,
        isEgenAnsatt: Boolean
    ): List<NorgEnhet> {
        val requestBody = ArbeidsfordelingCriteria(
            diskresjonskode = diskresjonskode?.name,
            tema = "OPP",
            geografiskOmraade = geografiskTilknytning,
            skjermet = isEgenAnsatt
        )
        try {
            val result = restTemplate
                .exchange(
                    getArbeidsfordelingUrl(),
                    HttpMethod.POST,
                    createRequestEntity(requestBody),
                    object : ParameterizedTypeReference<List<NorgEnhet>>() {}
                )

            val enhetList = Objects.requireNonNull(result.body)
            metric.countOutgoingRequests("getArbeidsfordelingEnheter")
            return enhetList
        } catch (e: RestClientResponseException) {
            metric.countOutgoingRequestsFailed("getArbeidsfordelingEnheter", e.rawStatusCode.toString())
            log.error(
                "Call to NORG2-arbeidsfordeling failed with status HTTP-{} for GeografiskTilknytning {}. {}",
                e.rawStatusCode,
                geografiskTilknytning,
                callIdArgument(callId)
            )
            throw e
        }
    }

    private fun getArbeidsfordelingUrl(): String {
        return "$norg2BaseUrl$ARBEIDSFORDELING_BESTMATCH_PATH"
    }

    private fun createRequestEntity(body: ArbeidsfordelingCriteria): HttpEntity<ArbeidsfordelingCriteria>? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, headers)
    }

    companion object {
        private val log = getLogger(NorgConsumer::class.java)

        const val ARBEIDSFORDELING_BESTMATCH_PATH = "/arbeidsfordeling/enheter/bestmatch"
    }
}

package no.nav.syfo.consumers

import no.nav.syfo.domain.model.*
import no.nav.syfo.metric.Metric
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*
import javax.inject.Inject

@Service
class NorgConsumer @Inject
constructor(
        @param:Value("\${norg2.url}") private val norg2BaseUrl: String,
        private val metric: Metric,
        private val restTemplate: RestTemplate
) {
    fun getArbeidsfordelingEnhet(geografiskTilknytning: String, isEgenAnsatt: Boolean): BehandlendeEnhet? {
        val enheter = getArbeidsfordelingEnheter(geografiskTilknytning, isEgenAnsatt)
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

    fun getArbeidsfordelingEnheter(geografiskTilknytning: String, isEgenAnsatt: Boolean): List<NorgEnhet> {
        val requestBody = ArbeidsfordelingCriteria(
                tema = "OPP",
                geografiskOmraade = geografiskTilknytning,
                skjermet = isEgenAnsatt
        )
        val result = restTemplate
                .exchange<List<NorgEnhet>>(
                        getArbeidsfordelingUrl(),
                        HttpMethod.POST,
                        createRequestEntity(requestBody),
                        object : ParameterizedTypeReference<List<NorgEnhet>>() {}
                )

        if (result.statusCode != HttpStatus.OK) {
            metric.countOutgoingRequestsFailed("getArbeidsfordelingEnheter", result.statusCode.toString())
            log.error("Kall mot NORG2 feiler med HTTP-{} for geografisk tilknytning {}", result.statusCode, geografiskTilknytning)
            throw RuntimeException("Henting av behandlendeenhet feilet med HTTP-" + result.statusCode)
        }
        val enhetList = Objects.requireNonNull<List<NorgEnhet>>(result.body)
        metric.countOutgoingRequests("getArbeidsfordelingEnheter")
        return enhetList
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

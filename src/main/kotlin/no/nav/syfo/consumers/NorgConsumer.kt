package no.nav.syfo.consumers

import no.nav.syfo.domain.model.BehandlendeEnhet
import no.nav.syfo.domain.model.NorgEnhet
import no.nav.syfo.metric.Metric
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.inject.Inject

@Service
class NorgConsumer @Inject
constructor(
        @param:Value("\${norg2.url}") private val norg2BaseUrl: String,
        private val metric: Metric,
        private val restTemplate: RestTemplate
) {

    private fun getNorg2Url(enhetId: String): String {
        return "$norg2BaseUrl/enhet/$enhetId/overordnet?organiseringsType=HABILITET"
    }

    fun getSetteKontor(enhetId: String): BehandlendeEnhet? {
        val url = getNorg2Url(enhetId)

        log.info("getSetteKontor for url {}", url)

        val queryBuilder = UriComponentsBuilder
                .fromHttpUrl(url)
        try {
            val result = restTemplate
                    .exchange<List<NorgEnhet>>(
                            queryBuilder.toUriString(),
                            HttpMethod.GET,
                            null,
                            object : ParameterizedTypeReference<List<NorgEnhet>>() {}
                    )
            val enhet = Objects.requireNonNull<List<NorgEnhet>>(result.body).first()
            metric.countOutgoingRequests("OrganisasjonEnhetConsumer")
            return BehandlendeEnhet(
                    enhet.enhetNr,
                    enhet.navn
            )

        } catch (e: HttpClientErrorException) {
            return if (e.rawStatusCode == 404) {
                log.warn("EnhetNr {} har ikke overordnet, Kall mot syfobehandlendeenhet returnerte med HTTP-{}", enhetId, e.statusCode)
                null
            } else {
                metric.countOutgoingRequestsFailed("OrganisasjonEnhetConsumer", "HentOverordnetEnhetListeEnhetIkkeFunnet")
                log.error("Kall mot syfobehandlendeenhet feiler med HTTP-{} for enhetNr {}", e.statusCode, enhetId)
                throw e
            }
        }
    }

    companion object {

        private val log = getLogger(NorgConsumer::class.java)
    }
}

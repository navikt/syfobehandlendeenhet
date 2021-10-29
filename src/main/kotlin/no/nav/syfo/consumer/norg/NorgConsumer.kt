package no.nav.syfo.consumer.norg

import no.nav.syfo.behandlendeenhet.BehandlendeEnhet
import no.nav.syfo.consumer.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.consumer.pdl.GeografiskTilknytning
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
    private val azureAdV2TokenConsumer: AzureAdV2TokenConsumer,
    @Value("\${isproxy.client.id}") private val isproxyClientId: String,
    @Value("\${isproxy.url}") private val baseUrl: String,
    private val metric: Metric,
    private val restTemplate: RestTemplate,
) {
    private val norg2ArbeidsfordelingBestmatchUrl: String = "$baseUrl$ARBEIDSFORDELING_BESTMATCH_PATH"

    fun getArbeidsfordelingEnhet(
        callId: String,
        diskresjonskode: ArbeidsfordelingCriteriaDiskresjonskode?,
        geografiskTilknytning: GeografiskTilknytning,
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
        geografiskTilknytning: GeografiskTilknytning,
        isEgenAnsatt: Boolean
    ): List<NorgEnhet> {
        val systemToken = azureAdV2TokenConsumer.getSystemToken(
            scopeClientId = isproxyClientId,
        )

        val requestBody = ArbeidsfordelingCriteria(
            diskresjonskode = diskresjonskode?.name,
            behandlingstype = ArbeidsfordelingCriteriaBehandlingstype.SYKEFRAVAERSOPPFØLGING.behandlingstype,
            tema = "OPP",
            geografiskOmraade = geografiskTilknytning.value,
            skjermet = isEgenAnsatt
        )
        try {
            val requestEntity = createRequestEntity(
                body = requestBody,
                token = systemToken,
            )
            val result = restTemplate
                .exchange(
                    norg2ArbeidsfordelingBestmatchUrl,
                    HttpMethod.POST,
                    requestEntity,
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

    private fun createRequestEntity(
        body: ArbeidsfordelingCriteria,
        token: String,
    ): HttpEntity<ArbeidsfordelingCriteria> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(token)
        return HttpEntity(body, headers)
    }

    companion object {
        private val log = getLogger(NorgConsumer::class.java)

        const val ARBEIDSFORDELING_BESTMATCH_PATH = "/api/v1/norg2/arbeidsfordeling/enheter/bestmatch"
    }
}

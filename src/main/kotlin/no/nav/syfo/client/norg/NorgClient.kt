package no.nav.syfo.client.norg

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.behandlendeenhet.BehandlendeEnhet
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.client.norg.domain.*
import no.nav.syfo.client.pdl.GeografiskTilknytning
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory.getLogger

class NorgClient(
    private val azureAdClient: AzureAdClient,
    private val clientId: String,
    baseUrl: String,
) {
    private val httpClient = httpClientDefault()

    private val norg2ArbeidsfordelingBestmatchUrl: String = "$baseUrl$ARBEIDSFORDELING_BESTMATCH_PATH"

    suspend fun getArbeidsfordelingEnhet(
        callId: String,
        diskresjonskode: ArbeidsfordelingCriteriaDiskresjonskode?,
        geografiskTilknytning: GeografiskTilknytning,
        isEgenAnsatt: Boolean,
    ): BehandlendeEnhet? {
        val enheter = getArbeidsfordelingEnheter(
            callId = callId,
            diskresjonskode = diskresjonskode,
            geografiskTilknytning = geografiskTilknytning,
            isEgenAnsatt = isEgenAnsatt,
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

    suspend fun getArbeidsfordelingEnheter(
        callId: String,
        diskresjonskode: ArbeidsfordelingCriteriaDiskresjonskode?,
        geografiskTilknytning: GeografiskTilknytning,
        isEgenAnsatt: Boolean,
    ): List<NorgEnhet> {
        val systemToken = azureAdClient.getSystemToken(
            scopeClientId = clientId,
        )?.accessToken
            ?: throw RuntimeException("Failed to request NorgEnhet from Isproxy-Norg2: Failed to get system token from AzureAD")

        val requestBody = ArbeidsfordelingCriteria(
            diskresjonskode = diskresjonskode?.name,
            behandlingstype = ArbeidsfordelingCriteriaBehandlingstype.SYKEFRAVAERSOPPFOLGING.behandlingstype,
            tema = "OPP",
            geografiskOmraade = geografiskTilknytning.value,
            skjermet = isEgenAnsatt,
        )
        try {
            val response: List<NorgEnhet> = httpClient.post(norg2ArbeidsfordelingBestmatchUrl) {
                header(HttpHeaders.Authorization, bearerHeader(systemToken))
                header(NAV_CALL_ID_HEADER, callId)
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                body = requestBody
            }
            COUNT_CALL_NORG_ARBEIDSFORDELING_SUCCESS.increment()
            return response
        } catch (e: ResponseException) {
            COUNT_CALL_NORG_ARBEIDSFORDELING_FAIL.increment()
            log.error(
                "Call to Isproxy-NORG2-arbeidsfordeling failed with status HTTP-{} for GeografiskTilknytning {}. {}",
                e.response.status,
                geografiskTilknytning,
                callIdArgument(callId)
            )
            throw e
        }
    }

    companion object {
        private val log = getLogger(NorgClient::class.java)

        const val ARBEIDSFORDELING_BESTMATCH_PATH = "/api/v1/norg2/arbeidsfordeling/enheter/bestmatch"
    }
}
package no.nav.syfo.infrastructure.client.norg

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.behandlendeenhet.BehandlendeEnhet
import no.nav.syfo.infrastructure.client.norg.domain.*
import no.nav.syfo.infrastructure.client.pdl.GeografiskTilknytning
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory.getLogger

class NorgClient(
    baseUrl: String,
    private val httpClient: HttpClient = no.nav.syfo.infrastructure.client.httpClientDefault(),
) {

    private val norg2ArbeidsfordelingBestmatchUrl: String = "$baseUrl$ARBEIDSFORDELING_BESTMATCH_PATH"
    private val norg2Enhetsnavn: String = "$baseUrl$ENHETSNAVN_PATH"

    suspend fun getEnhetsnavn(
        enhetsnr: String,
    ): String? =
        try {
            val response: NorgEnhet? = httpClient.get("$norg2Enhetsnavn$enhetsnr") {
                accept(ContentType.Application.Json)
            }.body()
            response?.navn
        } catch (e: ResponseException) {
            log.error("Call to NORG2-enhet failed with status HTTP-{} for enhetsnr {}", e.response.status, enhetsnr)
            null
        }

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
        val requestBody = ArbeidsfordelingCriteria(
            diskresjonskode = diskresjonskode?.name,
            behandlingstype = getBehandlingstype(),
            tema = "OPP",
            geografiskOmraade = geografiskTilknytning.value,
            skjermet = isEgenAnsatt,
        )
        try {
            val response: List<NorgEnhet> = httpClient.post(norg2ArbeidsfordelingBestmatchUrl) {
                header(NAV_CALL_ID_HEADER, callId)
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()
            COUNT_CALL_NORG_ARBEIDSFORDELING_SUCCESS.increment()
            return response
        } catch (e: ResponseException) {
            COUNT_CALL_NORG_ARBEIDSFORDELING_FAIL.increment()
            log.error(
                "Call to NORG2-arbeidsfordeling failed with status HTTP-{} for GeografiskTilknytning {}. {}",
                e.response.status,
                geografiskTilknytning,
                callIdArgument(callId)
            )
            throw e
        }
    }

    private fun getBehandlingstype() =
        ArbeidsfordelingCriteriaBehandlingstype.SYKEFRAVAERSOPPFOLGING.behandlingstype

    companion object {
        private val log = getLogger(NorgClient::class.java)

        const val ARBEIDSFORDELING_BESTMATCH_PATH = "/norg2/api/v1/arbeidsfordeling/enheter/bestmatch"
        const val ENHETSNAVN_PATH = "/norg2/api/v1/enhet/"
    }
}

package no.nav.syfo.infrastructure.client.norg

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.behandlendeenhet.BehandlendeEnhet
import no.nav.syfo.domain.Enhet
import no.nav.syfo.infrastructure.cache.ValkeyStore
import no.nav.syfo.infrastructure.client.norg.domain.*
import no.nav.syfo.infrastructure.client.pdl.GeografiskTilknytning
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory.getLogger

class NorgClient(
    val baseUrl: String,
    val valkeyStore: ValkeyStore,
    private val httpClient: HttpClient = no.nav.syfo.infrastructure.client.httpClientDefault(),
) {

    private val norg2ArbeidsfordelingBestmatchUrl: String = "$baseUrl$ARBEIDSFORDELING_BESTMATCH_PATH"
    private val norg2Enhetsnavn: String = "$baseUrl$ENHETSNAVN_PATH"

    suspend fun getEnhetsnavn(
        enhetsnr: String,
    ): String? =
        getNorgEnhet(enhetsnr)?.navn

    suspend fun getNorgEnhet(
        enhetsnr: String,
    ): NorgEnhet? {
        val cacheKey = "${CACHE_NORGENHET_KEY_PREFIX}$enhetsnr"
        val cachedEnhet: NorgEnhet? = valkeyStore.getObject(key = cacheKey)
        return if (cachedEnhet != null) {
            COUNT_CALL_NORG_ENHET_CACHE_HIT.increment()
            cachedEnhet
        } else {
            COUNT_CALL_NORG_ENHET_CACHE_MISS.increment()
            val enhet: NorgEnhet? = try {
                httpClient.get("$norg2Enhetsnavn$enhetsnr") {
                    accept(ContentType.Application.Json)
                }.body()
            } catch (e: ResponseException) {
                log.error("Call to NORG2-enhet failed with status HTTP-{} for enhetsnr {}", e.response.status, enhetsnr)
                null
            }
            if (enhet != null) {
                valkeyStore.setObject(
                    key = cacheKey,
                    value = enhet,
                    expireSeconds = CACHE_NORG_EXPIRE_SECONDS
                )
            }
            enhet
        }
    }

    suspend fun getArbeidsfordelingEnhet(
        callId: String,
        diskresjonskode: ArbeidsfordelingCriteriaDiskresjonskode?,
        geografiskTilknytning: GeografiskTilknytning,
        isEgenAnsatt: Boolean,
    ): BehandlendeEnhet? =
        getArbeidsfordelingEnheter(
            callId = callId,
            diskresjonskode = diskresjonskode,
            geografiskTilknytning = geografiskTilknytning,
            isEgenAnsatt = isEgenAnsatt,
        )
            .filter { it.status == Enhetsstatus.AKTIV.formattedName }
            .map {
                BehandlendeEnhet(
                    it.enhetNr,
                    it.navn
                )
            }
            .firstOrNull()

    private suspend fun getArbeidsfordelingEnheter(
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

    suspend fun getOverordnetEnhet(
        callId: String,
        enhet: Enhet,
    ): NorgEnhet? {
        val cacheKey = "${CACHE_NORGENHET_OVERORDNET_KEY_PREFIX}${enhet.value}"
        val cachedEnhet: NorgEnhet? = valkeyStore.getObject(key = cacheKey)
        return if (cachedEnhet != null) {
            COUNT_CALL_NORG_OVERORDNET_ENHET_CACHE_HIT.increment()
            cachedEnhet
        } else {
            COUNT_CALL_NORG_OVERORDNET_ENHET_CACHE_MISS.increment()
            val url = getOverordnetEnhetForNAVKontorUrl(enhet.value)
            val norgEnhet: NorgEnhet? = try {
                val response: List<NorgEnhet> = httpClient.get(url) {
                    header(NAV_CALL_ID_HEADER, callId)
                    accept(ContentType.Application.Json)
                }.body()

                if (response.isEmpty()) {
                    log.warn("No overordnet enhet returned from NORG2 for enhet $enhet, callId=$callId")
                }
                response.firstOrNull()
            } catch (e: ResponseException) {
                if (e.response.status == HttpStatusCode.NotFound) {
                    null
                } else {
                    val message =
                        "Call to NORG2 for overordnet enhet failed with status HTTP-${e.response.status} for enhet $enhet, callId=$callId"
                    log.error(message)
                    throw e
                }
            }
            if (norgEnhet != null) {
                valkeyStore.setObject(
                    key = cacheKey,
                    value = norgEnhet,
                    expireSeconds = CACHE_NORG_EXPIRE_SECONDS
                )
            }
            norgEnhet
        }
    }

    suspend fun getUnderenheter(
        callId: String,
        enhet: Enhet,
    ): List<NorgEnhet> {
        val cacheKey = "${CACHE_NORGENHET_UNDERORDNET_KEY_PREFIX}${enhet.value}"
        val cachedEnhet: List<NorgEnhet>? = valkeyStore.getListObject<NorgEnhet>(cacheKey)
        return if (cachedEnhet != null) {
            COUNT_CALL_NORG_UNDERORDNET_ENHET_CACHE_HIT.increment()
            cachedEnhet
        } else {
            COUNT_CALL_NORG_UNDERORDNET_ENHET_CACHE_MISS.increment()
            val url = getOrganiseringForEnhetUrl(enhet.value)
            val underenheter = try {
                val response: List<RsOrganisering> = httpClient.get(url) {
                    header(NAV_CALL_ID_HEADER, callId)
                    accept(ContentType.Application.Json)
                }.body()

                if (response.isEmpty()) {
                    log.error("No underenheter returned from NORG2 for enhet $enhet, callId=$callId")
                    throw RuntimeException("No underenheter returned from NORG2 for enhet $enhet, callId=$callId")
                }
                response
                    .mapNotNull { it.organisertUnder?.nr }
                    .mapNotNull { getNorgEnhet(it) }
                    .filter { it.type == ENHET_TYPE_LOKAL && it.status == Enhetsstatus.AKTIV.formattedName }
            } catch (e: ResponseException) {
                if (e.response.status == HttpStatusCode.NotFound) {
                    emptyList()
                } else {
                    val message =
                        "Call to NORG2 for overordnet enhet failed with status HTTP-${e.response.status} for enhet $enhet, callId=$callId"
                    log.error(message)
                    throw e
                }
            }
            if (underenheter.isNotEmpty()) {
                valkeyStore.setObject(
                    key = cacheKey,
                    value = underenheter,
                    expireSeconds = CACHE_NORG_EXPIRE_SECONDS
                )
            }
            underenheter
        }
    }

    private fun getBehandlingstype() =
        ArbeidsfordelingCriteriaBehandlingstype.SYKEFRAVAERSOPPFOLGING.behandlingstype

    private fun getOverordnetEnhetForNAVKontorUrl(enhetNr: String): String {
        return "$baseUrl/norg2/api/v1/enhet/$enhetNr/overordnet?organiseringsType=$ORGANISERINGSTYPE"
    }

    private fun getOrganiseringForEnhetUrl(enhetNr: String): String {
        return "$baseUrl/norg2/api/v1/enhet/$enhetNr/organisering"
    }

    companion object {
        private val log = getLogger(NorgClient::class.java)

        const val CACHE_NORGENHET_KEY_PREFIX = "norgenhet-enhetnr-"
        const val CACHE_NORGENHET_OVERORDNET_KEY_PREFIX = "norgenhet-overordnet-"
        const val CACHE_NORGENHET_UNDERORDNET_KEY_PREFIX = "norgenhet-underordnet-"
        const val CACHE_NORG_EXPIRE_SECONDS = 12 * 60 * 60L

        const val ARBEIDSFORDELING_BESTMATCH_PATH = "/norg2/api/v1/arbeidsfordeling/enheter/bestmatch"
        const val ENHETSNAVN_PATH = "/norg2/api/v1/enhet/"
        const val ORGANISERINGSTYPE = "FYLKE"
        const val ENHET_TYPE_LOKAL = "LOKAL"
    }
}

package no.nav.syfo.infrastructure.client.pdl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.infrastructure.client.azuread.AzureAdClient
import no.nav.syfo.infrastructure.client.pdl.domain.*
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.infrastructure.client.pdl.domain.IdentType
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class PdlClient(
    private val azureAdClient: AzureAdClient,
    private val baseUrl: String,
    private val clientId: String,
    private val httpClient: HttpClient = no.nav.syfo.infrastructure.client.httpClientDefault(),
) {

    suspend fun geografiskTilknytning(
        callId: String,
        personIdentNumber: PersonIdentNumber,
    ): GeografiskTilknytning {
        val systemToken = azureAdClient.getSystemToken(
            scopeClientId = clientId,
        )?.accessToken
            ?: throw RuntimeException("Failed to request PDL: Failed to get system token from AzureAD")

        val query = getPdlQuery("/pdl/hentGeografiskTilknytning.graphql")
        val request = PdlGeografiskTilknytningRequest(
            query = query,
            variables = PdlGeografiskTilknytningRequestVariables(personIdentNumber.value)
        )
        try {
            val pdlPersonResponse: PdlGeografiskTilknytningResponse = httpClient.post(baseUrl) {
                header(HttpHeaders.Authorization, bearerHeader(systemToken))
                header(BEHANDLINGSNUMMER_HEADER_KEY, BEHANDLINGSNUMMER_HEADER_VALUE)
                header(NAV_CALL_ID_HEADER, callId)
                header(GT_HEADER, GT_HEADER)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            if (pdlPersonResponse.errors != null && pdlPersonResponse.errors.isNotEmpty()) {
                COUNT_CALL_PDL_GT_FAIL.increment()
                pdlPersonResponse.errors.forEach {
                    log.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                }
                throw RuntimeException("No Geografisk Tilknytning was found in response from PDL: Errors found in response")
            } else if (pdlPersonResponse.data == null) {
                COUNT_CALL_PDL_GT_FAIL.increment()
                val errorMessage =
                    "No Geografisk Tilknytning was found in response from PDL: No data was found in response"
                log.error("Error while requesting person from PersonDataLosningen: $errorMessage")
                throw throw RuntimeException(errorMessage)
            } else {
                COUNT_CALL_PDL_GT_SUCCESS.increment()
                return pdlPersonResponse.data.hentGeografiskTilknytning?.geografiskTilknytning()
                    ?: throw GeografiskTilknytningNotFoundException()
            }
        } catch (e: ResponseException) {
            log.error(
                "Error while requesting GeografiskTilknytning from PersonDataLosningen {}, {}, {}",
                StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                StructuredArguments.keyValue("message", e.message),
                callIdArgument(callId),
            )
            COUNT_CALL_PDL_GT_FAIL.increment()
            throw e
        }
    }

    suspend fun person(
        callId: String,
        personIdentNumber: PersonIdentNumber,
    ): PdlHentPerson? {
        val systemToken = azureAdClient.getSystemToken(
            scopeClientId = clientId,
        )?.accessToken
            ?: throw RuntimeException("Failed to request PDL: Failed to get system token from AzureAD")

        val query = getPdlQuery("/pdl/hentPerson.graphql")
        val request = PdlRequest(
            query = query,
            variables = Variables(personIdentNumber.value)
        )
        try {
            val pdlPersonResponse: PdlPersonResponse = httpClient.post(baseUrl) {
                header(HttpHeaders.Authorization, bearerHeader(systemToken))
                header(BEHANDLINGSNUMMER_HEADER_KEY, BEHANDLINGSNUMMER_HEADER_VALUE)
                header(NAV_CALL_ID_HEADER, callId)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            return if (pdlPersonResponse.errors != null && pdlPersonResponse.errors.isNotEmpty()) {
                COUNT_CALL_PDL_PERSON_FAIL.increment()
                pdlPersonResponse.errors.forEach {
                    log.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                }
                null
            } else {
                COUNT_CALL_PDL_PERSON_SUCCESS.increment()
                pdlPersonResponse.data
            }
        } catch (e: ResponseException) {
            log.error(
                "Error while requesting Person from PersonDataLosningen {}, {}, {}",
                StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                StructuredArguments.keyValue("message", e.message),
                callIdArgument(callId),
            )
            COUNT_CALL_PDL_GT_FAIL.increment()
            throw e
        }
    }

    suspend fun getPdlIdenter(
        personIdent: PersonIdentNumber,
        callId: String? = null,
    ): PdlHentIdenter? {
        val token = azureAdClient.getSystemToken(clientId)?.accessToken
            ?: throw RuntimeException("Failed to send PdlHentIdenterRequest to PDL: No token was found")

        val query = getPdlQuery(
            queryFilePath = "/pdl/hentIdenter.graphql",
        )

        val request = PdlHentIdenterRequest(
            query = query,
            variables = PdlHentIdenterRequestVariables(
                ident = personIdent.value,
                historikk = true,
                grupper = listOf(
                    IdentType.FOLKEREGISTERIDENT,
                ),
            ),
        )

        val response: HttpResponse = httpClient.post(baseUrl) {
            header(HttpHeaders.Authorization, bearerHeader(token))
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(BEHANDLINGSNUMMER_HEADER_KEY, BEHANDLINGSNUMMER_HEADER_VALUE)
            header(NAV_CALL_ID_HEADER, callId)
            header(IDENTER_HEADER, IDENTER_HEADER)
            setBody(request)
        }

        when (response.status) {
            HttpStatusCode.OK -> {
                val pdlIdenterResponse = response.body<PdlIdenterResponse>()
                return if (!pdlIdenterResponse.errors.isNullOrEmpty()) {
                    COUNT_CALL_PDL_IDENTER_FAIL.increment()
                    pdlIdenterResponse.errors.forEach {
                        log.error("Error while requesting IdentList from PersonDataLosningen: ${it.errorMessage()}")
                    }
                    null
                } else {
                    COUNT_CALL_PDL_IDENTER_SUCCESS.increment()
                    pdlIdenterResponse.data
                }
            }
            else -> {
                COUNT_CALL_PDL_IDENTER_FAIL.increment()
                log.error("Request to get IdentList with url: $clientId failed with reponse code ${response.status.value}")
                return null
            }
        }
    }

    private fun getPdlQuery(queryFilePath: String): String {
        return this::class.java.getResource(queryFilePath)
            .readText()
            .replace("[\n\r]", "")
    }

    companion object {
        private val log = LoggerFactory.getLogger(PdlClient::class.java)
        const val IDENTER_HEADER = "identer"
        const val GT_HEADER = "geografisktilknytning"

        // Se behandlingskatalog https://behandlingskatalog.intern.nav.no/
        // Behandling: Sykefraværsoppfølging: Vurdere behov for oppfølging og rett til sykepenger etter §§ 8-4 og 8-8
        private const val BEHANDLINGSNUMMER_HEADER_KEY = "behandlingsnummer"
        private const val BEHANDLINGSNUMMER_HEADER_VALUE = "B426"
    }
}

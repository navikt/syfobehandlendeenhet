package no.nav.syfo.client.pdl

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.client.pdl.domain.*
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class PdlClient(
    private val azureAdClient: AzureAdClient,
    private val baseUrl: String,
    private val clientId: String,
) {
    private val httpClient = httpClientDefault()

    suspend fun geografiskTilknytning(
        callId: String,
        personIdentNumber: PersonIdentNumber,
    ): GeografiskTilknytning {
        return geografiskTilknytningResponse(
            callId = callId,
            personIdentNumber = personIdentNumber,
        )?.geografiskTilknytning()
            ?: throw RuntimeException("No Geografisk Tilknytning was found in response from PDL")
    }

    suspend fun geografiskTilknytningResponse(
        callId: String,
        personIdentNumber: PersonIdentNumber,
    ): PdlHentGeografiskTilknytning? {
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
            val pdlPersonReponse: PdlGeografiskTilknytningResponse = httpClient.post(baseUrl) {
                header(HttpHeaders.Authorization, bearerHeader(systemToken))
                header(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
                header(NAV_CALL_ID_HEADER, callId)
                header(GT_HEADER, GT_HEADER)
                contentType(ContentType.Application.Json)
                body = request
            }
            return if (pdlPersonReponse.errors != null && pdlPersonReponse.errors.isNotEmpty()) {
                COUNT_CALL_PDL_GT_FAIL.increment()
                pdlPersonReponse.errors.forEach {
                    log.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                }
                null
            } else {
                COUNT_CALL_PDL_GT_SUCCESS.increment()
                pdlPersonReponse.data
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
            val pdlPersonReponse: PdlPersonResponse = httpClient.post(baseUrl) {
                header(HttpHeaders.Authorization, bearerHeader(systemToken))
                header(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
                header(NAV_CALL_ID_HEADER, callId)
                contentType(ContentType.Application.Json)
                body = request
            }
            return if (pdlPersonReponse.errors != null && pdlPersonReponse.errors.isNotEmpty()) {
                COUNT_CALL_PDL_PERSON_FAIL.increment()
                pdlPersonReponse.errors.forEach {
                    log.error("Error while requesting person from PersonDataLosningen: ${it.errorMessage()}")
                }
                null
            } else {
                COUNT_CALL_PDL_PERSON_SUCCESS.increment()
                pdlPersonReponse.data
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

    private fun getPdlQuery(queryFilePath: String): String {
        return this::class.java.getResource(queryFilePath)
            .readText()
            .replace("[\n\r]", "")
    }

    companion object {
        private val log = LoggerFactory.getLogger(PdlClient::class.java)

        const val GT_HEADER = "geografisktilknytning"
    }
}

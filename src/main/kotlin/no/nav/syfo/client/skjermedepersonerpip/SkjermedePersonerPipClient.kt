package no.nav.syfo.client.skjermedepersonerpip

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.httpClientDefault
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

class SkjermedePersonerPipClient(
    private val azureAdClient: AzureAdClient,
    private val baseUrl: String,
    private val clientId: String,
    private val redisStore: RedisStore,
) {
    private val httpClient = httpClientDefault()

    suspend fun isSkjermet(
        callId: String,
        personIdentNumber: PersonIdentNumber,
    ): Boolean {
        val oboToken = azureAdClient.getSystemToken(
            scopeClientId = clientId,
        )?.accessToken ?: throw RuntimeException("Failed to request access to Skjerming: Failed to get OBO token")

        val cacheKey = "$CACHE_SKJERMET_PERSONIDENT_KEY_PREFIX${personIdentNumber.value}"
        val cachedValue: Boolean? = redisStore.getObject(key = cacheKey)
        if (cachedValue != null) {
            return cachedValue
        } else {
            try {
                val url = getSkjermedePersonerPipUrl(personIdentNumber = personIdentNumber)
                val skjermedePersonerResponse: Boolean = httpClient.get(url) {
                    header(HttpHeaders.Authorization, bearerHeader(oboToken))
                    header(NAV_CALL_ID_HEADER, callId)
                    header(NAV_CONSUMER_ID_HEADER, NAV_APP_CONSUMER_ID)
                    header(NAV_PERSONIDENTER_HEADER, personIdentNumber.value)
                }

                COUNT_CALL_SKJERMEDE_PERSONER_SKJERMET_SUCCESS.increment()
                redisStore.setObject(
                    expireSeconds = CACHE_SKJERMET_PERSONIDENT_EXPIRE_SECONDS,
                    key = cacheKey,
                    value = skjermedePersonerResponse,
                )
                return skjermedePersonerResponse
            } catch (e: ResponseException) {
                log.error(
                    "Error while requesting Response from Skjermede Person {}, {}, {}",
                    StructuredArguments.keyValue("statusCode", e.response.status.value.toString()),
                    StructuredArguments.keyValue("message", e.message),
                    StructuredArguments.keyValue("callId", callId),
                )
                COUNT_CALL_SKJERMEDE_PERSONER__SKJERMET_FAIL.increment()
                throw e
            }
        }
    }

    private fun getSkjermedePersonerPipUrl(personIdentNumber: PersonIdentNumber): String {
        return "$baseUrl/skjermet?personident=${personIdentNumber.value}"
    }

    companion object {
        const val CACHE_SKJERMET_PERSONIDENT_KEY_PREFIX = "skjermet-personident"
        const val CACHE_SKJERMET_PERSONIDENT_EXPIRE_SECONDS = 60 * 60L

        private val log = LoggerFactory.getLogger(SkjermedePersonerPipClient::class.java)
    }
}

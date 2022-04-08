package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.client.features.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.metrics.micrometer.*
import io.ktor.response.*
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import no.nav.syfo.application.metric.METRICS_REGISTRY
import no.nav.syfo.behandlendeenhet.api.access.ForbiddenAccessSystemConsumer
import no.nav.syfo.client.pdl.GeografiskTilknytningNotFoundException
import no.nav.syfo.util.*
import java.time.Duration
import java.util.*

fun Application.installMetrics() {
    install(MicrometerMetrics) {
        registry = METRICS_REGISTRY
        distributionStatisticConfig = DistributionStatisticConfig.Builder()
            .percentilesHistogram(true)
            .maximumExpectedValue(Duration.ofSeconds(20).toNanos().toDouble())
            .build()
    }
}

fun Application.installCallId() {
    install(CallId) {
        header(NAV_CALL_ID_HEADER)
        generate { "generated-${UUID.randomUUID()}" }
        verify { callId: String -> callId.isNotEmpty() }
    }
}

fun Application.installContentNegotiation() {
    install(ContentNegotiation) {
        jackson(block = configureJacksonMapper())
    }
}

fun Application.installStatusPages() {
    install(StatusPages) {
        exception<Throwable> { cause ->
            val responseStatus: HttpStatusCode = when (cause) {
                is ResponseException -> {
                    cause.response.status
                }
                is IllegalArgumentException -> {
                    HttpStatusCode.BadRequest
                }
                is ForbiddenAccessVeilederException -> {
                    HttpStatusCode.Forbidden
                }
                is ForbiddenAccessSystemConsumer -> {
                    HttpStatusCode.Forbidden
                }
                is GeografiskTilknytningNotFoundException -> {
                    HttpStatusCode.NoContent
                }
                else -> {
                    HttpStatusCode.InternalServerError
                }
            }

            val callId = getCallId()
            val consumerClientId = getConsumerClientId()
            val errorMessage = "Caught exception, callId=$callId, consumerClientId=$consumerClientId"
            if (cause is ForbiddenAccessVeilederException || cause is ForbiddenAccessSystemConsumer) {
                log.info(errorMessage, cause)
            } else if (cause is IllegalArgumentException || cause is GeografiskTilknytningNotFoundException) {
                log.warn(errorMessage, cause)
            } else {
                log.error(errorMessage, cause)
            }
            val message = cause.message ?: "Unknown error"
            call.respond(responseStatus, message)
        }
    }
}

class ForbiddenAccessVeilederException(
    message: String = "Denied NAVIdent access to personIdent",
) : RuntimeException(message)

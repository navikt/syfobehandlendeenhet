package no.nav.syfo.application.api

import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
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
        jackson {
            configure()
        }
    }
}

fun Application.installStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val log = call.application.log

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

            val callId = call.getCallId()
            val consumerClientId = call.getConsumerClientId()
            val errorMessage = "Caught exception, callId=$callId, consumerClientId=$consumerClientId"
            when (cause) {
                is ForbiddenAccessVeilederException, is ForbiddenAccessSystemConsumer -> {
                    log.info(errorMessage, cause)
                }
                is IllegalArgumentException, is GeografiskTilknytningNotFoundException -> {
                    log.warn(errorMessage, cause)
                }
                else -> {
                    log.error(errorMessage, cause)
                }
            }
            val message = cause.message ?: "Unknown error"
            call.respond(responseStatus, message)
        }
    }
}

class ForbiddenAccessVeilederException(
    message: String = "Denied NAVIdent access to personIdent",
) : RuntimeException(message)

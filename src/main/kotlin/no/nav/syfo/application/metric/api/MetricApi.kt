package no.nav.syfo.application.metric.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.application.metric.METRICS_REGISTRY

const val metricApiPath = "/internal/metrics"

fun Routing.registerMetricApi() {
    get(metricApiPath) {
        call.respondText(METRICS_REGISTRY.scrape())
    }
}

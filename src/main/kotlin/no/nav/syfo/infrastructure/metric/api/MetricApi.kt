package no.nav.syfo.infrastructure.metric.api

import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.infrastructure.metric.METRICS_REGISTRY

const val metricApiPath = "/internal/metrics"

fun Routing.registerMetricApi() {
    get(metricApiPath) {
        call.respondText(METRICS_REGISTRY.scrape())
    }
}

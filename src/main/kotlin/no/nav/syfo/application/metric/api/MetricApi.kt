package no.nav.syfo.application.metric.api

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.syfo.application.metric.METRICS_REGISTRY

const val metricApiPath = "/internal/metrics"

fun Routing.registerMetricApi() {
    get(metricApiPath) {
        call.respondText(METRICS_REGISTRY.scrape())
    }
}

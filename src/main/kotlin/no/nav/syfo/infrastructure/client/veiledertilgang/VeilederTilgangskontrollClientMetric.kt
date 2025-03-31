package no.nav.syfo.infrastructure.client.veiledertilgang

import io.micrometer.core.instrument.Counter
import no.nav.syfo.infrastructure.metric.METRICS_NS
import no.nav.syfo.infrastructure.metric.METRICS_REGISTRY

const val CALL_TILGANGSKONTROLL_SYFO_BASE = "${METRICS_NS}_call_tilgangskontroll_syfo"
const val CALL_TILGANGSKONTROLL_SYFO_SUCCESS = "${CALL_TILGANGSKONTROLL_SYFO_BASE}_success_count"
const val CALL_TILGANGSKONTROLL_SYFO_FAIL = "${CALL_TILGANGSKONTROLL_SYFO_BASE}_fail_count"
const val CALL_TILGANGSKONTROLL_SYFO_FORBIDDEN = "${CALL_TILGANGSKONTROLL_SYFO_BASE}_forbidden_count"

const val CALL_TILGANGSKONTROLL_BRUKERE_BASE = "${METRICS_NS}_call_tilgangskontroll_brukere"
const val CALL_TILGANGSKONTROLL_BRUKERE_SUCCESS = "${CALL_TILGANGSKONTROLL_BRUKERE_BASE}_success_count"
const val CALL_TILGANGSKONTROLL_BRUKERE_FAIL = "${CALL_TILGANGSKONTROLL_BRUKERE_BASE}_fail_count"
const val CALL_TILGANGSKONTROLL_BRUKERE_FORBIDDEN = "${CALL_TILGANGSKONTROLL_BRUKERE_BASE}_forbidden_count"

val COUNT_CALL_TILGANGSKONTROLL_SYFO_SUCCESS: Counter = Counter.builder(CALL_TILGANGSKONTROLL_SYFO_SUCCESS)
    .description("Counts the number of successful calls to istilgangskontroll - person")
    .register(METRICS_REGISTRY)
val COUNT_CALL_TILGANGSKONTROLL_SYFO_FAIL: Counter = Counter.builder(CALL_TILGANGSKONTROLL_SYFO_FAIL)
    .description("Counts the number of failed calls to istilgangskontroll - person")
    .register(METRICS_REGISTRY)
val COUNT_CALL_TILGANGSKONTROLL_SYFO_FORBIDDEN: Counter = Counter.builder(CALL_TILGANGSKONTROLL_SYFO_FORBIDDEN)
    .description("Counts the number of forbidden calls to istilgangskontroll - person")
    .register(METRICS_REGISTRY)

val COUNT_CALL_TILGANGSKONTROLL_BRUKERE_SUCCESS: Counter = Counter.builder(CALL_TILGANGSKONTROLL_BRUKERE_SUCCESS)
    .description("Counts the number of successful calls to istilgangskontroll - /brukere")
    .register(METRICS_REGISTRY)
val COUNT_CALL_TILGANGSKONTROLL_BRUKERE_FAIL: Counter = Counter.builder(CALL_TILGANGSKONTROLL_BRUKERE_FAIL)
    .description("Counts the number of failed calls to istilgangskontroll - /brukere")
    .register(METRICS_REGISTRY)
val COUNT_CALL_TILGANGSKONTROLL_BRUKERE_FORBIDDEN: Counter = Counter.builder(CALL_TILGANGSKONTROLL_BRUKERE_FORBIDDEN)
    .description("Counts the number of forbidden calls to istilgangskontroll - /brukere")
    .register(METRICS_REGISTRY)

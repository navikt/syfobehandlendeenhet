package no.nav.syfo.infrastructure.client.skjermedepersonerpip

import io.micrometer.core.instrument.Counter
import no.nav.syfo.infrastructure.metric.METRICS_NS
import no.nav.syfo.infrastructure.metric.METRICS_REGISTRY

const val CALL_SKJERMEDE_PERSONER_BASE = "${METRICS_NS}_skjermede_personer"
const val CALL_SKJERMEDE_PERSONER_SKJERMET_BASE = "${CALL_SKJERMEDE_PERSONER_BASE}_skjermet"
const val CALL_SKJERMEDE_PERSONER_SKJERMET_SUCCESS = "${CALL_SKJERMEDE_PERSONER_SKJERMET_BASE}_success_count"
const val CALL_SKJERMEDE_PERSONER_SKJERMET_FAIL = "${CALL_SKJERMEDE_PERSONER_SKJERMET_BASE}_fail_count"

val COUNT_CALL_SKJERMEDE_PERSONER_SKJERMET_SUCCESS: Counter = Counter.builder(CALL_SKJERMEDE_PERSONER_SKJERMET_SUCCESS)
    .description("Counts the number of successful calls to skjermedePersonerPip - skjermed")
    .register(METRICS_REGISTRY)
val COUNT_CALL_SKJERMEDE_PERSONER__SKJERMET_FAIL: Counter = Counter.builder(CALL_SKJERMEDE_PERSONER_SKJERMET_FAIL)
    .description("Counts the number of failed calls to skjermedePersonerPip - skjermed")
    .register(METRICS_REGISTRY)

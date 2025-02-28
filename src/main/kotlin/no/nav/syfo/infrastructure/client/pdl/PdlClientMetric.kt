package no.nav.syfo.infrastructure.client.pdl

import io.micrometer.core.instrument.Counter
import no.nav.syfo.infrastructure.metric.METRICS_NS
import no.nav.syfo.infrastructure.metric.METRICS_REGISTRY

const val CALL_PDL_BASE = "${METRICS_NS}_call_pdl"

const val CALL_PDL_GT_BASE = "${CALL_PDL_BASE}_gt"
const val CALL_PDL_GT_SUCCESS = "${CALL_PDL_GT_BASE}_success_count"
const val CALL_PDL_GT_FAIL = "${CALL_PDL_GT_BASE}_fail_count"

val COUNT_CALL_PDL_GT_SUCCESS: Counter = Counter.builder(CALL_PDL_GT_SUCCESS)
    .description("Counts the number of successful calls to persondatalosning - geografiskTilknytnin")
    .register(METRICS_REGISTRY)
val COUNT_CALL_PDL_GT_FAIL: Counter = Counter.builder(CALL_PDL_GT_FAIL)
    .description("Counts the number of failed calls to persondatalosning - geografiskTilknytnin")
    .register(METRICS_REGISTRY)

const val CALL_PDL_PERSON_BASE = "${CALL_PDL_BASE}_person"
const val CALL_PDL_PERSON_SUCCESS = "${CALL_PDL_PERSON_BASE}_success_count"
const val CALL_PDL_PERSON_FAIL = "${CALL_PDL_PERSON_BASE}_fail_count"

val COUNT_CALL_PDL_PERSON_SUCCESS: Counter = Counter.builder(CALL_PDL_PERSON_SUCCESS)
    .description("Counts the number of successful calls to persondatalosning - person")
    .register(METRICS_REGISTRY)
val COUNT_CALL_PDL_PERSON_FAIL: Counter = Counter.builder(CALL_PDL_PERSON_FAIL)
    .description("Counts the number of failed calls to persondatalosning - person")
    .register(METRICS_REGISTRY)

const val CALL_PDL_IDENTER_BASE = "${METRICS_NS}_call_pdl_identer"
const val CALL_PDL_IDENTER_SUCCESS = "${CALL_PDL_IDENTER_BASE}_success_count"
const val CALL_PDL_IDENTER_FAIL = "${CALL_PDL_IDENTER_BASE}_fail_count"

val COUNT_CALL_PDL_IDENTER_SUCCESS: Counter = Counter.builder(CALL_PDL_IDENTER_SUCCESS)
    .description("Counts the number of successful calls to persondatalosning - hentIdenter")
    .register(METRICS_REGISTRY)
val COUNT_CALL_PDL_IDENTER_FAIL: Counter = Counter.builder(CALL_PDL_IDENTER_FAIL)
    .description("Counts the number of failed calls to persondatalosning - hentIdenter")
    .register(METRICS_REGISTRY)

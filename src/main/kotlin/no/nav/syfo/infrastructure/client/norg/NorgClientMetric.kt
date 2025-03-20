package no.nav.syfo.infrastructure.client.norg

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Counter.builder
import no.nav.syfo.infrastructure.metric.METRICS_NS
import no.nav.syfo.infrastructure.metric.METRICS_REGISTRY

const val CALL_NORG_ARBEIDSFORDELING_BASE = "${METRICS_NS}_call_norg_arbeidsfordeling"
const val CALL_NORG_ARBEIDSFORDELING_SUCCESS = "${CALL_NORG_ARBEIDSFORDELING_BASE}_success_count"
const val CALL_NORG_ARBEIDSFORDELING_FAIL = "${CALL_NORG_ARBEIDSFORDELING_BASE}_fail_count"
const val CALL_NORG_ENHET_CACHE_HIT = "${METRICS_NS}_call_norg_enhet_cache_hit"
const val CALL_NORG_ENHET_CACHE_MISS = "${METRICS_NS}_call_norg_enhet_cache_miss"
const val CALL_NORG_ARBEIDSFORDELING_ENHET_CACHE_HIT = "${METRICS_NS}_call_norg_arbeidsfordeling_enhet_cache_hit"
const val CALL_NORG_ARBEIDSFORDELING_ENHET_CACHE_MISS = "${METRICS_NS}_call_norg_arbeidsfordeling_enhet_cache_miss"
const val CALL_NORG_OVERORDNET_ENHET_CACHE_HIT = "${METRICS_NS}_call_norg_overordnet_enhet_cache_hit"
const val CALL_NORG_OVERORDNET_ENHET_CACHE_MISS = "${METRICS_NS}_call_norg_overordnet_enhet_cache_miss"
const val CALL_NORG_UNDERORDNET_ENHET_CACHE_HIT = "${METRICS_NS}_call_norg_underordnet_enhet_cache_hit"
const val CALL_NORG_UNDERORDNET_ENHET_CACHE_MISS = "${METRICS_NS}_call_norg_underordnet_enhet_cache_miss"

val COUNT_CALL_NORG_ARBEIDSFORDELING_SUCCESS: Counter = builder(CALL_NORG_ARBEIDSFORDELING_SUCCESS)
    .description("Counts the number of successful calls to Norg - Arbeidsfordeling")
    .register(METRICS_REGISTRY)

val COUNT_CALL_NORG_ARBEIDSFORDELING_FAIL: Counter = builder(CALL_NORG_ARBEIDSFORDELING_FAIL)
    .description("Counts the number of failed calls to Norg - Arbeidsfordeling")
    .register(METRICS_REGISTRY)

val COUNT_CALL_NORG_ENHET_CACHE_HIT: Counter = builder(CALL_NORG_ENHET_CACHE_HIT)
    .description("Counts the number of cache hits for NorgEnhet")
    .register(METRICS_REGISTRY)

val COUNT_CALL_NORG_ENHET_CACHE_MISS: Counter = builder(CALL_NORG_ENHET_CACHE_MISS)
    .description("Counts the number of cache miss for NorgEnhet")
    .register(METRICS_REGISTRY)

val COUNT_CALL_NORG_ARBEIDSFORDELING_ENHET_CACHE_HIT: Counter = builder(CALL_NORG_ARBEIDSFORDELING_ENHET_CACHE_HIT)
    .description("Counts the number of cache hits for NorgArbeidsfordelingEnhet")
    .register(METRICS_REGISTRY)

val COUNT_CALL_NORG_ARBEIDSFORDELING_ENHET_CACHE_MISS: Counter = builder(CALL_NORG_ARBEIDSFORDELING_ENHET_CACHE_MISS)
    .description("Counts the number of cache miss for NorgArbeidsfordelingEnhet")
    .register(METRICS_REGISTRY)

val COUNT_CALL_NORG_OVERORDNET_ENHET_CACHE_HIT: Counter = builder(CALL_NORG_OVERORDNET_ENHET_CACHE_HIT)
    .description("Counts the number of cache hits for overordnet enhet")
    .register(METRICS_REGISTRY)

val COUNT_CALL_NORG_OVERORDNET_ENHET_CACHE_MISS: Counter = builder(CALL_NORG_OVERORDNET_ENHET_CACHE_MISS)
    .description("Counts the number of cache miss for overordnet enhet")
    .register(METRICS_REGISTRY)

val COUNT_CALL_NORG_UNDERORDNET_ENHET_CACHE_HIT: Counter = builder(CALL_NORG_UNDERORDNET_ENHET_CACHE_HIT)
    .description("Counts the number of cache hits for underordnet enheter")
    .register(METRICS_REGISTRY)

val COUNT_CALL_NORG_UNDERORDNET_ENHET_CACHE_MISS: Counter = builder(CALL_NORG_UNDERORDNET_ENHET_CACHE_MISS)
    .description("Counts the number of cache miss for underordnet enheter")
    .register(METRICS_REGISTRY)

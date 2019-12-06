package no.nav.syfo.consumers

import no.nav.syfo.config.CacheConfig.Companion.CACHENAME_EGENANSATT
import no.nav.syfo.metric.Metric
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class EgenAnsattConsumer @Inject constructor(
        private val egenAnsattV1: EgenAnsattV1,
        private val metric: Metric
) {
    
    @Cacheable(value = [CACHENAME_EGENANSATT], key = "#fnr", condition = "#fnr != null")
    fun isEgenAnsatt(fnr: String): Boolean {
        metric.countOutgoingRequests("EgenAnsattConsumer")
        return egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(
            WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                .withIdent(fnr)
        ).isEgenAnsatt
    }
}

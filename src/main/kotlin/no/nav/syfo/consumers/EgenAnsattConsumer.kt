package no.nav.syfo.consumers

import no.nav.syfo.config.CacheConfig.Companion.CACHENAME_EGENANSATT
import no.nav.syfo.metric.Metric
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.retry.annotation.*
import org.springframework.stereotype.Service
import javax.inject.Inject
import javax.xml.ws.soap.SOAPFaultException

@Service
class EgenAnsattConsumer @Inject constructor(
        private val egenAnsattV1: EgenAnsattV1,
        private val metric: Metric
) {
    private val LOG = LoggerFactory.getLogger(EgenAnsattConsumer::class.java)

    @Retryable(
            value = [SOAPFaultException::class],
            backoff = Backoff(delay = 200, maxDelay = 1000)
    )
    @Cacheable(value = [CACHENAME_EGENANSATT], key = "#fnr", condition = "#fnr != null")
    fun isEgenAnsatt(fnr: String): Boolean {
        metric.countOutgoingRequests("EgenAnsattConsumer")
        return egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(
            WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                .withIdent(fnr)
        ).isEgenAnsatt
    }

    @Recover
    fun recover(e: SOAPFaultException) {
        LOG.error("Failed to request isEgenAnsatt from EgenAnsattV1 after max retry attempts", e)
        throw e
    }
}

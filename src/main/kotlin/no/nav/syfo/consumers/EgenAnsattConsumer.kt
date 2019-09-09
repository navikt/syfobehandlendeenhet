package no.nav.syfo.consumers

import no.nav.syfo.config.CacheConfig.Companion.CACHENAME_EGENANSATT
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest
import org.springframework.beans.factory.InitializingBean
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class EgenAnsattConsumer @Inject constructor(private val egenAnsattV1: EgenAnsattV1): InitializingBean {

    private var instance: EgenAnsattConsumer? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    fun egenAnsattConsumer(): EgenAnsattConsumer? {
        return instance
    }

    @Cacheable(value = [CACHENAME_EGENANSATT], key = "#fnr", condition = "#fnr != null")
    fun erEgenAnsatt(fnr: String): Boolean {
        return egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(
            WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                .withIdent(fnr)
        ).isEgenAnsatt
    }
}

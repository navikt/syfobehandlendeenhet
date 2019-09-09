package no.nav.syfo.consumers

import lombok.extern.slf4j.Slf4j
import no.nav.syfo.config.CacheConfig.Companion.CACHENAME_AKTOR_FNR
import no.nav.syfo.config.CacheConfig.Companion.CACHENAME_AKTOR_ID
import no.nav.syfo.service.exceptions.MoteException
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2
import no.nav.tjeneste.virksomhet.aktoer.v2.HentAktoerIdForIdentPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.aktoer.v2.HentIdentForAktoerIdPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentAktoerIdForIdentRequest
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest
import org.apache.commons.lang3.StringUtils.isBlank
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Slf4j
@Service
class AktoerConsumer @Autowired constructor(private val aktoerV2: AktoerV2) : InitializingBean {

    private var instance: AktoerConsumer? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    fun aktoerConsumer() = instance

    @Cacheable(value = [CACHENAME_AKTOR_ID], key = "#fnr", condition = "#fnr != null")
    fun hentAktoerIdForIdent(fnr: String): String {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$".toRegex())) {
            LOG.error("Forsøker å hente aktørId for fnr {} på feil format", fnr)
            throw RuntimeException()
        }

        try {
            return aktoerV2.hentAktoerIdForIdent(
                WSHentAktoerIdForIdentRequest()
                    .withIdent(fnr)
            ).aktoerId
        } catch (e: HentAktoerIdForIdentPersonIkkeFunnet) {
            throw MoteException("AktoerID ikke funnet for fødselsnummer!")
        }

    }

    @Cacheable(value = [CACHENAME_AKTOR_FNR], key = "#aktoerId", condition = "#aktoerId != null")
    fun hentFnrForAktoer(aktoerId: String): String {
        if (isBlank(aktoerId) || !aktoerId.matches("\\d{13}$".toRegex())) {
            LOG.error("Forsøker å hente fnr for aktørId {} på feil format", aktoerId)
            throw RuntimeException()
        }

        try {
            return aktoerV2.hentIdentForAktoerId(
                WSHentIdentForAktoerIdRequest()
                    .withAktoerId(aktoerId)
            ).ident
        } catch (e: HentIdentForAktoerIdPersonIkkeFunnet) {
            throw MoteException("FNR ikke funnet for aktoerId!")
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AktoerConsumer::class.java)
    }
}

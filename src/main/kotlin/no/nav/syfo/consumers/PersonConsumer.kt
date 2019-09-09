package no.nav.syfo.consumers

import no.nav.syfo.config.CacheConfig.Companion.CACHENAME_PERSON_GEOGRAFISK
import no.nav.tjeneste.virksomhet.person.v3.HentGeografiskTilknytningPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.HentGeografiskTilknytningSikkerhetsbegrensing
import no.nav.tjeneste.virksomhet.person.v3.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSGeografiskTilknytning
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSNorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSPersonIdent
import no.nav.tjeneste.virksomhet.person.v3.meldinger.WSHentGeografiskTilknytningRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.Optional.ofNullable
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@Service
class PersonConsumer @Inject constructor(private val personV3: PersonV3): InitializingBean {

    private var instance: PersonConsumer? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    fun personService() = instance

    @Cacheable(cacheNames = [CACHENAME_PERSON_GEOGRAFISK], key = "#fnr", condition = "#fnr != null")
    fun hentGeografiskTilknytning(fnr: String): String {
        try {
            val geografiskTilknytning = personV3.hentGeografiskTilknytning(
                WSHentGeografiskTilknytningRequest()
                    .withAktoer(WSPersonIdent().withIdent(WSNorskIdent().withIdent(fnr)))
            )
                .geografiskTilknytning
            return ofNullable(geografiskTilknytning).map(WSGeografiskTilknytning::getGeografiskTilknytning)
                .orElse("")
        } catch (e: HentGeografiskTilknytningSikkerhetsbegrensing) {
            LOG.error("Fikk sikkerhetsbegrensing ved henting av geografiskTilknytning")
            throw ForbiddenException()
        } catch (e: HentGeografiskTilknytningPersonIkkeFunnet) {
            LOG.error("Fant ikke person ved henting av geografiskTilknytning")
            throw RuntimeException()
        } catch (e: RuntimeException) {
            LOG.error("Fikk RuntimeException ved henting av geografisk tilknytning", e)
            throw e
        }

    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PersonConsumer::class.java)
    }
}

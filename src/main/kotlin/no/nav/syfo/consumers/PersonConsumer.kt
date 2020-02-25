package no.nav.syfo.consumers

import no.nav.syfo.config.CacheConfig.Companion.CACHENAME_PERSON_GEOGRAFISK
import no.nav.syfo.metric.Metric
import no.nav.tjeneste.virksomhet.person.v3.binding.*
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.Optional.ofNullable
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@Service
class PersonConsumer @Inject constructor(
        private val personV3: PersonV3,
        private val metric: Metric
) {

    private val LOG = LoggerFactory.getLogger(PersonConsumer::class.java)

    @Cacheable(cacheNames = [CACHENAME_PERSON_GEOGRAFISK], key = "#fnr", condition = "#fnr != null")
    fun geografiskTilknytning(fnr: String): String {
        try {
            metric.countOutgoingRequests("PersonConsumer")
            val geografiskTilknytning = personV3.hentGeografiskTilknytning(
                HentGeografiskTilknytningRequest()
                    .withAktoer(PersonIdent().withIdent(NorskIdent().withIdent(fnr)))
            )
                .geografiskTilknytning
            return ofNullable(geografiskTilknytning).map(GeografiskTilknytning::getGeografiskTilknytning)
                .orElse("")
        } catch (e: HentGeografiskTilknytningSikkerhetsbegrensing) {
            LOG.error("Received security constraint when requesting geografiskTilknytning")
            metric.countOutgoingRequestsFailed("PersonConsumer", "HentGeografiskTilknytningSikkerhetsbegrensing")
            throw ForbiddenException()
        } catch (e: HentGeografiskTilknytningPersonIkkeFunnet) {
            LOG.error("Couldn't find person when requesting geografiskTilknytning")
            metric.countOutgoingRequestsFailed("PersonConsumer", "HentGeografiskTilknytningPersonIkkeFunnet")
            throw RuntimeException()
        } catch (e: RuntimeException) {
            LOG.error("Received RunTimeException when requesting geografiskTilknytning: ${e.message}", e)
            metric.countOutgoingRequestsFailed("PersonConsumer", "RuntimeException")
            throw e
        }

    }
}

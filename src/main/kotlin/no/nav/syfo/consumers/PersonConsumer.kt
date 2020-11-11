package no.nav.syfo.consumers

import no.nav.syfo.config.CacheConfig.Companion.CACHENAME_PERSON_GEOGRAFISK
import no.nav.syfo.exception.EmptyGTResponse
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.*
import no.nav.tjeneste.virksomhet.person.v3.binding.*
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import javax.inject.Inject
import javax.ws.rs.ForbiddenException
import javax.xml.ws.soap.SOAPFaultException

@Service
class PersonConsumer @Inject constructor(
    private val personV3: PersonV3,
    private val metric: Metric
) {
    private val LOG = LoggerFactory.getLogger(PersonConsumer::class.java)

    @Cacheable(cacheNames = [CACHENAME_PERSON_GEOGRAFISK], key = "#fnr", condition = "#fnr != null")
    fun geografiskTilknytning(
        callId: String,
        fnr: String
    ): String? {
        try {
            metric.countOutgoingRequests("PersonConsumer")
            val geografiskTilknytning = personV3.hentGeografiskTilknytning(
                HentGeografiskTilknytningRequest()
                    .withAktoer(PersonIdent().withIdent(NorskIdent().withIdent(fnr)))
            ).geografiskTilknytning
            val gt: String? = geografiskTilknytning?.geografiskTilknytning
            return if (gt.isNullOrEmpty()) {
                val personNumberType = personIdentType(fnr).toString().toLowerCase()
                metric.countEvent("empty_gt_$personNumberType")
                LOG.info(
                    "TPS returned empty response for Geografisk Tilknytningfor type=$personNumberType. {}",
                    callIdArgument(callId)
                )
                null
            } else {
                gt
            }
        } catch (e: HentGeografiskTilknytningSikkerhetsbegrensing) {
            LOG.error("Received security constraint when requesting geografiskTilknytning. {}", callIdArgument(callId))
            metric.countOutgoingRequestsFailed("PersonConsumer", "HentGeografiskTilknytningSikkerhetsbegrensing")
            throw ForbiddenException()
        } catch (e: HentGeografiskTilknytningPersonIkkeFunnet) {
            LOG.error("Couldn't find person when requesting geografiskTilknytning. {}", callIdArgument(callId))
            metric.countOutgoingRequestsFailed("PersonConsumer", "HentGeografiskTilknytningPersonIkkeFunnet/")
            throw RuntimeException()
        } catch (e: RuntimeException) {
            when (e) {
                is SOAPFaultException -> {
                    LOG.error("Received SOAPFaultException when requesting geografiskTilknytning: ${e.message}, {}", e, callIdArgument(callId))
                    throw e
                }
                is EmptyGTResponse -> {
                    val personNumberType = personIdentType(fnr).toString().toLowerCase()
                    metric.countEvent("empty_gt_$personNumberType")
                    LOG.info(
                        "${e.message} for type=$personNumberType. {}, {}",
                        callIdArgument(callId),
                        e
                    )
                    return null
                }
                else -> {
                    LOG.error(
                        "Received RunTimeException when requesting geografiskTilknytning: ${e.message}. {}, {}",
                        callIdArgument(callId),
                        e
                    )
                    metric.countOutgoingRequestsFailed("PersonConsumer", "RuntimeException")
                    throw e
                }
            }
        }
    }
}

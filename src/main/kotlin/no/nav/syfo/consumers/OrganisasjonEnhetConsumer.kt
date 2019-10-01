package no.nav.syfo.consumers

import no.nav.syfo.config.CacheConfig.Companion.CACHENAME_ORGANISASJONENHET
import no.nav.syfo.domain.model.BehandlendeEnhet
import no.nav.syfo.metric.Metric
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.HentOverordnetEnhetListeEnhetIkkeFunnet
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetRelasjonstyper
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetsstatus.AKTIV
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSHentOverordnetEnhetListeRequest
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.*
import javax.inject.Inject

@Service
class OrganisasjonEnhetConsumer @Inject constructor(
    private val organisasjonEnhetV2: OrganisasjonEnhetV2,
    private val metric: Metric
) {

    private val LOG = LoggerFactory.getLogger(OrganisasjonEnhetConsumer::class.java)

    @Cacheable(value = [CACHENAME_ORGANISASJONENHET], key = "#enhet", condition = "#enhet != null")
    fun setteKontor(enhet: String): Optional<BehandlendeEnhet> {
        try {
            metric.countOutgoingRequests("OrganisasjonEnhetConsumer")
            return organisasjonEnhetV2.hentOverordnetEnhetListe(
                WSHentOverordnetEnhetListeRequest()
                    .withEnhetId(enhet).withEnhetRelasjonstype(WSEnhetRelasjonstyper().withValue("HABILITET"))
            )
                .overordnetEnhetListe
                .stream()
                .filter { wsOrganisasjonsenhet -> AKTIV == wsOrganisasjonsenhet.status }
                .map { wsOrganisasjonsenhet ->
                    BehandlendeEnhet(
                        wsOrganisasjonsenhet.enhetId,
                        wsOrganisasjonsenhet.enhetNavn
                    )
                }
                .findFirst()
        } catch (e: HentOverordnetEnhetListeEnhetIkkeFunnet) {
            LOG.error("Couldn't find parent enhet for enhet {}", enhet)
            metric.countOutgoingRequestsFailed("OrganisasjonEnhetConsumer", "HentOverordnetEnhetListeEnhetIkkeFunnet")
            throw RuntimeException()
        }
    }
}

package no.nav.syfo.consumers

import no.nav.syfo.domain.model.Enhet
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.HentOverordnetEnhetListeEnhetIkkeFunnet
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetRelasjonstyper
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSHentOverordnetEnhetListeRequest
import org.springframework.stereotype.Component

import javax.inject.Inject
import java.util.Optional

import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetsstatus.AKTIV
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean

@Component
class OrganisasjonEnhetConsumer @Inject constructor(private val organisasjonEnhetV2: OrganisasjonEnhetV2): InitializingBean {

    private var instance: OrganisasjonEnhetConsumer? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    fun organisasjonEnhetConsumer() = instance

    fun finnSetteKontor(enhet: String): Optional<Enhet> {
        try {
            return organisasjonEnhetV2.hentOverordnetEnhetListe(
                WSHentOverordnetEnhetListeRequest()
                    .withEnhetId(enhet).withEnhetRelasjonstype(WSEnhetRelasjonstyper().withValue("HABILITET"))
            )
                .overordnetEnhetListe
                .stream()
                .filter { wsOrganisasjonsenhet -> AKTIV == wsOrganisasjonsenhet.status }
                .map { wsOrganisasjonsenhet ->
                    Enhet(wsOrganisasjonsenhet.enhetId,
                        wsOrganisasjonsenhet.enhetNavn)
                }
                .findFirst()
        } catch (e: HentOverordnetEnhetListeEnhetIkkeFunnet) {
            LOG.error("Fant ingen overordnet enhet for enhet {}", enhet)
            throw RuntimeException()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OrganisasjonEnhetConsumer::class.java)
    }

}

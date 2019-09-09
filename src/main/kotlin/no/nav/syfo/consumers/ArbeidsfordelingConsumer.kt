package no.nav.syfo.consumers

import no.nav.syfo.domain.model.Enhet
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnBehandlendeEnhetListeUgyldigInput
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSArbeidsfordelingKriterier
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSEnhetsstatus.AKTIV
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSGeografi
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSTema
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

@Component
class ArbeidsfordelingConsumer(private val arbeidsfordelingV1: ArbeidsfordelingV1): InitializingBean {

    private var instance: ArbeidsfordelingConsumer? = null

    override fun afterPropertiesSet() {
        instance = this
    }

    fun arbeidsfordelingConsumer() = instance

    fun finnAktivBehandlendeEnhet(geografiskTilknytning: String): Enhet {
        try {
            return arbeidsfordelingV1.finnBehandlendeEnhetListe(
                WSFinnBehandlendeEnhetListeRequest()
                    .withArbeidsfordelingKriterier(
                        WSArbeidsfordelingKriterier()
                            .withGeografiskTilknytning(WSGeografi().withValue(geografiskTilknytning))
                            .withTema(WSTema().withValue("OPP"))
                    )
            ).behandlendeEnhetListe
                .stream()
                .filter { wsOrganisasjonsenhet -> AKTIV == wsOrganisasjonsenhet.status }
                .map { wsOrganisasjonsenhet ->
                    Enhet(wsOrganisasjonsenhet.enhetId,
                        wsOrganisasjonsenhet.enhetNavn)
                }
                .findFirst()
                .orElse(
                    Enhet(
                        geografiskTilknytning,
                        geografiskTilknytning)
                )
        } catch (e: FinnBehandlendeEnhetListeUgyldigInput) {
            LOG.error("Feil ved henting av brukers forvaltningsenhet med geografiskTilknytning: $geografiskTilknytning")
            throw RuntimeException("Feil ved henting av brukers forvaltningsenhet", e)
        } catch (e: RuntimeException) {
            LOG.error("Feil ved henting av behandlende enhet for geografiskTilknytning: $geografiskTilknytning")
            throw e
        }

    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ArbeidsfordelingConsumer::class.java)
    }

}

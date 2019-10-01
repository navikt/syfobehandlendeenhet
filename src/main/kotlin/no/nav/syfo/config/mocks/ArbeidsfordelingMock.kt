package no.nav.syfo.config.mocks

import no.nav.syfo.config.consumer.ArbeidsfordelingConfig
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSEnhetsstatus
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSOrganisasjonsenhet
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnAlleBehandlendeEnheterListeRequest
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnAlleBehandlendeEnheterListeResponse
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeRequest
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = [ArbeidsfordelingConfig.MOCK_KEY], havingValue = "true")
class ArbeidsfordelingMock : ArbeidsfordelingV1 {

    override fun ping() {}

    override fun finnAlleBehandlendeEnheterListe(request: WSFinnAlleBehandlendeEnheterListeRequest): WSFinnAlleBehandlendeEnheterListeResponse? {
        return null
    }

    override fun finnBehandlendeEnhetListe(request: WSFinnBehandlendeEnhetListeRequest): WSFinnBehandlendeEnhetListeResponse {
        return WSFinnBehandlendeEnhetListeResponse().withBehandlendeEnhetListe(
            listOf(
                WSOrganisasjonsenhet()
                    .withEnhetId("0330")
                    .withEnhetNavn("NAV Bjerke")
                    .withStatus(
                        WSEnhetsstatus.fromValue("AKTIV")
                    ),
                WSOrganisasjonsenhet()
                    .withEnhetNavn("0314")
                    .withEnhetNavn("NAV Sagene")
                    .withStatus(
                        WSEnhetsstatus.fromValue("AKTIV")
                    )
            )
        )
    }
}

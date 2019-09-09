package no.nav.syfo.config.mocks

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSEnhetsstatus
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSOrganisasjonsenhet
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

import java.util.Arrays.asList
import no.nav.syfo.config.consumer.ArbeidsfordelingConfig.MOCK_KEY

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
class ArbeidsfordelingMock : ArbeidsfordelingV1 {

    override fun ping() {}

    override fun finnAlleBehandlendeEnheterListe(request: WSFinnAlleBehandlendeEnheterListeRequest): WSFinnAlleBehandlendeEnheterListeResponse {
        return WSFinnAlleBehandlendeEnheterListeResponse().withBehandlendeEnhetListe(
            asList(
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

    override fun finnBehandlendeEnhetListe(request: WSFinnBehandlendeEnhetListeRequest): WSFinnBehandlendeEnhetListeResponse {
        return WSFinnBehandlendeEnhetListeResponse().withBehandlendeEnhetListe(
            asList(
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

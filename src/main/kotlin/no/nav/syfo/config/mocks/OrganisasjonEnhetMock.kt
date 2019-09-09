package no.nav.syfo.config.mocks

import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetsstatus
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSOrganisasjonsenhet
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

import no.nav.syfo.config.consumer.OrganisasjonEnhetConfig.MOCK_KEY

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
class OrganisasjonEnhetMock : OrganisasjonEnhetV2 {

    override fun ping() {}

    override fun finnNAVKontor(wsFinnNAVKontorRequest: WSFinnNAVKontorRequest): WSFinnNAVKontorResponse {
        return WSFinnNAVKontorResponse()
            .withNAVKontor(
                WSOrganisasjonsenhet()
                    .withEnhetId("0330")
                    .withEnhetNavn("Bjerke")
                    .withStatus(WSEnhetsstatus.AKTIV)
            )
    }

    override fun hentFullstendigEnhetListe(wsHentFullstendigEnhetListeRequest: WSHentFullstendigEnhetListeRequest): WSHentFullstendigEnhetListeResponse? {
        return null
    }

    override fun hentEnhetBolk(wsHentEnhetBolkRequest: WSHentEnhetBolkRequest): WSHentEnhetBolkResponse? {
        return null
    }

    override fun hentOverordnetEnhetListe(wsHentOverordnetEnhetListeRequest: WSHentOverordnetEnhetListeRequest): WSHentOverordnetEnhetListeResponse? {
        return null
    }


}

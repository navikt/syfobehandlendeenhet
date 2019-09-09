package no.nav.syfo.config.mocks

import no.nav.tjeneste.virksomhet.person.v3.*
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSDiskresjonskoder
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSNorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSPerson
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSPersonIdent
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

import no.nav.syfo.config.consumer.PersonConfig.MOCK_KEY

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
class PersonV3Mock : PersonV3 {
    @Throws(HentPersonSikkerhetsbegrensning::class, HentPersonPersonIkkeFunnet::class)
    override fun hentPerson(wsHentPersonRequest: WSHentPersonRequest): WSHentPersonResponse {
        return WSHentPersonResponse()
            .withPerson(
                WSPerson()
                    .withAktoer(
                        WSPersonIdent()
                            .withIdent(
                                WSNorskIdent()
                                    .withIdent("1234567890123")
                            )
                    )
                    .withDiskresjonskode(WSDiskresjonskoder().withValue("SPSF"))
            )
    }

    @Throws(HentGeografiskTilknytningSikkerhetsbegrensing::class, HentGeografiskTilknytningPersonIkkeFunnet::class)
    override fun hentGeografiskTilknytning(wsHentGeografiskTilknytningRequest: WSHentGeografiskTilknytningRequest): WSHentGeografiskTilknytningResponse? {
        return null
    }

    @Throws(HentSikkerhetstiltakPersonIkkeFunnet::class)
    override fun hentSikkerhetstiltak(wsHentSikkerhetstiltakRequest: WSHentSikkerhetstiltakRequest): WSHentSikkerhetstiltakResponse? {
        return null
    }

    override fun ping() {

    }

    override fun hentPersonnavnBolk(wsHentPersonnavnBolkRequest: WSHentPersonnavnBolkRequest): WSHentPersonnavnBolkResponse? {
        return null
    }
}

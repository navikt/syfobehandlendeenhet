package no.nav.syfo.config.mocks

import no.nav.syfo.config.consumer.PersonConfig
import no.nav.tjeneste.virksomhet.person.v3.*
import no.nav.tjeneste.virksomhet.person.v3.informasjon.WSKommune
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = [PersonConfig.MOCK_KEY], havingValue = "true")
class PersonMock : PersonV3 {

    @Throws(HentPersonSikkerhetsbegrensning::class, HentPersonPersonIkkeFunnet::class)
    override fun hentPerson(wsHentPersonRequest: WSHentPersonRequest): WSHentPersonResponse? {
        return null
    }

    @Throws(HentGeografiskTilknytningSikkerhetsbegrensing::class, HentGeografiskTilknytningPersonIkkeFunnet::class)
    override fun hentGeografiskTilknytning(wsHentGeografiskTilknytningRequest: WSHentGeografiskTilknytningRequest): WSHentGeografiskTilknytningResponse {
        return WSHentGeografiskTilknytningResponse()
            .withDiskresjonskode(null)
            .withGeografiskTilknytning(
                WSKommune().withGeografiskTilknytning("0314")
            )

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

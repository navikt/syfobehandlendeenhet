package no.nav.syfo.config.mocks

import no.nav.syfo.config.consumer.PersonConfig
import no.nav.tjeneste.virksomhet.person.v3.binding.*
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kommune
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = [PersonConfig.MOCK_KEY], havingValue = "true")
class PersonMock : PersonV3 {

    @Throws(HentPersonSikkerhetsbegrensning::class, HentPersonPersonIkkeFunnet::class)
    override fun hentPerson(wsHentPersonRequest: HentPersonRequest): HentPersonResponse? {
        return null
    }

    @Throws(HentGeografiskTilknytningSikkerhetsbegrensing::class, HentGeografiskTilknytningPersonIkkeFunnet::class)
    override fun hentGeografiskTilknytning(wsHentGeografiskTilknytningRequest: HentGeografiskTilknytningRequest): HentGeografiskTilknytningResponse {
        return HentGeografiskTilknytningResponse()
                .withDiskresjonskode(null)
                .withGeografiskTilknytning(
                        Kommune().withGeografiskTilknytning("0314")
                )
    }

    @Throws(HentSikkerhetstiltakPersonIkkeFunnet::class)
    override fun hentSikkerhetstiltak(wsHentSikkerhetstiltakRequest: HentSikkerhetstiltakRequest): HentSikkerhetstiltakResponse? {
        return null
    }

    override fun hentVerge(request: HentVergeRequest?): HentVergeResponse? {
        return null
    }

    override fun hentEkteskapshistorikk(request: HentEkteskapshistorikkRequest?): HentEkteskapshistorikkResponse? {
        return null
    }

    override fun hentPersonerMedSammeAdresse(request: HentPersonerMedSammeAdresseRequest?): HentPersonerMedSammeAdresseResponse? {
        return null
    }

    override fun ping() {

    }

    override fun hentPersonhistorikk(request: HentPersonhistorikkRequest?): HentPersonhistorikkResponse? {
        return null
    }

    override fun hentPersonnavnBolk(wsHentPersonnavnBolkRequest: HentPersonnavnBolkRequest): HentPersonnavnBolkResponse? {
        return null
    }
}

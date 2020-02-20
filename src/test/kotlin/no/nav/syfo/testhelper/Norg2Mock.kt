package no.nav.syfo.testhelper

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.syfo.consumers.NorgConsumer
import no.nav.syfo.domain.model.Enhetsstatus
import no.nav.syfo.domain.model.NorgEnhet
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.util.UriComponentsBuilder

const val ENHET_NR = "0101"
const val ENHET_NAVN = "Enhet"

fun mockAndExpectNorgArbeidsfordeling(
        mockRestServiceServer: MockRestServiceServer,
        url: String,
        enhetList: List<NorgEnhet>
) {
    val uriString = UriComponentsBuilder.fromHttpUrl(url)
            .path(NorgConsumer.ARBEIDSFORDELING_BESTMATCH_PATH)
            .toUriString()

    try {
        val json = ObjectMapper().writeValueAsString(enhetList)

        mockRestServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.requestTo(uriString))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess(json, MediaType.APPLICATION_JSON))
    } catch (e: JsonProcessingException) {
        e.printStackTrace()
    }
}

fun generateNorgEnhet(): NorgEnhet {
    return NorgEnhet(
            enhetNr = ENHET_NR,
            navn = ENHET_NAVN,
            status = Enhetsstatus.AKTIV.formattedName,
            aktiveringsdato = null,
            antallRessurser = null,
            enhetId = null,
            kanalstrategi = null,
            nedleggelsesdato = null,
            oppgavebehandler = null,
            orgNivaa = null,
            orgNrTilKommunaltNavKontor = null,
            organisasjonsnummer = null,
            sosialeTjenester = null,
            type = null,
            underAvviklingDato = null,
            underEtableringDato = null,
            versjon = null
    )
}

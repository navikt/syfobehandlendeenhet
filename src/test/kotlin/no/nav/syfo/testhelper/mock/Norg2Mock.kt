package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.syfo.infrastructure.client.norg.domain.ArbeidsfordelingCriteriaBehandlingstype
import no.nav.syfo.infrastructure.client.norg.domain.Enhetsstatus
import no.nav.syfo.infrastructure.client.norg.domain.NorgEnhet
import no.nav.syfo.infrastructure.client.norg.domain.ArbeidsfordelingCriteria

const val ENHET_NR = "0101"
const val ENHET_NAVN = "Enhet"

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
        versjon = null,
    )
}

val norg2Response = listOf(generateNorgEnhet())
val norg2ResponseNavUtland = listOf(generateNorgEnhet().copy(enhetNr = "0393", navn = "Nav utland"))

suspend fun MockRequestHandleScope.getNorg2Response(request: HttpRequestData): HttpResponseData {
    val body = request.receiveBody<ArbeidsfordelingCriteria>()
    return if (body.behandlingstype == ArbeidsfordelingCriteriaBehandlingstype.NAV_UTLAND.behandlingstype) {
        respond(norg2ResponseNavUtland)
    } else {
        respond(norg2Response)
    }
}

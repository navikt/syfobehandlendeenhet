package no.nav.syfo.testhelper.mock

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.norg.NorgClient
import no.nav.syfo.client.norg.domain.ArbeidsfordelingCriteria
import no.nav.syfo.client.norg.domain.ArbeidsfordelingCriteriaBehandlingstype
import no.nav.syfo.client.norg.domain.Enhetsstatus
import no.nav.syfo.client.norg.domain.NorgEnhet
import no.nav.syfo.testhelper.getRandomPort

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

class Norg2Mock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    val norg2Response = listOf(
        generateNorgEnhet(),
    )

    val norg2ResponseNavUtland = norg2Response.map {
        it.copy(
            enhetNr = "0393",
            navn = "NAV Utland"
        )
    }

    val name = "norg2"
    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            post(NorgClient.ARBEIDSFORDELING_BESTMATCH_PATH) {
                val body = call.receive<ArbeidsfordelingCriteria>()
                if (body.behandlingstype == ArbeidsfordelingCriteriaBehandlingstype.NAV_UTLAND.behandlingstype) {
                    call.respond(norg2ResponseNavUtland)
                } else {
                    call.respond(norg2Response)
                }
            }
        }
    }
}

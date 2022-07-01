package no.nav.syfo.testhelper.mock

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.norg.NorgClient
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

    val name = "norg2"
    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            post(NorgClient.ARBEIDSFORDELING_BESTMATCH_PATH) {
                call.respond(norg2Response)
            }
        }
    }
}

package no.nav.syfo.testhelper.mock

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.application.api.authentication.getNAVIdentFromToken
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.veiledertilgang.TilgangDTO
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient.Companion.ACCESS_TO_SYFO_PATH
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_NO_ACCESS
import no.nav.syfo.testhelper.getRandomPort
import no.nav.syfo.util.getBearerHeader

val tilgangFalse = TilgangDTO(
    erGodkjent = false,
)

val tilgangTrue = TilgangDTO(
    erGodkjent = true,
)

class VeilederTilgangskontrollMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    val responseAccessFalse = tilgangFalse
    val responseAccessTrue = tilgangTrue

    val name = "veiledertilgangskontroll"
    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            get(ACCESS_TO_SYFO_PATH) {
                val navIdent = getNAVIdentFromToken(token = getBearerHeader()!!)
                if (navIdent == VEILEDER_IDENT_NO_ACCESS) {
                    call.respond(responseAccessFalse)
                } else {
                    call.respond(responseAccessTrue)
                }
            }
        }
    }
}

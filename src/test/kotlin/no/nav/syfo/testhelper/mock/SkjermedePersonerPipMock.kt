package no.nav.syfo.testhelper.mock

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.getRandomPort

class SkjermedePersonerPipMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    val name = "skjermedpersonerpip"
    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            get("/skjermet") {
                if (call.request.queryParameters["personident"] == ARBEIDSTAKER_PERSONIDENT.value) {
                    call.respond(true)
                }
            }
        }
    }
}

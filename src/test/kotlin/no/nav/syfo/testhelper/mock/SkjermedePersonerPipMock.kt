package no.nav.syfo.testhelper.mock

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.getRandomPort

class SkjermedePersonerPipMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    val name = "skjermedpersonerpip"
    val server = mockTilgangServer()

    private fun mockTilgangServer(): NettyApplicationEngine {
        return embeddedServer(
            factory = Netty,
            port = port
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
}

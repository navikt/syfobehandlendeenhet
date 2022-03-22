package no.nav.syfo.testhelper.mock

import com.auth0.jwt.JWT
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.authentication.getNAVIdentFromToken
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.azuread.AzureAdTokenResponse
import no.nav.syfo.client.wellknown.WellKnown
import no.nav.syfo.testhelper.generateJWT
import no.nav.syfo.testhelper.getRandomPort
import java.nio.file.Paths

fun wellKnownInternalAzureAD(): WellKnown {
    val path = "src/test/resources/jwkset.json"
    val uri = Paths.get(path).toUri().toURL()
    return WellKnown(
        issuer = "https://sts.issuer.net/veileder/v2",
        jwksUri = uri.toString()
    )
}

fun generateAzureAdTokenResponse(
    assertion: String? = null,
): AzureAdTokenResponse {
    return if (assertion == null) {
        AzureAdTokenResponse(
            access_token = "toke ",
            expires_in = 3600,
            token_type = "type",
        )
    } else {
        val deodedToken = JWT.decode(assertion)
        val accessToken = generateJWT(
            audience = deodedToken.audience.first(),
            issuer = deodedToken.issuer,
            navIdent = getNAVIdentFromToken(assertion),
        )
        AzureAdTokenResponse(
            access_token = accessToken,
            expires_in = 3600,
            token_type = "type",
        )
    }
}

class AzureAdMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"

    val name = "azuread"
    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            post {
                val requestBody: Parameters = call.receive()
                val assertion = requestBody["assertion"]
                if (assertion != null) {
                    val response = generateAzureAdTokenResponse(
                        assertion = assertion,
                    )
                    call.respond(response)
                } else {
                    call.respond(generateAzureAdTokenResponse())
                }
            }
        }
    }
}

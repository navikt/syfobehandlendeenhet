package no.nav.syfo.testhelper.mock

import com.auth0.jwt.JWT
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import no.nav.syfo.api.authentication.Token
import no.nav.syfo.api.authentication.getNAVIdent
import no.nav.syfo.infrastructure.client.azuread.AzureAdTokenResponse
import no.nav.syfo.infrastructure.client.wellknown.WellKnown
import no.nav.syfo.testhelper.generateJWT
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
            navIdent = Token(assertion).getNAVIdent(),
        )
        AzureAdTokenResponse(
            access_token = accessToken,
            expires_in = 3600,
            token_type = "type",
        )
    }
}

fun MockRequestHandleScope.getAzureAdResponse(request: HttpRequestData): HttpResponseData {
    val assertion = (request.body as FormDataContent).formData["assertion"]
    return when {
        assertion != null -> {
            respond(
                generateAzureAdTokenResponse(
                    assertion = assertion,
                )
            )
        }
        else -> {
            respond(generateAzureAdTokenResponse())
        }
    }
}

package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.application.api.authentication.Token
import no.nav.syfo.application.api.authentication.getNAVIdent
import no.nav.syfo.infrastructure.client.veiledertilgang.TilgangDTO
import no.nav.syfo.infrastructure.client.veiledertilgang.VeilederTilgangskontrollClient.Companion.ACCESS_TO_BRUKERE_PATH
import no.nav.syfo.infrastructure.client.veiledertilgang.VeilederTilgangskontrollClient.Companion.ACCESS_TO_SYFO_PATH
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_NO_ACCESS

val tilgangFalse = TilgangDTO(
    erGodkjent = false,
)

val tilgangTrue = TilgangDTO(
    erGodkjent = true,
)

suspend fun MockRequestHandleScope.tilgangskontrollResponse(request: HttpRequestData): HttpResponseData {
    val requestUrl = request.url.encodedPath

    return when {
        requestUrl.endsWith(ACCESS_TO_SYFO_PATH) -> {
            val token = Token(request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")!!)
            val navIdent = token.getNAVIdent()

            return if (navIdent == VEILEDER_IDENT_NO_ACCESS) {
                respond(tilgangFalse)
            } else {
                respond(tilgangTrue)
            }
        }
        requestUrl.endsWith(ACCESS_TO_BRUKERE_PATH) -> {
            val personidenter = request.receiveBody<List<String>>()
            val personerWhereTilgangOk = personidenter.filter { it != ARBEIDSTAKER_ADRESSEBESKYTTET.value }
            respond(personerWhereTilgangOk)
        }
        else -> error("Unhandled path $requestUrl")
    }
}

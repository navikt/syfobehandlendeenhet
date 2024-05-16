package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.syfo.application.api.authentication.Token
import no.nav.syfo.application.api.authentication.getNAVIdent
import no.nav.syfo.client.veiledertilgang.TilgangDTO
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_NO_ACCESS

val tilgangFalse = TilgangDTO(
    erGodkjent = false,
)

val tilgangTrue = TilgangDTO(
    erGodkjent = true,
)

fun MockRequestHandleScope.tilgangskontrollResponse(request: HttpRequestData): HttpResponseData {
    val token = Token(request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")!!)
    val navIdent = token.getNAVIdent()

    return if (navIdent == VEILEDER_IDENT_NO_ACCESS) {
        respond(tilgangFalse)
    } else {
        respond(tilgangTrue)
    }
}

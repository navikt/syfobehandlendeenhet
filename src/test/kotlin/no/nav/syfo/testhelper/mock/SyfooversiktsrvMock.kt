package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.infrastructure.client.syfooversiktsrv.VeilederBrukerKnytningDTO
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ENHET_ID
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER

fun MockRequestHandleScope.getSyfooversiktsrvResponse(request: HttpRequestData): HttpResponseData {
    val personident = request.headers.get(NAV_PERSONIDENT_HEADER)!!
    return respond(
        VeilederBrukerKnytningDTO(
            personident = PersonIdentNumber(personident),
            tildeltVeilederident = if (personident == ARBEIDSTAKER_PERSONIDENT.value) VEILEDER_IDENT else null,
            tildeltEnhet = ENHET_ID,
        )
    )
}

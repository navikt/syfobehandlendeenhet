package no.nav.syfo.api.feil


import no.nav.syfo.api.feil.Feilmelding.Companion.NO_BIGIP_5XX_REDIRECT
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

import javax.ws.rs.core.MediaType.APPLICATION_JSON

@Provider
class SyfoException(val feil: Feilmelding.Feil) : RuntimeException(), ExceptionMapper<SyfoException> {

    override fun toResponse(e: SyfoException): Response {
        val melding = Feilmelding().withFeil(e.feil)

        return Response
            .status(e.status())
            .entity(melding)
            .type(APPLICATION_JSON)
            .header(NO_BIGIP_5XX_REDIRECT, true)
            .build()
    }

    private fun status(): Int {
        return this.feil.status.statusCode
    }
}

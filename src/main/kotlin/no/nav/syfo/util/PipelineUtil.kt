package no.nav.syfo.util

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import no.nav.syfo.application.api.authentication.Token
import no.nav.syfo.application.api.authentication.getConsumerClientId

fun ApplicationCall.getBearerHeader(): Token? =
    this.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")?.let { Token(it) }

fun PipelineContext<out Unit, ApplicationCall>.getBearerHeader(): Token? = this.call.getBearerHeader()

fun ApplicationCall.getCallId(): String {
    return this.request.headers[NAV_CALL_ID_HEADER].toString()
}

fun PipelineContext<out Unit, ApplicationCall>.getCallId(): String {
    return this.call.getCallId()
}

fun ApplicationCall.getConsumerClientId(): String? = getBearerHeader()?.getConsumerClientId()

fun PipelineContext<out Unit, ApplicationCall>.personIdentHeader(): String? {
    return this.call.request.headers[NAV_PERSONIDENT_HEADER]
}

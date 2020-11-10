package no.nav.syfo.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class EmptyGTResponse(message: String = "TPS returned empty response for Geografisk Tilknytning") : RuntimeException(message)

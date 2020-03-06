package no.nav.syfo.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class RequestInvalid(message: String = "Bad request failed") : RuntimeException(message)

package no.nav.syfo.selftest

import no.nav.security.spring.oidc.validation.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

const val APPLICATION_LIVENESS = "Application is alive!"
const val APPLICATION_READY = "Application is ready!"

@RestController
@RequestMapping(value = ["/internal"])
class Selftest {

    val isAlive: String
        @Unprotected
        @ResponseBody
        @RequestMapping(value = ["/isAlive"], produces = [MediaType.TEXT_PLAIN_VALUE])
        get() = APPLICATION_LIVENESS

    val isReady: String
        @Unprotected
        @ResponseBody
        @RequestMapping(value = ["/isReady"], produces = [MediaType.TEXT_PLAIN_VALUE])
        get() = APPLICATION_READY
}

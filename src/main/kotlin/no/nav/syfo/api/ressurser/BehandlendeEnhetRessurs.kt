package no.nav.syfo.api.ressurser

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.consumers.AktoerConsumer
import no.nav.syfo.service.EnhetService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.inject.Inject

import no.nav.syfo.oidc.OIDCIssuer.INTERN

@RestController
@RequestMapping(value = ["/api/behandlendeenhet"])
@ProtectedWithClaims(issuer = INTERN)
class BehandlendeEnhetRessurs @Inject
constructor(
    private val contextHolder: OIDCRequestContextHolder,
    private val metrikk: Metrikk,
    private val aktoerConsumer: AktoerConsumer,
    private val enhetService: EnhetService
) {

    fun ting(): String {
        return "asd"
    }
}

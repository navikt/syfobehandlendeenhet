package no.nav.syfo.api.controllers

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims
import no.nav.syfo.domain.model.BehandlendeEnhet
import no.nav.syfo.metric.Metric
import no.nav.syfo.oidc.OIDCIssuer.STS
import no.nav.syfo.service.EnhetService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject


@RestController
@RequestMapping(value = ["/api"])
class BehandlendeEnhetController @Inject
constructor(
    private val enhetService: EnhetService,
    private val metric: Metric
) {

    @ProtectedWithClaims(issuer = STS)
    @GetMapping(value = ["/{fnr}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getArbeidstakersBehandlendeEnhet(@PathVariable fnr: String): BehandlendeEnhet {
        metric.countIncomingRequests("behandlendeEnhet")
        return enhetService.arbeidstakersBehandlendeEnhet(fnr)
            ?: throw RuntimeException("Couldn't find any behandlende enhet for this person")
    }
}

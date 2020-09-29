package no.nav.syfo.api.controllers

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.domain.model.BehandlendeEnhet
import no.nav.syfo.metric.Metric
import no.nav.syfo.oidc.OIDCIssuer.STS
import no.nav.syfo.service.EnhetService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
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
    fun getArbeidstakersBehandlendeEnhet(@PathVariable fnr: String): ResponseEntity<BehandlendeEnhet> {
        metric.countIncomingRequests("behandlendeEnhet")

        return createResponse(enhetService.arbeidstakersBehandlendeEnhet(fnr))
    }

    private fun createResponse(behandlendeEnhet: BehandlendeEnhet?): ResponseEntity<BehandlendeEnhet> {
        return if (behandlendeEnhet == null) {
            metric.countOutgoingReponses("get_behandlendeenhet_sts", 204)
            noContent().build()
        } else {
            ok().contentType(MediaType.APPLICATION_JSON).body(behandlendeEnhet)
        }
    }
}

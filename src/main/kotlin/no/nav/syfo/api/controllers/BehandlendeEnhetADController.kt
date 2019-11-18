package no.nav.syfo.api.controllers

import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims
import no.nav.syfo.consumers.TilgangConsumer
import no.nav.syfo.domain.model.BehandlendeEnhet
import no.nav.syfo.metric.Metric
import no.nav.syfo.oidc.OIDCIssuer.AZURE
import no.nav.syfo.service.EnhetService
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import javax.inject.Inject


@RestController
@ProtectedWithClaims(issuer = AZURE)
@RequestMapping(value = ["/api/internad"])
class BehandlendeEnhetADController @Inject
constructor(
        private val enhetService: EnhetService,
        private val metric: Metric,
        private val tilgangConsumer: TilgangConsumer
) {

    @GetMapping(value = ["/{fnr}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getBehandlendeEnhet(@PathVariable fnr: String): ResponseEntity<BehandlendeEnhet?> {
        metric.countIncomingRequests("internad_behandlendeEnhet")

        tilgangConsumer.throwExceptionIfVeilederWithoutAccessToSYFO()

        return ok()
                .contentType(APPLICATION_JSON)
                .body(enhetService.arbeidstakersBehandlendeEnhet(fnr))
                ?: noContent().build()
    }
}

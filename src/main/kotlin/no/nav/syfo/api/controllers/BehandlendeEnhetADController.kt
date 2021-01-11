package no.nav.syfo.api.controllers

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.consumers.TilgangConsumer
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.domain.model.BehandlendeEnhet
import no.nav.syfo.metric.Metric
import no.nav.syfo.oidc.OIDCIssuer.AZURE
import no.nav.syfo.service.EnhetService
import no.nav.syfo.util.getOrCreateCallId
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.util.MultiValueMap
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
    fun getBehandlendeEnhet(
        @RequestHeader headers: MultiValueMap<String, String>,
        @PathVariable fnr: String
    ): ResponseEntity<BehandlendeEnhet> {
        val callId = getOrCreateCallId(headers)

        metric.countIncomingRequests("internad_behandlendeEnhet")

        tilgangConsumer.throwExceptionIfVeilederWithoutAccessToSYFO()

        val personIdentNumber = PersonIdentNumber(fnr)

        return createResponse(enhetService.arbeidstakersBehandlendeEnhet(callId, personIdentNumber))
    }

    private fun createResponse(behandlendeEnhet: BehandlendeEnhet?): ResponseEntity<BehandlendeEnhet> {
        return if (behandlendeEnhet == null) {
            noContent().build()
        } else {
            ok().contentType(APPLICATION_JSON).body(behandlendeEnhet)
        }
    }
}

package no.nav.syfo.behandlendeenhet

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.api.auth.OIDCIssuer.AZURE
import no.nav.syfo.consumer.veiledertilgang.TilgangConsumer
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.getOrCreateCallId
import no.nav.syfo.util.getPersonIdent
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
    @GetMapping(value = ["/personident"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getBehandlendeEnhet(
        @RequestHeader headers: MultiValueMap<String, String>,
    ): ResponseEntity<BehandlendeEnhet> {
        val callId = getOrCreateCallId(headers)

        metric.countIncomingRequests("internad_behandlendeEnhet")

        val personIdentNumber = headers.getPersonIdent()?.let { personIdent ->
            PersonIdentNumber(personIdent)
        } ?: throw IllegalArgumentException("No PersonIdent supplied")

        tilgangConsumer.throwExceptionIfVeilederWithoutAccessToSYFO()

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

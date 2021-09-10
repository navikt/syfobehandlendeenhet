package no.nav.syfo.behandlendeenhet.api.system.v2

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.api.auth.OIDCIssuer.VEILEDER_AZURE_V2
import no.nav.syfo.behandlendeenhet.BehandlendeEnhet
import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.behandlendeenhet.api.access.APIConsumerAccessService
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.metric.Metric
import no.nav.syfo.util.getOrCreateCallId
import no.nav.syfo.util.getPersonIdent
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@RequestMapping(value = ["/api/system/v2"])
class BehandlendeEnhetSystemControllerV2 @Inject
constructor(
    private val aPIConsumerAccessService: APIConsumerAccessService,
    private val enhetService: EnhetService,
    private val metric: Metric
) {
    @ProtectedWithClaims(issuer = VEILEDER_AZURE_V2)
    @GetMapping(
        value = ["/personident"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getBehandlendeEnhet(
        @RequestHeader headers: MultiValueMap<String, String>,
    ): ResponseEntity<BehandlendeEnhet> {
        val callId = getOrCreateCallId(headers)

        metric.countIncomingRequests("behandlendeEnhet")

        val personIdentNumber = headers.getPersonIdent()?.let { personIdent ->
            PersonIdentNumber(personIdent)
        } ?: throw IllegalArgumentException("No PersonIdent supplied")

        aPIConsumerAccessService.validateConsumerApplicationAZP(
            authorizedApplicationNameList = authorizedAPIConsumerApplicationNameList
        )

        return createResponse(enhetService.arbeidstakersBehandlendeEnhet(callId, personIdentNumber))
    }

    private fun createResponse(behandlendeEnhet: BehandlendeEnhet?): ResponseEntity<BehandlendeEnhet> {
        return if (behandlendeEnhet == null) {
            metric.countOutgoingReponses("get_behandlendeenhet_sts", 204)
            noContent().build()
        } else {
            ok().contentType(MediaType.APPLICATION_JSON).body(behandlendeEnhet)
        }
    }

    companion object {
        private const val SYFOTILGANGSKONTROLL_NAME_AZP = "syfo-tilgangskontroll"
        val authorizedAPIConsumerApplicationNameList = listOf(
            SYFOTILGANGSKONTROLL_NAME_AZP,
        )
    }
}

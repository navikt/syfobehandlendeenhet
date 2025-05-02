package no.nav.syfo.behandlendeenhet.api.system

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.behandlendeenhet.api.BehandlendeEnhetResponseDTO
import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.behandlendeenhet.api.access.APIConsumerAccessService
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.*

const val systemBehandlendeEnhetApiV2BasePath = "/api/system/v2"
const val systemdBehandlendeEnhetApiV2PersonIdentPath = "/personident"

fun Route.registrerSystemApi(
    apiConsumerAccessService: APIConsumerAccessService,
    authorizedApplicationNameList: List<String>,
    enhetService: EnhetService,
) {
    route(systemBehandlendeEnhetApiV2BasePath) {
        get(systemdBehandlendeEnhetApiV2PersonIdentPath) {
            val callId = getCallId()
            val token = getBearerHeader()
                ?: throw IllegalArgumentException("Could not retrieve BehandlendeEnhet: No Authorization header supplied")

            apiConsumerAccessService.validateConsumerApplicationAZP(
                authorizedApplicationNameList = authorizedApplicationNameList,
                token = token,
            )

            val personIdentNumber = personIdentHeader()?.let { personIdent ->
                PersonIdentNumber(personIdent)
            }
                ?: throw IllegalArgumentException("Could not retrieve BehandlendeEnhet: No $NAV_PERSONIDENT_HEADER supplied in request header")

            enhetService.arbeidstakersBehandlendeEnhet(
                callId = callId,
                personIdentNumber = personIdentNumber,
                veilederToken = null,
            )
                .let { BehandlendeEnhetResponseDTO.fromBehandlendeEnhet(it) }
                .run { call.respond(this) }
        }
        post(systemdBehandlendeEnhetApiV2PersonIdentPath) {
            val callId = getCallId()
            val token = getBearerHeader()
                ?: throw IllegalArgumentException("Could not set BehandlendeEnhet: No Authorization header supplied")

            apiConsumerAccessService.validateConsumerApplicationAZP(
                authorizedApplicationNameList = authorizedApplicationNameList,
                token = token,
            )

            val personIdentNumber = personIdentHeader()?.let { personIdent ->
                PersonIdentNumber(personIdent)
            }
                ?: throw IllegalArgumentException("Could not set BehandlendeEnhet: No $NAV_PERSONIDENT_HEADER supplied in request header")

            enhetService.updateOppfolgingsenhet(
                callId = callId,
                personIdent = personIdentNumber,
                enhetId = null,
            )
            call.respond(HttpStatusCode.OK)
        }
    }
}

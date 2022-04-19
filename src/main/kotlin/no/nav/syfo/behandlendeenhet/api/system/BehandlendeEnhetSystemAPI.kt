package no.nav.syfo.behandlendeenhet.api.system

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
            )?.let { behandlendeEnhet ->
                call.respond(behandlendeEnhet)
            } ?: call.respond(HttpStatusCode.NoContent)
        }
    }
}

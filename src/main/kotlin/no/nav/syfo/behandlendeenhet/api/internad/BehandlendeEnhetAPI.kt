package no.nav.syfo.behandlendeenhet.api.internad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.*

const val internadBehandlendeEnhetApiV2BasePath = "/api/internad/v2"
const val internadBehandlendeEnhetApiV2PersonIdentPath = "/personident"

fun Route.registrerPersonApi(
    enhetService: EnhetService,
    veilederTilgangskontrollClient: VeilederTilgangskontrollClient
) {
    route(internadBehandlendeEnhetApiV2BasePath) {
        get(internadBehandlendeEnhetApiV2PersonIdentPath) {
            val callId = getCallId()
            val token = getBearerHeader()
                ?: throw IllegalArgumentException("Could not retrieve BehandlendeEnhet: No Authorization header supplied")

            val personIdentNumber = personIdentHeader()?.let { personIdent ->
                PersonIdentNumber(personIdent)
            }
                ?: throw IllegalArgumentException("Could not retrieve BehandlendeEnhet: No $NAV_PERSONIDENT_HEADER supplied in request header")

            veilederTilgangskontrollClient.throwExceptionIfVeilederWithoutAccessToSYFOWithOBO(
                callId = callId,
                token = token,
            )

            enhetService.arbeidstakersBehandlendeEnhet(
                callId = callId,
                personIdentNumber = personIdentNumber,
            )?.let { behandlendeEnhet ->
                call.respond(behandlendeEnhet)
            } ?: call.respond(HttpStatusCode.NoContent)
        }
    }
}

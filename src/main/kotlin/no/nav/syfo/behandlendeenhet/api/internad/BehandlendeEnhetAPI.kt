package no.nav.syfo.behandlendeenhet.api.internad

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.behandlendeenhet.api.BehandlendeEnhetResponseDTO
import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.behandlendeenhet.api.BehandlendeEnhetDTO
import no.nav.syfo.behandlendeenhet.domain.toBehandlendeEnhetDTO
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.infrastructure.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val ENHET_ID_PARAM = "enhetId"
const val internadBehandlendeEnhetApiV2BasePath = "/api/internad/v2"
const val internadBehandlendeEnhetApiV2PersonIdentPath = "/personident"
const val internadBehandlendeEnhetApiV2PersonPath = "/person"
const val internadBehandlendeEnhetApiV2TilordningsenheterPath = "/tilordningsenheter/{$ENHET_ID_PARAM}"

private val log: Logger = LoggerFactory.getLogger("no.nav.syfo.behandlendeenhet.api.internad")

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
                veilederToken = token,
            )
                .let { BehandlendeEnhetResponseDTO.fromBehandlendeEnhet(it) ?: HttpStatusCode.NoContent }
                .run { call.respond(this) }
        }

        get(internadBehandlendeEnhetApiV2TilordningsenheterPath) {
            val callId = getCallId()
            val token = getBearerHeader()
                ?: throw IllegalArgumentException("Could not retrieve BehandlendeEnhet: No Authorization header supplied")

            veilederTilgangskontrollClient.throwExceptionIfVeilederWithoutAccessToSYFOWithOBO(
                callId = callId,
                token = token,
            )
            val enhetId = call.parameters[ENHET_ID_PARAM]
                ?: throw IllegalArgumentException("Could not retrieve BehandlendeEnhet: No enhetId supplied in request")
            val tilordningsenheter = enhetService.getMuligeOppfolgingsenheter(callId, EnhetId(enhetId))

            call.respond(tilordningsenheter)
        }

        post(internadBehandlendeEnhetApiV2PersonPath) {
            val callId = getCallId()
            val token = getBearerHeader()
                ?: throw IllegalArgumentException("Could not retrieve Person: No Authorization header supplied")

            veilederTilgangskontrollClient.throwExceptionIfVeilederWithoutAccessToSYFOWithOBO(
                callId = callId,
                token = token,
            )

            val behandlendeEnhetDTO = call.receive<BehandlendeEnhetDTO>()

            val oppfolgingsenhet = enhetService.updateOppfolgingsenhet(
                callId = callId,
                personIdent = PersonIdentNumber(behandlendeEnhetDTO.personident),
                enhetId = behandlendeEnhetDTO.oppfolgingsenhet?.let { EnhetId(it) }
                    ?: if (behandlendeEnhetDTO.isNavUtland) EnhetId(EnhetId.ENHETNR_NAV_UTLAND) else null,
                veilederToken = token,
            )

            if (oppfolgingsenhet != null) {
                call.respond(oppfolgingsenhet.toBehandlendeEnhetDTO())
            } else {
                log.error("Could not set oppfolgingsenhet in database")
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

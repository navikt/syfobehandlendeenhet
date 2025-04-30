package no.nav.syfo.behandlendeenhet.api.internad

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.application.api.authentication.getNAVIdent
import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.behandlendeenhet.api.*
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.infrastructure.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import no.nav.syfo.util.getBearerHeader
import no.nav.syfo.util.getCallId
import no.nav.syfo.util.personIdentHeader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val ENHET_ID_PARAM = "enhetId"
const val internadBehandlendeEnhetApiV2BasePath = "/api/internad/v2"
const val internadBehandlendeEnhetApiV2PersonIdentPath = "/personident"
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
                .let { BehandlendeEnhetResponseDTO.fromBehandlendeEnhet(it) }
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
            val veilederident = token.getNAVIdent()

            val tilordningsenheter = enhetService.getMuligeOppfolgingsenheter(callId, EnhetId(enhetId), veilederident)

            call.respond(tilordningsenheter)
        }

        post("/oppfolgingsenhet-tildelinger") {
            val callId = call.getCallId()
            val token = call.getBearerHeader()
                ?: throw IllegalArgumentException("Failed to check tilgang to brukere for veileder. No Authorization header supplied")

            val tildelOppfolgingsenhetRequest = call.receive<TildelOppfolgingsenhetRequestDTO>()

            val personsWithVeilederAccess: List<String> =
                veilederTilgangskontrollClient.veilederPersonAccessListMedOBO(
                    personidenter = tildelOppfolgingsenhetRequest.personidenter,
                    token = token,
                    callId = callId,
                ) ?: emptyList()

            val personidenterNoAccess = tildelOppfolgingsenhetRequest.personidenter.subtract(personsWithVeilederAccess.toSet())
            val errorneousPersonidenter = mutableListOf<String>()
            val updatedOppfolgingsenheter = personsWithVeilederAccess.map { personIdent ->
                try {
                    val oppfolgingsenhet = enhetService.updateOppfolgingsenhet(
                        callId = callId,
                        personIdent = PersonIdentNumber(personIdent),
                        enhetId = EnhetId(tildelOppfolgingsenhetRequest.oppfolgingsenhet),
                        veilederToken = token,
                    )
                    if (oppfolgingsenhet != null) {
                        Result.success(oppfolgingsenhet)
                    } else {
                        Result.failure(IllegalStateException("Could not update oppfolgingsenhet"))
                    }
                } catch (exception: Exception) {
                    log.error(
                        "Failed to update oppfolgingsenhet ${tildelOppfolgingsenhetRequest.oppfolgingsenhet}, callId $callId",
                        exception
                    )
                    errorneousPersonidenter.add(personIdent)
                    Result.failure(exception)
                }
            }

            val successfulTildelinger = updatedOppfolgingsenheter
                .filter { it.isSuccess }
                .map {
                    val oppfolgingsenhet = it.getOrThrow()
                    TildelOppfolgingsenhetDTO(
                        personident = oppfolgingsenhet.personident.value,
                        oppfolgingsenhet = oppfolgingsenhet.enhetId?.value,
                    )
                }
            val unsuccessfulTildelinger = errorneousPersonidenter
                .map { personident ->
                    ErrorDTO(
                        personident = personident,
                        errorMessage = "Failed to update oppfolgingsenhet",
                    )
                }.plus(
                    personidenterNoAccess.map { personident ->
                        ErrorDTO(
                            personident = personident,
                            errorMessage = "Veileder does not have access to this person",
                            errorCode = HttpStatusCode.Forbidden.value,
                        )
                    }
                )

            val tildelOppfolgingsenhetResponseDTO = TildelOppfolgingsenhetResponseDTO(
                tildelinger = successfulTildelinger,
                errors = unsuccessfulTildelinger,
            )

            call.respond(
                status = if (successfulTildelinger.isNotEmpty()) {
                    HttpStatusCode.OK
                } else if (personidenterNoAccess.isNotEmpty()) {
                    HttpStatusCode.Forbidden
                } else {
                    HttpStatusCode.InternalServerError
                },
                message = tildelOppfolgingsenhetResponseDTO,
            )
        }
    }
}

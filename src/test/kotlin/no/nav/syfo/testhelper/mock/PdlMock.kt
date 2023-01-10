package no.nav.syfo.testhelper.mock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.client.pdl.PdlClient.Companion.GT_HEADER
import no.nav.syfo.client.pdl.domain.*
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.getRandomPort
import no.nav.syfo.util.configuredJacksonMapper

const val geografiskTilknytningKommune = "0330"

fun generatePdlHentGeografiskTilknytning(
    hentGeografiskTilknytning: PdlGeografiskTilknytning? = PdlGeografiskTilknytning(
        gtType = PdlGeografiskTilknytningType.KOMMUNE.name,
        gtBydel = null,
        gtKommune = geografiskTilknytningKommune,
        gtLand = null,
    )
) = PdlHentGeografiskTilknytning(
    hentGeografiskTilknytning = hentGeografiskTilknytning
)

fun generatePdlGeografiskTilknytningNotFoundResponse() =
    PdlGeografiskTilknytningResponse(
        data = generatePdlHentGeografiskTilknytning(
            hentGeografiskTilknytning = null,
        ),
        errors = null,
    )

fun generatePdlGeografiskTilknytningResponse() =
    PdlGeografiskTilknytningResponse(
        data = generatePdlHentGeografiskTilknytning(),
        errors = null,
    )

fun generatePdlPersonResponse(
    gradering: Gradering? = null,
) = PdlPersonResponse(
    errors = null,
    data = generatePdlHentPerson(
        adressebeskyttelse = generateAdressebeskyttelse(
            gradering = gradering,
        ),
    )
)

fun generateAdressebeskyttelse(
    gradering: Gradering? = null
): Adressebeskyttelse {
    return Adressebeskyttelse(
        gradering = gradering ?: Gradering.UGRADERT
    )
}

fun generatePdlHentPerson(
    adressebeskyttelse: Adressebeskyttelse? = null
): PdlHentPerson {
    return PdlHentPerson(
        hentPerson = PdlPerson(
            adressebeskyttelse = listOf(
                adressebeskyttelse ?: generateAdressebeskyttelse()
            ),
        )
    )
}

fun generatePdlIdenter(
    personident: String,
) = PdlIdenterResponse(
    data = PdlHentIdenter(
        hentIdenter = PdlIdenter(
            identer = listOf(
                PdlIdent(
                    ident = personident,
                    historisk = false,
                    gruppe = IdentType.FOLKEREGISTERIDENT,
                ),
                PdlIdent(
                    ident = "9${personident.drop(1)}",
                    historisk = true,
                    gruppe = IdentType.FOLKEREGISTERIDENT,
                ),
            ),
        ),
    ),
    errors = null,
)

class PdlMock {
    private val port = getRandomPort()
    private val objectMapper: ObjectMapper = configuredJacksonMapper()
    val url = "http://localhost:$port"
    val name = "pdl"
    val personResponseDefault = generatePdlPersonResponse()

    val server = embeddedServer(
        factory = Netty,
        port = port,
    ) {
        installContentNegotiation()
        routing {
            post {
                if (call.request.headers[GT_HEADER] == GT_HEADER) {
                    val pdlRequest = call.receive<PdlGeografiskTilknytningRequest>()
                    if (UserConstants.ARBEIDSTAKER_PERSONIDENT.value == pdlRequest.variables.ident) {
                        call.respond(generatePdlGeografiskTilknytningResponse())
                    } else if (UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value == pdlRequest.variables.ident) {
                        call.respond(generatePdlGeografiskTilknytningNotFoundResponse())
                    } else {
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                } else {
                    val pdlRequest = call.receiveText()
                    val isHentIdenter = pdlRequest.contains("hentIdenter")
                    if (isHentIdenter) {
                        val request: PdlHentIdenterRequest = objectMapper.readValue(pdlRequest)
                        if (request.variables.ident == UserConstants.ARBEIDSTAKER_PERSONIDENT_3.value) {
                            call.respond(generatePdlIdenter("enAnnenIdent"))
                        } else {
                            call.respond(generatePdlIdenter(request.variables.ident))
                        }
                    } else {
                        val request: PdlRequest = objectMapper.readValue(pdlRequest)
                        call.respond(generatePdlPersonResponse())
                        if (ARBEIDSTAKER_ADRESSEBESKYTTET.value == request.variables.ident) {
                            call.respond(generatePdlPersonResponse(Gradering.STRENGT_FORTROLIG))
                        } else {
                            call.respond(personResponseDefault)
                        }
                    }
                }
            }
        }
    }
}

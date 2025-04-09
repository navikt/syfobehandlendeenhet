package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.syfo.infrastructure.client.pdl.PdlClient.Companion.GT_HEADER
import no.nav.syfo.infrastructure.client.pdl.domain.*
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET

const val geografiskTilknytningKommune = "0330"
const val geografiskTilknytningAnnenKommune = "0440"

fun generateAnnenGeografiskTilknytningKommune() =
    PdlGeografiskTilknytning(
        gtType = PdlGeografiskTilknytningType.KOMMUNE.name,
        gtBydel = null,
        gtKommune = geografiskTilknytningAnnenKommune,
        gtLand = null,
    )

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

suspend fun MockRequestHandleScope.pdlMockResponse(request: HttpRequestData): HttpResponseData {
    return if (request.headers[GT_HEADER] == GT_HEADER) {
        val pdlRequest = request.receiveBody<PdlGeografiskTilknytningRequest>()
        when (pdlRequest.variables.ident) {
            UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value,
            UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND_2.value -> {
                respond(generatePdlGeografiskTilknytningNotFoundResponse())
            }
            UserConstants.ARBEIDSTAKER_PERSONIDENT_3.value -> {
                respond(
                    PdlGeografiskTilknytningResponse(
                        data = PdlHentGeografiskTilknytning(
                            hentGeografiskTilknytning = generateAnnenGeografiskTilknytningKommune()
                        ),
                        errors = emptyList(),
                    )
                )
            }
            else -> {
                respond(generatePdlGeografiskTilknytningResponse())
            }
        }
    } else {
        val isHentIdenterRequest = request.receiveBody<Any>().toString().contains("hentIdenter")
        return if (isHentIdenterRequest) {
            val pdlRequest = request.receiveBody<PdlHentIdenterRequest>()
            when (val personIdent = pdlRequest.variables.ident) {
                UserConstants.ARBEIDSTAKER_PERSONIDENT_3.value -> {
                    respond(generatePdlIdenter("enAnnenIdent"))
                }
                else -> {
                    respond(generatePdlIdenter(personIdent))
                }
            }
        } else {
            val pdlRequest = request.receiveBody<PdlRequest>()
            when (pdlRequest.variables.ident) {
                ARBEIDSTAKER_ADRESSEBESKYTTET.value -> {
                    respond(generatePdlPersonResponse(Gradering.STRENGT_FORTROLIG))
                }
                else -> {
                    respond(generatePdlPersonResponse())
                }
            }
        }
    }
}

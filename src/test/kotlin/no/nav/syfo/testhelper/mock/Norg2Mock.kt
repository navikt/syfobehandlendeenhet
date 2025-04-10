package no.nav.syfo.testhelper.mock

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.syfo.infrastructure.client.norg.NorgClient.Companion.ENHET_TYPE_LOKAL
import no.nav.syfo.infrastructure.client.norg.domain.*
import no.nav.syfo.testhelper.UserConstants.ENHET_ID

const val GEOGRAFISK_ENHET_NR = "0101"
const val GEOGRAFISK_ENHET_NR_2 = "0102"
const val OVERORDNET_NR = "0200"
const val UNDERORDNET_NR = "0103"
const val ENHET_NAVN = "Enhet"
const val ENHET_NAVN_2 = "Enhet2"

fun generateNorgEnhet(): NorgEnhet {
    return NorgEnhet(
        enhetNr = GEOGRAFISK_ENHET_NR,
        navn = ENHET_NAVN,
        status = Enhetsstatus.AKTIV.formattedName,
        aktiveringsdato = null,
        antallRessurser = null,
        enhetId = null,
        kanalstrategi = null,
        nedleggelsesdato = null,
        oppgavebehandler = null,
        orgNivaa = null,
        orgNrTilKommunaltNavKontor = null,
        organisasjonsnummer = null,
        sosialeTjenester = null,
        type = ENHET_TYPE_LOKAL,
        underAvviklingDato = null,
        underEtableringDato = null,
        versjon = null,
    )
}

val norg2Response = listOf(generateNorgEnhet())
val norg2ResponseAnnenKommune = listOf(generateNorgEnhet().copy(enhetNr = ENHET_ID, navn = "Annen kommune"))
val norg2ResponseNavUtland = listOf(generateNorgEnhet().copy(enhetNr = "0393", navn = "Nav utland"))
val norg2ResponseOverordnet = listOf(generateNorgEnhet().copy(enhetNr = OVERORDNET_NR, navn = "Overordnet"))

suspend fun MockRequestHandleScope.getNorg2Response(request: HttpRequestData): HttpResponseData {
    val path = request.url.encodedPath
    return if (path.endsWith("bestmatch")) {
        val body = request.receiveBody<ArbeidsfordelingCriteria>()
        if (body.behandlingstype == ArbeidsfordelingCriteriaBehandlingstype.NAV_UTLAND.behandlingstype) {
            respond(norg2ResponseNavUtland)
        } else if (body.geografiskOmraade == geografiskTilknytningAnnenKommune) {
            respond(norg2ResponseAnnenKommune)
        } else {
            respond(norg2Response)
        }
    } else if (path.contains("overordnet")) {
        respond(norg2ResponseOverordnet)
    } else if (path.contains("organisering")) {
        respond(
            listOf(
                RsOrganisering(
                    orgType = "ENHET",
                    organiserer = RsSimpleEnhet(OVERORDNET_NR, "Overordnet"),
                    organisertUnder = RsSimpleEnhet(UNDERORDNET_NR, "Underordnet"),
                    gyldigFra = null,
                    gyldigTil = null,
                ),
                RsOrganisering(
                    orgType = "ENHET",
                    organiserer = RsSimpleEnhet(OVERORDNET_NR, "Overordnet"),
                    organisertUnder = RsSimpleEnhet(UNDERORDNET_NR, "Underordnet"),
                    gyldigFra = null,
                    gyldigTil = null,
                ),
                RsOrganisering(
                    orgType = "ENHET",
                    organiserer = RsSimpleEnhet(OVERORDNET_NR, "Overordnet"),
                    organisertUnder = RsSimpleEnhet(GEOGRAFISK_ENHET_NR, ENHET_NAVN),
                    gyldigFra = null,
                    gyldigTil = null,
                ),
                RsOrganisering(
                    orgType = "ENHET",
                    organiserer = RsSimpleEnhet(OVERORDNET_NR, "Overordnet"),
                    organisertUnder = RsSimpleEnhet(GEOGRAFISK_ENHET_NR_2, ENHET_NAVN_2),
                    gyldigFra = null,
                    gyldigTil = null,
                ),
            )
        )
    } else if (path.contains("enhet")) {
        val enhetNr = path.substring(path.length - 4)
        respond(norg2Response.first().copy(enhetNr = enhetNr))
    } else {
        throw Exception("Unhandled ${request.url}")
    }
}

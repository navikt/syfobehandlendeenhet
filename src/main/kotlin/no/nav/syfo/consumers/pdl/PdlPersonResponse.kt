package no.nav.syfo.consumers.pdl

import no.nav.syfo.domain.model.ArbeidsfordelingCriteriaDiskresjonskode
import java.io.Serializable

data class PdlPersonResponse(
    val errors: List<PdlError>?,
    val data: PdlHentPerson?
)

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>?,
    val extensions: PdlErrorExtension
)

fun PdlError.errorMessage(): String {
    return "${this.message} with code: ${extensions.code} and classification: ${extensions.classification}"
}

data class PdlErrorLocation(
    val line: Int?,
    val column: Int?
)

data class PdlErrorExtension(
    val code: String?,
    val classification: String
)

data class PdlHentPerson(
    val hentPerson: PdlPerson?
) : Serializable

data class PdlPerson(
    val adressebeskyttelse: List<Adressebeskyttelse>?
) : Serializable

data class Adressebeskyttelse(
    val gradering: Gradering
) : Serializable

enum class Gradering : Serializable {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT
}

fun List<Gradering>.toArbeidsfordelingCriteriaDiskresjonskode(): ArbeidsfordelingCriteriaDiskresjonskode? {
    return when {
        this.any { it.isKode6() } -> {
            ArbeidsfordelingCriteriaDiskresjonskode.SPSF
        }
        this.any { it.isKode7() } -> {
            ArbeidsfordelingCriteriaDiskresjonskode.SPFO
        }
        else -> {
            null
        }
    }
}

fun PdlHentPerson.gradering(): List<Gradering>? {

    val adressebeskyttelseList = this.hentPerson?.adressebeskyttelse
    return if (adressebeskyttelseList.isNullOrEmpty()) {
        null
    } else {
        adressebeskyttelseList.map {
            it.gradering
        }
    }
}

fun Gradering.isKode6(): Boolean {
    return this == Gradering.STRENGT_FORTROLIG || this == Gradering.STRENGT_FORTROLIG_UTLAND
}

fun Gradering.isKode7(): Boolean {
    return this == Gradering.FORTROLIG
}

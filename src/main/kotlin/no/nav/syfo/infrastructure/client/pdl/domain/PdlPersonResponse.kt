package no.nav.syfo.infrastructure.client.pdl.domain

import no.nav.syfo.infrastructure.client.norg.domain.ArbeidsfordelingCriteriaDiskresjonskode
import java.io.Serializable

data class PdlPersonResponse(
    val errors: List<PdlError>?,
    val data: PdlHentPerson?,
)

data class PdlHentPerson(
    val hentPerson: PdlPerson?,
) : Serializable

data class PdlPerson(
    val adressebeskyttelse: List<Adressebeskyttelse>?,
) : Serializable

data class Adressebeskyttelse(
    val gradering: Gradering,
) : Serializable

enum class Gradering : Serializable {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT,
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

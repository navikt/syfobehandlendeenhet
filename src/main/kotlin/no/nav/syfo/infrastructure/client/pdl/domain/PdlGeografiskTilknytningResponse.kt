package no.nav.syfo.infrastructure.client.pdl.domain

import no.nav.syfo.infrastructure.client.pdl.GeografiskTilknytning
import no.nav.syfo.infrastructure.client.pdl.GeografiskTilknytningType
import java.io.Serializable
import java.time.LocalDateTime

data class PdlGeografiskTilknytningResponse(
    val data: PdlHentGeografiskTilknytning?,
    val errors: List<PdlError>?,
) : Serializable

data class PdlHentGeografiskTilknytning(
    val hentGeografiskTilknytning: PdlGeografiskTilknytning?,
    val hentPerson: PdlPerson? = null,
) : Serializable

data class PdlGeografiskTilknytning(
    val gtType: String,
    val gtBydel: String?,
    val gtKommune: String?,
    val gtLand: String?,
) : Serializable

enum class PdlGeografiskTilknytningType {
    BYDEL,
    KOMMUNE,
    UTLAND,
    UDEFINERT,
}

fun PdlHentGeografiskTilknytning.geografiskTilknytning(): GeografiskTilknytning? {
    val gt = hentGeografiskTilknytning ?: return null
    val geografiskTilknytning = gt.geografiskTilknytning() ?: return null
    val bostedsadresse = hentPerson?.bostedsadresse?.nyesteAdresse { it.gyldigFraOgMed }

    if (gt.gtKommune == null || gt.gtKommune != bostedsadresse?.ukjentBosted?.bostedskommune) {
        return geografiskTilknytning
    }

    val oppholdsadresse = hentPerson.oppholdsadresse?.nyesteAdresse { it.gyldigFraOgMed }
    return if (oppholdsadresse?.oppholdAnnetSted == "paaSvalbard") {
        GeografiskTilknytning(
            type = GeografiskTilknytningType.KOMMUNE,
            value = "2100", // Svalbard
        )
    } else {
        geografiskTilknytning
    }
}

private fun <T> List<T>.nyesteAdresse(gyldigFraOgMed: (T) -> LocalDateTime?): T? {
    val datoer = mapNotNull(gyldigFraOgMed)
    if (datoer.size != size) return null
    val nyesteDato = datoer.maxOrNull() ?: return null
    return singleOrNull { gyldigFraOgMed(it) == nyesteDato }
}

fun PdlGeografiskTilknytning.geografiskTilknytning(): GeografiskTilknytning? {
    this.let { gt ->
        return when (gt.gtType) {
            PdlGeografiskTilknytningType.BYDEL.name -> {
                GeografiskTilknytning(
                    type = GeografiskTilknytningType.valueOf(PdlGeografiskTilknytningType.BYDEL.name),
                    value = gt.gtBydel,
                )
            }
            PdlGeografiskTilknytningType.KOMMUNE.name -> {
                GeografiskTilknytning(
                    type = GeografiskTilknytningType.valueOf(PdlGeografiskTilknytningType.KOMMUNE.name),
                    value = gt.gtKommune,
                )
            }
            PdlGeografiskTilknytningType.UTLAND.name -> {
                GeografiskTilknytning(
                    type = GeografiskTilknytningType.valueOf(PdlGeografiskTilknytningType.UTLAND.name),
                    value = gt.gtLand,
                )
            }
            PdlGeografiskTilknytningType.UDEFINERT.name -> {
                GeografiskTilknytning(
                    type = GeografiskTilknytningType.valueOf(PdlGeografiskTilknytningType.UDEFINERT.name),
                    value = null,
                )
            }
            else -> null
        }
    }
}

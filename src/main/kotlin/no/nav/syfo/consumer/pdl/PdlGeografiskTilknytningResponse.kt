package no.nav.syfo.consumer.pdl

import java.io.Serializable

data class PdlGeografiskTilknytningResponse(
    val data: PdlHentGeografiskTilknytning?,
    val errors: List<PdlError>?
) : Serializable

data class PdlHentGeografiskTilknytning(
    val hentGeografiskTilknytning: PdlGeografiskTilknytning?
) : Serializable

data class PdlGeografiskTilknytning(
    val gtType: String,
    val gtBydel: String?,
    val gtKommune: String?,
    val gtLand: String?
) : Serializable

enum class PdlGeografiskTilknytningType {
    BYDEL,
    KOMMUNE,
    UTLAND,
    UDEFINERT
}

fun PdlHentGeografiskTilknytning.geografiskTilknytning(): String? {
    val geografiskTilknytning = this.hentGeografiskTilknytning
    geografiskTilknytning?.let { gt ->
        return when (gt.gtType) {
            PdlGeografiskTilknytningType.BYDEL.name -> {
                gt.gtBydel
            }
            PdlGeografiskTilknytningType.KOMMUNE.name -> {
                gt.gtKommune
            }
            PdlGeografiskTilknytningType.UTLAND.name -> {
                gt.gtLand
            }
            PdlGeografiskTilknytningType.UDEFINERT.name -> {
                null
            }
            else -> null
        }
    } ?: return null
}

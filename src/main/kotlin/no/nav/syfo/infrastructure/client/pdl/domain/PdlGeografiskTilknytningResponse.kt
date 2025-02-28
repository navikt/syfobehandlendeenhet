package no.nav.syfo.infrastructure.client.pdl.domain

import no.nav.syfo.infrastructure.client.pdl.GeografiskTilknytning
import no.nav.syfo.infrastructure.client.pdl.GeografiskTilknytningType
import java.io.Serializable

data class PdlGeografiskTilknytningResponse(
    val data: PdlHentGeografiskTilknytning?,
    val errors: List<PdlError>?,
) : Serializable

data class PdlHentGeografiskTilknytning(
    val hentGeografiskTilknytning: PdlGeografiskTilknytning?,
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

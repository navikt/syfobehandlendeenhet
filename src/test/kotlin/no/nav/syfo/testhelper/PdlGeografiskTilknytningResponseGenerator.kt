package no.nav.syfo.testhelper

import no.nav.syfo.consumer.pdl.PdlGeografiskTilknytning
import no.nav.syfo.consumer.pdl.PdlGeografiskTilknytningType
import no.nav.syfo.consumer.pdl.PdlHentGeografiskTilknytning

const val geografiskTilknytningKommune = "0330"

fun generatePdlHentGeografiskTilknytning(
    hentGeografiskTilknytning: PdlGeografiskTilknytning? = null
): PdlHentGeografiskTilknytning {
    return PdlHentGeografiskTilknytning(
        hentGeografiskTilknytning = hentGeografiskTilknytning ?: PdlGeografiskTilknytning(
            gtType = PdlGeografiskTilknytningType.KOMMUNE.name,
            gtBydel = null,
            gtKommune = geografiskTilknytningKommune,
            gtLand = null
        )
    )
}

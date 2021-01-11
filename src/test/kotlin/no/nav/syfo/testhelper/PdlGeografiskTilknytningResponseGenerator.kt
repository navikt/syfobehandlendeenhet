package no.nav.syfo.testhelper

import no.nav.syfo.consumers.pdl.PdlGeografiskTilknytning
import no.nav.syfo.consumers.pdl.PdlGeografiskTilknytningType
import no.nav.syfo.consumers.pdl.PdlHentGeografiskTilknytning

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

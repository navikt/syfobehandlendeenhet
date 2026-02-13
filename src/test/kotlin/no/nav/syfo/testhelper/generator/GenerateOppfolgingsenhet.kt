package no.nav.syfo.testhelper.generator

import no.nav.syfo.api.TildelOppfolgingsenhetRequestDTO
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT

fun generateTildelOppfolgingsenhetRequestDTO(
    personidenter: List<String> = listOf(ARBEIDSTAKER_PERSONIDENT.value),
    oppfolgingsenhet: String,
) = TildelOppfolgingsenhetRequestDTO(
    personidenter = personidenter,
    oppfolgingsenhet = oppfolgingsenhet,
)

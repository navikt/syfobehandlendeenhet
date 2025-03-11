package no.nav.syfo.testhelper.generator

import no.nav.syfo.behandlendeenhet.api.BehandlendeEnhetDTO
import no.nav.syfo.domain.Enhet.Companion.ENHETNR_NAV_UTLAND
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT

fun generateBehandlendeEnhetDTO(
    personident: String = ARBEIDSTAKER_PERSONIDENT.value,
    isNavUtland: Boolean = true,
    oppfolgingsenhet: String? = null,
) = BehandlendeEnhetDTO(
    personident = personident,
    isNavUtland = isNavUtland,
    oppfolgingsenhet = if (isNavUtland) ENHETNR_NAV_UTLAND else oppfolgingsenhet,
)

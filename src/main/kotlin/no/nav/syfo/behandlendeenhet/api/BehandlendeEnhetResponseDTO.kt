package no.nav.syfo.behandlendeenhet.api

import no.nav.syfo.behandlendeenhet.Enhet
import no.nav.syfo.domain.BehandlendeEnhet

data class BehandlendeEnhetResponseDTO(
    val geografiskEnhet: Enhet,
    val oppfolgingsenhet: Enhet,
) {
    companion object {
        fun fromBehandlendeEnhet(behandlendeEnhet: BehandlendeEnhet): BehandlendeEnhetResponseDTO {
            val oppfolgingsenhet = behandlendeEnhet.oppfolgingsenhet ?: behandlendeEnhet.geografiskEnhet

            return BehandlendeEnhetResponseDTO(
                geografiskEnhet = behandlendeEnhet.geografiskEnhet,
                oppfolgingsenhet = oppfolgingsenhet,
            )
        }
    }
}

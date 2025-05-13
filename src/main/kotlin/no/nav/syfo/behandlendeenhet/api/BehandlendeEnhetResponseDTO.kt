package no.nav.syfo.behandlendeenhet.api

import no.nav.syfo.behandlendeenhet.Enhet
import no.nav.syfo.domain.BehandlendeEnhet

data class BehandlendeEnhetResponseDTO(
    @Deprecated("Erstattet av geografisk enhet og oppfolgingsenhet")
    val enhetId: String,
    @Deprecated("Erstattet av geografisk enhet og oppfolgingsenhet")
    val navn: String,
    val geografiskEnhet: Enhet,
    val oppfolgingsenhet: Enhet,
) {
    companion object {
        fun fromBehandlendeEnhet(behandlendeEnhet: BehandlendeEnhet): BehandlendeEnhetResponseDTO {
            val oppfolgingsenhet = behandlendeEnhet.oppfolgingsenhet ?: behandlendeEnhet.geografiskEnhet

            return BehandlendeEnhetResponseDTO(
                enhetId = oppfolgingsenhet.enhetId,
                navn = oppfolgingsenhet.navn,
                geografiskEnhet = behandlendeEnhet.geografiskEnhet,
                oppfolgingsenhet = oppfolgingsenhet,
            )
        }
    }
}

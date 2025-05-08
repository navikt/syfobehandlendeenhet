package no.nav.syfo.behandlendeenhet.api

import no.nav.syfo.behandlendeenhet.domain.Enhet
import no.nav.syfo.behandlendeenhet.domain.BehandlendeEnhet
import java.time.OffsetDateTime

data class BehandlendeEnhetResponseDTO(
    val geografiskEnhet: Enhet,
    val oppfolgingsenhet: Enhet, // TODO: remove when not used anymore
    val oppfolgingsenhetDTO: OppfolgingsenhetDTO?,
) {
    companion object {
        fun fromBehandlendeEnhet(behandlendeEnhet: BehandlendeEnhet): BehandlendeEnhetResponseDTO {
            return BehandlendeEnhetResponseDTO(
                geografiskEnhet = behandlendeEnhet.geografiskEnhet,
                oppfolgingsenhet = behandlendeEnhet.oppfolgingsenhet?.enhet ?: behandlendeEnhet.geografiskEnhet,
                oppfolgingsenhetDTO = behandlendeEnhet.oppfolgingsenhet?.enhet?.let {
                    OppfolgingsenhetDTO(
                        enhet = behandlendeEnhet.oppfolgingsenhet.enhet,
                        createdAt = behandlendeEnhet.oppfolgingsenhet.createdAt,
                        veilederident = behandlendeEnhet.oppfolgingsenhet.veilederident,
                    )
                }
            )
        }
    }
}

data class OppfolgingsenhetDTO(
    val enhet: Enhet,
    val createdAt: OffsetDateTime,
    val veilederident: String,
)

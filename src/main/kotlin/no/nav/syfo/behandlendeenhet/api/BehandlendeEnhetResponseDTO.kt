package no.nav.syfo.behandlendeenhet.api

import no.nav.syfo.behandlendeenhet.domain.Enhet
import no.nav.syfo.behandlendeenhet.domain.BehandlendeEnhet
import java.time.OffsetDateTime

data class BehandlendeEnhetResponseDTO(
    @Deprecated("Erstattet av geografisk enhet og oppfolgingsenhet")
    val enhetId: String,
    @Deprecated("Erstattet av geografisk enhet og oppfolgingsenhet")
    val navn: String,

    val geografiskEnhet: Enhet,
    val oppfolgingsenhet: Enhet, // TODO: remove when not used anymore
    val oppfolgingsenhetDTO: OppfolgingsenhetDTO?,
) {
    companion object {
        fun fromBehandlendeEnhet(behandlendeEnhet: BehandlendeEnhet): BehandlendeEnhetResponseDTO {
            val oppfolgingsenhet = behandlendeEnhet.oppfolgingsenhet?.enhet ?: behandlendeEnhet.geografiskEnhet
            return BehandlendeEnhetResponseDTO(
                enhetId = oppfolgingsenhet.enhetId.value,
                navn = oppfolgingsenhet.navn,
                geografiskEnhet = behandlendeEnhet.geografiskEnhet,
                oppfolgingsenhet = oppfolgingsenhet,
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

package no.nav.syfo.behandlendeenhet.api

import no.nav.syfo.behandlendeenhet.domain.Enhet
import no.nav.syfo.behandlendeenhet.domain.BehandlendeEnhet
import java.time.LocalDateTime

data class BehandlendeEnhetResponseDTO(
    val geografiskEnhet: EnhetDTO,
    val oppfolgingsenhetDTO: OppfolgingsenhetDTO?,
) {
    companion object {
        fun fromBehandlendeEnhet(behandlendeEnhet: BehandlendeEnhet): BehandlendeEnhetResponseDTO {
            return BehandlendeEnhetResponseDTO(
                geografiskEnhet = behandlendeEnhet.geografiskEnhet.toEnhetDTO(),
                oppfolgingsenhetDTO = behandlendeEnhet.oppfolgingsenhet?.enhet?.let {
                    OppfolgingsenhetDTO(
                        enhet = behandlendeEnhet.oppfolgingsenhet.enhet.toEnhetDTO(),
                        createdAt = behandlendeEnhet.oppfolgingsenhet.createdAt.toLocalDateTime(),
                        veilederident = behandlendeEnhet.oppfolgingsenhet.veilederident,
                    )
                }
            )
        }
    }
}

data class OppfolgingsenhetDTO(
    val enhet: EnhetDTO,
    val createdAt: LocalDateTime,
    val veilederident: String,
)

data class EnhetDTO(
    val enhetId: String,
    val navn: String,
)

fun Enhet.toEnhetDTO() =
    EnhetDTO(
        enhetId = this.enhetId.value,
        navn = this.navn,
    )

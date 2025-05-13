package no.nav.syfo.behandlendeenhet.api

import no.nav.syfo.behandlendeenhet.domain.Enhet
import no.nav.syfo.behandlendeenhet.domain.BehandlendeEnhet
import java.time.OffsetDateTime

data class BehandlendeEnhetResponseDTO(
    @Deprecated("Erstattet av geografisk enhet og oppfolgingsenhet")
    val enhetId: String,
    @Deprecated("Erstattet av geografisk enhet og oppfolgingsenhet")
    val navn: String,

    val geografiskEnhet: EnhetDTO,
    val oppfolgingsenhet: EnhetDTO, // TODO: remove when not used anymore
    val oppfolgingsenhetDTO: OppfolgingsenhetDTO?,
) {
    companion object {
        fun fromBehandlendeEnhet(behandlendeEnhet: BehandlendeEnhet): BehandlendeEnhetResponseDTO {
            val oppfolgingsenhet = behandlendeEnhet.oppfolgingsenhet?.enhet ?: behandlendeEnhet.geografiskEnhet
            return BehandlendeEnhetResponseDTO(
                enhetId = oppfolgingsenhet.enhetId.value,
                navn = oppfolgingsenhet.navn,
                geografiskEnhet = behandlendeEnhet.geografiskEnhet.toEnhetDTO(),
                oppfolgingsenhet = oppfolgingsenhet.toEnhetDTO(),
                oppfolgingsenhetDTO = behandlendeEnhet.oppfolgingsenhet?.enhet?.let {
                    OppfolgingsenhetDTO(
                        enhet = behandlendeEnhet.oppfolgingsenhet.enhet.toEnhetDTO(),
                        createdAt = behandlendeEnhet.oppfolgingsenhet.createdAt,
                        veilederident = behandlendeEnhet.oppfolgingsenhet.veilederident,
                    )
                }
            )
        }
    }
}

data class OppfolgingsenhetDTO(
    val enhet: EnhetDTO,
    val createdAt: OffsetDateTime,
    val veilederident: String,
)

data class EnhetDTO(
    val enhetId: String,
    val navn: String,
)

fun Enhet.toEnhetDTO(): EnhetDTO {
    return EnhetDTO(
        enhetId = this.enhetId.value,
        navn = this.navn,
    )
}

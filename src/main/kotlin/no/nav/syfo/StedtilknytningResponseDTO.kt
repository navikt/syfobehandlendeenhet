package no.nav.syfo

import no.nav.syfo.behandlendeenhet.BehandlendeEnhet
import no.nav.syfo.domain.Stedtilknytning

data class StedtilknytningResponseDTO(
    @Deprecated("Erstattet av geografisk enhet og oppfolgingsenhet")
    val enhetId: String,
    @Deprecated("Erstattet av geografisk enhet og oppfolgingsenhet")
    val navn: String,
    val geografiskEnhet: BehandlendeEnhet?,
    val oppfolgingsenhet: BehandlendeEnhet?,
) {
    companion object {
        fun fromStedtilknytning(stedtilknytning: Stedtilknytning): StedtilknytningResponseDTO? {
            val behandlendeEnhet = stedtilknytning.oppfolgingsenhet ?: stedtilknytning.geografiskEnhet

            if (behandlendeEnhet == null) {
                return null
            }
            return StedtilknytningResponseDTO(
                enhetId = behandlendeEnhet.enhetId,
                navn = behandlendeEnhet.navn,
                geografiskEnhet = stedtilknytning.geografiskEnhet,
                oppfolgingsenhet = stedtilknytning.oppfolgingsenhet,
            )
        }
    }
}

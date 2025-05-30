package no.nav.syfo.behandlendeenhet.api

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.syfo.behandlendeenhet.EnhetService.Companion.SYSTEM_USER_IDENT
import no.nav.syfo.behandlendeenhet.domain.Oppfolgingsenhet
import java.time.LocalDateTime

data class TildeltHistorikkResponseDTO(
    val tildelteOppfolgingsenheter: List<TildeltOppfolgingsenhetHistorikkDTO>,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
sealed class TildeltOppfolgingsenhetHistorikkDTO {
    abstract val createdAt: LocalDateTime
    abstract val veilederident: String

    companion object {
        fun fromOppfolgingsenhet(oppfolgingsenhet: Oppfolgingsenhet): TildeltOppfolgingsenhetHistorikkDTO {
            val isSystembruker = oppfolgingsenhet.veilederident == SYSTEM_USER_IDENT
            val isTildeltAvSystem = oppfolgingsenhet.enhet == null && isSystembruker

            return if (isTildeltAvSystem) {
                TildeltTilbakeAvSystem(
                    createdAt = oppfolgingsenhet.createdAt.toLocalDateTime(),
                    veilederident = oppfolgingsenhet.veilederident,
                )
            } else if (oppfolgingsenhet.enhet == null) {
                TildeltTilbake(
                    createdAt = oppfolgingsenhet.createdAt.toLocalDateTime(),
                    veilederident = oppfolgingsenhet.veilederident,
                )
            } else {
                Tildelt(
                    createdAt = oppfolgingsenhet.createdAt.toLocalDateTime(),
                    veilederident = oppfolgingsenhet.veilederident,
                    enhet = oppfolgingsenhet.enhet.toEnhetDTO(),
                )
            }
        }
    }
}

@JsonTypeName("TILDELT_ANNEN_ENHET_AV_VEILEDER")
data class Tildelt(
    override val createdAt: LocalDateTime,
    override val veilederident: String,
    val enhet: EnhetDTO,
) : TildeltOppfolgingsenhetHistorikkDTO()

@JsonTypeName("TILDELT_TILBAKE_TIL_GEOGRAFISK_ENHET_AV_VEILEDER")
data class TildeltTilbake(
    override val createdAt: LocalDateTime,
    override val veilederident: String,
) : TildeltOppfolgingsenhetHistorikkDTO()

@JsonTypeName("TILDELT_TILBAKE_TIL_GEOGRAFISK_ENHET_AV_SYSTEM")
data class TildeltTilbakeAvSystem(
    override val createdAt: LocalDateTime,
    override val veilederident: String,
) : TildeltOppfolgingsenhetHistorikkDTO()

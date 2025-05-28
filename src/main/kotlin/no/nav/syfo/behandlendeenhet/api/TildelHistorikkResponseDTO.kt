package no.nav.syfo.behandlendeenhet.api

import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.syfo.behandlendeenhet.EnhetService.Companion.SYSTEM_USER_IDENT
import no.nav.syfo.behandlendeenhet.api.TildelOppfolgingsenhetHistorikkType.*
import no.nav.syfo.behandlendeenhet.domain.Oppfolgingsenhet
import java.time.LocalDateTime

data class TildelHistorikkResponseDTO(
    val oppfolgingsenheter: List<TildelOppfolgingsenhetHistorikkDTO>,
)

enum class TildelOppfolgingsenhetHistorikkType {
    TILDELT_ANNEN_ENHET_AV_VEILEDER,
    TILDELT_TILBAKE_TIL_GEOGRAFISK_ENHET_AV_VEILEDER,
    TILDELT_TILBAKE_TIL_GEOGRAFISK_ENHET_AV_SYSTEM,
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
sealed class TildelOppfolgingsenhetHistorikkDTO {
    abstract val createdAt: LocalDateTime
    abstract val veilederident: String
    abstract val tildelOppfolgingsenhetHistorikkType: TildelOppfolgingsenhetHistorikkType

    companion object {
        fun fromOppfolgingsenhet(oppfolgingsenhet: Oppfolgingsenhet): TildelOppfolgingsenhetHistorikkDTO {
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

data class Tildelt(
    override val createdAt: LocalDateTime,
    override val veilederident: String,
    override val tildelOppfolgingsenhetHistorikkType: TildelOppfolgingsenhetHistorikkType = TILDELT_ANNEN_ENHET_AV_VEILEDER,
    val enhet: EnhetDTO,
) : TildelOppfolgingsenhetHistorikkDTO()

data class TildeltTilbake(
    override val createdAt: LocalDateTime,
    override val veilederident: String,
    override val tildelOppfolgingsenhetHistorikkType: TildelOppfolgingsenhetHistorikkType = TILDELT_TILBAKE_TIL_GEOGRAFISK_ENHET_AV_VEILEDER,
) : TildelOppfolgingsenhetHistorikkDTO()

data class TildeltTilbakeAvSystem(
    override val createdAt: LocalDateTime,
    override val veilederident: String,
    override val tildelOppfolgingsenhetHistorikkType: TildelOppfolgingsenhetHistorikkType = TILDELT_TILBAKE_TIL_GEOGRAFISK_ENHET_AV_SYSTEM,
) : TildelOppfolgingsenhetHistorikkDTO()

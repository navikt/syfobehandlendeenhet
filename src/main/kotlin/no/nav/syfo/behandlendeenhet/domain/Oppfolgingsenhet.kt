package no.nav.syfo.behandlendeenhet.domain

import no.nav.syfo.behandlendeenhet.api.BehandlendeEnhetDTO
import no.nav.syfo.domain.Enhet
import no.nav.syfo.domain.PersonIdentNumber
import java.time.OffsetDateTime
import java.util.*

data class Oppfolgingsenhet(
    val uuid: UUID,
    val personident: PersonIdentNumber,
    val enhet: Enhet?,
    val veilederident: String,
    val createdAt: OffsetDateTime,
)

fun Oppfolgingsenhet.toBehandlendeEnhetDTO() = BehandlendeEnhetDTO(
    personident = this.personident.value,
    isNavUtland = this.enhet?.isNavUtland() ?: false,
    oppfolgingsenhet = this.enhet?.value,
)

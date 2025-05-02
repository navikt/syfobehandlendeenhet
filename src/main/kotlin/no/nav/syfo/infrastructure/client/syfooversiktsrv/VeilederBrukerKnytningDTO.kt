package no.nav.syfo.infrastructure.client.syfooversiktsrv

import no.nav.syfo.domain.PersonIdentNumber

data class VeilederBrukerKnytningDTO(
    val personident: PersonIdentNumber,
    val tildeltVeilederident: String?,
    val tildeltEnhet: String?,
)

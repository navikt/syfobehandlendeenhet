package no.nav.syfo.api.mappers

import no.nav.syfo.api.domain.RSEnhet
import no.nav.syfo.domain.model.Enhet

import java.util.function.Function

object RSEnhetMapper {

    var enhet2rs = { enhet ->
        RSEnhet()
            .enhetId(enhet.enhetId)
            .navn(enhet.navn)
    }
}

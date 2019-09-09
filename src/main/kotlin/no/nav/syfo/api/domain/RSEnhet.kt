package no.nav.syfo.api.domain

import lombok.Data
import lombok.experimental.Accessors

@Data
@Accessors(fluent = true)
class RSEnhet {
    var enhetId: String? = null
    var navn: String? = null
}

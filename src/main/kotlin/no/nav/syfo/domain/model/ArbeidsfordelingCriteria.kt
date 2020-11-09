package no.nav.syfo.domain.model

data class ArbeidsfordelingCriteria(
    var diskresjonskode: String? = null,
    var oppgavetype: String? = null,
    var behandlingstype: String? = null,
    var behandlingstema: String? = null,
    var tema: String,
    var temagruppe: String? = null,
    var geografiskOmraade: String? = null,
    var enhetNummer: String? = null,
    var skjermet: Boolean
)

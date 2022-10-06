package no.nav.syfo.client.norg.domain

data class ArbeidsfordelingCriteria(
    var diskresjonskode: String? = null,
    var oppgavetype: String? = null,
    var behandlingstype: String,
    var behandlingstema: String? = null,
    var tema: String,
    var temagruppe: String? = null,
    var geografiskOmraade: String? = null,
    var enhetNummer: String? = null,
    var skjermet: Boolean,
)

enum class ArbeidsfordelingCriteriaDiskresjonskode {
    SPSF,
    SPFO,
}

enum class ArbeidsfordelingCriteriaBehandlingstype(
    val behandlingstype: String,
) {
    SYKEFRAVAERSOPPFOLGING("ae0257"),
    NAV_UTLAND("ae0106"),
}

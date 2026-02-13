package no.nav.syfo.domain

/**
 * Representerer behandlende enhet for en sykmeldt
 *
 * @property geografiskEnhet Den geografiske enheten til den sykmeldte. Grunnregelen er at den sykmeldte får oppfølging her.
 * @property oppfolgingsenhet Om oppfølgingen av en eller annen grunn ikke skal skje ved den geografiske enheten er det oppgitt en oppfølgingsenhet.
 */
data class BehandlendeEnhet(
    val geografiskEnhet: Enhet,
    val oppfolgingsenhet: Oppfolgingsenhet?,
)

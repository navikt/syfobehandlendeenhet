package no.nav.syfo.infrastructure.client.norg.domain

data class RsOrganisering(
    var orgType: String,
    var organiserer: RsSimpleEnhet,
    var organisertUnder: RsSimpleEnhet,
    var gyldigFra: String?,
    var gyldigTil: String?,
)

data class RsSimpleEnhet(
    var nr: String,
    var navn: String,
)

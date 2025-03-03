package no.nav.syfo.infrastructure.client.pdl.domain

data class PdlGeografiskTilknytningRequest(
    val query: String,
    val variables: PdlGeografiskTilknytningRequestVariables,
)

data class PdlGeografiskTilknytningRequestVariables(
    val ident: String,
)

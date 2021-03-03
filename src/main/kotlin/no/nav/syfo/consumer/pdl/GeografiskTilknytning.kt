package no.nav.syfo.consumer.pdl

data class GeografiskTilknytning(
    val type: GeografiskTilknytningType,
    val value: String?
)

enum class GeografiskTilknytningType {
    BYDEL,
    KOMMUNE,
    UTLAND,
    UDEFINERT
}

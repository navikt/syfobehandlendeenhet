package no.nav.syfo.infrastructure.client.pdl

private const val messageStart =
    "Request to get Geografisk Tilknytning from PersonDataLosningen was succesful, but no Geografisk Tilknytning was found in response."

class GeografiskTilknytningNotFoundException : RuntimeException(messageStart)

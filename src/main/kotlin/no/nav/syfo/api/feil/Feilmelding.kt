package no.nav.syfo.api.feil

import javax.ws.rs.core.Response.Status

import javax.ws.rs.core.Response.Status.*

class Feilmelding {

    private var feil: Feil? = null

    val id: String
        get() = feil!!.id

    enum class Feil private constructor(internal var status: Status, internal var id: String) {
        GENERELL_FEIL(
            INTERNAL_SERVER_ERROR, "feilmelding.generell.feil"
        ),
        IKKE_FOEDSELSNUMMER(
            INTERNAL_SERVER_ERROR, "feilmelding.ikke.fnr"
        ),
        INGEN_AKTOER_ID(
            INTERNAL_SERVER_ERROR, "feilmelding.ingen.aktoer.id"
        ),
        AKTOER_IKKE_FUNNET(
            INTERNAL_SERVER_ERROR, "feilmelding.aktoer.ikke.funnet"
        ),
        ARBEIDSFORHOLD_GENERELL_FEIL(
            INTERNAL_SERVER_ERROR, "feilmelding.arbeidsgivere.generell.feil"
        ),
        ARBEIDSFORHOLD_UGYLDIG_INPUT(
            INTERNAL_SERVER_ERROR, "feilmelding.arbeidsgivere.ugyldig.input"
        ),
        ARBEIDSFORHOLD_INGEN_TILGANG(
            INTERNAL_SERVER_ERROR, "feilmelding.arbeidsgivere.sikkerhetsbegrensning"
        ),
        SEND_SYKMELDING_INGEN_ID(
            BAD_REQUEST, "feilmelding.send.sykmelding.ingen.id"
        ),
        SEND_SYKMELDING_INGEN_ARBEIDSGIVER(
            BAD_REQUEST, "feilmelidng.send.sykmelding.ingen.arbeidsgiver"
        ),
        SEND_SYKMELDING_INGEN_TILGANG(
            FORBIDDEN, "feilmelding.send.sykmelding.ingen.tilgang"
        ),
        SYKMELDING_INGEN_TILGANG(
            FORBIDDEN, "feilmelding.sykmelding.ingen.tilgang"
        ),
        SEND_SYKMELDING_GENERELL_FEIL(
            INTERNAL_SERVER_ERROR, "feilmelding.send.sykmelding.generell.feil"
        ),
        TPS_GENERELL_FEIL(
            INTERNAL_SERVER_ERROR, "feilmelding.tps.generell.feil"
        ),
        ORGANISASJON_IKKE_FUNNET(
            NOT_FOUND, "feilmelding.organisasjon.ikke.funnet"
        ),
        ORGANISASJON_UGYLDIG_INPUT(
            INTERNAL_SERVER_ERROR, "feilmelding.organisasjon.ugyldig.input"
        ),
        ORGANISASJON_GENERELL_FEIL(
            INTERNAL_SERVER_ERROR, "feilmelding.organisasjon.generell.feil"
        ),
        SYKMELDING_GENERELL_FEIL(
            INTERNAL_SERVER_ERROR, "feilmelding.sykmelding.generell.feil"
        ),
        SYKMELDING_IKKE_FUNNET(
            NOT_FOUND, "feilmelding.sykmelding.ikke.funnet"
        ),
        SYKMELDING_LAGRE_STATUS_FEIL(
            INTERNAL_SERVER_ERROR, "feilmelding.sykmelding.lagre.status.feil"
        ),
        SYKMELDING_BEKREFT_MANGLER_ARBEIDSSITUASJON(
            BAD_REQUEST, "feilmelding.sykmelding.bekreft.mangler.arbeidssituasjon"
        )
    }

    internal fun withFeil(feil: Feil): Feilmelding {
        this.feil = feil
        return this
    }

    companion object {

        internal val NO_BIGIP_5XX_REDIRECT = "X-Escape-5xx-Redirect"
    }
}

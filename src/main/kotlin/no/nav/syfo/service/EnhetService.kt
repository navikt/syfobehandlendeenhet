package no.nav.syfo.service

import lombok.extern.slf4j.Slf4j
import no.nav.syfo.consumers.ArbeidsfordelingConsumer
import no.nav.syfo.consumers.EgenAnsattConsumer
import no.nav.syfo.consumers.OrganisasjonEnhetConsumer
import no.nav.syfo.consumers.PersonConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class EnhetService @Autowired
constructor(
    private val arbeidsfordelingConsumer: ArbeidsfordelingConsumer,
    private val egenAnsattConsumer: EgenAnsattConsumer,
    private val organisasjonEnhetConsumer: OrganisasjonEnhetConsumer,
    private val personConsumer: PersonConsumer
) {

    fun finnArbeidstakersBehandlendeEnhet(arbeidstakerFnr: String): String? {
        val geografiskTilknytning = personConsumer.hentGeografiskTilknytning(arbeidstakerFnr)
        val enhet = arbeidsfordelingConsumer.finnAktivBehandlendeEnhet(geografiskTilknytning)
        if (egenAnsattConsumer.erEgenAnsatt(arbeidstakerFnr)) {
            val overordnetEnhet = organisasjonEnhetConsumer.finnSetteKontor(enhet.enhetId).orElse(enhet)
            return overordnetEnhet.enhetId
        }
        return enhet.enhetId
    }
}

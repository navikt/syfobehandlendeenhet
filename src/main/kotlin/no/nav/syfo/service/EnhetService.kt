package no.nav.syfo.service

import no.nav.syfo.consumers.ArbeidsfordelingConsumer
import no.nav.syfo.consumers.EgenAnsattConsumer
import no.nav.syfo.consumers.OrganisasjonEnhetConsumer
import no.nav.syfo.consumers.PersonConsumer
import no.nav.syfo.domain.model.BehandlendeEnhet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EnhetService @Autowired
constructor(
    private val arbeidsfordelingConsumer: ArbeidsfordelingConsumer,
    private val egenAnsattConsumer: EgenAnsattConsumer,
    private val organisasjonEnhetConsumer: OrganisasjonEnhetConsumer,
    private val personConsumer: PersonConsumer
) {

    fun arbeidstakersBehandlendeEnhet(arbeidstakerFnr: String): BehandlendeEnhet? {
        val geografiskTilknytning = personConsumer.geografiskTilknytning(arbeidstakerFnr)
        val enhet = arbeidsfordelingConsumer.aktivBehandlendeEnhet(geografiskTilknytning)
        if (egenAnsattConsumer.isEgenAnsatt(arbeidstakerFnr)) {
            val overordnetEnhet = organisasjonEnhetConsumer.setteKontor(enhet.enhetId).orElse(enhet)
            return overordnetEnhet
        }
        return enhet
    }
}

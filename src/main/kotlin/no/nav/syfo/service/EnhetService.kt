package no.nav.syfo.service

import no.nav.syfo.consumers.NorgConsumer
import no.nav.syfo.consumers.*
import no.nav.syfo.domain.model.BehandlendeEnhet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EnhetService @Autowired
constructor(
    private val arbeidsfordelingConsumer: ArbeidsfordelingConsumer,
    private val egenAnsattConsumer: EgenAnsattConsumer,
    private val norgConsumer: NorgConsumer,
    private val personConsumer: PersonConsumer
) {

    fun arbeidstakersBehandlendeEnhet(arbeidstakerFnr: String): BehandlendeEnhet? {
        val geografiskTilknytning = personConsumer.geografiskTilknytning(arbeidstakerFnr)
        val enhet = arbeidsfordelingConsumer.aktivBehandlendeEnhet(geografiskTilknytning)
        if (egenAnsattConsumer.isEgenAnsatt(arbeidstakerFnr)) {
            val overordnetEnhet = norgConsumer.getSetteKontor(enhet.enhetId)
            return overordnetEnhet ?: enhet
        }
        return enhet
    }
}

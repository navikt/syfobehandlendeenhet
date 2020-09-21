package no.nav.syfo.service

import no.nav.syfo.consumers.*
import no.nav.syfo.domain.model.BehandlendeEnhet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EnhetService @Autowired
constructor(
    private val norgConsumer: NorgConsumer,
    private val personConsumer: PersonConsumer,
    private val skjermedePersonerPipConsumer: SkjermedePersonerPipConsumer
) {
    private val geografiskTilknytningUtvandret = "NOR"
    private val enhetnrNAVUtland = "0393"

    fun arbeidstakersBehandlendeEnhet(arbeidstakerFnr: String): BehandlendeEnhet? {
        val geografiskTilknytning = personConsumer.geografiskTilknytning(arbeidstakerFnr)
        val isEgenAnsatt = skjermedePersonerPipConsumer.erSkjermet(arbeidstakerFnr)

        val behandlendeEnhet = norgConsumer.getArbeidsfordelingEnhet(geografiskTilknytning, isEgenAnsatt) ?: return null

        return if (isEnhetUtvandret(behandlendeEnhet)) {
            getEnhetNAVUtland(behandlendeEnhet)
        } else {
            behandlendeEnhet
        }
    }

    fun isEnhetUtvandret(enhet: BehandlendeEnhet): Boolean {
        return enhet.enhetId == geografiskTilknytningUtvandret
    }

    fun getEnhetNAVUtland(enhet: BehandlendeEnhet): BehandlendeEnhet {
        return BehandlendeEnhet(
            enhetId = enhetnrNAVUtland,
            navn = enhet.navn
        )
    }
}

package no.nav.syfo.behandlendeenhet

import no.nav.syfo.config.CacheConfig.Companion.CACHENAME_BEHANDLENDEENHET
import no.nav.syfo.consumer.norg.NorgConsumer
import no.nav.syfo.consumer.skjermedepersonerpip.SkjermedePersonerPipConsumer
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.pdl.gradering
import no.nav.syfo.consumer.pdl.toArbeidsfordelingCriteriaDiskresjonskode
import no.nav.syfo.domain.PersonIdentNumber
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class EnhetService @Autowired
constructor(
    private val norgConsumer: NorgConsumer,
    private val pdlConsumer: PdlConsumer,
    private val skjermedePersonerPipConsumer: SkjermedePersonerPipConsumer
) {
    private val geografiskTilknytningUtvandret = "NOR"
    private val enhetnrNAVUtland = "0393"

    @Cacheable(cacheNames = [CACHENAME_BEHANDLENDEENHET], key = "#personIdent", condition = "#personIdent != null")
    fun arbeidstakersBehandlendeEnhet(
        callId: String,
        personIdentNumber: PersonIdentNumber
    ): BehandlendeEnhet? {
        val geografiskTilknytning = pdlConsumer.geografiskTilknytning(personIdentNumber)
        val isEgenAnsatt = skjermedePersonerPipConsumer.erSkjermet(callId, personIdentNumber.value)

        val graderingList = pdlConsumer.person(personIdentNumber)?.gradering()

        val behandlendeEnhet = norgConsumer.getArbeidsfordelingEnhet(
            callId,
            graderingList?.toArbeidsfordelingCriteriaDiskresjonskode(),
            geografiskTilknytning,
            isEgenAnsatt
        ) ?: return null

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

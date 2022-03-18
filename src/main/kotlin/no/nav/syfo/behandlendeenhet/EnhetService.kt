package no.nav.syfo.behandlendeenhet

import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.client.norg.NorgClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.pdl.domain.gradering
import no.nav.syfo.client.pdl.domain.toArbeidsfordelingCriteriaDiskresjonskode
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.domain.PersonIdentNumber

class EnhetService(
    private val norgClient: NorgClient,
    private val pdlClient: PdlClient,
    private val redisStore: RedisStore,
    private val skjermedePersonerPipClient: SkjermedePersonerPipClient,
) {
    private val geografiskTilknytningUtvandret = "NOR"
    private val enhetnrNAVUtland = "0393"

    suspend fun arbeidstakersBehandlendeEnhet(
        callId: String,
        personIdentNumber: PersonIdentNumber,
    ): BehandlendeEnhet? {
        val cacheKey = "$CACHE_BEHANDLENDEENHET_PERSONIDENT_KEY_PREFIX${personIdentNumber.value}"
        val cachedBehandlendeEnhet: BehandlendeEnhet? = redisStore.getObject(key = cacheKey)
        if (cachedBehandlendeEnhet != null) {
            return cachedBehandlendeEnhet
        } else {
            val geografiskTilknytning = pdlClient.geografiskTilknytning(
                callId = callId,
                personIdentNumber = personIdentNumber,
            )
            val isEgenAnsatt = skjermedePersonerPipClient.isSkjermet(
                callId = callId,
                personIdentNumber = personIdentNumber,
            )

            val graderingList = pdlClient.person(
                callId = callId,
                personIdentNumber = personIdentNumber,
            )?.gradering()

            val behandlendeEnhet = norgClient.getArbeidsfordelingEnhet(
                callId = callId,
                diskresjonskode = graderingList?.toArbeidsfordelingCriteriaDiskresjonskode(),
                geografiskTilknytning = geografiskTilknytning,
                isEgenAnsatt = isEgenAnsatt,
            ) ?: return null

            val behandlendeEnhetResponse = if (isEnhetUtvandret(behandlendeEnhet)) {
                getEnhetNAVUtland(behandlendeEnhet)
            } else {
                behandlendeEnhet
            }
            redisStore.setObject(
                key = cacheKey,
                value = behandlendeEnhetResponse,
                expireSeconds = CACHE_BEHANDLENDEENHET_PERSONIDENT_EXPIRE_SECONDS,
            )
            return behandlendeEnhetResponse
        }
    }

    fun isEnhetUtvandret(enhet: BehandlendeEnhet): Boolean {
        return enhet.enhetId == geografiskTilknytningUtvandret
    }

    fun getEnhetNAVUtland(enhet: BehandlendeEnhet): BehandlendeEnhet {
        return BehandlendeEnhet(
            enhetId = enhetnrNAVUtland,
            navn = enhet.navn,
        )
    }

    companion object {
        const val CACHE_BEHANDLENDEENHET_PERSONIDENT_KEY_PREFIX = "behandlendeenhet-personident-"
        const val CACHE_BEHANDLENDEENHET_PERSONIDENT_EXPIRE_SECONDS = 2 * 60 * 60L
    }
}

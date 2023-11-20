package no.nav.syfo.behandlendeenhet

import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.application.database.DatabaseInterface
import no.nav.syfo.behandlendeenhet.database.domain.toPerson
import no.nav.syfo.behandlendeenhet.database.getPersonByIdent
import no.nav.syfo.behandlendeenhet.database.createOrUpdatePerson
import no.nav.syfo.behandlendeenhet.domain.Person
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
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
    private val database: DatabaseInterface,
    private val behandlendeEnhetProducer: BehandlendeEnhetProducer,
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

            val person = getPerson(personIdentNumber)

            val behandlendeEnhet = norgClient.getArbeidsfordelingEnhet(
                callId = callId,
                diskresjonskode = graderingList?.toArbeidsfordelingCriteriaDiskresjonskode(),
                geografiskTilknytning = geografiskTilknytning,
                isEgenAnsatt = isEgenAnsatt,
                isNavUtland = person?.isNavUtland ?: false,
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

    fun updatePerson(personIdent: PersonIdentNumber, isNavUtland: Boolean): Person? {
        val pPerson = database.createOrUpdatePerson(personIdent, isNavUtland)
        val person = pPerson?.toPerson()
        if (person != null) {
            val cacheKey = "$CACHE_BEHANDLENDEENHET_PERSONIDENT_KEY_PREFIX${personIdent.value}"
            redisStore.setObject(
                key = cacheKey,
                value = null,
                expireSeconds = CACHE_BEHANDLENDEENHET_PERSONIDENT_EXPIRE_SECONDS,
            )
            behandlendeEnhetProducer.sendBehandlendeEnhetUpdate(person, pPerson.updatedAt)
        }
        return person
    }

    private fun getPerson(personIdent: PersonIdentNumber): Person? {
        return database.getPersonByIdent(personIdent)?.toPerson()
    }

    private fun isEnhetUtvandret(enhet: BehandlendeEnhet): Boolean {
        return enhet.enhetId == geografiskTilknytningUtvandret
    }

    private fun getEnhetNAVUtland(enhet: BehandlendeEnhet): BehandlendeEnhet {
        return BehandlendeEnhet(
            enhetId = enhetnrNAVUtland,
            navn = enhet.navn,
        )
    }

    companion object {
        const val CACHE_BEHANDLENDEENHET_PERSONIDENT_KEY_PREFIX = "behandlendeenhet-personident-"
        const val CACHE_BEHANDLENDEENHET_PERSONIDENT_EXPIRE_SECONDS = 12 * 60 * 60L
    }
}

package no.nav.syfo.behandlendeenhet

import no.nav.syfo.application.api.authentication.Token
import no.nav.syfo.infrastructure.cache.ValkeyStore
import no.nav.syfo.behandlendeenhet.domain.Oppfolgingsenhet
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.domain.Enhet
import no.nav.syfo.domain.Enhet.Companion.ENHETNAVN_NAV_UTLAND
import no.nav.syfo.domain.Enhet.Companion.ENHETNR_NAV_UTLAND
import no.nav.syfo.infrastructure.client.norg.NorgClient
import no.nav.syfo.infrastructure.client.pdl.PdlClient
import no.nav.syfo.infrastructure.client.pdl.domain.gradering
import no.nav.syfo.infrastructure.client.pdl.domain.toArbeidsfordelingCriteriaDiskresjonskode
import no.nav.syfo.infrastructure.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.domain.PersonIdentNumber

class EnhetService(
    private val norgClient: NorgClient,
    private val pdlClient: PdlClient,
    private val valkeyStore: ValkeyStore,
    private val skjermedePersonerPipClient: SkjermedePersonerPipClient,
    private val repository: IEnhetRepository,
    private val behandlendeEnhetProducer: BehandlendeEnhetProducer,
) {

    suspend fun arbeidstakersBehandlendeEnhet(
        callId: String,
        personIdentNumber: PersonIdentNumber,
        veilederToken: Token?,
    ): BehandlendeEnhet? {
        val cacheKey = "$CACHE_BEHANDLENDEENHET_PERSONIDENT_KEY_PREFIX${personIdentNumber.value}"
        val cachedBehandlendeEnhet: BehandlendeEnhet? = valkeyStore.getObject(key = cacheKey)
        if (cachedBehandlendeEnhet != null && cachedBehandlendeEnhet.navn != ENHETSNAVN_MANGLER) {
            return cachedBehandlendeEnhet
        } else {
            val oppfolgingsenhet = getOppfolgingsenhet(personIdentNumber)
            val behandlendeEnhetResponse = if (oppfolgingsenhet?.enhet != null) {
                BehandlendeEnhet(
                    enhetId = oppfolgingsenhet.enhet.value,
                    navn = getEnhetsnavn(oppfolgingsenhet.enhet),
                )
            } else {
                val geografiskTilknytning = pdlClient.geografiskTilknytning(
                    callId = callId,
                    personIdentNumber = personIdentNumber,
                )
                val isEgenAnsatt = skjermedePersonerPipClient.isSkjermet(
                    callId = callId,
                    personIdentNumber = personIdentNumber,
                    veilederToken = veilederToken,
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

                if (isEnhetUtvandret(behandlendeEnhet)) {
                    getEnhetNAVUtland()
                } else {
                    behandlendeEnhet
                }
            }
            valkeyStore.setObject(
                key = cacheKey,
                value = behandlendeEnhetResponse,
                expireSeconds = CACHE_BEHANDLENDEENHET_PERSONIDENT_EXPIRE_SECONDS,
            )
            return behandlendeEnhetResponse
        }
    }

    fun updateOppfolgingsenhet(personIdent: PersonIdentNumber, isNavUtland: Boolean): Oppfolgingsenhet? {
        val enhet = if (isNavUtland) {
            Enhet(ENHETNR_NAV_UTLAND)
        } else {
            null
        }
        return updateOppfolgingsenhet(personIdent, enhet)
    }

    fun updateOppfolgingsenhet(personIdent: PersonIdentNumber, enhet: Enhet?): Oppfolgingsenhet {
        // TODO: set veilederident from token
        val oppfolgingenhet = repository.createOppfolgingsenhet(personIdent, enhet, SYSTEM_BRUKER)
        valkeyStore.setObject(
            key = "$CACHE_BEHANDLENDEENHET_PERSONIDENT_KEY_PREFIX${personIdent.value}",
            value = null,
            expireSeconds = CACHE_BEHANDLENDEENHET_PERSONIDENT_EXPIRE_SECONDS,
        )
        behandlendeEnhetProducer.sendBehandlendeEnhetUpdate(oppfolgingenhet, oppfolgingenhet.createdAt)
        return oppfolgingenhet
    }

    private fun getOppfolgingsenhet(personIdent: PersonIdentNumber): Oppfolgingsenhet? {
        return repository.getOppfolgingsenhetByPersonident(personIdent)
    }

    private suspend fun getEnhetsnavn(oppfolgingsenhet: Enhet) =
        if (oppfolgingsenhet.isNavUtland()) {
            ENHETNAVN_NAV_UTLAND
        } else {
            norgClient.getEnhetsnavn(oppfolgingsenhet.value) ?: ENHETSNAVN_MANGLER
        }

    private fun isEnhetUtvandret(enhet: BehandlendeEnhet): Boolean {
        return enhet.enhetId == GEOGRAFISK_TILKNYTNING_UTVANDRET
    }

    private fun getEnhetNAVUtland(): BehandlendeEnhet {
        return BehandlendeEnhet(
            enhetId = ENHETNR_NAV_UTLAND,
            navn = ENHETNAVN_NAV_UTLAND,
        )
    }

    companion object {
        const val SYSTEM_BRUKER = "Z999999"
        private const val GEOGRAFISK_TILKNYTNING_UTVANDRET = "NOR"
        private const val ENHETSNAVN_MANGLER = "Enhetsnavn mangler"
        const val CACHE_BEHANDLENDEENHET_PERSONIDENT_KEY_PREFIX = "behandlendeenhet-personident-"
        const val CACHE_BEHANDLENDEENHET_PERSONIDENT_EXPIRE_SECONDS = 12 * 60 * 60L
    }
}

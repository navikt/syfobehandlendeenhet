package no.nav.syfo.behandlendeenhet

import no.nav.syfo.application.api.authentication.Token
import no.nav.syfo.application.api.authentication.getNAVIdent
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
import no.nav.syfo.domain.Stedtilknytning
import no.nav.syfo.infrastructure.client.pdl.domain.isKode6
import no.nav.syfo.infrastructure.client.pdl.domain.isKode7

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
        val oppfolgingsenhet = getOppfolgingsenhet(personIdentNumber)
        return if (oppfolgingsenhet?.enhet != null) {
            BehandlendeEnhet(
                enhetId = oppfolgingsenhet.enhet.value,
                navn = getEnhetsnavn(oppfolgingsenhet.enhet),
            )
        } else {
            findGeografiskEnhet(callId, personIdentNumber, veilederToken)
        }
    }

    private suspend fun findOppfolgingsenhet(
        personIdentNumber: PersonIdentNumber,
    ): BehandlendeEnhet? {
        return getOppfolgingsenhet(personIdentNumber)?.enhet?.let { enhet ->
            BehandlendeEnhet(
                enhetId = enhet.value,
                navn = getEnhetsnavn(enhet),
            )
        }
    }

    suspend fun updateOppfolgingsenhet(
        callId: String,
        personIdent: PersonIdentNumber,
        enhet: Enhet?,
        veilederToken: Token,
    ): Oppfolgingsenhet? =
        if (validateForOppfolgingsenhet(callId, personIdent, veilederToken)) {
            val geografiskEnhet = findGeografiskEnhet(
                callId = callId,
                personIdentNumber = personIdent,
                veilederToken = veilederToken,
            )
            val newBehandlendeEnhet = if (enhet?.value != geografiskEnhet?.enhetId) enhet else null
            val currentOppfolgingsenhet = getOppfolgingsenhet(personIdent)
            if (newBehandlendeEnhet != null || currentOppfolgingsenhet != null) {
                repository.createOppfolgingsenhet(personIdent, newBehandlendeEnhet, veilederToken.getNAVIdent()).also {
                    behandlendeEnhetProducer.sendBehandlendeEnhetUpdate(it, it.createdAt)
                }
            } else {
                null
            }
        } else {
            null
        }

    private suspend fun findGeografiskEnhet(
        callId: String,
        personIdentNumber: PersonIdentNumber,
        veilederToken: Token?
    ): BehandlendeEnhet? {
        val cacheKey = "$CACHE_GEOGRAFISKENHET_PERSONIDENT_KEY_PREFIX${personIdentNumber.value}"
        val cachedBehandlendeEnhet: BehandlendeEnhet? = valkeyStore.getObject(key = cacheKey)
        return if (cachedBehandlendeEnhet != null) {
            cachedBehandlendeEnhet
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

            val behandlendeEnhetFraNorg = norgClient.getArbeidsfordelingEnhet(
                callId = callId,
                diskresjonskode = graderingList?.toArbeidsfordelingCriteriaDiskresjonskode(),
                geografiskTilknytning = geografiskTilknytning,
                isEgenAnsatt = isEgenAnsatt,
            )

            val behandlendeEnhet = if (isEnhetUtvandret(behandlendeEnhetFraNorg)) {
                getEnhetNAVUtland()
            } else {
                behandlendeEnhetFraNorg
            }
            valkeyStore.setObject(
                key = cacheKey,
                value = behandlendeEnhet,
                expireSeconds = CACHE_GEOGRAFISKENHET_PERSONIDENT_EXPIRE_SECONDS,
            )
            behandlendeEnhet
        }
    }

    suspend fun getMuligeOppfolgingsenheter(
        callId: String,
        enhet: Enhet,
    ): List<BehandlendeEnhet> {
        val overordnet = norgClient.getOverordnetEnhet(callId, enhet)
        return if (overordnet != null) {
            norgClient.getUnderenheter(callId, Enhet(overordnet.enhetNr)).map {
                BehandlendeEnhet(
                    enhetId = it.enhetNr,
                    navn = it.navn,
                )
            }
        } else {
            emptyList()
        }
    }

    suspend fun arbeidstakersStedtilknytning(
        callId: String,
        personIdentNumber: PersonIdentNumber,
        veilederToken: Token?,
    ): Stedtilknytning {
        val oppfolgingsenhet = findOppfolgingsenhet(personIdentNumber)
        val geografiskEnhet = findGeografiskEnhet(callId, personIdentNumber, veilederToken)
        return Stedtilknytning(geografiskEnhet, oppfolgingsenhet)
    }

    private suspend fun validateForOppfolgingsenhet(
        callId: String,
        personIdent: PersonIdentNumber,
        veilederToken: Token,
    ): Boolean {
        val isEgenAnsatt = skjermedePersonerPipClient.isSkjermet(
            callId = callId,
            personIdentNumber = personIdent,
            veilederToken = veilederToken,
        )
        val graderingList = pdlClient.person(
            callId = callId,
            personIdentNumber = personIdent,
        )?.gradering()

        return !isEgenAnsatt && (graderingList == null || graderingList.none { it.isKode6() || it.isKode7() })
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

    private fun isEnhetUtvandret(enhet: BehandlendeEnhet?): Boolean {
        return enhet?.enhetId == GEOGRAFISK_TILKNYTNING_UTVANDRET
    }

    private fun getEnhetNAVUtland(): BehandlendeEnhet {
        return BehandlendeEnhet(
            enhetId = ENHETNR_NAV_UTLAND,
            navn = ENHETNAVN_NAV_UTLAND,
        )
    }

    companion object {
        private const val GEOGRAFISK_TILKNYTNING_UTVANDRET = "NOR"
        private const val ENHETSNAVN_MANGLER = "Enhetsnavn mangler"
        const val CACHE_GEOGRAFISKENHET_PERSONIDENT_KEY_PREFIX = "geografiskenhet-personident-"
        const val CACHE_GEOGRAFISKENHET_PERSONIDENT_EXPIRE_SECONDS = 12 * 60 * 60L
    }
}

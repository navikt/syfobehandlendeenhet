package no.nav.syfo.behandlendeenhet

import no.nav.syfo.application.api.authentication.Token
import no.nav.syfo.application.api.authentication.getNAVIdent
import no.nav.syfo.infrastructure.cache.ValkeyStore
import no.nav.syfo.behandlendeenhet.domain.Oppfolgingsenhet
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.domain.EnhetId.Companion.ENHETNAVN_NAV_UTLAND
import no.nav.syfo.domain.EnhetId.Companion.ENHETNR_NAV_UTLAND
import no.nav.syfo.infrastructure.client.norg.NorgClient
import no.nav.syfo.infrastructure.client.pdl.PdlClient
import no.nav.syfo.infrastructure.client.pdl.domain.gradering
import no.nav.syfo.infrastructure.client.pdl.domain.toArbeidsfordelingCriteriaDiskresjonskode
import no.nav.syfo.infrastructure.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.domain.BehandlendeEnhet
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
        veilederToken: Token? = null,
    ): BehandlendeEnhet {
        val oppfolgingsenhet = getOppfolgingsenhet(personIdentNumber)?.enhetId?.let { enhet ->
            Enhet(
                enhetId = enhet.value,
                navn = getEnhetsnavn(enhet),
            )
        }
        val geografiskEnhet = findGeografiskEnhet(callId, personIdentNumber, veilederToken)
        return BehandlendeEnhet(geografiskEnhet, oppfolgingsenhet)
    }

    suspend fun updateOppfolgingsenhet(
        callId: String,
        personIdent: PersonIdentNumber,
        enhetId: EnhetId?,
        veilederToken: Token? = null,
    ): Oppfolgingsenhet? =
        if (enhetId == null || validateForOppfolgingsenhet(callId, personIdent, veilederToken)) {
            val geografiskEnhet = findGeografiskEnhet(
                callId = callId,
                personIdentNumber = personIdent,
                veilederToken = veilederToken,
            )
            val newBehandlendeEnhet = if (enhetId?.value != geografiskEnhet.enhetId) enhetId else null
            val currentOppfolgingsenhet = getOppfolgingsenhet(personIdent)
            val navIdent = veilederToken?.getNAVIdent() ?: SYSTEM_USER_IDENT
            if (newBehandlendeEnhet != null || currentOppfolgingsenhet != null) {
                repository.createOppfolgingsenhet(personIdent, newBehandlendeEnhet, navIdent).also {
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
    ): Enhet {
        val cacheKey = "$CACHE_GEOGRAFISKENHET_PERSONIDENT_KEY_PREFIX${personIdentNumber.value}"
        val cachedEnhet: Enhet? = valkeyStore.getObject(key = cacheKey)
        return if (cachedEnhet != null) {
            cachedEnhet
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
        enhetId: EnhetId,
    ): List<Enhet> {
        val mulige = mutableListOf(Enhet(ENHETNR_NAV_UTLAND, ENHETNAVN_NAV_UTLAND))
        val overordnet = norgClient.getOverordnetEnhet(callId, enhetId)
        if (overordnet != null) {
            mulige.addAll(
                norgClient.getUnderenheter(callId, EnhetId(overordnet.enhetNr)).map {
                    Enhet(
                        enhetId = it.enhetNr,
                        navn = it.navn,
                    )
                }
            )
        }
        return mulige
    }

    suspend fun validateForOppfolgingsenhet(
        callId: String,
        personIdent: PersonIdentNumber,
        veilederToken: Token? = null,
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

    private suspend fun getEnhetsnavn(oppfolgingsenhet: EnhetId) =
        if (oppfolgingsenhet.isNavUtland()) {
            ENHETNAVN_NAV_UTLAND
        } else {
            norgClient.getEnhetsnavn(oppfolgingsenhet.value) ?: ENHETSNAVN_MANGLER
        }

    private fun isEnhetUtvandret(enhet: Enhet?): Boolean {
        return enhet?.enhetId == GEOGRAFISK_TILKNYTNING_UTVANDRET
    }

    private fun getEnhetNAVUtland(): Enhet {
        return Enhet(
            enhetId = ENHETNR_NAV_UTLAND,
            navn = ENHETNAVN_NAV_UTLAND,
        )
    }

    companion object {
        private const val GEOGRAFISK_TILKNYTNING_UTVANDRET = "NOR"
        private const val ENHETSNAVN_MANGLER = "Enhetsnavn mangler"
        const val SYSTEM_USER_IDENT = "Z999999"
        const val CACHE_GEOGRAFISKENHET_PERSONIDENT_KEY_PREFIX = "geografiskenhet-personident-"
        const val CACHE_GEOGRAFISKENHET_PERSONIDENT_EXPIRE_SECONDS = 12 * 60 * 60L
    }
}

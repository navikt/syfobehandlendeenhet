package no.nav.syfo.config.mocks

import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

import no.nav.syfo.config.consumer.AktoerConfig.MOCK_KEY

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
class AktoerMock : AktoerV2 {

    override fun hentAktoerIdForIdentListe(wsHentAktoerIdForIdentListeRequest: WSHentAktoerIdForIdentListeRequest): WSHentAktoerIdForIdentListeResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun hentAktoerIdForIdent(wsHentAktoerIdForIdentRequest: WSHentAktoerIdForIdentRequest): WSHentAktoerIdForIdentResponse {
        return WSHentAktoerIdForIdentResponse()
            .withAktoerId(mockAktorId(wsHentAktoerIdForIdentRequest.ident))
    }

    override fun hentIdentForAktoerIdListe(wsHentIdentForAktoerIdListeRequest: WSHentIdentForAktoerIdListeRequest): WSHentIdentForAktoerIdListeResponse {
        throw RuntimeException("Ikke implementert i mock.")
    }

    override fun hentIdentForAktoerId(wsHentIdentForAktoerIdRequest: WSHentIdentForAktoerIdRequest): WSHentIdentForAktoerIdResponse {
        return WSHentIdentForAktoerIdResponse()
            .withIdent(getFnrFromMockedAktorId(wsHentIdentForAktoerIdRequest.aktoerId))
    }

    override fun ping() {}

    companion object {

        private val MOCK_AKTORID_PREFIX = "10"

        fun mockAktorId(fnr: String): String {
            return MOCK_AKTORID_PREFIX + fnr
        }

        private fun getFnrFromMockedAktorId(aktorId: String): String {
            return aktorId.replace(MOCK_AKTORID_PREFIX, "")
        }
    }

}

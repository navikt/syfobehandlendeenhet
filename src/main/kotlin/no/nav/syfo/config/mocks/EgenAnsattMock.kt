package no.nav.syfo.config.mocks

import no.nav.syfo.config.consumer.EgenAnsattConfig
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = [EgenAnsattConfig.MOCK_KEY], havingValue = "true")
class EgenAnsattMock : EgenAnsattV1 {
    override fun ping() {

    }

    override fun hentErEgenAnsattEllerIFamilieMedEgenAnsatt(wsHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest: WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest): WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse {
        return WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse()
            .withEgenAnsatt(false)
    }
}

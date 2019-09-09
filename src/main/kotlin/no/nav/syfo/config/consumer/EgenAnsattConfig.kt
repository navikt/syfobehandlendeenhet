package no.nav.syfo.config.consumer

import no.nav.syfo.service.ws.LogErrorHandler
import no.nav.syfo.service.ws.STSClientConfig
import no.nav.syfo.service.ws.WsClient
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

import java.util.Collections.singletonList

@Configuration
class EgenAnsattConfig {
    @Value("\${virksomhet.egenansatt.v1.endpointurl}")
    private val serviceUrl: String? = null

    @Bean
    @Primary
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    fun egenAnsattV1(): EgenAnsattV1 {
        val port = factory()
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

    private fun factory(): EgenAnsattV1 {
        return WsClient<EgenAnsattV1>()
            .createPort(serviceUrl, EgenAnsattV1::class.java, listOf<Handler>(LogErrorHandler()))
    }

    companion object {

        val MOCK_KEY = "egenansatt.withmock"
    }
}

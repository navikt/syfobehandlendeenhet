package no.nav.syfo.config.consumer

import no.nav.syfo.service.ws.LogErrorHandler
import no.nav.syfo.service.ws.STSClientConfig
import no.nav.syfo.service.ws.WsClient
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

import java.util.Collections.singletonList

@Configuration
class AktoerConfig {
    @Value("\${aktoer.v2.endpointurl}")
    private val serviceUrl: String? = null

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    fun aktoerV2(): AktoerV2 {
        val port = factory()
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

    private fun factory(): AktoerV2 {
        return WsClient<AktoerV2>()
            .createPort(serviceUrl, AktoerV2::class.java, listOf<Handler>(LogErrorHandler()))
    }

    companion object {

        val MOCK_KEY = "aktoer.withmock"
    }
}

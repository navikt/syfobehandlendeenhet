package no.nav.syfo.config.consumer

import no.nav.syfo.service.ws.*
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.*

import java.util.Collections

@Configuration
class ArbeidsfordelingConfig {
    @Value("\${virksomhet.arbeidsfordeling.v1.endpointurl}")
    private val serviceUrl: String? = null

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    fun arbeidsfordelingV1(): ArbeidsfordelingV1 {
        val port = WsClient<ArbeidsfordelingV1>().createPort(
            serviceUrl,
            ArbeidsfordelingV1::class.java,
            listOf<Handler>(LogErrorHandler())
        )
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

    companion object {

        val MOCK_KEY = "arbeidsfordeling.withmock"
    }
}

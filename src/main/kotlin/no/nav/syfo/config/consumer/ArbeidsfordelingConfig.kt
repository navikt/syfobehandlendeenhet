package no.nav.syfo.config.consumer

import no.nav.syfo.config.EnvironmentUtil.getEnvVar
import no.nav.syfo.ws.util.LogErrorHandler
import no.nav.syfo.ws.util.STSClientConfig
import no.nav.syfo.ws.util.WsClient
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.*

@Configuration
class ArbeidsfordelingConfig {

    val serviceUrl = getEnvVar("ARBEIDSFORDELING_V1_URL", "http://eksempel.no/ws/ArbeidsfordelingV1")

    @Bean
    @ConditionalOnProperty(value = [MOCK_KEY], havingValue = "false", matchIfMissing = true)
    @Primary
    fun arbeidsfordelingV1(): ArbeidsfordelingV1 {
        val port = WsClient<ArbeidsfordelingV1>().createPort(
            serviceUrl,
            ArbeidsfordelingV1::class.java,
            listOf(LogErrorHandler())
        )
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

    companion object {
        const val MOCK_KEY = "arbeidsfordeling.withmock"
    }
}

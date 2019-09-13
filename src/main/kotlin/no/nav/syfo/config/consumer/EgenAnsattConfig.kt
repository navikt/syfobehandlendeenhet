package no.nav.syfo.config.consumer

import no.nav.syfo.config.EnvironmentUtil.getEnvVar
import no.nav.syfo.ws.util.*
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class EgenAnsattConfig {

    private val serviceUrl = getEnvVar("EGENANSATT_V1_URL", "http://eksempel.no/ws/EgenAnsattV1")

    @Bean
    @Primary
    @ConditionalOnProperty(value = [MOCK_KEY], havingValue = "false", matchIfMissing = true)
    fun egenAnsattV1(): EgenAnsattV1 {
        val port = factory()
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

    private fun factory(): EgenAnsattV1 {
        return WsClient<EgenAnsattV1>()
            .createPort(serviceUrl, EgenAnsattV1::class.java, listOf(LogErrorHandler()))
    }

    companion object {
        const val MOCK_KEY = "egenansatt.withmock"
    }
}

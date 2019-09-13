package no.nav.syfo.config.consumer

import no.nav.syfo.config.EnvironmentUtil.getEnvVar
import no.nav.syfo.ws.util.LogErrorHandler
import no.nav.syfo.ws.util.STSClientConfig
import no.nav.syfo.ws.util.WsClient
import no.nav.tjeneste.virksomhet.person.v3.PersonV3
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class PersonConfig {

    private val serviceUrl = getEnvVar("PERSON_V3_URL", "http://eksempel.no/ws/EgenAnsattV1")

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = [MOCK_KEY], havingValue = "false", matchIfMissing = true)
    @Primary
    fun personV3(): PersonV3 {
        val port = WsClient<PersonV3>().createPort(serviceUrl, PersonV3::class.java, listOf(LogErrorHandler()))
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

    companion object {
        const val MOCK_KEY = "person.withmock"
    }
}

package no.nav.syfo.config.consumer

import no.nav.syfo.service.ws.LogErrorHandler
import no.nav.syfo.service.ws.STSClientConfig
import no.nav.syfo.service.ws.WsClient
import no.nav.tjeneste.virksomhet.person.v3.PersonV3
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

import java.util.Collections.singletonList

@Configuration
class PersonConfig {

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    fun personV3(@Value("\${virksomhet.person.v3.endpointurl}") serviceUrl: String): PersonV3 {
        val port = WsClient<PersonV3>().createPort(serviceUrl, PersonV3::class.java, listOf<Handler>(LogErrorHandler()))
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

    companion object {

        val MOCK_KEY = "person.withmock"
    }
}

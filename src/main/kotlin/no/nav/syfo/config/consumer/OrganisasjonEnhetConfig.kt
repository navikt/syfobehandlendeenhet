package no.nav.syfo.config.consumer

import no.nav.syfo.service.ws.*
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.*

import java.util.Collections.singletonList

@Configuration
class OrganisasjonEnhetConfig {
    @Value("\${virksomhet.organisasjonEnhet.v2.endpointurl}")
    private val serviceUrl: String? = null

    @Bean
    @ConditionalOnProperty(value = MOCK_KEY, havingValue = "false", matchIfMissing = true)
    @Primary
    fun organisasjonEnhetV2(): OrganisasjonEnhetV2 {
        val port = WsClient<OrganisasjonEnhetV2>().createPort(
            serviceUrl,
            OrganisasjonEnhetV2::class.java,
            listOf<Handler>(LogErrorHandler())
        )
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

    companion object {

        val MOCK_KEY = "organisasjonenhet.withmock"
    }
}

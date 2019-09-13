package no.nav.syfo.config.consumer

import no.nav.syfo.config.EnvironmentUtil.getEnvVar
import no.nav.syfo.ws.util.*
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.*

@Configuration
class OrganisasjonEnhetConfig {

    private val serviceUrl = getEnvVar("ORGANISASJONENHET_V2_URL","http://eksempel.no/ws/OrganisasjonEnhetV2")

    @Bean
    @ConditionalOnProperty(value = [MOCK_KEY], havingValue = "false", matchIfMissing = true)
    @Primary
    fun organisasjonEnhetV2(): OrganisasjonEnhetV2 {
        val port = WsClient<OrganisasjonEnhetV2>().createPort(
            serviceUrl,
            OrganisasjonEnhetV2::class.java,
            listOf(LogErrorHandler())
        )
        STSClientConfig.configureRequestSamlToken(port)
        return port
    }

    companion object {
        const val MOCK_KEY = "organisasjonenhet.withmock"
    }
}

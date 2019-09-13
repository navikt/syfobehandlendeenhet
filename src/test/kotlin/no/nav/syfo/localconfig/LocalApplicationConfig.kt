package no.nav.syfo.localconfig

import no.nav.security.spring.oidc.test.TokenGeneratorConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import java.util.Objects.requireNonNull

@Configuration
@Import(TokenGeneratorConfiguration::class)
class LocalApplicationConfig(environment: Environment) {

    init {
        System.setProperty(
            "SECURITYTOKENSERVICE_URL",
            requireNonNull(environment.getProperty("securitytokenservice.url"))
        )
        System.setProperty(
            "SRVSYFOBEHANDLENDEENHET_USERNAME",
            requireNonNull(environment.getProperty("srvsyfobehandlendeenhet.username"))
        )
        System.setProperty(
            "SRVSYFOBEHANDLENDEENHET_PASSWORD",
            requireNonNull(environment.getProperty("srvsyfobehandlendeenhet.password"))
        )
    }

    @Bean
    fun restTemplate(vararg interceptors: ClientHttpRequestInterceptor): RestTemplate {
        val template = RestTemplate()
        template.interceptors = listOf(*interceptors)
        return template
    }
}

package no.nav.syfo.localconfig

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
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
}

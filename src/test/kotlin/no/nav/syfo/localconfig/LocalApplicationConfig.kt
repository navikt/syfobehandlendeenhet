package no.nav.syfo.localconfig

import no.nav.security.spring.oidc.test.TokenGeneratorConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

import java.util.Arrays.asList
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
            "SRVsyfobehandlendeenhet_USERNAME",
            requireNonNull(environment.getProperty("srvsyfobehandlendeenhet.username"))
        )
        System.setProperty(
            "SRVsyfobehandlendeenhet_PASSWORD",
            requireNonNull(environment.getProperty("srvsyfobehandlendeenhet.password"))
        )

        System.setProperty("LDAP_URL", requireNonNull(environment.getProperty("ldap.url")))
        System.setProperty("LDAP_USERNAME", requireNonNull(environment.getProperty("ldap.username")))
        System.setProperty("LDAP_PASSWORD", requireNonNull(environment.getProperty("ldap.password")))
        System.setProperty("LDAP_BASEDN", requireNonNull(environment.getProperty("ldap.basedn")))

        System.setProperty("SMTPSERVER_HOST", requireNonNull(environment.getProperty("smtpserver.host")))
        System.setProperty("SMTPSERVER_PORT", requireNonNull(environment.getProperty("smtpserver.port")))

        System.setProperty(
            "HENVENDELSE_OPPGAVE_HENVENDELSE_QUEUENAME",
            requireNonNull(environment.getProperty("henvendelse.oppgave.henvendelse.queuename"))
        )
        System.setProperty(
            "VARSELPRODUKSJON_BEST_VARSEL_M_HANDLING_QUEUENAME",
            requireNonNull(environment.getProperty("varselproduksjon.best.varsel.m.handling.queuename"))
        )
        System.setProperty(
            "VARSELPRODUKSJON_STOPP_VARSEL_UTSENDING_QUEUENAME",
            requireNonNull(environment.getProperty("varselproduksjon.topp.varsel.utsending.queuename"))
        )
        System.setProperty(
            "VARSELPRODUKSJON_VARSLINGER_QUEUENAME",
            requireNonNull(environment.getProperty("varselproduksjon.varslinger.queuename"))
        )
    }

    @Bean
    fun restTemplate(vararg interceptors: ClientHttpRequestInterceptor): RestTemplate {
        val template = RestTemplate()
        template.interceptors = asList(*interceptors)
        return template
    }
}

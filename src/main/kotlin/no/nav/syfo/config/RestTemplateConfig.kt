package no.nav.syfo.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.*
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Primary
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder().build()
    }
}

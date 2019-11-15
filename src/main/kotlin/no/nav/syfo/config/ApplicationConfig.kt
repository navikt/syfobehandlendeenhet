package no.nav.syfo.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestTemplate

@Configuration
@EnableCaching
@EnableScheduling
class ApplicationConfig

@Bean
fun restTemplate(vararg interceptors: ClientHttpRequestInterceptor): RestTemplate {
    val template = RestTemplate()
    template.interceptors = listOf(*interceptors)
    return template
}

package no.nav.syfo.consumer.azuread

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.net.ProxySelector

class NaisProxyCustomizer : RestTemplateCustomizer {
    override fun customize(restTemplate: RestTemplate) {
        val client: HttpClient = HttpClientBuilder.create()
            .setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault()))
            .build()
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory(client)
    }
}

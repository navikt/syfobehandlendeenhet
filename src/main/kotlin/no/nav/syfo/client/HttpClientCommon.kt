package no.nav.syfo.client

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import no.nav.syfo.util.configuredJacksonMapper
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector

val defaultConfig: HttpClientConfig<CIOEngineConfig>.() -> Unit = {
    install(JsonFeature) {
        serializer = JacksonSerializer(configuredJacksonMapper())
    }
    engine {
        requestTimeout = 30000
        endpoint {
            keepAliveTime = 30000
            connectTimeout = 30000
        }
    }
}

val proxyConfig: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
    install(JsonFeature) {
        serializer = JacksonSerializer(configuredJacksonMapper())
    }
    engine {
        customizeClient {
            setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault()))
        }
    }
}

fun httpClientDefault() = HttpClient(
    engineFactory = CIO,
    block = defaultConfig
)

fun httpClientProxy() = HttpClient(
    engineFactory = Apache,
    block = proxyConfig,
)

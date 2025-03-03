package no.nav.syfo.infrastructure.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import no.nav.syfo.util.configure
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector

val commonConfig: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit = {
    install(ContentNegotiation) {
        jackson {
            configure()
        }
    }
    install(HttpRequestRetry) {
        retryOnExceptionIf(2) { _, cause ->
            cause !is ClientRequestException
        }
        constantDelay(500L)
    }
    expectSuccess = true
}

val defaultConfig: HttpClientConfig<CIOEngineConfig>.() -> Unit = {
    this.commonConfig()
    engine {
        requestTimeout = 30000
        endpoint {
            keepAliveTime = 30000
            connectTimeout = 30000
        }
    }
}

val proxyConfig: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
    this.commonConfig()
    engine {
        customizeClient {
            setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault()))
        }
    }
}

fun httpClientDefault() = HttpClient(
    engineFactory = CIO,
    block = no.nav.syfo.infrastructure.client.defaultConfig
)

fun httpClientProxy() = HttpClient(
    engineFactory = Apache,
    block = no.nav.syfo.infrastructure.client.proxyConfig,
)

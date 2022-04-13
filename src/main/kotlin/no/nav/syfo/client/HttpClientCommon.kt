package no.nav.syfo.client

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import no.nav.syfo.util.applyConfig
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector

val defaultConfig: HttpClientConfig<CIOEngineConfig>.() -> Unit = {
    install(ContentNegotiation) {
        jackson {
            applyConfig()
        }
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
    install(ContentNegotiation) {
        jackson {
            applyConfig()
        }
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

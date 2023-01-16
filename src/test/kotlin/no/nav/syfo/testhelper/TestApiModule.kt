package no.nav.syfo.testhelper

import io.ktor.server.application.*
import no.nav.syfo.application.api.apiModule
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.pdl.PdlClient
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.Protocol

fun Application.testApiModule(
    externalMockEnvironment: ExternalMockEnvironment,
    behandlendeEnhetProducer: BehandlendeEnhetProducer,
) {
    val redisStore = RedisStore(
        JedisPool(
            JedisPoolConfig(),
            externalMockEnvironment.environment.redisHost,
            externalMockEnvironment.environment.redisPort,
            Protocol.DEFAULT_TIMEOUT,
            externalMockEnvironment.environment.redisSecret,
        )
    )
    val azureAdClient = AzureAdClient(
        azureAppClientId = externalMockEnvironment.environment.azureAppClientId,
        azureAppClientSecret = externalMockEnvironment.environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = externalMockEnvironment.environment.azureOpenidConfigTokenEndpoint,
        redisStore = redisStore,
    )
    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        baseUrl = externalMockEnvironment.environment.pdlUrl,
        clientId = externalMockEnvironment.environment.pdlClientId,
    )
    this.apiModule(
        applicationState = externalMockEnvironment.applicationState,
        azureAdClient = azureAdClient,
        environment = externalMockEnvironment.environment,
        wellKnownInternalAzureAD = externalMockEnvironment.wellKnownInternalAzureAD,
        database = externalMockEnvironment.database,
        behandlendeEnhetProducer = behandlendeEnhetProducer,
        pdlClient = pdlClient,
        redisStore = redisStore,

    )
}

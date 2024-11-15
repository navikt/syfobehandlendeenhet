package no.nav.syfo.testhelper

import io.ktor.server.application.*
import no.nav.syfo.application.api.apiModule
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.norg.NorgClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import redis.clients.jedis.*

fun Application.testApiModule(
    externalMockEnvironment: ExternalMockEnvironment,
    behandlendeEnhetProducer: BehandlendeEnhetProducer,
) {
    val redisConfig = externalMockEnvironment.environment.redisConfig
    val redisStore = RedisStore(
        JedisPool(
            JedisPoolConfig(),
            HostAndPort(redisConfig.host, redisConfig.port),
            DefaultJedisClientConfig.builder()
                .ssl(redisConfig.ssl)
                .password(redisConfig.redisPassword)
                .build()
        )
    )
    val azureAdClient = AzureAdClient(
        azureAppClientId = externalMockEnvironment.environment.azureAppClientId,
        azureAppClientSecret = externalMockEnvironment.environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = externalMockEnvironment.environment.azureOpenidConfigTokenEndpoint,
        redisStore = redisStore,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        baseUrl = externalMockEnvironment.environment.pdlUrl,
        clientId = externalMockEnvironment.environment.pdlClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    val norgClient = NorgClient(
        baseUrl = externalMockEnvironment.environment.norg2Url,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    val skjermedePersonerPipClient = SkjermedePersonerPipClient(
        azureAdClient = azureAdClient,
        baseUrl = externalMockEnvironment.environment.skjermedePersonerPipUrl,
        clientId = externalMockEnvironment.environment.skjermedePersonerPipClientId,
        redisStore = redisStore,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    val veilederTilgangskontrollClient = VeilederTilgangskontrollClient(
        azureAdClient = azureAdClient,
        clientId = externalMockEnvironment.environment.istilgangskontrollClientId,
        baseUrl = externalMockEnvironment.environment.istilgangskontrollUrl,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    this.apiModule(
        applicationState = externalMockEnvironment.applicationState,
        environment = externalMockEnvironment.environment,
        wellKnownInternalAzureAD = externalMockEnvironment.wellKnownInternalAzureAD,
        database = externalMockEnvironment.database,
        behandlendeEnhetProducer = behandlendeEnhetProducer,
        pdlClient = pdlClient,
        redisStore = redisStore,
        norgClient = norgClient,
        skjermedePersonerPipClient = skjermedePersonerPipClient,
        veilederTilgangskontrollClient = veilederTilgangskontrollClient,
    )
}

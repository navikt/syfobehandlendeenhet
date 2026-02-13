package no.nav.syfo.testhelper

import io.ktor.server.application.*
import no.nav.syfo.api.apiModule
import no.nav.syfo.application.EnhetService
import no.nav.syfo.infrastructure.cache.ValkeyStore
import no.nav.syfo.infrastructure.kafka.BehandlendeEnhetProducer
import no.nav.syfo.infrastructure.client.azuread.AzureAdClient
import no.nav.syfo.infrastructure.client.norg.NorgClient
import no.nav.syfo.infrastructure.client.pdl.PdlClient
import no.nav.syfo.infrastructure.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.infrastructure.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.infrastructure.database.repository.EnhetRepository
import redis.clients.jedis.*

fun Application.testApiModule(
    externalMockEnvironment: ExternalMockEnvironment,
    behandlendeEnhetProducer: BehandlendeEnhetProducer,
) {
    val valkeyConfig = externalMockEnvironment.environment.valkeyConfig
    val valkeyStore = ValkeyStore(
        JedisPool(
            JedisPoolConfig(),
            HostAndPort(valkeyConfig.host, valkeyConfig.port),
            DefaultJedisClientConfig.builder()
                .ssl(valkeyConfig.ssl)
                .password(valkeyConfig.valkeyPassword)
                .build()
        )
    )
    val azureAdClient = AzureAdClient(
        azureAppClientId = externalMockEnvironment.environment.azureAppClientId,
        azureAppClientSecret = externalMockEnvironment.environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = externalMockEnvironment.environment.azureOpenidConfigTokenEndpoint,
        valkeyStore = valkeyStore,
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
        valkeyStore = valkeyStore,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    val skjermedePersonerPipClient = SkjermedePersonerPipClient(
        azureAdClient = azureAdClient,
        baseUrl = externalMockEnvironment.environment.skjermedePersonerPipUrl,
        clientId = externalMockEnvironment.environment.skjermedePersonerPipClientId,
        valkeyStore = valkeyStore,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    val veilederTilgangskontrollClient = VeilederTilgangskontrollClient(
        azureAdClient = azureAdClient,
        clientId = externalMockEnvironment.environment.istilgangskontrollClientId,
        baseUrl = externalMockEnvironment.environment.istilgangskontrollUrl,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    val repository = EnhetRepository(externalMockEnvironment.database)
    val enhetService = EnhetService(
        norgClient = norgClient,
        pdlClient = pdlClient,
        valkeyStore = valkeyStore,
        skjermedePersonerPipClient = skjermedePersonerPipClient,
        repository = repository,
        behandlendeEnhetProducer = behandlendeEnhetProducer,
    )
    this.apiModule(
        applicationState = externalMockEnvironment.applicationState,
        environment = externalMockEnvironment.environment,
        wellKnownInternalAzureAD = externalMockEnvironment.wellKnownInternalAzureAD,
        enhetService = enhetService,
        veilederTilgangskontrollClient = veilederTilgangskontrollClient,
    )
}

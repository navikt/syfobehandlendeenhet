package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.api.authentication.*
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.application.metric.api.registerMetricApi
import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.behandlendeenhet.api.access.APIConsumerAccessService
import no.nav.syfo.behandlendeenhet.api.internad.registrerPersonApi
import no.nav.syfo.behandlendeenhet.api.system.registrerSystemApi
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.norg.NorgClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.client.wellknown.WellKnown
import redis.clients.jedis.*

fun Application.apiModule(
    applicationState: ApplicationState,
    environment: Environment,
    wellKnownInternalAzureAD: WellKnown,
) {
    installMetrics()
    installCallId()
    installContentNegotiation()
    installJwtAuthentication(
        jwtIssuerList = listOf(
            JwtIssuer(
                acceptedAudienceList = listOf(environment.azureAppClientId),
                jwtIssuerType = JwtIssuerType.INTERNAL_AZUREAD,
                wellKnown = wellKnownInternalAzureAD,
            ),
        ),
    )
    installStatusPages()

    val redisStore = RedisStore(
        jedisPool = JedisPool(
            JedisPoolConfig(),
            environment.redisHost,
            environment.redisPort,
            Protocol.DEFAULT_TIMEOUT,
            environment.redisSecret,
        ),
    )

    val azureAdClient = AzureAdClient(
        azureAppClientId = environment.azureAppClientId,
        azureAppClientSecret = environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = environment.azureOpenidConfigTokenEndpoint,
        redisStore = redisStore,
    )

    val norgClient = NorgClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.isproxyUrl,
        clientId = environment.isproxyClientId,
    )
    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.pdlUrl,
        clientId = environment.pdlClientId,
    )
    val skjermedePersonerPipClient = SkjermedePersonerPipClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.skjermedePersonerPipUrl,
        clientId = environment.skjermedePersonerPipClientId,
        redisStore = redisStore,
    )

    val enhetService = EnhetService(
        norgClient = norgClient,
        pdlClient = pdlClient,
        redisStore = redisStore,
        skjermedePersonerPipClient = skjermedePersonerPipClient,
    )

    val veilederTilgangskontrollClient = VeilederTilgangskontrollClient(
        azureAdClient = azureAdClient,
        clientId = environment.syfotilgangskontrollClientId,
        baseUrl = environment.syfotilgangskontrollUrl,
    )

    val apiConsumerAccessService = APIConsumerAccessService(
        azureAppPreAuthorizedApps = environment.azureAppPreAuthorizedApps,
    )

    routing {
        registerPodApi(
            applicationState = applicationState,
        )
        registerMetricApi()
        authenticate(JwtIssuerType.INTERNAL_AZUREAD.name) {
            registrerPersonApi(
                enhetService = enhetService,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            )
            registrerSystemApi(
                apiConsumerAccessService = apiConsumerAccessService,
                authorizedApplicationNameList = environment.systemAPIAuthorizedConsumerApplicationNameList,
                enhetService = enhetService,
            )
        }
    }
}

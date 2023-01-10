package no.nav.syfo.application.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.api.authentication.*
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.application.database.DatabaseInterface
import no.nav.syfo.application.metric.api.registerMetricApi
import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.behandlendeenhet.api.access.APIConsumerAccessService
import no.nav.syfo.behandlendeenhet.api.internad.registrerPersonApi
import no.nav.syfo.behandlendeenhet.api.system.registrerSystemApi
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.norg.NorgClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.client.wellknown.WellKnown

fun Application.apiModule(
    applicationState: ApplicationState,
    azureAdClient: AzureAdClient,
    environment: Environment,
    wellKnownInternalAzureAD: WellKnown,
    database: DatabaseInterface,
    behandlendeEnhetProducer: BehandlendeEnhetProducer,
    pdlClient: PdlClient,
    redisStore: RedisStore,
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

    val norgClient = NorgClient(
        baseUrl = environment.norg2Url,
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
        database = database,
        behandlendeEnhetProducer = behandlendeEnhetProducer,
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

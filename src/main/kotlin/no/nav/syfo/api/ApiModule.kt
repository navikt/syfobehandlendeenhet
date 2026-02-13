package no.nav.syfo.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.syfo.api.authentication.JwtIssuer
import no.nav.syfo.api.authentication.JwtIssuerType
import no.nav.syfo.api.authentication.installJwtAuthentication
import no.nav.syfo.ApplicationState
import no.nav.syfo.Environment
import no.nav.syfo.infrastructure.metric.api.registerMetricApi
import no.nav.syfo.application.EnhetService
import no.nav.syfo.api.access.APIConsumerAccessService
import no.nav.syfo.api.internad.registrerPersonApi
import no.nav.syfo.api.system.registrerSystemApi
import no.nav.syfo.infrastructure.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.infrastructure.client.wellknown.WellKnown

fun Application.apiModule(
    applicationState: ApplicationState,
    environment: Environment,
    wellKnownInternalAzureAD: WellKnown,
    veilederTilgangskontrollClient: VeilederTilgangskontrollClient,
    enhetService: EnhetService,
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

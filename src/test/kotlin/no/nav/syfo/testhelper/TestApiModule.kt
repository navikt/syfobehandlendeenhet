package no.nav.syfo.testhelper

import io.ktor.server.application.*
import no.nav.syfo.application.api.apiModule
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer

fun Application.testApiModule(
    externalMockEnvironment: ExternalMockEnvironment,
    behandlendeEnhetProducer: BehandlendeEnhetProducer,
) {
    this.apiModule(
        applicationState = externalMockEnvironment.applicationState,
        environment = externalMockEnvironment.environment,
        wellKnownInternalAzureAD = externalMockEnvironment.wellKnownInternalAzureAD,
        database = externalMockEnvironment.database,
        behandlendeEnhetProducer = behandlendeEnhetProducer,
    )
}

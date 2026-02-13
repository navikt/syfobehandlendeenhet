package no.nav.syfo.api.access

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.api.authentication.Token
import no.nav.syfo.api.authentication.getConsumerClientId
import no.nav.syfo.util.configuredJacksonMapper

class APIConsumerAccessService(
    azureAppPreAuthorizedApps: String,
) {
    private val preAuthorizedClientList: List<PreAuthorizedClient> = configuredJacksonMapper()
        .readValue(azureAppPreAuthorizedApps)

    fun validateConsumerApplicationAZP(
        authorizedApplicationNameList: List<String>,
        token: Token,
    ) {
        val consumerClientIdAzp: String = token.getConsumerClientId()
        val clientIdList = preAuthorizedClientList
            .filter {
                authorizedApplicationNameList.contains(
                    it.toNamespaceAndApplicationName().applicationName
                )
            }
            .map { it.clientId }

        if (!clientIdList.contains(consumerClientIdAzp)) {
            throw ForbiddenAccessSystemConsumer(consumerClientIdAzp = consumerClientIdAzp)
        }
    }
}

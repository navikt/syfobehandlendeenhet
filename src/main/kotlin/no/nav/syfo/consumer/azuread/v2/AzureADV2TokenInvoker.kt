package no.nav.syfo.consumer.azuread.v2

import org.springframework.beans.factory.annotation.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class AzureADV2TokenInvoker @Autowired constructor(
    @Qualifier("restTemplateWithProxy") private val restTemplateWithProxy: RestTemplate,
    @Value("\${azure.openid.config.token.endpoint}") private val azureTokenEndpoint: String
) {

    fun getToken(
        requestEntity: HttpEntity<MultiValueMap<String, String>>,
    ): AzureAdV2Token {
        val response = restTemplateWithProxy.exchange(
            azureTokenEndpoint,
            HttpMethod.POST,
            requestEntity,
            AzureAdV2TokenResponse::class.java
        )
        val tokenResponse = response.body!!

        return tokenResponse.toAzureAdV2Token()
    }
}

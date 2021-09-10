package no.nav.syfo.api.auth

import no.nav.security.token.support.core.context.TokenValidationContextHolder

const val CLAIM_AZP = "azp"

object OIDCUtil {

    fun tokenFraOIDC(contextHolder: TokenValidationContextHolder, issuer: String): String {
        val context = contextHolder.tokenValidationContext
        return context.getJwtToken(issuer).tokenAsString
    }
}

fun getConsumerClientId(contextHolder: TokenValidationContextHolder): String? {
    return contextHolder
        .tokenValidationContext
        .getClaims(OIDCIssuer.VEILEDER_AZURE_V2)
        .getStringClaim(CLAIM_AZP)
}

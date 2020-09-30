package no.nav.syfo.oidc

import no.nav.security.token.support.core.context.TokenValidationContextHolder

object OIDCUtil {

    fun tokenFraOIDC(contextHolder: TokenValidationContextHolder, issuer: String): String {
        val context = contextHolder.tokenValidationContext
        return context.getJwtToken(issuer).tokenAsString
    }
}

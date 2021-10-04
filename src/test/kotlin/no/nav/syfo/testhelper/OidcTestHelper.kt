package no.nav.syfo.testhelper

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.test.JwtTokenGenerator
import no.nav.syfo.api.auth.OIDCIssuer
import java.text.ParseException
import java.util.*

object OidcTestHelper {

    @Throws(ParseException::class)
    fun logInVeilederADV2(oidcRequestContextHolder: TokenValidationContextHolder, veilederIdent: String) {
        val claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"$veilederIdent\", \"azp\":\"clientid\"}")
        val jwt = JwtTokenGenerator.createSignedJWT(claimsSet)
        settOIDCValidationContext(oidcRequestContextHolder, jwt, OIDCIssuer.VEILEDER_AZURE_V2)
    }

    fun settOIDCValidationContext(tokenValidationContextHolder: TokenValidationContextHolder, jwt: SignedJWT, issuer: String) {
        val jwtToken = JwtToken(jwt.serialize())
        val issuerTokenMap: MutableMap<String, JwtToken> = HashMap()
        issuerTokenMap[issuer] = jwtToken
        val tokenValidationContext = TokenValidationContext(issuerTokenMap)
        tokenValidationContextHolder.tokenValidationContext = tokenValidationContext
    }

    fun clearOIDCContext(tokenValidationContextHolder: TokenValidationContextHolder) {
        tokenValidationContextHolder.tokenValidationContext = null
    }
}

fun logInSystemConsumerClient(
    oidcRequestContextHolder: TokenValidationContextHolder,
    consumerClientId: String = ""
) {
    val claimsSet = JWTClaimsSet.parse("{ \"azp\": \"$consumerClientId\"}")
    val jwt = JwtTokenGenerator.createSignedJWT(claimsSet)
    OidcTestHelper.settOIDCValidationContext(oidcRequestContextHolder, jwt, OIDCIssuer.VEILEDER_AZURE_V2)
}

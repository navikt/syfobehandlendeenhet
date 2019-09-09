package no.nav.syfo.testhelper

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.security.oidc.context.*
import no.nav.security.spring.oidc.test.JwtTokenGenerator
import no.nav.syfo.oidc.OIDCIssuer

import java.text.ParseException

object OidcTestHelper {

    @Throws(ParseException::class)
    fun loggInnVeilederAzure(oidcRequestContextHolder: OIDCRequestContextHolder, veilederIdent: String) {
        val claimsSet = JWTClaimsSet.parse("{\"NAVident\":\"$veilederIdent\"}")
        val jwt = JwtTokenGenerator.createSignedJWT(claimsSet)
        settOIDCValidationContext(oidcRequestContextHolder, jwt, OIDCIssuer.AZURE)
    }

    fun loggInnVeileder(oidcRequestContextHolder: OIDCRequestContextHolder, subject: String) {
        val jwt = JwtTokenGenerator.createSignedJWT(subject)

        settOIDCValidationContext(oidcRequestContextHolder, jwt, OIDCIssuer.INTERN)
    }

    fun loggInnBruker(oidcRequestContextHolder: OIDCRequestContextHolder, subject: String) {
        val jwt = JwtTokenGenerator.createSignedJWT(subject)
        settOIDCValidationContext(oidcRequestContextHolder, jwt, OIDCIssuer.EKSTERN)
    }

    private fun settOIDCValidationContext(
        oidcRequestContextHolder: OIDCRequestContextHolder,
        jwt: SignedJWT,
        issuer: String
    ) {
        val tokenContext = TokenContext(issuer, jwt.serialize())
        val oidcClaims = OIDCClaims(jwt)
        val oidcValidationContext = OIDCValidationContext()
        oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims)
        oidcRequestContextHolder.oidcValidationContext = oidcValidationContext
    }

    fun lagOIDCValidationContextIntern(subject: String): OIDCValidationContext {
        return lagOIDCValidationContext(subject, OIDCIssuer.INTERN)
    }

    fun lagOIDCValidationContextEkstern(subject: String): OIDCValidationContext {
        return lagOIDCValidationContext(subject, OIDCIssuer.EKSTERN)
    }

    private fun lagOIDCValidationContext(subject: String, issuer: String): OIDCValidationContext {
        val jwt = JwtTokenGenerator.createSignedJWT(subject)
        val tokenContext = TokenContext(issuer, jwt.serialize())
        val oidcClaims = OIDCClaims(jwt)
        val oidcValidationContext = OIDCValidationContext()
        oidcValidationContext.addValidatedToken(issuer, tokenContext, oidcClaims)
        return oidcValidationContext
    }

    fun loggUtAlle(oidcRequestContextHolder: OIDCRequestContextHolder) {
        oidcRequestContextHolder.oidcValidationContext = null
    }

}

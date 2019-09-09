package no.nav.syfo.util

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.security.oidc.context.*
import no.nav.syfo.oidc.OIDCClaim
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.service.ws.OnBehalfOfOutInterceptor
import org.apache.cxf.endpoint.Client

import java.text.ParseException
import java.util.Optional

import no.nav.security.oidc.OIDCConstants.OIDC_VALIDATION_CONTEXT

object OIDCUtil {

    fun leggTilOnBehalfOfOutInterceptorForOIDC(client: Client, OIDCToken: String) {
        client.requestContext[OnBehalfOfOutInterceptor.REQUEST_CONTEXT_ONBEHALFOF_TOKEN_TYPE] =
            OnBehalfOfOutInterceptor.TokenType.OIDC
        client.requestContext[OnBehalfOfOutInterceptor.REQUEST_CONTEXT_ONBEHALFOF_TOKEN] = OIDCToken
    }

    private fun context(contextHolder: OIDCRequestContextHolder): OIDCValidationContext {
        return Optional.ofNullable(contextHolder.oidcValidationContext)
            .orElse(null)
    }

    private fun claims(contextHolder: OIDCRequestContextHolder, issuer: String): OIDCClaims {
        return Optional.ofNullable(context(contextHolder))
            .map { s -> s.getClaims(issuer) }
            .orElse(null)
    }

    private fun claimSet(contextHolder: OIDCRequestContextHolder, issuer: String): JWTClaimsSet {
        return Optional.ofNullable(claims(contextHolder, issuer))
            .map<JWTClaimsSet>(Function<OIDCClaims, JWTClaimsSet> { it.getClaimSet() })
            .orElse(null)
    }

    fun getSubjectIntern(contextHolder: OIDCRequestContextHolder): String {
        return Optional.ofNullable(claimSet(contextHolder, OIDCIssuer.INTERN))
            .map<String>(Function<JWTClaimsSet, String> { it.getSubject() })
            .orElse(null)
    }

    fun getSubjectEkstern(contextHolder: OIDCRequestContextHolder): String {
        return Optional.ofNullable(claimSet(contextHolder, OIDCIssuer.EKSTERN))
            .map<String>(Function<JWTClaimsSet, String> { it.getSubject() })
            .orElse(null)
    }

    fun getIssuerToken(contextHolder: OIDCRequestContextHolder, issuer: String): String {
        val context = contextHolder
            .getRequestAttribute(OIDC_VALIDATION_CONTEXT) as OIDCValidationContext
        val tokenContext = context.getToken(issuer)
        return tokenContext.idToken
    }

    fun getSubjectInternAzure(contextHolder: OIDCRequestContextHolder): String {
        val context = contextHolder
            .getRequestAttribute(OIDC_VALIDATION_CONTEXT) as OIDCValidationContext
        try {
            return context.getClaims(OIDCIssuer.AZURE).claimSet.getStringClaim(OIDCClaim.NAVIDENT)
        } catch (e: ParseException) {
            throw RuntimeException("Klarte ikke hente veileder-ident ut av OIDC-token (Azure)")
        }

    }
}

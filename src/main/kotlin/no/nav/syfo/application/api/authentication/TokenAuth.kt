package no.nav.syfo.application.api.authentication

import com.auth0.jwt.JWT

const val JWT_CLAIM_NAVIDENT = "NAVident"
const val JWT_CLAIM_AZP = "azp"

@JvmInline
value class Token(val value: String)

fun Token.getNAVIdent(): String =
    JWT.decode(this.value).claims[JWT_CLAIM_NAVIDENT]?.asString()
        ?: throw RuntimeException("Missing NAVident in private claims")

fun Token.getConsumerClientId(): String =
    JWT.decode(this.value).claims[JWT_CLAIM_AZP]?.asString()
        ?: throw IllegalArgumentException("Claim AZP was not found in token")

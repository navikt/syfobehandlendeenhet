package no.nav.syfo.application.api.authentication

import com.auth0.jwt.JWT

const val JWT_CLAIM_NAVIDENT = "NAVident"
const val JWT_CLAIM_AZP = "azp"

fun getNAVIdentFromToken(token: String): String =
    JWT.decode(token).claims[JWT_CLAIM_NAVIDENT]?.asString()
        ?: throw RuntimeException("Missing NAVident in private claims")

fun getConsumerClientId(token: String): String =
    JWT.decode(token).claims[JWT_CLAIM_AZP]?.asString()
        ?: throw IllegalArgumentException("Claim AZP was not found in token")

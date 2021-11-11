package no.nav.syfo.util

import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments
import java.util.*

const val TEMA_HEADER = "Tema"
const val ALLE_TEMA_HEADERVERDI = "GEN"

const val NAV_PERSONIDENT_HEADER = "nav-personident"

const val NAV_PERSONIDENTER_HEADER = "Nav-Personidenter"

const val NAV_CONSUMER_ID_HEADER = "Nav-Consumer-Id"
const val NAV_APP_CONSUMER_ID = "syfobehandlendeenhet"
const val NAV_CALL_ID_HEADER = "Nav-Call-Id"

fun createCallId(): String = UUID.randomUUID().toString()

fun callIdArgument(callId: String): StructuredArgument = StructuredArguments.keyValue("callId", callId)

fun bearerHeader(token: String) = "Bearer $token"

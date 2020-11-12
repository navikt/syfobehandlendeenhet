package no.nav.syfo.util

import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments
import org.springframework.util.MultiValueMap
import java.util.*

const val TEMA_HEADER = "Tema"
const val ALLE_TEMA_HEADERVERDI = "GEN"
const val NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token"

const val NAV_CONSUMER_ID_HEADER = "Nav-Consumer-Id"
const val APP_CONSUMER_ID = "syfobehandlendeenhet"
const val NAV_CALL_ID_HEADER = "Nav-Call-Id"

fun createCallId(): String = UUID.randomUUID().toString()

fun callIdArgument(callId: String): StructuredArgument = StructuredArguments.keyValue("callId", callId)

fun getOrCreateCallId(headers: MultiValueMap<String, String>): String = headers.getFirst(NAV_CALL_ID_HEADER.toLowerCase())
    ?: createCallId()

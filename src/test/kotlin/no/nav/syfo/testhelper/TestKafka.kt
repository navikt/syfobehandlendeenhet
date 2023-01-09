package no.nav.syfo.testhelper

import no.nav.common.KafkaEnvironment
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.identhendelse.kafka.PDL_AKTOR_TOPIC

fun testKafka(
    autoStart: Boolean = false,
    withSchemaRegistry: Boolean = false,
    topicNames: List<String> = listOf(
        BehandlendeEnhetProducer.BEHANDLENDE_ENHET_UPDATE_TOPIC,
        PDL_AKTOR_TOPIC,
    ),
) = KafkaEnvironment(
    autoStart = autoStart,
    withSchemaRegistry = withSchemaRegistry,
    topicNames = topicNames,
)

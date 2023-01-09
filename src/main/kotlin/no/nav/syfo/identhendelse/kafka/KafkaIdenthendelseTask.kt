package no.nav.syfo.identhendelse.kafka

import kotlinx.coroutines.runBlocking
import no.nav.syfo.application.ApplicationEnvironmentKafka
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.launchBackgroundTask
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val PDL_AKTOR_TOPIC = "pdl.aktor-v2"
private val log: Logger = LoggerFactory.getLogger("no.nav.syfo.identhendelse")

fun launchKafkaTaskIdenthendelse(
    applicationState: ApplicationState,
    applicationEnvironmentKafka: ApplicationEnvironmentKafka,
    kafkaIdenthendelseConsumerService: IdenthendelseConsumerService,
) {
    launchBackgroundTask(
        applicationState = applicationState
    ) {
        log.info("Setting up kafka consumer for ${KafkaIdenthendelseDTO::class.java.simpleName}")

        val kafkaConfig = kafkaIdenthendelseConsumerConfig(applicationEnvironmentKafka)
        val kafkaConsumer = KafkaConsumer<String, GenericRecord>(kafkaConfig)

        kafkaConsumer.subscribe(
            listOf(PDL_AKTOR_TOPIC)
        )
        while (applicationState.ready) {
            runBlocking {
                kafkaIdenthendelseConsumerService.pollAndProcessRecords(
                    kafkaConsumer = kafkaConsumer,
                )
            }
        }
    }
}

package no.nav.syfo.infrastructure.kafka

import no.nav.syfo.ApplicationEnvironmentKafka
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

fun kafkaBehandlendeEnhetProducerConfig(
    applicationEnvironmentKafka: ApplicationEnvironmentKafka,
): Properties {
    return Properties().apply {
        putAll(commonKafkaAivenProducerConfig(applicationEnvironmentKafka))
        this[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.canonicalName
        this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JacksonKafkaSerializer::class.java.canonicalName
    }
}

package no.nav.syfo.identhendelse.kafka

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import no.nav.syfo.application.ApplicationEnvironmentKafka
import no.nav.syfo.infrastructure.kafka.commonKafkaConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import java.util.*

fun kafkaIdenthendelseConsumerConfig(
    applicationEnvironmentKafka: ApplicationEnvironmentKafka,
): Properties {
    return Properties().apply {
        putAll(commonKafkaConsumerConfig(applicationEnvironmentKafka))
        this[ConsumerConfig.GROUP_ID_CONFIG] = "syfobehandlendeenhet-v1"
        this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java.canonicalName
        this[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "1"

        this[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = applicationEnvironmentKafka.aivenSchemaRegistryUrl
        this[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = false
        this[KafkaAvroDeserializerConfig.USER_INFO_CONFIG] = "${applicationEnvironmentKafka.aivenRegistryUser}:${applicationEnvironmentKafka.aivenRegistryPassword}"
        this[KafkaAvroDeserializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE] = "USER_INFO"
    }
}

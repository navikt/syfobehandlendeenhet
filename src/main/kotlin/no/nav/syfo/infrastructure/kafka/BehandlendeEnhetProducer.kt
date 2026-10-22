package no.nav.syfo.infrastructure.kafka

import no.nav.syfo.domain.Oppfolgingsenhet
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.*

class BehandlendeEnhetProducer(
    private val kafkaProducerBehandlendeEnhet: KafkaProducer<String, KBehandlendeEnhetUpdate>,
) {
    fun sendBehandlendeEnhetUpdate(
        oppfolgingsenhet: Oppfolgingsenhet,
        updatedAt: OffsetDateTime,
    ) {
        val key = UUID.nameUUIDFromBytes(oppfolgingsenhet.personident.value.toByteArray()).toString()
        try {
            kafkaProducerBehandlendeEnhet.send(
                ProducerRecord(
                    BEHANDLENDE_ENHET_UPDATE_TOPIC,
                    key,
                    KBehandlendeEnhetUpdate(
                        oppfolgingsenhet.personident.value,
                        updatedAt,
                    ),
                )
            ).also { it.get() }
        } catch (e: Exception) {
            log.error(
                """
                    Exception was thrown when attempting to send enhet update on person.
                    Person-uuid: ${oppfolgingsenhet.uuid}
                    Kafka-key: $key
                    Error-message: ${e.message}
                """.trimIndent(),
                e
            )
            throw e
        }
    }

    companion object {
        const val BEHANDLENDE_ENHET_UPDATE_TOPIC = "teamsykefravr.behandlendeenhet"
        private val log = LoggerFactory.getLogger(BehandlendeEnhetProducer::class.java)
    }
}

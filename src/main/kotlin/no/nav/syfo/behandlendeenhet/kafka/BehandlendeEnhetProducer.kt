package no.nav.syfo.behandlendeenhet.kafka

import no.nav.syfo.behandlendeenhet.domain.Oppfolgingsenhet
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.*

class BehandlendeEnhetProducer(
    private val producer: KafkaProducer<String, BehandlendeEnhetUpdateRecord>,
) {
    fun sendBehandlendeEnhetUpdate(
        oppfolgingsenhet: Oppfolgingsenhet,
        updatedAt: OffsetDateTime,
    ) {
        val key = UUID.nameUUIDFromBytes(oppfolgingsenhet.personident.value.toByteArray()).toString()
        try {
            producer.send(
                ProducerRecord(
                    TOPIC,
                    key,
                    BehandlendeEnhetUpdateRecord(
                        personident = oppfolgingsenhet.personident.value,
                        oppfolgingsenhet = oppfolgingsenhet.enhet?.value,
                        updatedAt = updatedAt,
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
        private const val TOPIC = "teamsykefravr.behandlendeenhet"
        private val log = LoggerFactory.getLogger(BehandlendeEnhetProducer::class.java)
    }
}

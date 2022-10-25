package no.nav.syfo.behandlendeenhet.kafka

import no.nav.syfo.behandlendeenhet.database.domain.PPerson
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.util.*

class BehandlendeEnhetProducer(
    private val kafkaProducerBehandlendeEnhet: KafkaProducer<String, KBehandlendeEnhetUpdate>,
) {
    fun updateBehandlendeEnhet(
        pPerson: PPerson,
    ) {
        try {
            val key = UUID.nameUUIDFromBytes(pPerson.personident.toByteArray()).toString()
            kafkaProducerBehandlendeEnhet.send(
                ProducerRecord(
                    BEHANDLENDE_ENHET_UPDATE_TOPIC,
                    key,
                    KBehandlendeEnhetUpdate(
                        pPerson.personident,
                        pPerson.updatedAt,
                    ),
                )
            ).get()
        } catch (e: Exception) {
            log.error(
                "Exception was thrown when attempting to send dialogmelding with id {}: ${e.message}",
                pPerson.uuid.toString(),
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

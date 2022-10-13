package no.nav.syfo.behandlendeenhet.kafka

import no.nav.syfo.behandlendeenhet.database.domain.PPerson
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory

class BehandlendeEnhetProducer(
    private val kafkaProducerBehandlendeEnhet: KafkaProducer<String, KBehandlendeEnhetUpdate>,
) {
    fun updateBehandlendeEnhet(
        pPerson: PPerson,
    ) {
        try {
            kafkaProducerBehandlendeEnhet.send(
                ProducerRecord(
                    BEHANDLENDE_ENHET_UPDATE_TOPIC,
                    pPerson.personident,
                    KBehandlendeEnhetUpdate(
                        pPerson.personident,
                        pPerson.updatedAt.toLocalDateTime(),
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
        const val BEHANDLENDE_ENHET_UPDATE_TOPIC = "teamsykefravr.syfobehandlendeenhet-enhet-update"
        private val log = LoggerFactory.getLogger(BehandlendeEnhetProducer::class.java)
    }
}

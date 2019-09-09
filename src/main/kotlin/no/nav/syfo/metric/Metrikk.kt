package no.nav.syfo.metric

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Controller

import javax.inject.Inject
import java.time.LocalDateTime

import java.time.LocalDate.now
import no.nav.syfo.util.DatoService.dagerMellom

@Controller
class Metrikk @Inject
constructor(private val registry: MeterRegistry) {

    fun tellEndepunktKall(navn: String) {
        registry.counter(
            addPrefix(navn),
            Tags.of("type", "info")
        ).increment()
    }

    fun tellHttpKall(kode: Int) {
        registry.counter(
            addPrefix("httpstatus"),
            Tags.of(
                "type", "info",
                "kode", kode.toString()
            )
        ).increment()
    }

    fun tellTredjepartVarselSendt(type: String) {
        registry.counter(addPrefix("tredjepartvarsler_sendt"), Tags.of("type", "info", "varseltype", type))
            .increment()
    }

    fun reportAntallDagerSiden(tidspunkt: LocalDateTime, navn: String) {
        val dagerIMellom = dagerMellom(tidspunkt.toLocalDate(), now())

        registry.counter(
            addPrefix(navn + "FraOpprettetMote"),
            Tags.of("type", "info")
        ).increment(dagerIMellom.toDouble())
    }

    private fun addPrefix(navn: String): String {
        val METRIKK_PREFIX = "syfobehandlendeenhet_"
        return METRIKK_PREFIX + navn
    }
}

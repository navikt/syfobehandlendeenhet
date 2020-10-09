package no.nav.syfo.metric

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Controller
import javax.inject.Inject

@Controller
class Metric @Inject constructor(private val registry: MeterRegistry) {

    fun countIncomingRequests(name: String) {
        registry.counter(
            addPrefix("incomingRequest_$name"),
            Tags.of("type", "info")
        ).increment()
    }

    fun tellHttpKall(kode: Int) {
        registry.counter(
            addPrefix("httpstatus"),
            Tags.of(
                "type",
                "info",
                "kode",
                kode.toString()
            )
        ).increment()
    }

    fun countOutgoingReponses(name: String, statusCode: Int) {
        registry.counter(
            addPrefix("outgoingResponse_$name"),
            Tags.of(
                "type",
                "info",
                "status",
                statusCode.toString()
            )
        ).increment()
    }

    fun countOutgoingRequests(name: String) {
        registry.counter(
            addPrefix("outgoingRequest_$name"),
            Tags.of("type", "info")
        ).increment()
    }

    fun countOutgoingRequestsFailed(name: String, errorType: String) {
        registry.counter(
            addPrefix("outgoingRequest_${name}_failed"),
            Tags.of(
                "type",
                "info",
                "errorType",
                errorType
            )
        ).increment()
    }

    private fun addPrefix(navn: String): String {
        val METRIKK_PREFIX = "syfobehandlendeenhet_"
        return METRIKK_PREFIX + navn
    }
}

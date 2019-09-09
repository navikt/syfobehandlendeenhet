package no.nav.syfo.config.converter

import javax.xml.bind.annotation.adapters.XmlAdapter
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : XmlAdapter<String, LocalDateTime>() {
    @Throws(Exception::class)
    override fun unmarshal(dateTime: String): LocalDateTime {
        return ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME)
            .withZoneSameInstant(ZoneOffset.systemDefault())
            .toLocalDateTime()
    }

    @Throws(Exception::class)
    override fun marshal(dateTime: LocalDateTime): String {
        return marshalLocalDateTime(dateTime)
    }

    companion object {

        @Throws(Exception::class)
        fun marshalLocalDateTime(dateTime: LocalDateTime): String {
            return dateTime.atZone(ZoneOffset.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT)
        }
    }
}

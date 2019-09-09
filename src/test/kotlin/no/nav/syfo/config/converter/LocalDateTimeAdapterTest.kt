package no.nav.syfo.config.converter

import org.junit.Test

import java.time.LocalDateTime

import org.assertj.core.api.Assertions.assertThat

class LocalDateTimeAdapterTest {

    internal var adapter = LocalDateTimeAdapter()

    @Test
    @Throws(Exception::class)
    fun unmarshalSommertidMinus1Time() {
        val tid = "2016-10-20T21:59:59Z"
        val dateTime = adapter.unmarshal(tid)
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 10, 20, 23, 59, 59))
    }

    @Test
    @Throws(Exception::class)
    fun unmarshalSommertid() {
        val tid = "2016-10-20T22:00:00Z"
        val dateTime = adapter.unmarshal(tid)
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 10, 21, 0, 0))
    }

    @Test
    @Throws(Exception::class)
    fun unmarshalSommertidPluss1Time() {
        val tid = "2016-10-20T23:00:00Z"
        val dateTime = adapter.unmarshal(tid)
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 10, 21, 1, 0))
    }

    @Test
    @Throws(Exception::class)
    fun unmarshalVintertidMinus1Time() {
        val tid = "2016-01-20T22:59:59Z"
        val dateTime = adapter.unmarshal(tid)
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 1, 20, 23, 59, 59))
    }

    @Test
    @Throws(Exception::class)
    fun unmarshalVintertid() {
        val tid = "2016-01-20T23:00:00Z"
        val dateTime = adapter.unmarshal(tid)
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 1, 21, 0, 0))
    }

    @Test
    @Throws(Exception::class)
    fun unmarshalVintertidPluss1Time() {
        val tid = "2016-01-21T00:00:00Z"
        val dateTime = adapter.unmarshal(tid)
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2016, 1, 21, 1, 0))
    }

    @Test
    @Throws(Exception::class)
    fun marshalSommertidMinus1Time() {
        val dateTime = LocalDateTime.of(2016, 10, 20, 23, 59, 59)
        val tid = adapter.marshal(dateTime)
        assertThat(tid).isEqualTo("2016-10-20T21:59:59Z")
    }

    @Test
    @Throws(Exception::class)
    fun marshalSommertid() {
        val dateTime = LocalDateTime.of(2016, 10, 21, 0, 0)
        val tid = adapter.marshal(dateTime)
        assertThat(tid).isEqualTo("2016-10-20T22:00:00Z")
    }

    @Test
    @Throws(Exception::class)
    fun marshalSommertidPluss1Time() {
        val dateTime = LocalDateTime.of(2016, 10, 21, 1, 0)
        val tid = adapter.marshal(dateTime)
        assertThat(tid).isEqualTo("2016-10-20T23:00:00Z")
    }

    @Test
    @Throws(Exception::class)
    fun marshalVintertidMinus1Time() {
        val dateTime = LocalDateTime.of(2016, 1, 20, 23, 59, 59)
        val tid = adapter.marshal(dateTime)
        assertThat(tid).isEqualTo("2016-01-20T22:59:59Z")
    }

    @Test
    @Throws(Exception::class)
    fun marshalVintertid() {
        val dateTime = LocalDateTime.of(2016, 1, 21, 0, 0)
        val tid = adapter.marshal(dateTime)
        assertThat(tid).isEqualTo("2016-01-20T23:00:00Z")
    }

    @Test
    @Throws(Exception::class)
    fun marshalVintertidPluss1Time() {
        val dateTime = LocalDateTime.of(2016, 1, 21, 1, 0)
        val tid = adapter.marshal(dateTime)
        assertThat(tid).isEqualTo("2016-01-21T00:00:00Z")
    }

}

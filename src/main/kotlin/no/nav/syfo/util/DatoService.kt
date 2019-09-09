package no.nav.syfo.util

import org.springframework.stereotype.Service

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.EnumSet

import java.time.LocalDate.now

@Service
class DatoService {

    //for å forenkle testing
    fun dagensDato(): LocalDate {
        return now()
    }

    companion object {

        fun dagerMellom(tidspunkt1: LocalDate, tidspunkt2: LocalDate): Int {
            var tidspunkt1 = tidspunkt1
            val helgedager = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            var dagerMellom = 0
            while (tidspunkt1.isBefore(tidspunkt2)) {
                if (!helgedager.contains(tidspunkt1.dayOfWeek)) {
                    dagerMellom++
                }
                tidspunkt1 = tidspunkt1.plusDays(1)
            }

            return dagerMellom
        }
    }
}

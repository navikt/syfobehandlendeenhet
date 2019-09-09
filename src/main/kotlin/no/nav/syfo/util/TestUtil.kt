package no.nav.syfo.util

import java.util.function.BiConsumer

object TestUtil {

    fun <T, U> biForEach(c1: List<T>, c2: List<U>, consumer: BiConsumer<T, U>) {
        biForEach(c1, c2, 0, consumer)
    }

    fun <T, U> biForEach(c1: List<T>, c2: List<U>, offset: Int, consumer: BiConsumer<T, U>) {
        for (i in c1.indices) {
            consumer.accept(c1[i], c2[i + offset])
        }
    }
}

package no.nav.syfo.util

import java.util.ArrayList
import java.util.function.*
import java.util.stream.Stream

import java.util.Optional.of
import java.util.Optional.ofNullable
import java.util.stream.Collectors.toList
import java.util.stream.Stream.empty

object MapUtil {

    fun <T, R, S : R> mapNullable(fra: T, til: S, exp: BiConsumer<T, R>): S {
        return ofNullable(fra).map { f ->
            exp.accept(f, til)
            til
        }.orElse(null)
    }

    fun <T, R, S : R> mapNullable(fra: T, til: S, exp: BiConsumer<T, R>, other: S): S {
        return ofNullable(fra).map { f ->
            exp.accept(f, til)
            til
        }.orElse(other)
    }

    fun <T, R, S : R> map(fra: T, til: S, exp: BiConsumer<T, R>): S {
        return of(fra).map { f ->
            exp.accept(f, til)
            til
        }.orElseThrow { RuntimeException("Resultatet fra exp ble null") }
    }

    fun <T, U : T, R, S : R> mapListe(fra: List<U>, til: Supplier<S>, exp: BiConsumer<T, R>): List<S> {
        return ofNullable(fra).map<List<S>> { f -> mapStream<T, U, R, S>(f.stream(), til, exp).collect(toList<S>()) }
            .orElse(ArrayList())
    }

    fun <T, U : T, R, S : R> mapListe(
        fra: List<U>,
        til: Supplier<S>,
        filter: Predicate<U>,
        exp: BiConsumer<T, R>
    ): List<S> {
        return ofNullable(fra).map<List<S>> { f ->
            mapStream<T, U, R, S>(f.stream().filter(filter), til, exp).collect(
                toList<S>()
            )
        }.orElse(ArrayList())
    }

    fun <T, U : T, R, S : R> mapStream(fra: Stream<U>, til: Supplier<S>, exp: BiConsumer<T, R>): Stream<S> {
        return ofNullable(fra).map { f ->
            f.map { f1 ->
                val s = til.get()
                exp.accept(f1, s)
                s
            }
        }.orElse(empty())
    }

    fun <T, U : T, R, S : R> mapListe(fra: List<U>, til: Function<U, S>, exp: BiConsumer<T, R>): List<S> {
        return ofNullable(fra).map<List<S>> { f -> mapStream<T, U, R, S>(f.stream(), til, exp).collect(toList<S>()) }
            .orElse(ArrayList())
    }

    fun <T, U : T, R, S : R> mapListe(
        fra: List<U>,
        til: Function<U, S>,
        filter: Predicate<U>,
        exp: BiConsumer<T, R>
    ): List<S> {
        return ofNullable(fra).map<List<S>> { f ->
            mapStream<T, U, R, S>(f.stream().filter(filter), til, exp).collect(
                toList<S>()
            )
        }.orElse(ArrayList())
    }

    fun <T, U : T, R, S : R> mapStream(fra: Stream<U>, til: Function<U, S>, exp: BiConsumer<T, R>): Stream<S> {
        return ofNullable(fra).map { f ->
            f.map { f1 ->
                val s = map<U, S>(f1, til)
                exp.accept(f1, s)
                s
            }
        }.orElse(empty())
    }

    fun <T, R> mapNullable(fra: T, exp: Function<T, R>): R {
        return ofNullable(fra).map<R>(exp).orElse(null)
    }

    fun <T, R> mapNullable(fra: T, exp: Function<T, R>, other: R): R {
        return ofNullable(fra).map<R>(exp).orElse(other)
    }

    fun <T, R> map(fra: T, exp: Function<T, R>): R {
        return of(fra).map<R>(exp).orElseThrow { RuntimeException("Resultatet fra exp ble null") }
    }

    fun <T, R> mapNullableFilter(fra: T, p: Predicate<T>, exp: Function<T, R>): R {
        return ofNullable(fra).filter(p).map<R>(exp).orElse(null)
    }

    fun <T, R> mapListe(fra: List<T>, filter: Predicate<T>, exp: Function<T, R>): List<R> {
        return ofNullable(fra).map { f ->
            mapStream<T, R>(
                f.stream().filter(filter),
                exp
            ).collect<List<R>, Any>(toList())
        }.orElse(ArrayList())
    }

    fun <T, R> mapListe(fra: List<T>, exp: Function<T, R>): List<R> {
        return ofNullable(fra).map { f -> mapStream<T, R>(f.stream(), exp).collect<List<R>, Any>(toList()) }
            .orElse(ArrayList())
    }

    fun <T, R> mapStream(fra: Stream<T>, exp: Function<T, R>): Stream<R> {
        return ofNullable(fra).map { f -> f.map<R>(exp) }.orElse(empty())
    }

    fun <T> filterListe(fra: List<T>, filter: Predicate<T>): List<T> {
        return ofNullable(fra).map { f -> f.stream().filter(filter).collect<List<T>, Any>(toList()) }
            .orElse(ArrayList())
    }
}

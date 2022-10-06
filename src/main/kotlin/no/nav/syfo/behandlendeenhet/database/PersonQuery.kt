package no.nav.syfo.behandlendeenhet.database

import no.nav.syfo.application.database.DatabaseInterface
import no.nav.syfo.application.database.toList
import no.nav.syfo.behandlendeenhet.database.domain.PPerson
import no.nav.syfo.domain.PersonIdentNumber
import java.sql.*
import java.time.OffsetDateTime
import java.util.*

const val queryUpdatePerson =
    """
        INSERT INTO PERSON (
            id,
            uuid,
            personident,
            is_nav_utland,
            created_at,
            updated_at
            ) VALUES (DEFAULT, ?, ?, ?, ?, ?)
            ON CONFLICT (personident)
            DO
                UPDATE SET is_nav_utland = EXCLUDED.is_nav_utland, updated_at = EXCLUDED.updated_at
            RETURNING
            id,
            uuid,
            personident,
            is_nav_utland,
            created_at,
            updated_at;
    """

fun DatabaseInterface.updatePerson(
    personIdent: PersonIdentNumber,
    isNavUtland: Boolean
): PPerson? {
    return this.connection.use { connection ->
        val now = OffsetDateTime.now()
        connection.prepareStatement(queryUpdatePerson).use {
            it.setObject(1, UUID.randomUUID())
            it.setString(2, personIdent.value)
            it.setBoolean(3, isNavUtland)
            it.setObject(4, now)
            it.setObject(5, now)
            it.executeQuery().toList { toPPerson() }
        }.also {
            connection.commit()
        }
    }.firstOrNull()
}

const val queryPersonByIdent =
    """
        SELECT *
        FROM PERSON N
        WHERE N.personident = ?
    """

fun DatabaseInterface.getPersonByIdent(personIdent: PersonIdentNumber): PPerson? {
    return this.connection.use { connection ->
        connection.prepareStatement(queryPersonByIdent)
            .use {
                it.setString(1, personIdent.value)
                it.executeQuery().toList { toPPerson() }
            }
    }.firstOrNull()
}

fun ResultSet.toPPerson() =
    PPerson(
        id = getInt("id"),
        uuid = UUID.fromString(getString("uuid")),
        personident = getString("personident"),
        isNavUtland = getBoolean("is_nav_utland"),
        createdAt = getObject("created_at", OffsetDateTime::class.java),
        updatedAt = getObject("updated_at", OffsetDateTime::class.java),
    )

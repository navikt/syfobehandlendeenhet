package no.nav.syfo.infrastructure.database.repository

import no.nav.syfo.behandlendeenhet.IEnhetRepository
import no.nav.syfo.behandlendeenhet.domain.Person
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.infrastructure.database.DatabaseInterface
import no.nav.syfo.infrastructure.database.toList
import no.nav.syfo.util.nowUTC
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.*

class EnhetRepository(private val database: DatabaseInterface) : IEnhetRepository {

    override fun createOrUpdatePerson(
        personIdent: PersonIdentNumber,
        isNavUtland: Boolean
    ): Person? =
        database.connection.use { connection ->
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
        }.firstOrNull()?.toPerson()

    override fun getPersonByIdent(personIdent: PersonIdentNumber): Person? =
        database.connection.use { connection ->
            connection.prepareStatement(queryPersonByIdent)
                .use {
                    it.setString(1, personIdent.value)
                    it.executeQuery().toList { toPPerson() }
                }
        }.firstOrNull()?.toPerson()

    private fun ResultSet.toPPerson() =
        PPerson(
            id = getInt("id"),
            uuid = UUID.fromString(getString("uuid")),
            personident = getString("personident"),
            isNavUtland = getBoolean("is_nav_utland"),
            createdAt = getObject("created_at", OffsetDateTime::class.java),
            updatedAt = getObject("updated_at", OffsetDateTime::class.java),
        )

    override fun updatePersonident(nyPersonident: PersonIdentNumber, oldIdent: PersonIdentNumber): Int {
        var updatedRows: Int
        val now = nowUTC()
        database.connection.use { connection ->
            updatedRows = connection.prepareStatement(queryUpdatePersonident).use {
                it.setString(1, nyPersonident.value)
                it.setObject(2, now)
                it.setString(3, oldIdent.value)
                it.executeUpdate()
            }.also {
                connection.commit()
            }
        }
        return updatedRows
    }

    override fun deletePerson(personIdent: PersonIdentNumber): Int {
        var deletedRows: Int
        database.connection.use { connection ->
            deletedRows = connection.prepareStatement(queryDeletePerson).use {
                it.setString(1, personIdent.value)
                it.executeUpdate()
            }.also {
                connection.commit()
            }
        }
        return deletedRows
    }

    companion object {
        private const val queryUpdatePerson =
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

        private const val queryPersonByIdent =
            """
                SELECT *
                FROM PERSON N
                WHERE N.personident = ?
            """

        private const val queryUpdatePersonident =
            """
                UPDATE PERSON
                SET personident = ?, updated_at = ?
                WHERE personident = ?
            """

        private const val queryDeletePerson =
            """
                DELETE FROM PERSON
                WHERE personident = ?
            """
    }
}

package no.nav.syfo.infrastructure.database.repository

import no.nav.syfo.behandlendeenhet.IEnhetRepository
import no.nav.syfo.behandlendeenhet.domain.Person
import no.nav.syfo.domain.Enhet
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
        enhet: Enhet?,
    ): Person? =
        database.connection.use { connection ->
            val now = OffsetDateTime.now()
            connection.prepareStatement(queryUpdatePerson).use {
                it.setObject(1, UUID.randomUUID())
                it.setString(2, personIdent.value)
                it.setString(3, enhet?.value)
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
            oppfolgingsenhet = getString("oppfolgingsenhet"),
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
                oppfolgingsenhet,
                created_at,
                updated_at
                ) VALUES (DEFAULT, ?, ?, ?, ?, ?)
                ON CONFLICT (personident)
                DO
                    UPDATE SET oppfolgingsenhet = EXCLUDED.oppfolgingsenhet, updated_at = EXCLUDED.updated_at
                RETURNING *
            """

        private const val queryPersonByIdent =
            """
                SELECT *
                FROM PERSON
                WHERE personident = ?
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

package no.nav.syfo.infrastructure.database.repository

import no.nav.syfo.behandlendeenhet.IEnhetRepository
import no.nav.syfo.behandlendeenhet.domain.Oppfolgingsenhet
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.infrastructure.database.DatabaseInterface
import no.nav.syfo.infrastructure.database.toList
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.*

class EnhetRepository(private val database: DatabaseInterface) : IEnhetRepository {

    override fun createOppfolgingsenhet(
        personIdent: PersonIdentNumber,
        enhetId: EnhetId?,
        veilederident: String,
    ): Oppfolgingsenhet =
        database.connection.use { connection ->
            val now = OffsetDateTime.now()
            connection.prepareStatement(createOppfolgingsenhet).use {
                it.setObject(1, UUID.randomUUID())
                it.setString(2, personIdent.value)
                it.setString(3, enhetId?.value)
                it.setString(4, veilederident)
                it.setObject(5, now)
                it.executeQuery().toList { toPOppfolgingsenhet() }
            }.also {
                connection.commit()
            }
        }.first().toOppfolgingsenhet()

    override fun getOppfolgingsenhetByPersonident(personIdent: PersonIdentNumber): Oppfolgingsenhet? =
        database.connection.use { connection ->
            connection.prepareStatement(queryOppfolgingsenhetByPersonident)
                .use {
                    it.setString(1, personIdent.value)
                    it.executeQuery().toList { toPOppfolgingsenhet() }
                }
        }.firstOrNull()?.toOppfolgingsenhet()

    override fun getActiveOppfolgingsenheter(): List<PersonIdentNumber> =
        database.connection.use { connection ->
            connection.prepareStatement(queryActiveOppfolgingsenhet)
                .use {
                    it.executeQuery().toList { PersonIdentNumber(getString(1)) }
                }
        }

    private fun ResultSet.toPOppfolgingsenhet() =
        POppfolgingsenhet(
            id = getInt("id"),
            uuid = UUID.fromString(getString("uuid")),
            personident = getString("personident"),
            oppfolgingsenhet = getString("oppfolgingsenhet"),
            veilederident = getString("veilederident"),
            createdAt = getObject("created_at", OffsetDateTime::class.java),
        )

    override fun updatePersonident(nyPersonident: PersonIdentNumber, oldIdent: PersonIdentNumber): Int {
        var updatedRows: Int
        database.connection.use { connection ->
            updatedRows = connection.prepareStatement(queryUpdatePersonident).use {
                it.setString(1, nyPersonident.value)
                it.setString(2, oldIdent.value)
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
        private const val createOppfolgingsenhet =
            """
            INSERT INTO OPPFOLGINGSENHET (
                id,
                uuid,
                personident,
                oppfolgingsenhet,
                veilederident,
                created_at
                ) VALUES (DEFAULT, ?, ?, ?, ?, ?)
                RETURNING *
            """

        private const val queryOppfolgingsenhetByPersonident =
            """
                SELECT *
                FROM OPPFOLGINGSENHET
                WHERE personident = ?
                ORDER BY created_at DESC
            """

        private const val queryActiveOppfolgingsenhet =
            """
                SELECT DISTINCT personident
                FROM OPPFOLGINGSENHET o1
                WHERE oppfolgingsenhet IS NOT NULL AND NOT EXISTS (SELECT 1 FROM OPPFOLGINGSENHET o2 WHERE o1.personident = o2.personident AND o1.created_at < o2.created_at)
            """

        private const val queryUpdatePersonident =
            """
                UPDATE OPPFOLGINGSENHET
                SET personident = ?
                WHERE personident = ?
            """

        private const val queryDeletePerson =
            """
                DELETE FROM OPPFOLGINGSENHET
                WHERE personident = ?
            """
    }
}

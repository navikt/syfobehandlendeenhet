package no.nav.syfo.testhelper

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import no.nav.syfo.infrastructure.database.DatabaseInterface
import no.nav.syfo.infrastructure.database.toList
import org.flywaydb.core.Flyway
import java.sql.Connection
import java.time.OffsetDateTime
import java.util.UUID

class TestDatabase : DatabaseInterface {
    private val pg: EmbeddedPostgres

    override val connection: Connection
        get() = pg.postgresDatabase.connection.apply {
            autoCommit = false
        }

    init {
        pg = try {
            EmbeddedPostgres.start()
        } catch (e: Exception) {
            EmbeddedPostgres.builder().setLocaleConfig("locale", "en_US").start()
        }

        Flyway.configure().run {
            dataSource(pg.postgresDatabase).load().migrate()
        }
    }

    fun stop() {
        pg.close()
    }
}

class TestDatabaseNotResponding : DatabaseInterface {

    override val connection: Connection
        get() = throw Exception("Not working")

    fun stop() {
    }
}

fun DatabaseInterface.dropData() {
    val queryList = listOf(
        """
        DELETE FROM OPPFOLGINGSENHET
        """.trimIndent()
    )
    this.connection.use { connection ->
        queryList.forEach { query ->
            connection.prepareStatement(query).execute()
        }
        connection.commit()
    }
}

fun DatabaseInterface.setSkjermingCheckedAt(uuid: UUID, datetime: OffsetDateTime) {
    this.connection.use { connection ->
        connection.prepareStatement("update oppfolgingsenhet set skjerming_checked_at = ? where uuid=?").use {
            it.setObject(1, datetime)
            it.setString(2, uuid.toString())
            it.executeUpdate()
        }
        connection.commit()
    }
}

fun DatabaseInterface.addOppfolgingsenhet(uuid: UUID, datetime: OffsetDateTime) {
    this.connection.use { connection ->
        connection.prepareStatement("update oppfolgingsenhet set skjerming_checked_at = ? where uuid=?").use {
            it.setObject(1, datetime)
            it.setString(2, uuid.toString())
            it.executeUpdate()
        }
        connection.commit()
    }
}

fun DatabaseInterface.getVeilederCheckedOk(uuid: UUID) =
    this.connection.use { connection ->
        connection.prepareStatement("select veileder_checked_ok_at from oppfolgingsenhet where uuid=?").use {
            it.setString(1, uuid.toString())
            it.executeQuery().toList {
                getObject(1, OffsetDateTime::class.java)
            }.firstOrNull()
        }
    }

fun DatabaseInterface.setCreatedAt(uuid: UUID, datetime: OffsetDateTime) {
    this.connection.use { connection ->
        connection.prepareStatement("update oppfolgingsenhet set created_at = ? where uuid=?").use {
            it.setObject(1, datetime)
            it.setString(2, uuid.toString())
            it.executeUpdate()
        }
        connection.commit()
    }
}

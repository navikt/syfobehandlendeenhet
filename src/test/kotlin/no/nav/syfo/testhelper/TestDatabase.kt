package no.nav.syfo.testhelper

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import no.nav.syfo.infrastructure.database.DatabaseInterface
import org.flywaydb.core.Flyway
import java.sql.Connection

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

package no.nav.syfo.infrastructure.database

import io.ktor.server.application.*
import no.nav.syfo.application.Environment
import no.nav.syfo.application.isDev
import no.nav.syfo.application.isProd

lateinit var applicationDatabase: DatabaseInterface

fun Application.databaseModule(
    environment: Environment,
) {
    isDev {
        applicationDatabase = Database(
            DatabaseConfig(
                jdbcUrl = "jdbc:postgresql://localhost:5432/syfobehandlendeenhet_dev",
                password = "password",
                username = "username",
            )
        )
    }

    isProd {
        applicationDatabase = Database(
            DatabaseConfig(
                jdbcUrl = environment.jdbcUrl(),
                username = environment.syfobehandlendeenhetDbUsername,
                password = environment.syfobehandlendeenhetDbPassword,
            )
        )
    }
}

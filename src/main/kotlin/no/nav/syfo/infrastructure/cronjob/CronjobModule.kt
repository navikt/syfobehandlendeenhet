package no.nav.syfo.infrastructure.cronjob

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.launchBackgroundTask
import no.nav.syfo.infrastructure.clients.leaderelection.LeaderPodClient
import no.nav.syfo.infrastructure.database.DatabaseInterface

fun launchCronjobs(
    applicationState: ApplicationState,
    environment: Environment,
    database: DatabaseInterface,
) {
    val leaderPodClient = LeaderPodClient(
        electorPath = environment.electorPath
    )
    val cronjobRunner = CronjobRunner(
        applicationState = applicationState,
        leaderPodClient = leaderPodClient,
    )
    val cronjobs = mutableListOf<Cronjob>()

    cronjobs.add(RemoveOppfolgingsenhetCronjob(database))

    cronjobs.forEach {
        launchBackgroundTask(
            applicationState = applicationState,
        ) {
            cronjobRunner.start(cronjob = it)
        }
    }
}

package no.nav.syfo.infrastructure.cronjob

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.launchBackgroundTask
import no.nav.syfo.behandlendeenhet.EnhetService
import no.nav.syfo.behandlendeenhet.IEnhetRepository
import no.nav.syfo.infrastructure.client.syfooversiktsrv.SyfooversiktsrvClient
import no.nav.syfo.infrastructure.clients.leaderelection.LeaderPodClient

fun launchCronjobs(
    applicationState: ApplicationState,
    environment: Environment,
    enhetService: EnhetService,
    repository: IEnhetRepository,
    syfooversiktsrvClient: SyfooversiktsrvClient,
) {
    val leaderPodClient = LeaderPodClient(
        electorPath = environment.electorPath
    )
    val cronjobRunner = CronjobRunner(
        applicationState = applicationState,
        leaderPodClient = leaderPodClient,
    )
    val cronjobs = mutableListOf<Cronjob>()

    cronjobs.add(
        RemoveOppfolgingsenhetCronjob(
            enhetService = enhetService,
            repository = repository,
        )
    )
    cronjobs.add(
        ReturnToSenderIfNoVeilederCronjob(
            enhetService = enhetService,
            repository = repository,
            syfooversiktsrvClient = syfooversiktsrvClient,
        )
    )

    cronjobs.forEach {
        launchBackgroundTask(
            applicationState = applicationState,
        ) {
            cronjobRunner.start(cronjob = it)
        }
    }
}

package no.nav.syfo.infrastructure.cronjob

import no.nav.syfo.infrastructure.database.DatabaseInterface

class RemoveOppfolgingsenhetCronjob(
    database: DatabaseInterface,
) : Cronjob {
    override val initialDelayMinutes: Long = 2
    override val intervalDelayMinutes: Long = 1

    override suspend fun run() = emptyList<Result<Any>>()
}

# syfobehandlendeenhet

## Om syfobehandlendeenhet
syfobehandlendeenhet er en Spring Boot-applikasjon skrevet i Kotlin. Hovedoppgaven til syfobehandlendeenhet
er å finne den enheten som skal behandle en sykmeldt. Appen eksponerer et REST-endepunkt på `/api/{fnr}`.

For å finne behandlende enhet gjøres det WS-kall til `egenAnsatt` og `person`, og REST-kall til NORG; resultatet fra disse caches.

## Lokal utvikling
Appen bygges med `gradle` 
Start opp via `LocalApplication.main`. Kjører på port 8999.

## Pipeline
Pipeline er på Github Action.
Commits til Master-branch deployes automatisk til dev-fss og prod-fss.
Commits til ikke-master-branch bygges uten automatisk deploy.

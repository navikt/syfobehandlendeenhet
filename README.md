# syfobehandlendeenhet

## Om syfobehandlendeenhet
syfobehandlendeenhet er en Spring Boot-applikasjon skrevet i Kotlin. Hovedoppgaven til syfobehandlendeenhet
er å finne den enheten som skal behandle en sykmeldt. Appen eksponerer et REST-endepunkt på `/api/{fnr}`.

For å finne behandlende enhet gjøres det WS-kall til `person`, og REST-kall skjermed-personer-pip(`egenAnsatt`) til NORG; resultatet fra disse caches.

## Lokal utvikling
Appen bygges med `gradle` 
Start opp via `LocalApplication.main`. Kjører på port 8999.

## Lint
Kjør `./gradlew --continue ktlintCheck`

## Cache
For caching brukes Redis. Redis deployes automatisk til NAIS ved endringer i workflow eller i config. Redis kan også deployes manuelt ved å kjøre følgdende kommando: `kubectl apply -f .nais/redis-config.yaml`.

## Pipeline
Pipeline er på Github Action.
Commits til Master-branch deployes automatisk til dev-fss og prod-fss.
Commits til ikke-master-branch bygges uten automatisk deploy.

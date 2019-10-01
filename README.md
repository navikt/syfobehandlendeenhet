# syfobehandlendeenhet

## Om syfobehandlendeenhet
syfobehandlendeenhet er en Spring Boot-applikasjon skrevet i Kotlin. Hovedoppgaven til syfobehandlendeenhet
er å finne den enheten som skal behandle en sykmeldt. Appen eksponerer et REST-endepunkt på `/api/{fnr}`.

For å finne behandlende enhet gjøres det WS-kall til `arbeidsfordeling`, `egenAnsatt`, `organisasjonEnhet`, og `person`; resultatet fra disse caches.

## Lokal utvikling
Appen bygges med `gradle` 
Start opp via `LocalApplication.main`. Kjører på port 8999.

## Veien til prod
Bygg og Pipeline jobber ligger i jenkins: https://jenkins-digisyfo.adeo.no/job/digisyfo/job/syfobehandlendeenhet/

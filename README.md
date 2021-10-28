![Build status](https://github.com/navikt/syfobehandlendeenhet/workflows/main/badge.svg?branch=master)

# Syfobehandlendeenhet

## About
Syfobehandlendeenhet is a Spring Boot application written in Kotlin. 
Syfobehandlendeenhet takes a person's PersonIdent as input and outputs a NAV-enhet.
This NAV-enhet is the BehandlendeEnhet of the person in the context of SYFO(Sykefraværsoppfølging)

To find BehandlendeEnhet, the application makes requests to PersonDataLøsningen(PDL), to skjermed-personer-pip(`egenAnsatt`) til NORG; resultatet fra disse caches.

## Technologies Used
* Docker
* Gradle
* Kotlin
* Spring Boot
* Redis

## Lokal utvikling
Build the application with `gradle` 
Start the application with `LocalApplication.main`. The local application runs on port 8999.

### Lint (Ktlint)
##### Command line
Run checking: `./gradlew --continue ktlintCheck`

Run formatting: `./gradlew ktlintFormat`
##### Git Hooks
Apply checking: `./gradlew addKtlintCheckGitPreCommitHook`

Apply formatting: `./gradlew addKtlintFormatGitPreCommitHook`

## Cache
A single Redis pod is responsible for caching.
The Redis pod is deployed automatically to NAIS after changes are made to the workflow or config file.
The Redis can also be deployed manually with the following command: `kubectl apply -f .nais/redis-config.yaml`.

### Pipeline
Pipeline is run with Github Action workflows.
Commits to Master-branch is deployed automatically to dev-fss and prod-fss.
Commits to non-master-branch is built without automatic deploy.

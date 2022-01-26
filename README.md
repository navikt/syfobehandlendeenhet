![Build status](https://github.com/navikt/syfobehandlendeenhet/workflows/main/badge.svg?branch=master)

# Syfobehandlendeenhet

## About
Syfobehandlendeenhet is a Ktor application written in Kotlin.
Syfobehandlendeenhet takes a person's PersonIdent as input and outputs a NAV-enhet.
This NAV-enhet is the BehandlendeEnhet of the person in the context of SYFO(Sykefraværsoppfølging)

To find BehandlendeEnhet, the application makes requests to PersonDataLøsningen(PDL), to skjermed-personer-pip(`egenAnsatt`) til NORG.

## Technologies Used
* Docker
* Gradle
* Kotlin
* Ktor
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
This application uses Redis for caching. Redis is deployed automatically on changes to workflow or config on master
branch. For manual deploy, run: `kubectl apply -f .nais/redis/redis-config.yaml`
    or `kubectl apply -f .nais/redis/redisexporter.yaml`.

### Pipeline
Pipeline is run with Github Action workflows.
Commits to Master-branch is deployed automatically to dev and prod.
Commits to non-master-branch is built without automatic deploy.

## Contact

### For NAV employees

We are available at the Slack channel `#isyfo`.

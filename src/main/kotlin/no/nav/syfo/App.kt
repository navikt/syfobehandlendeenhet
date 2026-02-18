package no.nav.syfo

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.api.apiModule
import no.nav.syfo.application.EnhetService
import no.nav.syfo.infrastructure.cache.ValkeyStore
import no.nav.syfo.infrastructure.database.applicationDatabase
import no.nav.syfo.infrastructure.database.databaseModule
import no.nav.syfo.infrastructure.kafka.BehandlendeEnhetProducer
import no.nav.syfo.infrastructure.kafka.KBehandlendeEnhetUpdate
import no.nav.syfo.infrastructure.kafka.kafkaBehandlendeEnhetProducerConfig
import no.nav.syfo.infrastructure.client.azuread.AzureAdClient
import no.nav.syfo.infrastructure.client.norg.NorgClient
import no.nav.syfo.infrastructure.client.pdl.PdlClient
import no.nav.syfo.infrastructure.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.infrastructure.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.infrastructure.client.wellknown.getWellKnown
import no.nav.syfo.application.IdenthendelseService
import no.nav.syfo.infrastructure.kafka.identhendelse.IdenthendelseConsumerService
import no.nav.syfo.infrastructure.kafka.identhendelse.launchKafkaTaskIdenthendelse
import no.nav.syfo.infrastructure.client.syfooversiktsrv.SyfooversiktsrvClient
import no.nav.syfo.infrastructure.cronjob.launchCronjobs
import no.nav.syfo.infrastructure.database.repository.EnhetRepository
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory
import redis.clients.jedis.*
import java.util.concurrent.TimeUnit

const val applicationPort = 8080

fun main() {
    val applicationState = ApplicationState()
    val environment = Environment()

    val wellKnownInternalAzureAD = getWellKnown(
        wellKnownUrl = environment.azureAppWellKnownUrl,
    )

    val behandlendeEnhetProducer = BehandlendeEnhetProducer(
        kafkaProducerBehandlendeEnhet = KafkaProducer<String, KBehandlendeEnhetUpdate>(
            kafkaBehandlendeEnhetProducerConfig(environment.kafka)
        ),
    )
    val valkeyConfig = environment.valkeyConfig
    val valkeyStore = ValkeyStore(
        JedisPool(
            JedisPoolConfig(),
            HostAndPort(valkeyConfig.host, valkeyConfig.port),
            DefaultJedisClientConfig.builder()
                .ssl(valkeyConfig.ssl)
                .user(valkeyConfig.valkeyUsername)
                .password(valkeyConfig.valkeyPassword)
                .database(valkeyConfig.valkeyDB)
                .build()
        )
    )

    val azureAdClient = AzureAdClient(
        azureAppClientId = environment.azureAppClientId,
        azureAppClientSecret = environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = environment.azureOpenidConfigTokenEndpoint,
        valkeyStore = valkeyStore,
    )

    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.pdlUrl,
        clientId = environment.pdlClientId,
    )

    val norgClient = NorgClient(
        baseUrl = environment.norg2Url,
        valkeyStore = valkeyStore,
    )

    val skjermedePersonerPipClient = SkjermedePersonerPipClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.skjermedePersonerPipUrl,
        clientId = environment.skjermedePersonerPipClientId,
        valkeyStore = valkeyStore,
    )

    val veilederTilgangskontrollClient = VeilederTilgangskontrollClient(
        azureAdClient = azureAdClient,
        clientId = environment.istilgangskontrollClientId,
        baseUrl = environment.istilgangskontrollUrl,
    )

    val syfooversiktsrvClient = SyfooversiktsrvClient(
        azureAdClient = azureAdClient,
        clientId = environment.syfooversiktsrvClientId,
        baseUrl = environment.syfooversiktsrvUrl,
    )

    val applicationEnvironment = applicationEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        config = HoconApplicationConfig(ConfigFactory.load())
    }

    val server = embeddedServer(
        Netty,
        environment = applicationEnvironment,
        configure = {
            connector {
                port = applicationPort
            }
            connectionGroupSize = 8
            workerGroupSize = 8
            callGroupSize = 16
        },
        module = {
            databaseModule(environment = environment)
            val repository = EnhetRepository(database = applicationDatabase)
            val enhetService = EnhetService(
                norgClient = norgClient,
                pdlClient = pdlClient,
                valkeyStore = valkeyStore,
                skjermedePersonerPipClient = skjermedePersonerPipClient,
                repository = repository,
                behandlendeEnhetProducer = behandlendeEnhetProducer,
            )

            apiModule(
                applicationState = applicationState,
                environment = environment,
                wellKnownInternalAzureAD = wellKnownInternalAzureAD,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
                enhetService = enhetService,
            )
            monitor.subscribe(ApplicationStarted) {
                applicationState.ready = true
                log.info("Application is ready, running Java VM ${Runtime.version()}")
                val identhendelseService = IdenthendelseService(
                    repository = repository,
                    pdlClient = pdlClient,
                )
                val kafkaIdenthendelseConsumerService = IdenthendelseConsumerService(
                    identhendelseService = identhendelseService,
                )
                launchKafkaTaskIdenthendelse(
                    applicationState = applicationState,
                    applicationEnvironmentKafka = environment.kafka,
                    kafkaIdenthendelseConsumerService = kafkaIdenthendelseConsumerService,
                )
                launchCronjobs(
                    applicationState = applicationState,
                    environment = environment,
                    enhetService = enhetService,
                    repository = repository,
                    syfooversiktsrvClient = syfooversiktsrvClient,
                )
            }
            monitor.subscribe(ApplicationStopping) {
                applicationState.ready = false
                log.info("Application is stopping")
            }
        }
    )

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(10, 10, TimeUnit.SECONDS)
        }
    )

    server.start(wait = true)
}

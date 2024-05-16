package no.nav.syfo

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.Environment
import no.nav.syfo.application.api.apiModule
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.application.database.applicationDatabase
import no.nav.syfo.application.database.databaseModule
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.behandlendeenhet.kafka.KBehandlendeEnhetUpdate
import no.nav.syfo.behandlendeenhet.kafka.kafkaBehandlendeEnhetProducerConfig
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.norg.NorgClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.client.veiledertilgang.VeilederTilgangskontrollClient
import no.nav.syfo.client.wellknown.getWellKnown
import no.nav.syfo.identhendelse.IdenthendelseService
import no.nav.syfo.identhendelse.kafka.IdenthendelseConsumerService
import no.nav.syfo.identhendelse.kafka.launchKafkaTaskIdenthendelse
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.Protocol
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

    val redisStore = RedisStore(
        jedisPool = JedisPool(
            JedisPoolConfig(),
            environment.redisHost,
            environment.redisPort,
            Protocol.DEFAULT_TIMEOUT,
            environment.redisSecret,
        ),
    )

    val azureAdClient = AzureAdClient(
        azureAppClientId = environment.azureAppClientId,
        azureAppClientSecret = environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = environment.azureOpenidConfigTokenEndpoint,
        redisStore = redisStore,
    )

    val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.pdlUrl,
        clientId = environment.pdlClientId,
    )

    val norgClient = NorgClient(
        baseUrl = environment.norg2Url,
    )

    val skjermedePersonerPipClient = SkjermedePersonerPipClient(
        azureAdClient = azureAdClient,
        baseUrl = environment.skjermedePersonerPipUrl,
        clientId = environment.skjermedePersonerPipClientId,
        redisStore = redisStore,
    )

    val veilederTilgangskontrollClient = VeilederTilgangskontrollClient(
        azureAdClient = azureAdClient,
        clientId = environment.istilgangskontrollClientId,
        baseUrl = environment.istilgangskontrollUrl,
    )

    val applicationEngineEnvironment = applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor.application")
        config = HoconApplicationConfig(ConfigFactory.load())

        connector {
            port = applicationPort
        }

        module {
            databaseModule(environment = environment)
            apiModule(
                applicationState = applicationState,
                environment = environment,
                wellKnownInternalAzureAD = wellKnownInternalAzureAD,
                database = applicationDatabase,
                behandlendeEnhetProducer = behandlendeEnhetProducer,
                pdlClient = pdlClient,
                redisStore = redisStore,
                norgClient = norgClient,
                skjermedePersonerPipClient = skjermedePersonerPipClient,
                veilederTilgangskontrollClient = veilederTilgangskontrollClient,
            )
        }
    }

    applicationEngineEnvironment.monitor.subscribe(ApplicationStarted) { application ->
        applicationState.ready = true
        application.environment.log.info("Application is ready, running Java VM ${Runtime.version()}")

        val identhendelseService = IdenthendelseService(
            database = applicationDatabase,
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
    }

    val server = embeddedServer(
        factory = Netty,
        environment = applicationEngineEnvironment,
    ) {
        connectionGroupSize = 8
        workerGroupSize = 8
        callGroupSize = 16
    }

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(10, 10, TimeUnit.SECONDS)
        }
    )

    server.start(wait = true)
}

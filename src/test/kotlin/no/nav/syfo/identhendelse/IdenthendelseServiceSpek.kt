package no.nav.syfo.identhendelse

import io.ktor.server.testing.*
import kotlinx.coroutines.*
import no.nav.syfo.application.cache.RedisStore
import no.nav.syfo.behandlendeenhet.database.getPersonByIdent
import no.nav.syfo.behandlendeenhet.database.createOrUpdatePerson
import no.nav.syfo.client.azuread.AzureAdClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.dropData
import no.nav.syfo.testhelper.generator.generateKafkaIdenthendelseDTO
import no.nav.syfo.testhelper.mock.mockHttpClient
import org.amshove.kluent.internal.assertFailsWith
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.Protocol

object IdenthendelseServiceSpek : Spek({

    describe(IdenthendelseServiceSpek::class.java.simpleName) {

        with(TestApplicationEngine()) {
            start()

            val externalMockEnvironment = ExternalMockEnvironment.instance
            val database = externalMockEnvironment.database
            val azureAdClient = AzureAdClient(
                azureAppClientId = externalMockEnvironment.environment.azureAppClientId,
                azureAppClientSecret = externalMockEnvironment.environment.azureAppClientSecret,
                azureOpenidConfigTokenEndpoint = externalMockEnvironment.environment.azureOpenidConfigTokenEndpoint,
                redisStore = RedisStore(
                    JedisPool(
                        JedisPoolConfig(),
                        externalMockEnvironment.environment.redisHost,
                        externalMockEnvironment.environment.redisPort,
                        Protocol.DEFAULT_TIMEOUT,
                        externalMockEnvironment.environment.redisSecret,
                    )
                ),
                httpClient = externalMockEnvironment.mockHttpClient,
            )
            val pdlClient = PdlClient(
                azureAdClient = azureAdClient,
                baseUrl = externalMockEnvironment.environment.pdlUrl,
                clientId = externalMockEnvironment.environment.pdlClientId,
                httpClient = externalMockEnvironment.mockHttpClient,
            )

            val identhendelseService = IdenthendelseService(
                database = database,
                pdlClient = pdlClient,
            )

            afterEachTest {
                database.dropData()
            }

            describe("Happy path") {
                it("Skal oppdatere person når person har fått ny ident") {
                    val kafkaIdenthendelseDTO = generateKafkaIdenthendelseDTO(hasOldPersonident = true)
                    val newIdent = kafkaIdenthendelseDTO.getActivePersonident()!!
                    val oldIdent = kafkaIdenthendelseDTO.getInactivePersonidenter().first()

                    val oldUpdatedAt = database.createOrUpdatePerson(
                        personIdent = oldIdent,
                        isNavUtland = false,
                    )?.updatedAt

                    runBlocking {
                        identhendelseService.handleIdenthendelse(kafkaIdenthendelseDTO)
                    }

                    val updatedPerson = database.getPersonByIdent(newIdent)
                    updatedPerson?.personident shouldBeEqualTo newIdent.value
                    updatedPerson?.updatedAt shouldNotBeEqualTo oldUpdatedAt

                    val oldPerson = database.getPersonByIdent(oldIdent)
                    oldPerson shouldBeEqualTo null
                }

                it("Skal slette gammel person når ny ident allerede finnes i databasen") {
                    val kafkaIdenthendelseDTO = generateKafkaIdenthendelseDTO(hasOldPersonident = true)
                    val newIdent = kafkaIdenthendelseDTO.getActivePersonident()!!
                    val oldIdent = kafkaIdenthendelseDTO.getInactivePersonidenter().first()

                    database.createOrUpdatePerson(
                        personIdent = newIdent,
                        isNavUtland = true,
                    )

                    database.createOrUpdatePerson(
                        personIdent = oldIdent,
                        isNavUtland = false,
                    )

                    runBlocking {
                        identhendelseService.handleIdenthendelse(kafkaIdenthendelseDTO)
                    }

                    val updatedPerson = database.getPersonByIdent(newIdent)
                    updatedPerson?.personident shouldBeEqualTo newIdent.value

                    val oldPerson = database.getPersonByIdent(oldIdent)
                    oldPerson shouldBeEqualTo null
                }
            }

            describe("Unhappy path") {
                it("Skal kaste feil hvis PDL ikke har oppdatert identen") {
                    val kafkaIdenthendelseDTO = generateKafkaIdenthendelseDTO(
                        personident = UserConstants.ARBEIDSTAKER_PERSONIDENT_3,
                        hasOldPersonident = true,
                    )
                    val oldIdent = kafkaIdenthendelseDTO.getInactivePersonidenter().first()

                    database.createOrUpdatePerson(
                        personIdent = oldIdent,
                        isNavUtland = false,
                    )

                    runBlocking {
                        assertFailsWith(IllegalStateException::class) {
                            identhendelseService.handleIdenthendelse(kafkaIdenthendelseDTO)
                        }
                    }
                }
            }
        }
    }
})

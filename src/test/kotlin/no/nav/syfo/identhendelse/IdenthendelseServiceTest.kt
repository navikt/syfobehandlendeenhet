package no.nav.syfo.identhendelse

import kotlinx.coroutines.runBlocking
import no.nav.syfo.application.IdenthendelseService
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.domain.EnhetId.Companion.ENHETNR_NAV_UTLAND
import no.nav.syfo.infrastructure.cache.ValkeyStore
import no.nav.syfo.infrastructure.client.azuread.AzureAdClient
import no.nav.syfo.infrastructure.client.pdl.PdlClient
import no.nav.syfo.infrastructure.database.repository.EnhetRepository
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.dropData
import no.nav.syfo.testhelper.generator.generateKafkaIdenthendelseDTO
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

class IdenthendelseServiceTest {

    private val externalMockEnvironment = ExternalMockEnvironment.instance
    private val database = externalMockEnvironment.database
    private val repository = EnhetRepository(database)
    private val redisConfig = externalMockEnvironment.environment.valkeyConfig
    private val valkeyStore = ValkeyStore(
        JedisPool(
            JedisPoolConfig(),
            HostAndPort(redisConfig.host, redisConfig.port),
            DefaultJedisClientConfig.builder()
                .ssl(redisConfig.ssl)
                .password(redisConfig.valkeyPassword)
                .build()
        )
    )
    private val azureAdClient = AzureAdClient(
        azureAppClientId = externalMockEnvironment.environment.azureAppClientId,
        azureAppClientSecret = externalMockEnvironment.environment.azureAppClientSecret,
        azureOpenidConfigTokenEndpoint = externalMockEnvironment.environment.azureOpenidConfigTokenEndpoint,
        valkeyStore = valkeyStore,
        httpClient = externalMockEnvironment.mockHttpClient,
    )
    private val pdlClient = PdlClient(
        azureAdClient = azureAdClient,
        baseUrl = externalMockEnvironment.environment.pdlUrl,
        clientId = externalMockEnvironment.environment.pdlClientId,
        httpClient = externalMockEnvironment.mockHttpClient,
    )

    private val identhendelseService = IdenthendelseService(
        repository = repository,
        pdlClient = pdlClient,
    )

    @AfterEach
    fun afterEach() {
        database.dropData()
    }

    @Nested
    @DisplayName("Happy path")
    inner class HappyPath {

        @Test
        fun `Skal oppdatere person når person har fått ny ident`() {
            val kafkaIdenthendelseDTO = generateKafkaIdenthendelseDTO(hasOldPersonident = true)
            val newIdent = kafkaIdenthendelseDTO.getActivePersonident()!!
            val oldIdent = kafkaIdenthendelseDTO.getInactivePersonidenter().first()

            repository.createOppfolgingsenhet(
                personIdent = oldIdent,
                enhetId = null,
                veilederident = "Z999999",
            )

            runBlocking {
                identhendelseService.handleIdenthendelse(kafkaIdenthendelseDTO)
            }

            val updated = repository.getOppfolgingsenhetByPersonident(newIdent)
            assertEquals(newIdent.value, updated?.personident)

            val old = repository.getOppfolgingsenhetByPersonident(oldIdent)
            assertNull(old)
        }

        @Test
        fun `Skal slette gammel person når ny ident allerede finnes i databasen`() {
            val kafkaIdenthendelseDTO = generateKafkaIdenthendelseDTO(hasOldPersonident = true)
            val newIdent = kafkaIdenthendelseDTO.getActivePersonident()!!
            val oldIdent = kafkaIdenthendelseDTO.getInactivePersonidenter().first()

            repository.createOppfolgingsenhet(
                personIdent = newIdent,
                enhetId = EnhetId(ENHETNR_NAV_UTLAND),
                veilederident = "Z999999",
            )

            repository.createOppfolgingsenhet(
                personIdent = oldIdent,
                enhetId = null,
                veilederident = "Z999999",
            )

            runBlocking {
                identhendelseService.handleIdenthendelse(kafkaIdenthendelseDTO)
            }

            val updated = repository.getOppfolgingsenhetByPersonident(newIdent)
            assertEquals(newIdent.value, updated?.personident)

            val old = repository.getOppfolgingsenhetByPersonident(oldIdent)
            assertNull(old)
        }
    }

    @Nested
    @DisplayName("Unhappy path")
    inner class UnhappyPath {

        @Test
        fun `Skal kaste feil hvis PDL ikke har oppdatert identen`() {
            val kafkaIdenthendelseDTO = generateKafkaIdenthendelseDTO(
                personident = UserConstants.ARBEIDSTAKER_PERSONIDENT_3,
                hasOldPersonident = true,
            )
            val oldIdent = kafkaIdenthendelseDTO.getInactivePersonidenter().first()

            repository.createOppfolgingsenhet(
                personIdent = oldIdent,
                enhetId = null,
                veilederident = "Z999999",
            )

            assertThrows<IllegalStateException> {
                runBlocking {
                    identhendelseService.handleIdenthendelse(kafkaIdenthendelseDTO)
                }
            }
        }
    }
}

package no.nav.syfo.behandlendeenhet.api.internad

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import no.nav.syfo.api.BehandlendeEnhetResponseDTO
import no.nav.syfo.api.EnhetDTO
import no.nav.syfo.api.TildelOppfolgingsenhetResponseDTO
import no.nav.syfo.api.Tildelt
import no.nav.syfo.api.TildeltHistorikkResponseDTO
import no.nav.syfo.api.TildeltTilbake
import no.nav.syfo.api.TildeltTilbakeAvSystem
import no.nav.syfo.api.internad.ENHET_ID_PARAM
import no.nav.syfo.api.internad.internadBehandlendeEnhetApiV2BasePath
import no.nav.syfo.application.EnhetService.Companion.SYSTEM_USER_IDENT
import no.nav.syfo.infrastructure.kafka.BehandlendeEnhetProducer
import no.nav.syfo.infrastructure.kafka.KBehandlendeEnhetUpdate
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.domain.EnhetId.Companion.VEST_VIKEN_ROE_ID
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.infrastructure.database.repository.EnhetRepository
import no.nav.syfo.testhelper.ExternalMockEnvironment
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_EGENANSATT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND_2
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT_2
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT_3
import no.nav.syfo.testhelper.UserConstants.ENHET_ID
import no.nav.syfo.testhelper.UserConstants.OTHER_ENHET_ID
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_NO_ACCESS
import no.nav.syfo.testhelper.dropData
import no.nav.syfo.testhelper.generateJWT
import no.nav.syfo.testhelper.generator.generateTildelOppfolgingsenhetRequestDTO
import no.nav.syfo.testhelper.mock.ENHET_NAVN
import no.nav.syfo.testhelper.mock.GEOGRAFISK_ENHET_NR
import no.nav.syfo.testhelper.mock.UNDERORDNET_NR
import no.nav.syfo.testhelper.testApiModule
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import no.nav.syfo.util.configure
import org.apache.kafka.clients.producer.KafkaProducer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.LocalDate

class BehandlendeEnhetApiTest {
    private val externalMockEnvironment = ExternalMockEnvironment.instance
    private val database = externalMockEnvironment.database
    private val repository = EnhetRepository(database)

    private val kafkaProducerMock = mockk<KafkaProducer<String, KBehandlendeEnhetUpdate>>(relaxed = true)
    private val behandlendeEnhetProducer = BehandlendeEnhetProducer(kafkaProducerMock)

    private val behandlendeEnhetUrl = "$internadBehandlendeEnhetApiV2BasePath/personident"
    private val tildelthistorikkUrl = "$internadBehandlendeEnhetApiV2BasePath/historikk"
    private val oppfolgingsenhetTildelingerUrl = "$internadBehandlendeEnhetApiV2BasePath/oppfolgingsenhet-tildelinger"
    private val tilordningsenheterUrl =
        "$internadBehandlendeEnhetApiV2BasePath/tilordningsenheter/{$ENHET_ID_PARAM}".replace(
            "{$ENHET_ID_PARAM}",
            GEOGRAFISK_ENHET_NR
        )
    private val validToken = generateJWT(
        audience = externalMockEnvironment.environment.azureAppClientId,
        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
        navIdent = VEILEDER_IDENT,
    )

    private fun ApplicationTestBuilder.setupApiAndClient(): HttpClient {
        application {
            testApiModule(
                externalMockEnvironment = externalMockEnvironment,
                behandlendeEnhetProducer = behandlendeEnhetProducer,
            )
        }
        val client = createClient {
            install(ContentNegotiation) {
                jackson { configure() }
            }
        }
        return client
    }

    @BeforeEach
    fun beforeEach() {
        clearMocks(kafkaProducerMock)
    }

    @AfterEach
    fun afterEach() {
        database.dropData()
    }

    @Nested
    @DisplayName("Get BehandlendeEnhet for PersonIdent as veileder")
    inner class GetBehandlendeEnhetForPersonIdent {

        @Nested
        @DisplayName("Happy path")
        inner class HappyPath {

            @Test
            fun `Get BehandlendeEnhet for PersonIdent as NAVIdent`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {
                        bearerAuth(validToken)
                        header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                    }

                    assertEquals(HttpStatusCode.OK, response.status)
                    val behandlendeEnhet = response.body<BehandlendeEnhetResponseDTO>()

                    assertEquals("0101", behandlendeEnhet.geografiskEnhet.enhetId)
                    assertEquals("Enhet", behandlendeEnhet.geografiskEnhet.navn)
                    assertNull(behandlendeEnhet.oppfolgingsenhetDTO?.enhet)
                    assertNull(behandlendeEnhet.oppfolgingsenhetDTO?.createdAt)
                    assertNull(behandlendeEnhet.oppfolgingsenhetDTO?.veilederident)
                }
            }

            @Test
            fun `Get BehandlendeEnhet when oppfolgingsenhet has been set`() {
                testApplication {
                    repository.createOppfolgingsenhet(
                        personIdent = ARBEIDSTAKER_PERSONIDENT,
                        enhetId = EnhetId(UNDERORDNET_NR),
                        veilederident = VEILEDER_IDENT,
                    )
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {
                        bearerAuth(validToken)
                        header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                    }
                    assertEquals(HttpStatusCode.OK, response.status)
                    val behandlendeEnhet = response.body<BehandlendeEnhetResponseDTO>()

                    assertEquals("0101", behandlendeEnhet.geografiskEnhet.enhetId)
                    assertEquals("Enhet", behandlendeEnhet.geografiskEnhet.navn)
                    assertEquals(UNDERORDNET_NR, behandlendeEnhet.oppfolgingsenhetDTO?.enhet?.enhetId)
                    assertEquals("Enhet", behandlendeEnhet.oppfolgingsenhetDTO?.enhet?.navn)
                    assertEquals(LocalDate.now(), behandlendeEnhet.oppfolgingsenhetDTO?.createdAt?.toLocalDate())
                    assertEquals(VEILEDER_IDENT, behandlendeEnhet.oppfolgingsenhetDTO?.veilederident)
                }
            }

            @Test
            fun `should return NoContent if GeografiskTilknyning was not found for PersonIdent`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {
                        bearerAuth(validToken)
                        header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value)
                    }
                    assertEquals(HttpStatusCode.NoContent, response.status)
                }
            }
        }

        @Nested
        @DisplayName("Unhappy paths")
        inner class UnhappyPaths {

            @Test
            fun `should return status Unauthorized if no token is supplied`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {}
                    assertEquals(HttpStatusCode.Unauthorized, response.status)
                }
            }

            @Test
            fun `should return status BadRequest if no NAV_PERSONIDENT_HEADER is supplied`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {
                        bearerAuth(validToken)
                    }
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }
            }

            @Test
            fun `should return status BadRequest if NAV_PERSONIDENT_HEADER with invalid PersonIdent is supplied`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {
                        bearerAuth(validToken)
                        header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value.drop(1))
                    }
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                }
            }

            @Test
            fun `should return status Forbidden if NAVIdent is denied access`() {
                testApplication {
                    val validTokenNoAccess = generateJWT(
                        audience = externalMockEnvironment.environment.azureAppClientId,
                        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                        navIdent = VEILEDER_IDENT_NO_ACCESS,
                    )
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {
                        bearerAuth(validTokenNoAccess)
                        header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                    }
                    assertEquals(HttpStatusCode.Forbidden, response.status)
                }
            }
        }
    }

    @Test
    fun `Get mulige oppfolgingsenheter`() {
        testApplication {
            repository.createOppfolgingsenhet(
                personIdent = ARBEIDSTAKER_PERSONIDENT,
                enhetId = EnhetId(UNDERORDNET_NR),
                veilederident = VEILEDER_IDENT,
            )
            val client = setupApiAndClient()
            val response = client.get(tilordningsenheterUrl) {
                bearerAuth(validToken)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val behandlendeEnhetList = response.body<List<EnhetDTO>>()

            assertEquals(2, behandlendeEnhetList.size)
            assertEquals(EnhetId.ENHETNR_NAV_UTLAND, behandlendeEnhetList[0].enhetId)
            assertEquals(VEST_VIKEN_ROE_ID, behandlendeEnhetList[1].enhetId)
        }
    }

    @Nested
    @DisplayName("Get tildelthistorikk for PersonIdent")
    inner class GetTildelthistorikkForPersonIdent {

        @Test
        fun `Tildelt til annen enhet av veileder`() {
            testApplication {
                repository.createOppfolgingsenhet(
                    personIdent = ARBEIDSTAKER_PERSONIDENT,
                    enhetId = EnhetId(GEOGRAFISK_ENHET_NR),
                    veilederident = VEILEDER_IDENT,
                )

                val client = setupApiAndClient()
                val response = client.get(tildelthistorikkUrl) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val historikk = response.body<TildeltHistorikkResponseDTO>()

                val jsonStr = response.body<String>()
                val expectedType = """
                    "type":"TILDELT_ANNEN_ENHET_AV_VEILEDER"
                """.trimIndent()
                assertTrue(jsonStr.contains(expectedType))

                val tildelteOppfolgingsenheter = historikk.tildelteOppfolgingsenheter
                assertEquals(1, tildelteOppfolgingsenheter.size)

                val tildelt = tildelteOppfolgingsenheter[0] as Tildelt
                assertEquals(VEILEDER_IDENT, tildelt.veilederident)
                assertEquals(GEOGRAFISK_ENHET_NR, tildelt.enhet.enhetId)
                assertEquals(ENHET_NAVN, tildelt.enhet.navn)
            }
        }

        @Test
        fun `Tildelt tilbake av veileder`() {
            testApplication {
                repository.createOppfolgingsenhet(
                    personIdent = ARBEIDSTAKER_PERSONIDENT,
                    enhetId = null,
                    veilederident = VEILEDER_IDENT,
                )

                val client = setupApiAndClient()
                val response = client.get(tildelthistorikkUrl) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val historikk = response.body<TildeltHistorikkResponseDTO>()

                val jsonStr = response.body<String>()
                val expectedType = """
                    "type":"TILDELT_TILBAKE_TIL_GEOGRAFISK_ENHET_AV_VEILEDER"
                """.trimIndent()
                assertTrue(jsonStr.contains(expectedType))

                val tildelteOppfolgingsenheter = historikk.tildelteOppfolgingsenheter
                assertEquals(1, tildelteOppfolgingsenheter.size)

                val tildeltTilbake = tildelteOppfolgingsenheter[0] as TildeltTilbake
                assertEquals(VEILEDER_IDENT, tildeltTilbake.veilederident)
            }
        }

        @Test
        fun `Tildelt tilbake av systemet`() {
            testApplication {
                repository.createOppfolgingsenhet(
                    personIdent = ARBEIDSTAKER_PERSONIDENT,
                    enhetId = null,
                    veilederident = SYSTEM_USER_IDENT,
                )

                val client = setupApiAndClient()
                val response = client.get(tildelthistorikkUrl) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val historikk = response.body<TildeltHistorikkResponseDTO>()

                val jsonStr = response.body<String>()
                val expectedType = """
                    "type":"TILDELT_TILBAKE_TIL_GEOGRAFISK_ENHET_AV_SYSTEM"
                """.trimIndent()
                assertTrue(jsonStr.contains(expectedType))

                val tildelteOppfolgingsenheter = historikk.tildelteOppfolgingsenheter
                assertEquals(1, tildelteOppfolgingsenheter.size)

                val tildeltTilbakeSystem = tildelteOppfolgingsenheter[0] as TildeltTilbakeAvSystem
                assertEquals(SYSTEM_USER_IDENT, tildeltTilbakeSystem.veilederident)
            }
        }

        @Test
        fun `Historikk leveres sortert synkende på createdAt`() {
            testApplication {
                repository.createOppfolgingsenhet(
                    personIdent = ARBEIDSTAKER_PERSONIDENT,
                    enhetId = EnhetId(GEOGRAFISK_ENHET_NR),
                    veilederident = VEILEDER_IDENT,
                )

                repository.createOppfolgingsenhet(
                    personIdent = ARBEIDSTAKER_PERSONIDENT,
                    enhetId = null,
                    veilederident = VEILEDER_IDENT,
                )

                repository.createOppfolgingsenhet(
                    personIdent = ARBEIDSTAKER_PERSONIDENT,
                    enhetId = null,
                    veilederident = SYSTEM_USER_IDENT,
                )

                val client = setupApiAndClient()
                val response = client.get(tildelthistorikkUrl) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                }

                assertEquals(HttpStatusCode.OK, response.status)
                val historikk = response.body<TildeltHistorikkResponseDTO>()

                val tildelteOppfolgingsenheter = historikk.tildelteOppfolgingsenheter
                assertEquals(3, tildelteOppfolgingsenheter.size)

                val tildeltTilbakeSystem = tildelteOppfolgingsenheter[0] as TildeltTilbakeAvSystem
                val tildeltTilbake = tildelteOppfolgingsenheter[1] as TildeltTilbake
                val tildelt = tildelteOppfolgingsenheter[2] as Tildelt

                assertTrue(tildeltTilbake.createdAt.isBefore(tildeltTilbakeSystem.createdAt))
                assertTrue(tildelt.createdAt.isBefore(tildeltTilbake.createdAt))
            }
        }
    }

    @Nested
    @DisplayName("Update oppfolgingsenhet - bulk endpoint")
    inner class UpdateOppfolgingsenhetBulkEndpoint {

        @Test
        fun `Updates multiple oppfolgingsenheter successfully`() {
            val requestDTO = generateTildelOppfolgingsenhetRequestDTO(
                personidenter = listOf(
                    ARBEIDSTAKER_PERSONIDENT.value,
                    ARBEIDSTAKER_PERSONIDENT_2.value,
                ),
                oppfolgingsenhet = ENHET_ID,
            )

            testApplication {
                val client = setupApiAndClient()
                val response = client.post(oppfolgingsenhetTildelingerUrl) {
                    bearerAuth(validToken)
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(requestDTO)
                }
                assertEquals(HttpStatusCode.OK, response.status)

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                assertEquals(2, responseDTO.tildelinger.size)
                assertEquals(0, responseDTO.errors.size)
                assertTrue(responseDTO.tildelinger.all { it.oppfolgingsenhet == ENHET_ID })

                verify(exactly = 2) { kafkaProducerMock.send(any()) }
            }
        }

        @Test
        fun `Updates multiple oppfolgingsenheter only for persons where veileder has tilgang`() {
            val requestDTO = generateTildelOppfolgingsenhetRequestDTO(
                personidenter = listOf(
                    ARBEIDSTAKER_PERSONIDENT.value,
                    ARBEIDSTAKER_ADRESSEBESKYTTET.value,
                ),
                oppfolgingsenhet = ENHET_ID,
            )

            testApplication {
                val client = setupApiAndClient()
                val response = client.post(oppfolgingsenhetTildelingerUrl) {
                    bearerAuth(validToken)
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(requestDTO)
                }
                assertEquals(HttpStatusCode.OK, response.status)

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                assertEquals(1, responseDTO.tildelinger.size)
                assertEquals(ENHET_ID, responseDTO.tildelinger.first().oppfolgingsenhet)
                assertEquals(ARBEIDSTAKER_PERSONIDENT.value, responseDTO.tildelinger.first().personident)
                assertEquals(1, responseDTO.errors.size)
                assertEquals(ARBEIDSTAKER_ADRESSEBESKYTTET.value, responseDTO.errors.first().personident)
                assertEquals(403, responseDTO.errors.first().errorCode)

                verify(exactly = 1) { kafkaProducerMock.send(any()) }
            }
        }

        @Test
        fun `Updates nothing when veileder does not have tilgang`() {
            val requestDTO = generateTildelOppfolgingsenhetRequestDTO(
                personidenter = listOf(ARBEIDSTAKER_ADRESSEBESKYTTET.value),
                oppfolgingsenhet = ENHET_ID,
            )

            testApplication {
                val client = setupApiAndClient()
                val response = client.post(oppfolgingsenhetTildelingerUrl) {
                    bearerAuth(validToken)
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(requestDTO)
                }
                assertEquals(HttpStatusCode.Forbidden, response.status)

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                assertEquals(0, responseDTO.tildelinger.size)
                assertEquals(1, responseDTO.errors.size)
                assertEquals(ARBEIDSTAKER_ADRESSEBESKYTTET.value, responseDTO.errors.first().personident)

                verify(exactly = 0) { kafkaProducerMock.send(any()) }
            }
        }

        @Test
        fun `Updates multiple oppfolgingsenheter when one already has an oppfolgingsenhet`() {
            repository.createOppfolgingsenhet(
                personIdent = PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT_2.value),
                enhetId = EnhetId(OTHER_ENHET_ID),
                veilederident = VEILEDER_IDENT,
            )
            val requestDTO = generateTildelOppfolgingsenhetRequestDTO(
                personidenter = listOf(
                    ARBEIDSTAKER_PERSONIDENT.value,
                    ARBEIDSTAKER_PERSONIDENT_2.value,
                ),
                oppfolgingsenhet = ENHET_ID,
            )

            testApplication {
                val client = setupApiAndClient()
                val response = client.post(oppfolgingsenhetTildelingerUrl) {
                    bearerAuth(validToken)
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(requestDTO)
                }
                assertEquals(HttpStatusCode.OK, response.status)

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                assertEquals(2, responseDTO.tildelinger.size)
                assertEquals(0, responseDTO.errors.size)
                assertTrue(responseDTO.tildelinger.all { it.oppfolgingsenhet == ENHET_ID })

                verify(exactly = 2) { kafkaProducerMock.send(any()) }
            }
        }

        @Test
        fun `Updates multiple oppfolgingsenheter when one of them is moved back to its geografiske enhet`() {
            repository.createOppfolgingsenhet(
                personIdent = PersonIdentNumber(ARBEIDSTAKER_PERSONIDENT_3.value),
                enhetId = EnhetId(GEOGRAFISK_ENHET_NR), // Simulerer at den allerede har en oppfølgingsenhet-overstyring for enheten som flyttes fra
                veilederident = VEILEDER_IDENT,
            )
            val requestDTO = generateTildelOppfolgingsenhetRequestDTO(
                personidenter = listOf(
                    ARBEIDSTAKER_PERSONIDENT.value,
                    ARBEIDSTAKER_PERSONIDENT_3.value, // Har geografisk enhet tilsvarende den man skal flyte til
                ),
                oppfolgingsenhet = ENHET_ID,
            )

            testApplication {
                val client = setupApiAndClient()
                val response = client.post(oppfolgingsenhetTildelingerUrl) {
                    bearerAuth(validToken)
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(requestDTO)
                }
                assertEquals(HttpStatusCode.OK, response.status)

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                assertEquals(2, responseDTO.tildelinger.size)
                assertEquals(0, responseDTO.errors.size)
                val firstPerson = responseDTO.tildelinger.first { it.personident == ARBEIDSTAKER_PERSONIDENT.value }
                val otherPerson = responseDTO.tildelinger.first { it.personident == ARBEIDSTAKER_PERSONIDENT_3.value }
                assertEquals(ENHET_ID, firstPerson.oppfolgingsenhet)
                assertNull(otherPerson.oppfolgingsenhet)

                verify(exactly = 2) { kafkaProducerMock.send(any()) }
            }
        }

        @Test
        fun `Updates multiple oppfolgingsenheter even though one of them fails`() {
            val requestDTO = generateTildelOppfolgingsenhetRequestDTO(
                personidenter = listOf(
                    ARBEIDSTAKER_PERSONIDENT.value,
                    ARBEIDSTAKER_PERSONIDENT_2.value,
                    ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value, // Vil feile på kall til PDL
                ),
                oppfolgingsenhet = ENHET_ID,
            )

            testApplication {
                val client = setupApiAndClient()
                val response = client.post(oppfolgingsenhetTildelingerUrl) {
                    bearerAuth(validToken)
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(requestDTO)
                }
                assertEquals(HttpStatusCode.OK, response.status)

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                val oppfolgingsenhetPerson1 = repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_PERSONIDENT)
                val oppfolgingsenhetPerson2 = repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_PERSONIDENT_2)
                val oppfolgingsenhetPerson3 =
                    repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND)
                assertEquals(ENHET_ID, oppfolgingsenhetPerson1?.oppfolgingsenhet)
                assertEquals(ENHET_ID, oppfolgingsenhetPerson2?.oppfolgingsenhet)
                assertNull(oppfolgingsenhetPerson3)

                assertEquals(2, responseDTO.tildelinger.size)
                assertTrue(responseDTO.tildelinger.all { it.oppfolgingsenhet == ENHET_ID })
                assertEquals(1, responseDTO.errors.size)
                assertEquals(
                    ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value,
                    responseDTO.errors.first().personident
                )

                verify(exactly = 2) { kafkaProducerMock.send(any()) }
            }
        }

        @Test
        fun `Returns error if all of them fails`() {
            val requestDTO = generateTildelOppfolgingsenhetRequestDTO(
                personidenter = listOf(
                    ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value, // Vil feile på kall til PDL
                    ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND_2.value, // Vil feile på kall til PDL
                ),
                oppfolgingsenhet = ENHET_ID,
            )

            testApplication {
                val client = setupApiAndClient()
                val response = client.post(oppfolgingsenhetTildelingerUrl) {
                    bearerAuth(validToken)
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(requestDTO)
                }
                assertEquals(HttpStatusCode.InternalServerError, response.status)

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                assertEquals(0, responseDTO.tildelinger.size)
                assertEquals(2, responseDTO.errors.size)

                verify(exactly = 0) { kafkaProducerMock.send(any()) }
            }
        }

        @Test
        fun `Updates oppfolgingsenheter even though one of them fails, and one without access`() {
            val requestDTO = generateTildelOppfolgingsenhetRequestDTO(
                personidenter = listOf(
                    ARBEIDSTAKER_PERSONIDENT.value,
                    ARBEIDSTAKER_ADRESSEBESKYTTET.value,
                    ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value, // Vil feile på kall til PDL
                ),
                oppfolgingsenhet = ENHET_ID,
            )

            testApplication {
                val client = setupApiAndClient()
                val response = client.post(oppfolgingsenhetTildelingerUrl) {
                    bearerAuth(validToken)
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(requestDTO)
                }
                assertEquals(HttpStatusCode.OK, response.status)

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                val oppfolgingsenhetPerson1 = repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_PERSONIDENT)
                val oppfolgingsenhetPerson2 = repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_ADRESSEBESKYTTET)
                val oppfolgingsenhetPerson3 =
                    repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND)
                assertEquals(ENHET_ID, oppfolgingsenhetPerson1?.oppfolgingsenhet)
                assertNull(oppfolgingsenhetPerson2)
                assertNull(oppfolgingsenhetPerson3)

                assertEquals(1, responseDTO.tildelinger.size)
                assertEquals(ENHET_ID, responseDTO.tildelinger.first().oppfolgingsenhet)
                assertEquals(2, responseDTO.errors.size)
                assertNotNull(responseDTO.errors.find { it.personident == ARBEIDSTAKER_ADRESSEBESKYTTET.value })
                assertNotNull(responseDTO.errors.find { it.personident == ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value })

                verify(exactly = 1) { kafkaProducerMock.send(any()) }
            }
        }

        @Nested
        @DisplayName("Unhappy path")
        inner class UnhappyPath {
            private val requestDTO = generateTildelOppfolgingsenhetRequestDTO(
                personidenter = listOf(ARBEIDSTAKER_PERSONIDENT.value),
                oppfolgingsenhet = ENHET_ID,
            )

            @Test
            fun `should return status Unauthorized if no token is supplied`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.post(oppfolgingsenhetTildelingerUrl) {
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(requestDTO)
                    }
                    assertEquals(HttpStatusCode.Unauthorized, response.status)
                }
            }

            @Test
            fun `should return status BadRequest if kode 6 or 7`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.post(oppfolgingsenhetTildelingerUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(requestDTO.copy(personidenter = listOf(ARBEIDSTAKER_ADRESSEBESKYTTET.value)))
                    }
                    assertEquals(HttpStatusCode.Forbidden, response.status)
                }
            }

            @Test
            fun `should return status BadRequest if egen ansatt`() {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.post(oppfolgingsenhetTildelingerUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(requestDTO.copy(personidenter = listOf(ARBEIDSTAKER_EGENANSATT.value)))
                    }
                    assertEquals(HttpStatusCode.Forbidden, response.status)
                }
            }
        }
    }
}

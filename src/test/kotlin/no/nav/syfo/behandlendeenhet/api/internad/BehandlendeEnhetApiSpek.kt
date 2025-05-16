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
import no.nav.syfo.behandlendeenhet.api.BehandlendeEnhetResponseDTO
import no.nav.syfo.behandlendeenhet.api.EnhetDTO
import no.nav.syfo.behandlendeenhet.api.TildelOppfolgingsenhetResponseDTO
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.behandlendeenhet.kafka.KBehandlendeEnhetUpdate
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.infrastructure.database.repository.EnhetRepository
import no.nav.syfo.testhelper.*
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
import no.nav.syfo.testhelper.generator.generateTildelOppfolgingsenhetRequestDTO
import no.nav.syfo.testhelper.mock.GEOGRAFISK_ENHET_NR
import no.nav.syfo.testhelper.mock.GEOGRAFISK_ENHET_NR_2
import no.nav.syfo.testhelper.mock.UNDERORDNET_NR
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import no.nav.syfo.util.configure
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.apache.kafka.clients.producer.KafkaProducer
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDate

class BehandlendeEnhetApiSpek : Spek({
    val externalMockEnvironment = ExternalMockEnvironment.instance
    val database = externalMockEnvironment.database
    val repository = EnhetRepository(database)

    val kafkaProducerMock = mockk<KafkaProducer<String, KBehandlendeEnhetUpdate>>(relaxed = true)
    val behandlendeEnhetProducer = BehandlendeEnhetProducer(kafkaProducerMock)

    fun ApplicationTestBuilder.setupApiAndClient(): HttpClient {
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

    beforeEachTest {
        clearMocks(kafkaProducerMock)
    }

    afterEachTest {
        database.dropData()
    }

    val behandlendeEnhetUrl = "$internadBehandlendeEnhetApiV2BasePath$internadBehandlendeEnhetApiV2PersonIdentPath"
    val oppfolgingsenhetTildelingerUrl = "$internadBehandlendeEnhetApiV2BasePath/oppfolgingsenhet-tildelinger"
    val tilordningsenheterUrl = "$internadBehandlendeEnhetApiV2BasePath$internadBehandlendeEnhetApiV2TilordningsenheterPath".replace("{$ENHET_ID_PARAM}", GEOGRAFISK_ENHET_NR)
    val validToken = generateJWT(
        audience = externalMockEnvironment.environment.azureAppClientId,
        issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
        navIdent = VEILEDER_IDENT,
    )

    describe("Get BehandlendeEnhet for PersonIdent as veileder") {
        describe("Happy path") {
            it("Get BehandlendeEnhet for PersonIdent as NAVIdent") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {
                        bearerAuth(validToken)
                        header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.OK
                    val behandlendeEnhet = response.body<BehandlendeEnhetResponseDTO>()

                    behandlendeEnhet.geografiskEnhet.enhetId shouldBeEqualTo "0101"
                    behandlendeEnhet.geografiskEnhet.navn shouldBeEqualTo "Enhet"
                    behandlendeEnhet.oppfolgingsenhetDTO?.enhet shouldBe null
                    behandlendeEnhet.oppfolgingsenhetDTO?.createdAt shouldBe null
                    behandlendeEnhet.oppfolgingsenhetDTO?.veilederident shouldBe null
                }
            }
            it("Get BehandlendeEnhet when oppfolgingsenhet has been set") {
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
                    response.status shouldBeEqualTo HttpStatusCode.OK
                    val behandlendeEnhet = response.body<BehandlendeEnhetResponseDTO>()

                    behandlendeEnhet.geografiskEnhet.enhetId shouldBeEqualTo "0101"
                    behandlendeEnhet.geografiskEnhet.navn shouldBeEqualTo "Enhet"
                    behandlendeEnhet.oppfolgingsenhetDTO?.enhet?.enhetId shouldBeEqualTo UNDERORDNET_NR
                    behandlendeEnhet.oppfolgingsenhetDTO?.enhet?.navn shouldBeEqualTo "Enhet"
                    behandlendeEnhet.oppfolgingsenhetDTO?.createdAt?.toLocalDate() shouldBeEqualTo LocalDate.now()
                    behandlendeEnhet.oppfolgingsenhetDTO?.veilederident shouldBeEqualTo VEILEDER_IDENT
                }
            }

            it("should return NoContent if GeografiskTilknyning was not found for PersonIdent") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {
                        bearerAuth(validToken)
                        header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.NoContent
                }
            }
        }
        describe("Unhappy paths") {
            it("should return status Unauthorized if no token is supplied") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {}
                    response.status shouldBeEqualTo HttpStatusCode.Unauthorized
                }
            }

            it("should return status BadRequest if no $NAV_PERSONIDENT_HEADER is supplied") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {
                        bearerAuth(validToken)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.BadRequest
                }
            }

            it("should return status BadRequest if $NAV_PERSONIDENT_HEADER with invalid PersonIdent is supplied") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(behandlendeEnhetUrl) {
                        bearerAuth(validToken)
                        header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value.drop(1))
                    }
                    response.status shouldBeEqualTo HttpStatusCode.BadRequest
                }
            }

            it("should return status Forbidden if NAVIdent is denied access") {
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
                    response.status shouldBeEqualTo HttpStatusCode.Forbidden
                }
            }
        }
    }

    describe("Get mulige oppfolgingsenheter") {
        describe("Happy path") {
            it("Get mulige oppfolgingsenheter") {
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
                    response.status shouldBeEqualTo HttpStatusCode.OK
                    val behandlendeEnhetList = response.body<List<EnhetDTO>>()

                    behandlendeEnhetList.size shouldBeEqualTo 3
                    behandlendeEnhetList[0].enhetId shouldBeEqualTo EnhetId.ENHETNR_NAV_UTLAND
                    // UNDERORDNET_NR (0103) kommer foran GEOGRAFISK_ENHET_NR_2 (0102) fordi veileder har brukt 0103 en gang
                    behandlendeEnhetList[1].enhetId shouldBeEqualTo UNDERORDNET_NR
                    behandlendeEnhetList[2].enhetId shouldBeEqualTo GEOGRAFISK_ENHET_NR_2
                }
            }
        }
    }

    describe("Update oppfolgingsenhet - bulk endpoint") {
        it("Updates multiple oppfolgingsenheter successfully") {
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
                response.status shouldBeEqualTo HttpStatusCode.OK

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                responseDTO.tildelinger.size shouldBeEqualTo 2
                responseDTO.errors.size shouldBeEqualTo 0
                responseDTO.tildelinger.all { it.oppfolgingsenhet == ENHET_ID } shouldBeEqualTo true

                verify(exactly = 2) { kafkaProducerMock.send(any()) }
            }
        }

        it("Updates multiple oppfolgingsenheter only for persons where veileder has tilgang") {
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
                response.status shouldBeEqualTo HttpStatusCode.OK

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                responseDTO.tildelinger.size shouldBeEqualTo 1
                responseDTO.tildelinger.first().oppfolgingsenhet shouldBeEqualTo ENHET_ID
                responseDTO.tildelinger.first().personident shouldBeEqualTo ARBEIDSTAKER_PERSONIDENT.value
                responseDTO.errors.size shouldBeEqualTo 1
                responseDTO.errors.first().personident shouldBeEqualTo ARBEIDSTAKER_ADRESSEBESKYTTET.value
                responseDTO.errors.first().errorCode shouldBeEqualTo 403

                verify(exactly = 1) { kafkaProducerMock.send(any()) }
            }
        }

        it("Updates nothing when veileder does not have tilgang") {
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
                response.status shouldBeEqualTo HttpStatusCode.Forbidden

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                responseDTO.tildelinger.size shouldBeEqualTo 0
                responseDTO.errors.size shouldBeEqualTo 1
                responseDTO.errors.first().personident shouldBeEqualTo ARBEIDSTAKER_ADRESSEBESKYTTET.value

                verify(exactly = 0) { kafkaProducerMock.send(any()) }
            }
        }

        it("Updates multiple oppfolgingsenheter when one already has an oppfolgingsenhet") {
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
                response.status shouldBeEqualTo HttpStatusCode.OK

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                responseDTO.tildelinger.size shouldBeEqualTo 2
                responseDTO.errors.size shouldBeEqualTo 0
                responseDTO.tildelinger.all { it.oppfolgingsenhet == ENHET_ID } shouldBeEqualTo true

                verify(exactly = 2) { kafkaProducerMock.send(any()) }
            }
        }

        it("Updates multiple oppfolgingsenheter when one of them is moved back to its geografiske enhet") {
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
                response.status shouldBeEqualTo HttpStatusCode.OK

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                responseDTO.tildelinger.size shouldBeEqualTo 2
                responseDTO.errors.size shouldBeEqualTo 0
                val firstPerson = responseDTO.tildelinger.first { it.personident == ARBEIDSTAKER_PERSONIDENT.value }
                val otherPerson = responseDTO.tildelinger.first { it.personident == ARBEIDSTAKER_PERSONIDENT_3.value }
                firstPerson.oppfolgingsenhet shouldBeEqualTo ENHET_ID
                otherPerson.oppfolgingsenhet shouldBeEqualTo null

                verify(exactly = 2) { kafkaProducerMock.send(any()) }
            }
        }

        it("Updates multiple oppfolgingsenheter even though one of them fails") {
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
                response.status shouldBeEqualTo HttpStatusCode.OK

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                val oppfolgingsenhetPerson1 = repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_PERSONIDENT)
                val oppfolgingsenhetPerson2 = repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_PERSONIDENT_2)
                val oppfolgingsenhetPerson3 =
                    repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND)
                oppfolgingsenhetPerson1?.oppfolgingsenhet shouldBeEqualTo ENHET_ID
                oppfolgingsenhetPerson2?.oppfolgingsenhet shouldBeEqualTo ENHET_ID
                oppfolgingsenhetPerson3 shouldBeEqualTo null

                responseDTO.tildelinger.size shouldBeEqualTo 2
                responseDTO.tildelinger.all { it.oppfolgingsenhet == ENHET_ID } shouldBeEqualTo true
                responseDTO.errors.size shouldBeEqualTo 1
                responseDTO.errors.first().personident shouldBeEqualTo ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value

                verify(exactly = 2) { kafkaProducerMock.send(any()) }
            }
        }
        it("Returns error if all of them fails") {
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
                response.status shouldBeEqualTo HttpStatusCode.InternalServerError

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                responseDTO.tildelinger.size shouldBeEqualTo 0
                responseDTO.errors.size shouldBeEqualTo 2

                verify(exactly = 0) { kafkaProducerMock.send(any()) }
            }
        }

        it("Updates oppfolgingsenheter even though one of them fails, and one without access") {
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
                response.status shouldBeEqualTo HttpStatusCode.OK

                val responseDTO = response.body<TildelOppfolgingsenhetResponseDTO>()

                val oppfolgingsenhetPerson1 = repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_PERSONIDENT)
                val oppfolgingsenhetPerson2 = repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_ADRESSEBESKYTTET)
                val oppfolgingsenhetPerson3 = repository.getOppfolgingsenhetByPersonident(ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND)
                oppfolgingsenhetPerson1?.oppfolgingsenhet shouldBeEqualTo ENHET_ID
                oppfolgingsenhetPerson2 shouldBeEqualTo null
                oppfolgingsenhetPerson3 shouldBeEqualTo null

                responseDTO.tildelinger.size shouldBeEqualTo 1
                responseDTO.tildelinger.first().oppfolgingsenhet shouldBeEqualTo ENHET_ID
                responseDTO.errors.size shouldBeEqualTo 2
                responseDTO.errors.find { it.personident == ARBEIDSTAKER_ADRESSEBESKYTTET.value } shouldNotBe null
                responseDTO.errors.find { it.personident == ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value } shouldNotBe null

                verify(exactly = 1) { kafkaProducerMock.send(any()) }
            }
        }

        describe("Unhappy path") {
            val requestDTO = generateTildelOppfolgingsenhetRequestDTO(
                personidenter = listOf(ARBEIDSTAKER_PERSONIDENT.value),
                oppfolgingsenhet = ENHET_ID,
            )
            it("should return status Unauthorized if no token is supplied") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.post(oppfolgingsenhetTildelingerUrl) {
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(requestDTO)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.Unauthorized
                }
            }

            it("should return status BadRequest if kode 6/7") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.post(oppfolgingsenhetTildelingerUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(requestDTO.copy(personidenter = listOf(ARBEIDSTAKER_ADRESSEBESKYTTET.value)))
                    }
                    response.status shouldBeEqualTo HttpStatusCode.Forbidden
                }
            }
            it("should return status BadRequest if egen ansatt") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.post(oppfolgingsenhetTildelingerUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(requestDTO.copy(personidenter = listOf(ARBEIDSTAKER_EGENANSATT.value)))
                    }
                    response.status shouldBeEqualTo HttpStatusCode.Forbidden
                }
            }
        }
    }
})

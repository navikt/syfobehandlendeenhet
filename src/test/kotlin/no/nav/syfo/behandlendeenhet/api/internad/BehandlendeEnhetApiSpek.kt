package no.nav.syfo.behandlendeenhet.api.internad

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import io.mockk.*
import no.nav.syfo.behandlendeenhet.BehandlendeEnhet
import no.nav.syfo.behandlendeenhet.api.BehandlendeEnhetDTO
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.behandlendeenhet.kafka.KBehandlendeEnhetUpdate
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.infrastructure.database.repository.EnhetRepository
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_ADRESSEBESKYTTET
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_EGENANSATT
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.ENHET_ID
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_NO_ACCESS
import no.nav.syfo.testhelper.generator.generateBehandlendeEnhetDTO
import no.nav.syfo.testhelper.mock.ENHET_NR
import no.nav.syfo.testhelper.mock.UNDERORDNET_NR
import no.nav.syfo.testhelper.mock.norg2Response
import no.nav.syfo.testhelper.mock.norg2ResponseNavUtland
import no.nav.syfo.util.*
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeLessThan
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

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
    val personUrl = "$internadBehandlendeEnhetApiV2BasePath$internadBehandlendeEnhetApiV2PersonPath"
    val tilordningsenheterUrl = "$internadBehandlendeEnhetApiV2BasePath$internadBehandlendeEnhetApiV2TilordningsenheterPath".replace("{$ENHET_ID_PARAM}", "1234")
    val behandlendeEnhetDTO = generateBehandlendeEnhetDTO()
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
                    val behandlendeEnhet = response.body<BehandlendeEnhet>()

                    behandlendeEnhet.enhetId shouldBeEqualTo norg2Response.first().enhetNr
                    behandlendeEnhet.navn shouldBeEqualTo norg2Response.first().navn
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

            it("should send NavUtland behandlingstype if Person has entry in database") {
                testApplication {
                    val client = setupApiAndClient()
                    val responsePost = client.post(personUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(behandlendeEnhetDTO)
                    }
                    responsePost.status shouldBeEqualTo HttpStatusCode.OK
                    val response = client.get(behandlendeEnhetUrl) {
                        bearerAuth(validToken)
                        header(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.OK
                    val behandlendeEnhet = response.body<BehandlendeEnhet>()
                    behandlendeEnhet.enhetId shouldBeEqualTo norg2ResponseNavUtland.first().enhetNr
                    behandlendeEnhet.navn shouldBeEqualTo norg2ResponseNavUtland.first().navn
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

    describe("Update oppfolgingsenhet") {
        describe("Happy path") {
            it("should create oppfolgingsenhet in db") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.post(personUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(behandlendeEnhetDTO)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.OK
                    response.body<BehandlendeEnhetDTO>() shouldBeEqualTo behandlendeEnhetDTO

                    val oppfolgingsenhet = repository.getOppfolgingsenhetByPersonident(PersonIdentNumber(behandlendeEnhetDTO.personident))
                    oppfolgingsenhet?.enhet?.isNavUtland() shouldBeEqualTo true
                    oppfolgingsenhet?.personident?.value shouldBeEqualTo behandlendeEnhetDTO.personident

                    val kafkaRecordSlot = slot<ProducerRecord<String, KBehandlendeEnhetUpdate>>()
                    verify(exactly = 1) { kafkaProducerMock.send(capture(kafkaRecordSlot)) }
                    kafkaRecordSlot.captured.value().personident shouldBeEqualTo oppfolgingsenhet?.personident?.value
                    kafkaRecordSlot.captured.value().updatedAt shouldBeEqualTo oppfolgingsenhet?.createdAt
                }
            }
            it("should create oppfolgingsenhet other than Nav utland in db") {
                testApplication {
                    val client = setupApiAndClient()
                    val behandlendeEnhetDTO = BehandlendeEnhetDTO(
                        personident = ARBEIDSTAKER_PERSONIDENT.value,
                        isNavUtland = false,
                        oppfolgingsenhet = ENHET_ID,
                    )
                    val response = client.post(personUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(behandlendeEnhetDTO)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.OK
                    response.body<BehandlendeEnhetDTO>() shouldBeEqualTo behandlendeEnhetDTO

                    val oppfolgingsenhet =
                        repository.getOppfolgingsenhetByPersonident(PersonIdentNumber(behandlendeEnhetDTO.personident))
                    oppfolgingsenhet?.enhet?.isNavUtland() shouldBeEqualTo false
                    oppfolgingsenhet?.enhet?.value shouldBeEqualTo ENHET_ID
                    oppfolgingsenhet?.personident?.value shouldBeEqualTo ARBEIDSTAKER_PERSONIDENT.value

                    val kafkaRecordSlot = slot<ProducerRecord<String, KBehandlendeEnhetUpdate>>()
                    verify(exactly = 1) { kafkaProducerMock.send(capture(kafkaRecordSlot)) }
                    kafkaRecordSlot.captured.value().personident shouldBeEqualTo oppfolgingsenhet?.personident?.value
                    kafkaRecordSlot.captured.value().updatedAt shouldBeEqualTo oppfolgingsenhet?.createdAt
                }
            }
            it("should store null as oppfolgingsenhet if same as geografisk and current oppfolgingsenhet is not null") {
                testApplication {
                    val client = setupApiAndClient()
                    val behandlendeEnhetDTO = BehandlendeEnhetDTO(
                        personident = ARBEIDSTAKER_PERSONIDENT.value,
                        isNavUtland = false,
                        oppfolgingsenhet = ENHET_ID,
                    )
                    client.post(personUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(behandlendeEnhetDTO)
                    }
                    clearMocks(kafkaProducerMock)

                    val behandlendeEnhetUpdateDTO = BehandlendeEnhetDTO(
                        personident = ARBEIDSTAKER_PERSONIDENT.value,
                        isNavUtland = false,
                        oppfolgingsenhet = ENHET_NR, // norg2mock-value
                    )
                    val response = client.post(personUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(behandlendeEnhetUpdateDTO)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.OK

                    val oppfolgingsenhet = repository.getOppfolgingsenhetByPersonident(PersonIdentNumber(behandlendeEnhetDTO.personident))
                    oppfolgingsenhet?.enhet shouldBe null
                    oppfolgingsenhet?.personident?.value shouldBeEqualTo ARBEIDSTAKER_PERSONIDENT.value

                    val kafkaRecordSlot = slot<ProducerRecord<String, KBehandlendeEnhetUpdate>>()
                    verify(exactly = 1) { kafkaProducerMock.send(capture(kafkaRecordSlot)) }
                    kafkaRecordSlot.captured.value().personident shouldBeEqualTo oppfolgingsenhet?.personident?.value
                    kafkaRecordSlot.captured.value().updatedAt shouldBeEqualTo oppfolgingsenhet?.createdAt
                }
            }

            it("should update oppfolgingsenhet if already in db") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.post(personUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(behandlendeEnhetDTO)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.OK
                    val oppfolgingsenhet = repository.getOppfolgingsenhetByPersonident(PersonIdentNumber(behandlendeEnhetDTO.personident))
                    oppfolgingsenhet?.enhet?.isNavUtland() shouldBeEqualTo true

                    val updatePersonDTO = behandlendeEnhetDTO.copy(isNavUtland = false, oppfolgingsenhet = null)
                    val responsePost = client.post(personUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(updatePersonDTO)
                    }
                    responsePost.status shouldBeEqualTo HttpStatusCode.OK

                    val oppfolgingsenhetUpdate = repository.getOppfolgingsenhetByPersonident(PersonIdentNumber(updatePersonDTO.personident))
                    oppfolgingsenhetUpdate?.enhet shouldBe null

                    val kafkaRecordSlot = mutableListOf<ProducerRecord<String, KBehandlendeEnhetUpdate>>()
                    verify(exactly = 2) { kafkaProducerMock.send(capture(kafkaRecordSlot)) }
                    kafkaRecordSlot[0].value().personident shouldBeEqualTo oppfolgingsenhetUpdate?.personident?.value
                    kafkaRecordSlot[1].value().updatedAt shouldBeEqualTo oppfolgingsenhetUpdate?.createdAt
                    kafkaRecordSlot[0].value().updatedAt shouldBeLessThan kafkaRecordSlot[1].value().updatedAt
                }
            }
        }

        describe("Unhappy path") {
            it("should return status Unauthorized if no token is supplied") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.post(personUrl) {
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(behandlendeEnhetDTO)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.Unauthorized
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
                    val response = client.post(personUrl) {
                        bearerAuth(validTokenNoAccess)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(behandlendeEnhetDTO)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.Forbidden
                }
            }
            it("should return status BadRequest if kode 6/7") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.post(personUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(behandlendeEnhetDTO.copy(personident = ARBEIDSTAKER_ADRESSEBESKYTTET.value))
                    }
                    response.status shouldBeEqualTo HttpStatusCode.BadRequest
                }
            }
            it("should return status BadRequest if egen ansatt") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.post(personUrl) {
                        bearerAuth(validToken)
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(behandlendeEnhetDTO.copy(personident = ARBEIDSTAKER_EGENANSATT.value))
                    }
                    response.status shouldBeEqualTo HttpStatusCode.BadRequest
                }
            }
        }
    }
    describe("Get mulige oppfolgingsenheter") {
        describe("Happy path") {
            it("Get mulige oppfolgingsenheter") {
                testApplication {
                    val client = setupApiAndClient()
                    val response = client.get(tilordningsenheterUrl) {
                        bearerAuth(validToken)
                    }
                    response.status shouldBeEqualTo HttpStatusCode.OK
                    val behandlendeEnhetList = response.body<List<BehandlendeEnhet>>()

                    behandlendeEnhetList.size shouldBeEqualTo 2
                    behandlendeEnhetList[0].enhetId shouldBeEqualTo UNDERORDNET_NR
                    behandlendeEnhetList[1].enhetId shouldBeEqualTo ENHET_NR
                }
            }
        }
    }
})

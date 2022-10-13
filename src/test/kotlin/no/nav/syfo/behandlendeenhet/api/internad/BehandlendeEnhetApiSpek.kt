package no.nav.syfo.behandlendeenhet.api.internad

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.justRun
import io.mockk.mockk
import no.nav.syfo.behandlendeenhet.BehandlendeEnhet
import no.nav.syfo.behandlendeenhet.database.getPersonByIdent
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_NO_ACCESS
import no.nav.syfo.testhelper.generator.generatePersonDTO
import no.nav.syfo.util.*
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class BehandlendeEnhetApiSpek : Spek({
    val objectMapper: ObjectMapper = configuredJacksonMapper()

    describe(BehandlendeEnhetApiSpek::class.java.simpleName) {

        with(TestApplicationEngine()) {
            start()

            val externalMockEnvironment = ExternalMockEnvironment.instance
            val database = externalMockEnvironment.database

            val behandlendeEnhetProducer = mockk<BehandlendeEnhetProducer>()
            justRun { behandlendeEnhetProducer.updateBehandlendeEnhet(any()) }

            application.testApiModule(
                externalMockEnvironment = externalMockEnvironment,
                behandlendeEnhetProducer = behandlendeEnhetProducer,
            )

            afterEachTest {
                database.dropData()
            }

            val behandlendeEnhetUrl = "$internadBehandlendeEnhetApiV2BasePath$internadBehandlendeEnhetApiV2PersonIdentPath"
            val personUrl = "$internadBehandlendeEnhetApiV2BasePath$internadBehandlendeEnhetApiV2PersonPath"
            val personDTO = generatePersonDTO()
            val validToken = generateJWT(
                audience = externalMockEnvironment.environment.azureAppClientId,
                issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                navIdent = VEILEDER_IDENT,
            )

            describe("Get BehandlendeEnhet for PersonIdent as veileder") {
                describe("Happy path") {
                    it("Get BehandlendeEnhet for PersonIdent as NAVIdent") {
                        with(
                            handleRequest(HttpMethod.Get, behandlendeEnhetUrl) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val behandlendeEnhet: BehandlendeEnhet = objectMapper.readValue(response.content!!)

                            behandlendeEnhet.enhetId shouldBeEqualTo externalMockEnvironment.norg2Mock.norg2Response.first().enhetNr
                            behandlendeEnhet.navn shouldBeEqualTo externalMockEnvironment.norg2Mock.norg2Response.first().navn
                        }
                    }

                    it("should return NoContent if GeografiskTilknyning was not found for PersonIdent") {
                        with(
                            handleRequest(HttpMethod.Get, behandlendeEnhetUrl) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.NoContent
                        }
                    }

                    it("should send NavUtland behandlingstype if Person has entry in database") {
                        with(
                            handleRequest(HttpMethod.Post, personUrl) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                setBody(objectMapper.writeValueAsString(personDTO))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                        }

                        with(
                            handleRequest(HttpMethod.Get, behandlendeEnhetUrl) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            val behandlendeEnhet: BehandlendeEnhet = objectMapper.readValue(response.content!!)
                            behandlendeEnhet.enhetId shouldBeEqualTo externalMockEnvironment.norg2Mock.norg2ResponseNavUtland.first().enhetNr
                            behandlendeEnhet.navn shouldBeEqualTo externalMockEnvironment.norg2Mock.norg2ResponseNavUtland.first().navn
                        }
                    }
                }
                describe("Unhappy paths") {
                    it("should return status Unauthorized if no token is supplied") {
                        with(
                            handleRequest(HttpMethod.Get, behandlendeEnhetUrl) {}
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Unauthorized
                        }
                    }

                    it("should return status BadRequest if no $NAV_PERSONIDENT_HEADER is supplied") {
                        with(
                            handleRequest(HttpMethod.Get, behandlendeEnhetUrl) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.BadRequest
                        }
                    }

                    it("should return status BadRequest if $NAV_PERSONIDENT_HEADER with invalid PersonIdent is supplied") {
                        with(
                            handleRequest(HttpMethod.Get, behandlendeEnhetUrl) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value.drop(1))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.BadRequest
                        }
                    }

                    it("should return status Forbidden if NAVIdent is denied access") {
                        val validTokenNoAccess = generateJWT(
                            audience = externalMockEnvironment.environment.azureAppClientId,
                            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                            navIdent = VEILEDER_IDENT_NO_ACCESS,
                        )
                        with(
                            handleRequest(HttpMethod.Get, behandlendeEnhetUrl) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validTokenNoAccess))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Forbidden
                        }
                    }
                }
            }

            describe("Update Person") {
                describe("Happy path") {
                    it("should create new Person in db") {
                        with(
                            handleRequest(HttpMethod.Post, personUrl) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                setBody(objectMapper.writeValueAsString(personDTO))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                            response.content shouldBeEqualTo objectMapper.writeValueAsString(personDTO)
                        }
                        val pPerson = database.getPersonByIdent(PersonIdentNumber(personDTO.personident))
                        pPerson?.isNavUtland shouldBeEqualTo true
                        pPerson?.personident shouldBeEqualTo personDTO.personident
                    }

                    it("should update Person if already in db") {
                        with(
                            handleRequest(HttpMethod.Post, personUrl) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                setBody(objectMapper.writeValueAsString(personDTO))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                        }
                        val pPersonInsert = database.getPersonByIdent(PersonIdentNumber(personDTO.personident))
                        pPersonInsert?.isNavUtland shouldBeEqualTo true

                        val updatePersonDTO = personDTO.copy(isNavUtland = false)
                        with(
                            handleRequest(HttpMethod.Post, personUrl) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                setBody(objectMapper.writeValueAsString(updatePersonDTO))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.OK
                        }
                        val pPersonUpdate = database.getPersonByIdent(PersonIdentNumber(updatePersonDTO.personident))
                        pPersonUpdate?.isNavUtland shouldBeEqualTo false
                        pPersonUpdate?.id shouldBeEqualTo pPersonInsert?.id
                    }
                }

                describe("Unhappy path") {
                    it("should return status Unauthorized if no token is supplied") {
                        with(
                            handleRequest(HttpMethod.Post, personUrl) {}
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Unauthorized
                        }
                    }

                    it("should return status Forbidden if NAVIdent is denied access") {
                        val validTokenNoAccess = generateJWT(
                            audience = externalMockEnvironment.environment.azureAppClientId,
                            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                            navIdent = VEILEDER_IDENT_NO_ACCESS,
                        )
                        with(
                            handleRequest(HttpMethod.Post, personUrl) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validTokenNoAccess))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_PERSONIDENT.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Forbidden
                        }
                    }
                }
            }
        }
    }
})

package no.nav.syfo.behandlendeenhet.api.internad

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.syfo.behandlendeenhet.BehandlendeEnhet
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT
import no.nav.syfo.testhelper.UserConstants.VEILEDER_IDENT_NO_ACCESS
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

            application.testApiModule(
                externalMockEnvironment = externalMockEnvironment,
            )

            val url = "$internadBehandlendeEnhetApiV2BasePath$internadBehandlendeEnhetApiV2PersonIdentPath"

            describe("Get BehandlendeEnhet for PersonIdent as System") {
                val validToken = generateJWT(
                    audience = externalMockEnvironment.environment.azureAppClientId,
                    issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                    navIdent = VEILEDER_IDENT,
                )

                describe("Happy path") {
                    it("Get BehandlendeEnhet for PersonIdent as NAVIdent") {
                        with(
                            handleRequest(HttpMethod.Get, url) {
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
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.NoContent
                        }
                    }
                }
                describe("Unhappy paths") {
                    it("should return status Unauthorized if no token is supplied") {
                        with(
                            handleRequest(HttpMethod.Get, url) {}
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Unauthorized
                        }
                    }

                    it("should return status BadRequest if no $NAV_PERSONIDENT_HEADER is supplied") {
                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.BadRequest
                        }
                    }

                    it("should return status BadRequest if $NAV_PERSONIDENT_HEADER with invalid PersonIdent is supplied") {
                        with(
                            handleRequest(HttpMethod.Get, url) {
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
                            handleRequest(HttpMethod.Get, url) {
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

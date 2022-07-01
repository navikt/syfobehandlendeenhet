package no.nav.syfo.behandlendeenhet.api.system

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.syfo.behandlendeenhet.BehandlendeEnhet
import no.nav.syfo.testhelper.*
import no.nav.syfo.util.*
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class BehandlendeEnhetSystemApiSpek : Spek({
    val objectMapper: ObjectMapper = configuredJacksonMapper()

    describe(BehandlendeEnhetSystemApiSpek::class.java.simpleName) {

        with(TestApplicationEngine()) {
            start()

            val externalMockEnvironment = ExternalMockEnvironment.instance

            application.testApiModule(
                externalMockEnvironment = externalMockEnvironment,
            )

            val url = "$systemBehandlendeEnhetApiV2BasePath$systemdBehandlendeEnhetApiV2PersonIdentPath"

            describe("Get BehandlendeEnhet for PersonIdent as System") {
                describe("Happy path") {
                    externalMockEnvironment.environment.systemAPIAuthorizedConsumerApplicationNameList.forEach { consumerApplicationName ->

                        val azp = testAzureAppPreAuthorizedApps.find { preAuthorizedClient ->
                            preAuthorizedClient.clientId.contains(consumerApplicationName)
                        }?.clientId ?: ""

                        val validToken = generateJWT(
                            audience = externalMockEnvironment.environment.azureAppClientId,
                            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                            azp = azp,
                        )

                        it("Get BehandlendeEnhet for PersonIdent as $consumerApplicationName") {
                            with(
                                handleRequest(HttpMethod.Get, url) {
                                    addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                    addHeader(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                                }
                            ) {
                                response.status() shouldBeEqualTo HttpStatusCode.OK
                                val behandlendeEnhet: BehandlendeEnhet = objectMapper.readValue(response.content!!)

                                behandlendeEnhet.enhetId shouldBeEqualTo externalMockEnvironment.norg2Mock.norg2Response.first().enhetNr
                                behandlendeEnhet.navn shouldBeEqualTo externalMockEnvironment.norg2Mock.norg2Response.first().navn
                            }
                        }

                        it("should return NoContent if GeografiskTilknyning was not found for PersonIdent as $consumerApplicationName") {
                            with(
                                handleRequest(HttpMethod.Get, url) {
                                    addHeader(HttpHeaders.Authorization, bearerHeader(validToken))
                                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                    addHeader(
                                        NAV_PERSONIDENT_HEADER,
                                        UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value
                                    )
                                }
                            ) {
                                response.status() shouldBeEqualTo HttpStatusCode.NoContent
                            }
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

                    it("should return status Forbidden if unauthorized $testIsdialogmoteClientId AZP is supplied") {
                        val validTokenUnauthorizedAZP = generateJWT(
                            audience = externalMockEnvironment.environment.azureAppClientId,
                            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                            azp = testIsdialogmoteClientId,
                        )

                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validTokenUnauthorizedAZP))
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.Forbidden
                        }
                    }

                    it("should return status Forbidden if unauthorized $testSyfomodiapersonClientId AZP is supplied") {
                        val validTokenUnauthorizedAZP = generateJWT(
                            audience = externalMockEnvironment.environment.azureAppClientId,
                            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                            azp = testSyfomodiapersonClientId,
                        )

                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(HttpHeaders.Authorization, bearerHeader(validTokenUnauthorizedAZP))
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

package no.nav.syfo.behandlendeenhet.api.system

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import io.mockk.justRun
import io.mockk.mockk
import no.nav.syfo.behandlendeenhet.api.BehandlendeEnhetResponseDTO
import no.nav.syfo.behandlendeenhet.kafka.BehandlendeEnhetProducer
import no.nav.syfo.domain.EnhetId
import no.nav.syfo.infrastructure.database.repository.EnhetRepository
import no.nav.syfo.testhelper.*
import no.nav.syfo.util.*
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class BehandlendeEnhetSystemApiSpek : Spek({
    describe(BehandlendeEnhetSystemApiSpek::class.java.simpleName) {
        val externalMockEnvironment = ExternalMockEnvironment.instance
        val repository = EnhetRepository(externalMockEnvironment.database)

        val behandlendeEnhetProducer = mockk<BehandlendeEnhetProducer>()
        justRun { behandlendeEnhetProducer.sendBehandlendeEnhetUpdate(any(), any()) }

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

        val url = "$systemBehandlendeEnhetApiV2BasePath$systemdBehandlendeEnhetApiV2PersonIdentPath"

        describe("Get/Post BehandlendeEnhet for PersonIdent as System") {
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
                        testApplication {
                            val client = setupApiAndClient()
                            val response = client.get(url) {
                                bearerAuth(validToken)
                                header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                            }
                            response.status shouldBeEqualTo HttpStatusCode.OK
                            val behandlendeEnhet = response.body<BehandlendeEnhetResponseDTO>()

                            behandlendeEnhet.geografiskEnhet.enhetId.value shouldBeEqualTo "0101"
                            behandlendeEnhet.geografiskEnhet.navn shouldBeEqualTo "Enhet"
                            behandlendeEnhet.oppfolgingsenhet.enhetId.value shouldBeEqualTo "0101"
                            behandlendeEnhet.oppfolgingsenhet.navn shouldBeEqualTo "Enhet"
                        }
                    }
                    it("Post BehandlendeEnhet for PersonIdent as $consumerApplicationName") {
                        testApplication {
                            val client = setupApiAndClient()
                            val responsePost = client.post(url) {
                                bearerAuth(validToken)
                                header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                            }
                            responsePost.status shouldBeEqualTo HttpStatusCode.OK

                            val response = client.get(url) {
                                bearerAuth(validToken)
                                header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                            }
                            response.status shouldBeEqualTo HttpStatusCode.OK
                            val behandlendeEnhet = response.body<BehandlendeEnhetResponseDTO>()
                            behandlendeEnhet.geografiskEnhet.enhetId.value shouldBeEqualTo "0101"
                            behandlendeEnhet.geografiskEnhet.navn shouldBeEqualTo "Enhet"
                            behandlendeEnhet.oppfolgingsenhet.enhetId.value shouldBeEqualTo "0101"
                        }
                    }
                    it("Post BehandlendeEnhet for PersonIdent which has oppfolgingsenhet as $consumerApplicationName") {
                        testApplication {
                            val client = setupApiAndClient()
                            repository.createOppfolgingsenhet(
                                personIdent = UserConstants.ARBEIDSTAKER_PERSONIDENT,
                                enhetId = EnhetId("0102"),
                                veilederident = UserConstants.VEILEDER_IDENT,
                            )
                            val responsePre = client.get(url) {
                                bearerAuth(validToken)
                                header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                            }
                            responsePre.status shouldBeEqualTo HttpStatusCode.OK
                            val behandlendeEnhetPre = responsePre.body<BehandlendeEnhetResponseDTO>()
                            behandlendeEnhetPre.oppfolgingsenhet.enhetId.value shouldBeEqualTo "0102"

                            val responsePost = client.post(url) {
                                bearerAuth(validToken)
                                header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                            }
                            responsePost.status shouldBeEqualTo HttpStatusCode.OK

                            val responseGet = client.get(url) {
                                bearerAuth(validToken)
                                header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                            }
                            responseGet.status shouldBeEqualTo HttpStatusCode.OK
                            val behandlendeEnhet = responseGet.body<BehandlendeEnhetResponseDTO>()
                            behandlendeEnhet.oppfolgingsenhet.enhetId.value shouldBeEqualTo "0101"
                        }
                    }

                    it("should return NoContent if GeografiskTilknyning was not found for PersonIdent as $consumerApplicationName") {
                        testApplication {
                            val client = setupApiAndClient()
                            val response = client.get(url) {
                                bearerAuth(validToken)
                                header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value)
                            }
                            response.status shouldBeEqualTo HttpStatusCode.NoContent
                        }
                    }
                }
            }
            describe("Unhappy paths") {
                it("should return status Unauthorized if no token is supplied") {
                    testApplication {
                        val client = setupApiAndClient()
                        val response = client.get(url) {}
                        response.status shouldBeEqualTo HttpStatusCode.Unauthorized
                    }
                }

                it("should return status Forbidden if unauthorized $testIsdialogmoteClientId AZP is supplied") {
                    testApplication {
                        val validTokenUnauthorizedAZP = generateJWT(
                            audience = externalMockEnvironment.environment.azureAppClientId,
                            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                            azp = testIsdialogmoteClientId,
                        )
                        val client = setupApiAndClient()
                        val response = client.get(url) {
                            bearerAuth(validTokenUnauthorizedAZP)
                            header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value)
                        }
                        response.status shouldBeEqualTo HttpStatusCode.Forbidden
                    }
                }

                it("should return status Forbidden if unauthorized $testSyfomodiapersonClientId AZP is supplied") {
                    testApplication {
                        val validTokenUnauthorizedAZP = generateJWT(
                            audience = externalMockEnvironment.environment.azureAppClientId,
                            issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                            azp = testSyfomodiapersonClientId,
                        )
                        val client = setupApiAndClient()
                        val response = client.get(url) {
                            bearerAuth(validTokenUnauthorizedAZP)
                            header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value)
                        }
                        response.status shouldBeEqualTo HttpStatusCode.Forbidden
                    }
                }
            }
        }
    }
})

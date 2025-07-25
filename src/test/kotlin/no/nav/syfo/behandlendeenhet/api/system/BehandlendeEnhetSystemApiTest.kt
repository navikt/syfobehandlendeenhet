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
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import no.nav.syfo.util.configure
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class BehandlendeEnhetSystemApiTest {

    private val externalMockEnvironment = ExternalMockEnvironment.instance
    private val repository = EnhetRepository(externalMockEnvironment.database)

    private val behandlendeEnhetProducer = mockk<BehandlendeEnhetProducer>()
    private val url = "$systemBehandlendeEnhetApiV2BasePath$systemdBehandlendeEnhetApiV2PersonIdentPath"

    init {
        justRun { behandlendeEnhetProducer.sendBehandlendeEnhetUpdate(any(), any()) }
    }

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

    class AuthorizedConsumerApplicationNameArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?> {
            return ExternalMockEnvironment.instance.environment.systemAPIAuthorizedConsumerApplicationNameList.map {
                Arguments.of(it)
            }.stream()
        }
    }

    @Nested
    @DisplayName("Happy path")
    inner class HappyPath {

        @ParameterizedTest(name = "Get BehandlendeEnhet for PersonIdent as {0}")
        @ArgumentsSource(AuthorizedConsumerApplicationNameArgumentsProvider::class)
        fun `Get BehandlendeEnhet for PersonIdent`(consumerApplicationName: String) {
            val azp = testAzureAppPreAuthorizedApps.find { preAuthorizedClient ->
                preAuthorizedClient.clientId.contains(consumerApplicationName)
            }?.clientId ?: ""

            val validToken = generateJWT(
                audience = externalMockEnvironment.environment.azureAppClientId,
                issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                azp = azp,
            )

            testApplication {
                val client = setupApiAndClient()
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val behandlendeEnhet = response.body<BehandlendeEnhetResponseDTO>()

                assertEquals("0101", behandlendeEnhet.geografiskEnhet.enhetId)
                assertEquals("Enhet", behandlendeEnhet.geografiskEnhet.navn)
            }
        }

        @ParameterizedTest(name = "Post BehandlendeEnhet for PersonIdent as {0}")
        @ArgumentsSource(AuthorizedConsumerApplicationNameArgumentsProvider::class)
        fun `Post BehandlendeEnhet for PersonIdent`(consumerApplicationName: String) {
            val azp = testAzureAppPreAuthorizedApps.find { preAuthorizedClient ->
                preAuthorizedClient.clientId.contains(consumerApplicationName)
            }?.clientId ?: ""

            val validToken = generateJWT(
                audience = externalMockEnvironment.environment.azureAppClientId,
                issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                azp = azp,
            )

            testApplication {
                val client = setupApiAndClient()
                val responsePost = client.post(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                }
                assertEquals(HttpStatusCode.OK, responsePost.status)

                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val behandlendeEnhet = response.body<BehandlendeEnhetResponseDTO>()
                assertEquals("0101", behandlendeEnhet.geografiskEnhet.enhetId)
                assertEquals("Enhet", behandlendeEnhet.geografiskEnhet.navn)
            }
        }

        @ParameterizedTest(name = "Post BehandlendeEnhet for PersonIdent which has oppfolgingsenhet as {0}")
        @ArgumentsSource(AuthorizedConsumerApplicationNameArgumentsProvider::class)
        fun `Post BehandlendeEnhet for PersonIdent which has oppfolgingsenhet`(
            consumerApplicationName: String
        ) {
            val azp = testAzureAppPreAuthorizedApps.find { preAuthorizedClient ->
                preAuthorizedClient.clientId.contains(consumerApplicationName)
            }?.clientId ?: ""

            val validToken = generateJWT(
                audience = externalMockEnvironment.environment.azureAppClientId,
                issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                azp = azp,
            )

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
                assertEquals(HttpStatusCode.OK, responsePre.status)
                val behandlendeEnhetPre = responsePre.body<BehandlendeEnhetResponseDTO>()

                val responsePost = client.post(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                }
                assertEquals(HttpStatusCode.OK, responsePost.status)

                val responseGet = client.get(url) {
                    bearerAuth(validToken)
                    header(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_PERSONIDENT.value)
                }
                assertEquals(HttpStatusCode.OK, responseGet.status)
                val behandlendeEnhet = responseGet.body<BehandlendeEnhetResponseDTO>()
            }
        }

        @ParameterizedTest(name = "should return NoContent if GeografiskTilknyning was not found for PersonIdent as {0}")
        @ArgumentsSource(AuthorizedConsumerApplicationNameArgumentsProvider::class)
        fun `should return NoContent if GeografiskTilknyning was not found for PersonIdent`(
            consumerApplicationName: String
        ) {
            val azp = testAzureAppPreAuthorizedApps.find { preAuthorizedClient ->
                preAuthorizedClient.clientId.contains(consumerApplicationName)
            }?.clientId ?: ""

            val validToken = generateJWT(
                audience = externalMockEnvironment.environment.azureAppClientId,
                issuer = externalMockEnvironment.wellKnownInternalAzureAD.issuer,
                azp = azp,
            )

            testApplication {
                val client = setupApiAndClient()
                val response = client.get(url) {
                    bearerAuth(validToken)
                    header(
                        NAV_PERSONIDENT_HEADER,
                        UserConstants.ARBEIDSTAKER_GEOGRAFISK_TILKNYTNING_NOT_FOUND.value
                    )
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
                val response = client.get(url) {}
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }
        }

        @Test
        fun `should return status Forbidden if unauthorized testIsdialogmoteClientId AZP is supplied`() {
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
                assertEquals(HttpStatusCode.Forbidden, response.status)
            }
        }

        @Test
        fun `should return status Forbidden if unauthorized testSyfomodiapersonClientId AZP is supplied`() {
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
                assertEquals(HttpStatusCode.Forbidden, response.status)
            }
        }
    }
}

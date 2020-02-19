package no.nav.syfo.controller

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.api.controllers.BehandlendeEnhetADController
import no.nav.syfo.consumers.TilgangConsumer.Companion.ACCESS_TO_SYFO_WITH_AZURE_PATH
import no.nav.syfo.oidc.OIDCIssuer.AZURE
import no.nav.syfo.testhelper.OidcTestHelper.clearOIDCContext
import no.nav.syfo.testhelper.OidcTestHelper.logInVeilederAD
import no.nav.syfo.testhelper.UserConstants.USER_FNR
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.testhelper.generateNorgEnhet
import no.nav.syfo.testhelper.mockAndExpectNorgArbeidsfordeling
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount.manyTimes
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder.fromHttpUrl
import java.text.ParseException
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@DirtiesContext
@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
class BehandlendeEnhetADControllerTest {

    @Value("\${norg2.url}")
    private lateinit var norg2Url: String

    @Value("\${tilgangskontrollapi.url}")
    private lateinit var tilgangskontrollUrl: String

    @Inject
    private lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    @Inject
    private lateinit var behandlendeEnhetADController: BehandlendeEnhetADController

    @Inject
    private lateinit var restTemplate: RestTemplate

    private lateinit var mockRestServiceServer: MockRestServiceServer

    @Before
    @Throws(ParseException::class)
    fun setup() {
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
        logInVeilederAD(oidcRequestContextHolder, VEILEDER_ID)
    }

    @After
    fun tearDown() {
        clearOIDCContext(oidcRequestContextHolder)
        mockRestServiceServer.reset()
    }

    @Test
    fun getBehandlendeEnhetHasAccess() {
        mockAccessToSYFO(OK)

        mockAndExpectNorgArbeidsfordeling(mockRestServiceServer, norg2Url, listOf(generateNorgEnhet()))

        val behandlendeEnhetResponse = behandlendeEnhetADController.getBehandlendeEnhet(USER_FNR)
        assertThat(behandlendeEnhetResponse.statusCode).isEqualTo(OK)
    }

    @Test(expected = ForbiddenException::class)
    fun getBehandlendeEnhetAccessForbidden() {
        mockAccessToSYFO(FORBIDDEN)

        behandlendeEnhetADController.getBehandlendeEnhet(USER_FNR)
    }

    @Test(expected = RuntimeException::class)
    fun getBehandlendeEnhetAccessServerError() {
        mockAccessToSYFO(INTERNAL_SERVER_ERROR)

        behandlendeEnhetADController.getBehandlendeEnhet(USER_FNR)
    }

    private fun mockAccessToSYFO(status: HttpStatus) {
        val uriString = fromHttpUrl(tilgangskontrollUrl)
                .path(ACCESS_TO_SYFO_WITH_AZURE_PATH)
                .toUriString()

        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(AZURE).idToken

        mockRestServiceServer.expect(manyTimes(), requestTo(uriString))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, "Bearer $idToken"))
                .andRespond(withStatus(status))
    }
}

package no.nav.syfo.behanlendeenhet

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.behandlendeenhet.api.system.v2.BehandlendeEnhetSystemControllerV2
import no.nav.syfo.config.CacheConfig
import no.nav.syfo.consumer.pdl.PdlConsumer
import no.nav.syfo.consumer.pdl.geografiskTilknytning
import no.nav.syfo.consumer.skjermedepersonerpip.getSkjermedePersonerPipUrl
import no.nav.syfo.domain.PersonIdentNumber
import no.nav.syfo.testhelper.*
import no.nav.syfo.testhelper.OidcTestHelper.clearOIDCContext
import no.nav.syfo.testhelper.UserConstants.USER_FNR
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cache.CacheManager
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.text.ParseException
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@DirtiesContext
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [LocalApplication::class])
class BehandlendeEnhetSystemControllerV2Test {

    @Value("\${azure.openid.config.token.endpoint}")
    private lateinit var azureTokenEndpoint: String

    @Value("\${norg2.url}")
    private lateinit var norg2Url: String

    @Value("\${tilgangskontrollapi.url}")
    private lateinit var tilgangskontrollUrl: String

    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @Inject
    private lateinit var oidcRequestContextHolder: TokenValidationContextHolder

    @Inject
    private lateinit var behandlendeEnhetSystemControllerV2: BehandlendeEnhetSystemControllerV2

    @Inject
    private lateinit var restTemplate: RestTemplate

    @Inject
    @Qualifier("restTemplateWithProxy")
    private lateinit var restTemplateWithProxy: RestTemplate

    @Inject
    private lateinit var cacheManager: CacheManager

    private lateinit var mockRestServiceServer: MockRestServiceServer
    private lateinit var mockRestServiceWithProxyServer: MockRestServiceServer

    @BeforeEach
    @Throws(ParseException::class)
    fun setup() {
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
        this.mockRestServiceWithProxyServer = MockRestServiceServer.bindTo(restTemplateWithProxy).build()

        val pdlResponse = generatePdlHentPerson()
        Mockito.`when`(pdlConsumer.person(PersonIdentNumber(USER_FNR))).thenReturn(pdlResponse)

        val pdlGTResponse = generatePdlHentGeografiskTilknytning()
        Mockito.`when`(pdlConsumer.geografiskTilknytningResponse(PersonIdentNumber(USER_FNR))).thenReturn(pdlGTResponse)
        Mockito.`when`(pdlConsumer.geografiskTilknytning(PersonIdentNumber(USER_FNR))).thenReturn(
            pdlGTResponse.geografiskTilknytning()
        )
    }

    @AfterEach
    fun tearDown() {
        clearOIDCContext(oidcRequestContextHolder)
        mockRestServiceServer.reset()
        mockRestServiceWithProxyServer.reset()
        cacheManager.getCache(CacheConfig.CACHENAME_BEHANDLENDEENHET)?.clear()
        cacheManager.getCache(CacheConfig.CACHENAME_EGENANSATT)?.clear()
    }

    @Test
    fun getBehandlendeEnhetHasAccessContent() {
        logInSystemConsumerClient(oidcRequestContextHolder, "syfo-tilgangskontroll-client-id")

        mockAndExpectSkjermedPersonerEgenAnsatt(mockRestServiceServer, getSkjermedePersonerPipUrl(USER_FNR), true)

        val norgEnhet = generateNorgEnhet().copy()
        mockAndExpectNorgArbeidsfordeling(mockRestServiceServer, norg2Url, listOf(norgEnhet))

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, USER_FNR)

        val behandlendeEnhetResponse = behandlendeEnhetSystemControllerV2.getBehandlendeEnhet(headers)
        assertThat(behandlendeEnhetResponse.statusCode).isEqualTo(OK)
        assertThat(behandlendeEnhetResponse.body!!.enhetId).isEqualTo(norgEnhet.enhetNr)
        assertThat(behandlendeEnhetResponse.body!!.navn).isEqualTo(norgEnhet.navn)
    }

    @Test
    fun getBehandlendeEnhetHasAccessContentAsIspersonoppgave() {
        logInSystemConsumerClient(oidcRequestContextHolder, "ispersonoppgave-client-id")

        mockAndExpectSkjermedPersonerEgenAnsatt(mockRestServiceServer, getSkjermedePersonerPipUrl(USER_FNR), true)

        val norgEnhet = generateNorgEnhet().copy()
        mockAndExpectNorgArbeidsfordeling(mockRestServiceServer, norg2Url, listOf(norgEnhet))

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, USER_FNR)

        val behandlendeEnhetResponse = behandlendeEnhetSystemControllerV2.getBehandlendeEnhet(headers)
        assertThat(behandlendeEnhetResponse.statusCode).isEqualTo(OK)
        assertThat(behandlendeEnhetResponse.body!!.enhetId).isEqualTo(norgEnhet.enhetNr)
        assertThat(behandlendeEnhetResponse.body!!.navn).isEqualTo(norgEnhet.navn)
    }

    @Test
    fun getBehandlendeEnhetHasAccessContentAsSyfomoteadmin() {
        logInSystemConsumerClient(oidcRequestContextHolder, "syfomoteadmin-client-id")

        mockAndExpectSkjermedPersonerEgenAnsatt(mockRestServiceServer, getSkjermedePersonerPipUrl(USER_FNR), true)

        val norgEnhet = generateNorgEnhet().copy()
        mockAndExpectNorgArbeidsfordeling(mockRestServiceServer, norg2Url, listOf(norgEnhet))

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, USER_FNR)

        val behandlendeEnhetResponse = behandlendeEnhetSystemControllerV2.getBehandlendeEnhet(headers)
        assertThat(behandlendeEnhetResponse.statusCode).isEqualTo(OK)
        assertThat(behandlendeEnhetResponse.body!!.enhetId).isEqualTo(norgEnhet.enhetNr)
        assertThat(behandlendeEnhetResponse.body!!.navn).isEqualTo(norgEnhet.navn)
    }

    @Test
    fun getBehandlendeEnhetHasAccessNoContent() {
        logInSystemConsumerClient(oidcRequestContextHolder, "syfo-tilgangskontroll-client-id")

        mockAndExpectSkjermedPersonerEgenAnsatt(mockRestServiceServer, getSkjermedePersonerPipUrl(USER_FNR), true)

        mockAndExpectNorgArbeidsfordeling(mockRestServiceServer, norg2Url, emptyList())

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, USER_FNR)

        val behandlendeEnhetResponse = behandlendeEnhetSystemControllerV2.getBehandlendeEnhet(headers)
        assertThat(behandlendeEnhetResponse.statusCode).isEqualTo(NO_CONTENT)
    }

    @Test
    fun getBehandlendeEnhetAccessForbidden() {
        logInSystemConsumerClient(oidcRequestContextHolder, "syfo-client-id")

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, USER_FNR)

        assertThrows<ForbiddenException> {
            behandlendeEnhetSystemControllerV2.getBehandlendeEnhet(headers)
        }
    }
}

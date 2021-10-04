package no.nav.syfo.azuread.v2

import no.nav.syfo.LocalApplication
import no.nav.syfo.config.CacheConfig
import no.nav.syfo.consumer.azuread.v2.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cache.CacheManager
import org.springframework.http.HttpEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.MultiValueMap
import java.text.ParseException
import java.time.LocalDateTime
import javax.annotation.Resource

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [LocalApplication::class])
class AzureSystemTokenCacheTest {

    @MockBean
    private lateinit var azureAdV2TokenInvoker: AzureADV2TokenInvoker

    @Autowired
    private lateinit var azureAdTokenConsumer: AzureAdV2TokenConsumer

    @Resource
    private lateinit var cacheManager: CacheManager

    private val pdlScopeClientId = "pdl"
    private val anotherScopeClientId = "another"
    private lateinit var requestEntityPDL: HttpEntity<MultiValueMap<String, String>>
    private lateinit var requestEntityAnother: HttpEntity<MultiValueMap<String, String>>

    @BeforeEach
    @Throws(ParseException::class)
    fun setup() {
        requestEntityPDL = azureAdTokenConsumer.systemTokenRequestEntity(pdlScopeClientId)
        requestEntityAnother = azureAdTokenConsumer.systemTokenRequestEntity(anotherScopeClientId)
    }

    @AfterEach
    fun tearDown() {
        cacheManager.getCache(CacheConfig.CACHENAME_TOKENS)?.clear()
    }

    @Test
    fun `token is cached`() {
        // Two thenReturn-statements to get different result on first and second invocation
        Mockito.`when`(azureAdV2TokenInvoker.getToken(requestEntityPDL)).thenReturn(
            AzureAdV2Token(
                accessToken = "first",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        ).thenReturn(
            AzureAdV2Token(
                accessToken = "second",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        )
        val token = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        Assertions.assertEquals(token, "first")
        val anotherToken = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        Assertions.assertEquals(anotherToken, "first")
    }

    @Test
    fun `expired token is renewed`() {
        // Two thenReturn-statements to get different result on first and second invocation
        Mockito.`when`(azureAdV2TokenInvoker.getToken(requestEntityPDL)).thenReturn(
            AzureAdV2Token(
                accessToken = "first",
                expires = LocalDateTime.now(),
            )
        ).thenReturn(
            AzureAdV2Token(
                accessToken = "second",
                expires = LocalDateTime.now(),
            )
        )
        val token = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        Assertions.assertEquals(token, "first")
        val anotherToken = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        Assertions.assertEquals(anotherToken, "second")
    }

    @Test
    fun `different scope ids get separate tokens`() {
        Mockito.`when`(azureAdV2TokenInvoker.getToken(requestEntityPDL)).thenReturn(
            AzureAdV2Token(
                accessToken = "firstPDL",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        ).thenReturn(
            AzureAdV2Token(
                accessToken = "secondPDL",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        )
        Mockito.`when`(azureAdV2TokenInvoker.getToken(requestEntityAnother)).thenReturn(
            AzureAdV2Token(
                accessToken = "firstAnother",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        ).thenReturn(
            AzureAdV2Token(
                accessToken = "secondAnother",
                expires = LocalDateTime.now().plusSeconds(3600),
            )
        )
        val tokenPDL = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        Assertions.assertEquals(tokenPDL, "firstPDL")

        val tokenAnother = azureAdTokenConsumer.getSystemToken(anotherScopeClientId)
        Assertions.assertEquals(tokenAnother, "firstAnother")

        val newTokenPDL = azureAdTokenConsumer.getSystemToken(pdlScopeClientId)
        Assertions.assertEquals(newTokenPDL, "firstPDL")

        val newTokenAnother = azureAdTokenConsumer.getSystemToken(anotherScopeClientId)
        Assertions.assertEquals(newTokenAnother, "firstAnother")
    }
}

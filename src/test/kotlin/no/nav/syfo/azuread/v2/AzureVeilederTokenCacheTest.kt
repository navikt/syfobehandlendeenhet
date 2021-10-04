package no.nav.syfo.azuread.v2

import no.nav.syfo.LocalApplication
import no.nav.syfo.config.CacheConfig
import no.nav.syfo.consumer.azuread.v2.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.MultiValueMap
import java.text.ParseException
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import javax.annotation.Resource

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [LocalApplication::class])
class AzureVeilederTokenCacheTest {

    @MockBean
    private lateinit var azureAdV2TokenInvoker: AzureADV2TokenInvoker

    @Autowired
    private lateinit var azureAdTokenConsumer: AzureAdV2TokenConsumer

    @Resource
    private lateinit var cacheManager: CacheManager

    private val veilederScopeClientId = "veileder"
    private val azureToken = "test-token"
    private val navIdent = "Z999999"
    private val azp = "syfomodiaperson"
    private lateinit var requestEntityVeileder: HttpEntity<MultiValueMap<String, String>>

    @BeforeEach
    @Throws(ParseException::class)
    fun setup() {
        requestEntityVeileder = azureAdTokenConsumer.onBehalfOfRequestEntity(
            scopeClientId = veilederScopeClientId,
            token = azureToken,
        )
    }

    @AfterEach
    fun tearDown() {
        cacheManager.getCache(CacheConfig.CACHENAME_TOKENS)?.clear()
    }

    @Test
    fun `token is cached`() {
        // Two thenReturn-statements to get different result on first and second invocation
        Mockito.`when`(azureAdV2TokenInvoker.getToken(requestEntityVeileder)).thenReturn(
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
        val oboToken = azureAdTokenConsumer.getOnBehalfOfToken(
            scopeClientId = veilederScopeClientId,
            token = azureToken,
            veilederId = navIdent,
            azp = azp,
        )
        assertEquals(oboToken, "first")
        val anotherOboToken = azureAdTokenConsumer.getOnBehalfOfToken(
            scopeClientId = veilederScopeClientId,
            token = azureToken,
            veilederId = navIdent,
            azp = azp,
        )
        assertEquals(anotherOboToken, "first")
    }

    @Test
    fun `expired token is renewed`() {
        // Two thenReturn-statements to get different result on first and second invocation
        Mockito.`when`(azureAdV2TokenInvoker.getToken(requestEntityVeileder)).thenReturn(
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
        val oboToken = azureAdTokenConsumer.getOnBehalfOfToken(
            scopeClientId = veilederScopeClientId,
            token = azureToken,
            veilederId = navIdent,
            azp = azp,
        )
        assertEquals(oboToken, "first")
        val anotherOboToken = azureAdTokenConsumer.getOnBehalfOfToken(
            scopeClientId = veilederScopeClientId,
            token = azureToken,
            veilederId = navIdent,
            azp = azp,
        )
        assertEquals(anotherOboToken, "second")
    }
}

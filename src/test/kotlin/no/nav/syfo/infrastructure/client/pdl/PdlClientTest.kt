package no.nav.syfo.infrastructure.client.pdl

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.application.EnhetService
import no.nav.syfo.application.IEnhetRepository
import no.nav.syfo.infrastructure.cache.ValkeyStore
import no.nav.syfo.infrastructure.client.azuread.AzureAdClient
import no.nav.syfo.infrastructure.client.azuread.AzureAdToken
import no.nav.syfo.infrastructure.client.commonConfig
import no.nav.syfo.infrastructure.client.norg.NorgClient
import no.nav.syfo.infrastructure.client.norg.domain.ArbeidsfordelingCriteria
import no.nav.syfo.infrastructure.client.pdl.domain.PdlGeografiskTilknytningRequest
import no.nav.syfo.infrastructure.client.skjermedepersonerpip.SkjermedePersonerPipClient
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_PERSONIDENT
import no.nav.syfo.testhelper.mock.generateNorgEnhet
import no.nav.syfo.testhelper.mock.generatePdlPersonResponse
import no.nav.syfo.testhelper.mock.receiveBody
import no.nav.syfo.testhelper.mock.respond
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PdlClientTest {
    @Test
    fun `fetches GT and addresses together and sends Svalbard to NORG2`() = runBlocking {
        var antallGtKall = 0
        var antallAdressebeskyttelseKall = 0
        var antallNorgKall = 0
        val valkeyStore = mockk<ValkeyStore>(relaxed = true)
        every { valkeyStore.get(any()) } returns null
        val azureAdClient = mockk<AzureAdClient>()
        coEvery { azureAdClient.getSystemToken(any()) } returns AzureAdToken("token", LocalDateTime.now().plusHours(1))
        val skjermedePersonerPipClient = mockk<SkjermedePersonerPipClient>()
        coEvery { skjermedePersonerPipClient.isSkjermet(any(), any(), any()) } returns false
        val repository = mockk<IEnhetRepository>()
        every { repository.getOppfolgingsenhetByPersonident(any()) } returns null

        HttpClient(MockEngine) {
            commonConfig()
            engine {
                addHandler { request ->
                    when (request.url.encodedPath) {
                        "/pdl" -> {
                            if (request.headers[PdlClient.GT_HEADER] == PdlClient.GT_HEADER) {
                                antallGtKall++
                                val body = request.receiveBody<PdlGeografiskTilknytningRequest>()
                                assertEquals(ARBEIDSTAKER_PERSONIDENT.value, body.variables.ident)
                                assertTrue(body.query.contains("hentGeografiskTilknytning(ident: \$ident)"))
                                assertTrue(body.query.contains("hentPerson(ident: \$ident)"))
                                assertTrue(body.query.contains("bostedsadresse(historikk: false)"))
                                assertTrue(body.query.contains("oppholdsadresse(historikk: false)"))
                                respond(
                                    """
                                    {
                                      "data": {
                                        "hentGeografiskTilknytning": {
                                          "gtType": "KOMMUNE",
                                          "gtKommune": "0301",
                                          "gtBydel": null,
                                          "gtLand": null
                                        },
                                        "hentPerson": {
                                          "bostedsadresse": [{
                                            "gyldigFraOgMed": "2026-01-01T08:00:00",
                                            "ukjentBosted": {"bostedskommune": "0301"}
                                          }],
                                          "oppholdsadresse": [{
                                            "gyldigFraOgMed": "2026-01-01T09:00:00",
                                            "oppholdAnnetSted": "paaSvalbard"
                                          }]
                                        }
                                      }
                                    }
                                    """.trimIndent(),
                                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                                )
                            } else {
                                antallAdressebeskyttelseKall++
                                respond(generatePdlPersonResponse())
                            }
                        }
                        NorgClient.ARBEIDSFORDELING_BESTMATCH_PATH -> {
                            antallNorgKall++
                            val criteria = request.receiveBody<ArbeidsfordelingCriteria>()
                            assertEquals(
                                ArbeidsfordelingCriteria(
                                    behandlingstype = "ae0257",
                                    tema = "OPP",
                                    geografiskOmraade = "2100",
                                    skjermet = false,
                                ),
                                criteria,
                            )
                            respond(listOf(generateNorgEnhet()))
                        }
                        else -> error("Unexpected request: ${request.url.encodedPath}")
                    }
                }
            }
        }.use { httpClient ->
            val service = EnhetService(
                norgClient = NorgClient("https://norg", valkeyStore, httpClient),
                pdlClient = PdlClient(azureAdClient, "https://pdl/pdl", "pdl-client-id", httpClient),
                valkeyStore = valkeyStore,
                skjermedePersonerPipClient = skjermedePersonerPipClient,
                repository = repository,
                behandlendeEnhetProducer = mockk(),
            )
            service.arbeidstakersBehandlendeEnhet("call-id", ARBEIDSTAKER_PERSONIDENT)
        }
        assertEquals(1, antallGtKall)
        assertEquals(1, antallAdressebeskyttelseKall)
        assertEquals(1, antallNorgKall)
    }
}

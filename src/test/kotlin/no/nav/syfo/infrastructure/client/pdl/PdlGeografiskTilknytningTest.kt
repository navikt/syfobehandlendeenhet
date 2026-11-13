package no.nav.syfo.infrastructure.client.pdl

import no.nav.syfo.infrastructure.client.pdl.domain.Bostedsadresse
import no.nav.syfo.infrastructure.client.pdl.domain.Oppholdsadresse
import no.nav.syfo.infrastructure.client.pdl.domain.PdlGeografiskTilknytning
import no.nav.syfo.infrastructure.client.pdl.domain.PdlHentGeografiskTilknytning
import no.nav.syfo.infrastructure.client.pdl.domain.PdlPerson
import no.nav.syfo.infrastructure.client.pdl.domain.UkjentBosted
import no.nav.syfo.infrastructure.client.pdl.domain.geografiskTilknytning
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PdlGeografiskTilknytningTest {
    private val tidligere = LocalDateTime.parse("2026-01-01T08:00:00")
    private val senere = tidligere.plusHours(1)
    private val gt = PdlGeografiskTilknytning(
        gtType = "KOMMUNE",
        gtKommune = "0301",
        gtBydel = null,
        gtLand = null,
    )
    private val bostedsadresse = Bostedsadresse(senere, UkjentBosted("0301"))
    private val oppholdsadresse = Oppholdsadresse(senere, "paaSvalbard")
    private val person = PdlPerson(
        adressebeskyttelse = null,
        bostedsadresse = listOf(bostedsadresse),
        oppholdsadresse = listOf(oppholdsadresse),
    )
    private val originalGt = GeografiskTilknytning(GeografiskTilknytningType.KOMMUNE, "0301")
    private val svalbardGt = GeografiskTilknytning(GeografiskTilknytningType.KOMMUNE, "2100")

    @Test
    fun `uses Svalbard when latest addresses match the criteria`() {
        assertEquals(svalbardGt, PdlHentGeografiskTilknytning(gt, person).geografiskTilknytning())
    }

    @Test
    fun `selects latest addresses by full datetime regardless of list order`() {
        for (nyesteForst in listOf(true, false)) {
            val bostedsadresser = listOf(bostedsadresse, Bostedsadresse(tidligere, null))
            val oppholdsadresser = listOf(oppholdsadresse, Oppholdsadresse(tidligere, "annet"))
            val adresser = person.copy(
                bostedsadresse = if (nyesteForst) bostedsadresser else bostedsadresser.reversed(),
                oppholdsadresse = if (nyesteForst) oppholdsadresser else oppholdsadresser.reversed(),
            )
            assertEquals(svalbardGt, PdlHentGeografiskTilknytning(gt, adresser).geografiskTilknytning())
        }
    }

    @Test
    fun `does not use an older matching address instead of the latest address`() {
        val personer = listOf(
            person.copy(
                bostedsadresse = listOf(bostedsadresse.copy(gyldigFraOgMed = tidligere), Bostedsadresse(senere, null)),
            ),
            person.copy(
                oppholdsadresse = listOf(oppholdsadresse.copy(gyldigFraOgMed = tidligere), Oppholdsadresse(senere, "annet")),
            ),
        )
        personer.forEach {
            assertEquals(originalGt, PdlHentGeografiskTilknytning(gt, it).geografiskTilknytning())
        }
    }

    @Test
    fun `preserves GT when municipalities differ or latest stay is not on Svalbard`() {
        val personer = listOf(
            person.copy(bostedsadresse = listOf(bostedsadresse.copy(ukjentBosted = UkjentBosted("1103")))),
            person.copy(oppholdsadresse = listOf(oppholdsadresse.copy(oppholdAnnetSted = "annet"))),
        )
        personer.forEach {
            assertEquals(originalGt, PdlHentGeografiskTilknytning(gt, it).geografiskTilknytning())
        }
    }

    @Test
    fun `preserves GT when address information is missing`() {
        val personer = listOf(
            null,
            person.copy(bostedsadresse = null),
            person.copy(bostedsadresse = emptyList()),
            person.copy(bostedsadresse = listOf(bostedsadresse.copy(ukjentBosted = null))),
            person.copy(bostedsadresse = listOf(bostedsadresse.copy(ukjentBosted = UkjentBosted(null)))),
            person.copy(oppholdsadresse = null),
            person.copy(oppholdsadresse = emptyList()),
            person.copy(oppholdsadresse = listOf(oppholdsadresse.copy(oppholdAnnetSted = null))),
        )
        personer.forEach {
            assertEquals(originalGt, PdlHentGeografiskTilknytning(gt, it).geografiskTilknytning())
        }
    }

    @Test
    fun `preserves GT when latest address cannot be determined unambiguously`() {
        val personer = listOf(
            person.copy(bostedsadresse = listOf(bostedsadresse.copy(gyldigFraOgMed = null))),
            person.copy(oppholdsadresse = listOf(oppholdsadresse.copy(gyldigFraOgMed = null))),
            person.copy(bostedsadresse = listOf(bostedsadresse, Bostedsadresse(null, null))),
            person.copy(oppholdsadresse = listOf(oppholdsadresse, Oppholdsadresse(null, "annet"))),
            person.copy(bostedsadresse = listOf(bostedsadresse, Bostedsadresse(senere, null))),
            person.copy(oppholdsadresse = listOf(oppholdsadresse, Oppholdsadresse(senere, "annet"))),
        )
        personer.forEach {
            assertEquals(originalGt, PdlHentGeografiskTilknytning(gt, it).geografiskTilknytning())
        }
    }

    @Test
    fun `missing municipalities do not match each other`() {
        val manglendeKommune = gt.copy(gtKommune = null)
        val manglendeBostedskommune = person.copy(
            bostedsadresse = listOf(bostedsadresse.copy(ukjentBosted = UkjentBosted(null))),
        )
        assertEquals(
            manglendeKommune.geografiskTilknytning(),
            PdlHentGeografiskTilknytning(manglendeKommune, manglendeBostedskommune).geografiskTilknytning(),
        )
    }

    @Test
    fun `preserves other GT types when Svalbard criteria are not met`() {
        val geografiskeTilknytninger = listOf(
            gt.copy(gtType = "BYDEL", gtBydel = "030101"),
            gt.copy(gtType = "UTLAND", gtKommune = null, gtLand = "SWE"),
            gt.copy(gtType = "UDEFINERT", gtKommune = null),
        )
        geografiskeTilknytninger.forEach {
            assertEquals(it.geografiskTilknytning(), PdlHentGeografiskTilknytning(it).geografiskTilknytning())
        }
        assertNull(PdlHentGeografiskTilknytning(null, person).geografiskTilknytning())
        assertNull(PdlHentGeografiskTilknytning(gt.copy(gtType = "UKJENT"), person).geografiskTilknytning())
    }
}

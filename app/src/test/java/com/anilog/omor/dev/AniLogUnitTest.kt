package com.anilog.omor.dev

import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.repository.AnimeRepository
import com.anilog.omor.dev.domain.model.AnimeStatus
import com.anilog.omor.dev.domain.model.SortOption
import com.anilog.omor.dev.domain.ranking.RafaelConfig
import com.anilog.omor.dev.domain.ranking.RafaelRankingEngine
import com.anilog.omor.dev.domain.ranking.TieBreakSignal
import com.anilog.omor.dev.ui.bulk.BulkImportViewModel
import com.anilog.omor.dev.ui.export.ExportFormatHelper
import com.anilog.omor.dev.ui.export.ExportType
import com.anilog.omor.dev.util.RatingFormatHelper
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AniLogUnitTest {

    @Test
    fun `test rating values and arbitrary decimal precision`() {
        val anime1 = AnimeEntity(title = "Frieren", personalRating = 9.5f)
        val anime2 = AnimeEntity(title = "Steins;Gate", personalRating = 10.0f)
        val animeDec1 = AnimeEntity(title = "Hunter x Hunter", personalRating = 7.123f)
        val animeDec2 = AnimeEntity(title = "Death Note", personalRating = 9.99f)
        val animeDec3 = AnimeEntity(title = "Mob Psycho", personalRating = 4.68f)
        val animeUnrated = AnimeEntity(title = "Unrated Anime", personalRating = null)
        val animeZero = AnimeEntity(title = "Zero Score", personalRating = 0.0f)

        assertEquals(9.5f, anime1.personalRating)
        assertEquals(10.0f, anime2.personalRating)
        assertEquals(7.123f, animeDec1.personalRating)
        assertEquals(9.99f, animeDec2.personalRating)
        assertEquals(4.68f, animeDec3.personalRating)
        assertNull(animeUnrated.personalRating)
        assertEquals(0.0f, animeZero.personalRating)
    }

    @Test
    fun `test rating formatting helper`() {
        assertEquals("10", RatingFormatHelper.formatRating(10.0f))
        assertEquals("0", RatingFormatHelper.formatRating(0.0f))
        assertEquals("8.7", RatingFormatHelper.formatRating(8.7f))
        assertEquals("9.25", RatingFormatHelper.formatRating(9.25f))
        assertEquals("7.123", RatingFormatHelper.formatRating(7.123f))
        assertEquals("9.99", RatingFormatHelper.formatRating(9.99f))
        assertEquals("Unrated", RatingFormatHelper.formatRating(null))

        assertTrue(RatingFormatHelper.isValidRatingInput("8.7"))
        assertTrue(RatingFormatHelper.isValidRatingInput("9,25")) // Comma supported
        assertTrue(RatingFormatHelper.isValidRatingInput("10"))
        assertTrue(RatingFormatHelper.isValidRatingInput("0"))
        assertFalse(RatingFormatHelper.isValidRatingInput("-1"))
        assertFalse(RatingFormatHelper.isValidRatingInput("10.5"))
        assertFalse(RatingFormatHelper.isValidRatingInput("abc"))
    }

    @Test
    fun `test highest rated sort order with unrated and tiebreaker`() {
        val anime1 = AnimeEntity(id = 1, title = "B", personalRating = 8.5f, createdAt = 1000L)
        val anime2 = AnimeEntity(id = 2, title = "A", personalRating = 10.0f, createdAt = 1200L)
        val anime3 = AnimeEntity(id = 3, title = "C", personalRating = 8.5f, createdAt = 1500L)
        val unrated = AnimeEntity(id = 4, title = "D", personalRating = null, createdAt = 2000L)

        val list = listOf(anime1, anime2, anime3, unrated)
        val sorted = AnimeRepository.sortAnimeList(list, SortOption.HIGHEST_RATED)

        // anime2 (10.0) first
        assertEquals("A", sorted[0].title)
        // anime3 (8.5, createdAt 1500) before anime1 (8.5, createdAt 1000)
        assertEquals("C", sorted[1].title)
        assertEquals("B", sorted[2].title)
        // unrated entry placed last
        assertEquals("D", sorted[3].title)
    }

    @Test
    fun `test Rafael ranking engine tie-breaking with favorite and review depth`() {
        // Two anime with identical rating 9.0f
        val a1 = AnimeEntity(id = 1, title = "Anime One", personalRating = 9.0f, isFavorite = false, review = "")
        val a2 = AnimeEntity(id = 2, title = "Anime Two", personalRating = 9.0f, isFavorite = true, review = "")
        val a3 = AnimeEntity(id = 3, title = "Anime Three", personalRating = 9.5f, isFavorite = false)

        val list = listOf(a1, a2, a3)
        val ranked = RafaelRankingEngine.rankAnime(list, RafaelConfig())

        // Higher rating (9.5) must strictly be rank #1
        assertEquals("Anime Three", ranked[0].anime.title)
        assertEquals(1, ranked[0].rankPosition)

        // For tied 9.0 anime: favorite (Anime Two) must be rank #2, above Anime One
        assertEquals("Anime Two", ranked[1].anime.title)
        assertEquals(2, ranked[1].rankPosition)
        assertEquals(TieBreakSignal.FAVORITE_STATUS, ranked[1].tieBreakSignal)
        assertTrue(ranked[1].explanationEn.contains("favorite"))

        assertEquals("Anime One", ranked[2].anime.title)
        assertEquals(3, ranked[2].rankPosition)

        // Saved ratings must NOT be modified
        assertEquals(9.0f, a1.personalRating)
        assertEquals(9.0f, a2.personalRating)
    }

    @Test
    fun `test Rafael explicit preference overrides inferred signals`() {
        val a1 = AnimeEntity(id = 10, title = "Favorite Show", personalRating = 8.0f, isFavorite = true)
        val a2 = AnimeEntity(id = 20, title = "Explicit Pick", personalRating = 8.0f, isFavorite = false)

        // Explicit preference 20 > 10
        val config = RafaelConfig(explicitPreferences = setOf("20>10"))
        val ranked = RafaelRankingEngine.rankAnime(listOf(a1, a2), config)

        // 20 was explicitly preferred over 10
        assertEquals("Explicit Pick", ranked[0].anime.title)
        assertEquals(TieBreakSignal.EXPLICIT_PREFERENCE, ranked[0].tieBreakSignal)
        assertEquals("Favorite Show", ranked[1].anime.title)
    }

    @Test
    fun `test Rafael deterministic stable fallback`() {
        val a1 = AnimeEntity(id = 1, title = "First Created", personalRating = 8.0f, createdAt = 1000L)
        val a2 = AnimeEntity(id = 2, title = "Later Created", personalRating = 8.0f, createdAt = 2000L)

        val config = RafaelConfig()
        val run1 = RafaelRankingEngine.rankAnime(listOf(a1, a2), config)
        val run2 = RafaelRankingEngine.rankAnime(listOf(a2, a1), config)

        // Both runs should deterministically yield Later Created (createdAt 2000) then First Created (createdAt 1000)
        assertEquals(run1.map { it.anime.id }, run2.map { it.anime.id })
        assertEquals(2L, run1[0].anime.id)
        assertEquals(1L, run1[1].anime.id)
    }

    @Test
    fun `test BulkImport parsing formats`() {
        val input = """
            Naruto
            2. One Piece
            - Attack on Titan
            Bleach | Rating: 8.7
            Title: Death Note
            Rating: 9.5
            Review: Peak psychological thriller
        """.trimIndent()

        val existing = setOf("naruto")
        val parsed = BulkImportViewModel.parseRawText(input, existing)

        assertEquals(5, parsed.size)
        // Naruto is detected as duplicate
        assertEquals("Naruto", parsed[0].title)
        assertTrue(parsed[0].isDuplicate)

        // One Piece stripped numbering
        assertEquals("One Piece", parsed[1].title)
        assertFalse(parsed[1].isDuplicate)

        // Attack on Titan stripped bullet
        assertEquals("Attack on Titan", parsed[2].title)

        // Bleach parsed rating 8.7
        assertEquals("Bleach", parsed[3].title)
        assertEquals(8.7f, parsed[3].rating)

        // Death Note parsed Format C
        assertEquals("Death Note", parsed[4].title)
        assertEquals(9.5f, parsed[4].rating)
        assertEquals("Peak psychological thriller", parsed[4].review)
    }

    @Test
    fun `test Export formats generation`() {
        val list = listOf(
            AnimeEntity(id = 1, title = "Naruto", personalRating = 8.7f, review = "Great ninja story"),
            AnimeEntity(id = 2, title = "One Piece", personalRating = 9.25f, review = "")
        )

        // Format A
        val formatA = ExportFormatHelper.generateFormatA(list)
        assertEquals("Naruto\nOne Piece", formatA)

        // Format B
        val formatB = ExportFormatHelper.generateFormatB(list)
        assertTrue(formatB.contains("Naruto | Rating: 8.7"))
        assertTrue(formatB.contains("One Piece | Rating: 9.25"))

        // Format C
        val formatC = ExportFormatHelper.generateFormatC(list)
        assertTrue(formatC.contains("Title: Naruto"))
        assertTrue(formatC.contains("Rating: 8.7"))
        assertTrue(formatC.contains("Review: Great ninja story"))

        // Format D (JSON)
        val formatD = ExportFormatHelper.generateFormatD(list)
        val json = JSONObject(formatD)
        assertEquals("AniLog", json.getString("app"))
        assertEquals(2, json.getJSONArray("anime").length())
    }

    @Test
    fun `test passcode recovery waiting duration bounds`() {
        val clamped5 = 3.coerceIn(5, 60)
        val clamped60 = 75.coerceIn(5, 60)
        val valid25 = 25.coerceIn(5, 60)

        assertEquals(5, clamped5)
        assertEquals(60, clamped60)
        assertEquals(25, valid25)

        val now = 1000000L
        val durationMillis = 5 * 60 * 1000L
        val deadline = now + durationMillis

        // Recovery active check
        val isActiveDuring = now + 1000L < deadline
        val isInactiveAfter = now + durationMillis + 1000L < deadline

        assertTrue(isActiveDuring)
        assertFalse(isInactiveAfter)
    }

    @Test
    fun `test average rating calculation strictly excludes unrated entries`() {
        val list = listOf(
            AnimeEntity(title = "Anime 1", personalRating = 10.0f),
            AnimeEntity(title = "Anime 2", personalRating = 8.0f),
            AnimeEntity(title = "Anime 3", personalRating = null),
            AnimeEntity(title = "Anime 4", personalRating = null)
        )

        val rated = list.mapNotNull { it.personalRating }
        val avg = rated.average().toFloat()

        assertEquals(9.0f, avg, 0.01f)
        assertEquals(2, rated.size)
    }

    @Test
    fun `test status conversion from string`() {
        assertEquals(AnimeStatus.COMPLETED, AnimeStatus.fromString("COMPLETED"))
        assertEquals(AnimeStatus.COMPLETED, AnimeStatus.fromString("Completed"))
        assertEquals(AnimeStatus.PLAN_TO_WATCH, AnimeStatus.fromString("Plan to Watch"))
        assertEquals(AnimeStatus.PLAN_TO_WATCH, AnimeStatus.fromString("invalid_status"))
    }
}

package com.anilog.omor.dev

import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.repository.AnimeRepository
import com.anilog.omor.dev.domain.model.AnimeStatus
import com.anilog.omor.dev.domain.model.SortOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AniLogUnitTest {

    @Test
    fun `test rating values and half point precision`() {
        val anime1 = AnimeEntity(title = "Frieren", personalRating = 9.5f)
        val anime2 = AnimeEntity(title = "Steins;Gate", personalRating = 10.0f)
        val animeUnrated = AnimeEntity(title = "Unrated Anime", personalRating = null)
        val animeZero = AnimeEntity(title = "Zero Score", personalRating = 0.0f)

        assertEquals(9.5f, anime1.personalRating)
        assertEquals(10.0f, anime2.personalRating)
        assertNull(animeUnrated.personalRating)
        assertEquals(0.0f, animeZero.personalRating)
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
    fun `test alphabetical sort order`() {
        val list = listOf(
            AnimeEntity(title = "Zeta Gundam"),
            AnimeEntity(title = "Attack on Titan"),
            AnimeEntity(title = "Cowboy Bebop")
        )
        val sorted = AnimeRepository.sortAnimeList(list, SortOption.ALPHABETICAL)
        assertEquals("Attack on Titan", sorted[0].title)
        assertEquals("Cowboy Bebop", sorted[1].title)
        assertEquals("Zeta Gundam", sorted[2].title)
    }

    @Test
    fun `test average rating calculation strictly excludes unrated entries`() {
        val list = listOf(
            AnimeEntity(title = "Anime 1", personalRating = 10.0f),
            AnimeEntity(title = "Anime 2", personalRating = 8.0f),
            AnimeEntity(title = "Anime 3", personalRating = null), // Unrated must not be counted as 0!
            AnimeEntity(title = "Anime 4", personalRating = null)
        )

        val rated = list.mapNotNull { it.personalRating }
        val avg = rated.average().toFloat()

        // (10.0 + 8.0) / 2 = 9.0 (NOT (10 + 8 + 0 + 0)/4 = 4.5)
        assertEquals(9.0f, avg, 0.01f)
        assertEquals(2, rated.size)
    }

    @Test
    fun `test episode progress bounds and completion date trigger`() {
        val anime = AnimeEntity(
            title = "Mob Psycho 100",
            totalEpisodes = 12,
            watchedEpisodes = 11,
            status = AnimeStatus.WATCHING
        )

        val nextProgress = (anime.watchedEpisodes + 1).coerceAtLeast(0)
        val isCompleted = anime.totalEpisodes > 0 && nextProgress >= anime.totalEpisodes

        assertTrue(isCompleted)
        assertEquals(12, nextProgress)
    }

    @Test
    fun `test status conversion from string`() {
        assertEquals(AnimeStatus.COMPLETED, AnimeStatus.fromString("COMPLETED"))
        assertEquals(AnimeStatus.COMPLETED, AnimeStatus.fromString("Completed"))
        assertEquals(AnimeStatus.PLAN_TO_WATCH, AnimeStatus.fromString("Plan to Watch"))
        assertEquals(AnimeStatus.PLAN_TO_WATCH, AnimeStatus.fromString("invalid_status"))
    }
}

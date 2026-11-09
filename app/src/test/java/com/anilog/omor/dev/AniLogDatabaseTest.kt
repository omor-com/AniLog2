package com.anilog.omor.dev

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.anilog.omor.dev.data.database.AppDatabase
import com.anilog.omor.dev.data.database.dao.AnimeDao
import com.anilog.omor.dev.data.database.dao.CollectionDao
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.database.entity.CollectionAnimeCrossRef
import com.anilog.omor.dev.data.database.entity.CollectionEntity
import com.anilog.omor.dev.domain.model.AnimeStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AniLogDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var animeDao: AnimeDao
    private lateinit var collectionDao: CollectionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        animeDao = db.animeDao()
        collectionDao = db.collectionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetAnimeById() = runBlocking {
        val anime = AnimeEntity(
            title = "Hunter x Hunter",
            originalTitle = "ハンター×ハンター",
            personalRating = 9.5f,
            review = "Masterpiece chimera ant arc. অসাধারণ একটি এনিমে!",
            genres = listOf("Action", "Adventure", "Fantasy"),
            totalEpisodes = 148,
            watchedEpisodes = 148,
            status = AnimeStatus.COMPLETED
        )
        val id = animeDao.insertAnime(anime)
        val loaded = animeDao.getAnimeByIdOnce(id)

        assertNotNull(loaded)
        assertEquals("Hunter x Hunter", loaded?.title)
        assertEquals(9.5f, loaded?.personalRating)
        assertEquals("Masterpiece chimera ant arc. অসাধারণ একটি এনিমে!", loaded?.review)
        assertEquals(3, loaded?.genres?.size)
        assertEquals("Action", loaded?.genres?.get(0))
        assertEquals(AnimeStatus.COMPLETED, loaded?.status)
    }

    @Test
    fun updateAnimeRatingAndReview() = runBlocking {
        val anime = AnimeEntity(title = "Vinland Saga", personalRating = 8.5f, review = "Season 1")
        val id = animeDao.insertAnime(anime)

        val loaded = animeDao.getAnimeByIdOnce(id)!!
        val updated = loaded.copy(personalRating = 10.0f, review = "Farmland arc is peak fiction")
        animeDao.updateAnime(updated)

        val reloaded = animeDao.getAnimeByIdOnce(id)!!
        assertEquals(10.0f, reloaded.personalRating)
        assertEquals("Farmland arc is peak fiction", reloaded.review)
    }

    @Test
    fun collectionMembershipAndCascadeIsolation() = runBlocking {
        // 1. Insert anime
        val a1Id = animeDao.insertAnime(AnimeEntity(title = "Evangelion", personalRating = 9.0f))
        val a2Id = animeDao.insertAnime(AnimeEntity(title = "Serial Experiments Lain", personalRating = 9.5f))

        // 2. Insert collection
        val colId = collectionDao.insertCollection(
            CollectionEntity(name = "Psychological Masterpieces", description = "Mind bending shows")
        )

        // 3. Add anime to collection
        collectionDao.addAnimeToCollection(CollectionAnimeCrossRef(colId, a1Id, sortOrder = 0))
        collectionDao.addAnimeToCollection(CollectionAnimeCrossRef(colId, a2Id, sortOrder = 1))

        val inCol = collectionDao.getAnimeForCollectionOnce(colId)
        assertEquals(2, inCol.size)
        assertEquals("Evangelion", inCol[0].title)
        assertEquals("Serial Experiments Lain", inCol[1].title)

        // 4. CRITICAL: Deleting collection must NEVER delete its anime entries!
        collectionDao.deleteCollectionById(colId)
        val afterColDeleteList = animeDao.getAllAnimeList()
        assertEquals(2, afterColDeleteList.size)
        assertTrue(afterColDeleteList.any { it.id == a1Id })
        assertTrue(afterColDeleteList.any { it.id == a2Id })
    }

    @Test
    fun deletingAnimeCascadesCollectionCrossRefs() = runBlocking {
        val a1Id = animeDao.insertAnime(AnimeEntity(title = "Monster", personalRating = 10.0f))
        val colId = collectionDao.insertCollection(CollectionEntity(name = "Top Thrillers"))
        collectionDao.addAnimeToCollection(CollectionAnimeCrossRef(colId, a1Id, 0))

        assertEquals(1, collectionDao.getAnimeForCollectionOnce(colId).size)

        // Delete anime
        animeDao.deleteAnimeById(a1Id)

        // Collection still exists, but cross-ref was cleaned up
        val collection = collectionDao.getCollectionByIdOnce(colId)
        assertNotNull(collection)
        val inCol = collectionDao.getAnimeForCollectionOnce(colId)
        assertEquals(0, inCol.size)
    }
}

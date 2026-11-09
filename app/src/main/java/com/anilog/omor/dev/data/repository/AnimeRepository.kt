package com.anilog.omor.dev.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.anilog.omor.dev.data.database.AppDatabase
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.database.entity.CollectionAnimeCrossRef
import com.anilog.omor.dev.data.database.entity.CollectionEntity
import com.anilog.omor.dev.domain.model.AnimeStatistics
import com.anilog.omor.dev.domain.model.AnimeStatus
import com.anilog.omor.dev.domain.model.SortOption
import com.anilog.omor.dev.domain.model.TimelineItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AnimeRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val animeDao = database.animeDao()
    private val collectionDao = database.collectionDao()

    val allAnime: Flow<List<AnimeEntity>> = animeDao.getAllAnime()
    val allCollections: Flow<List<CollectionEntity>> = collectionDao.getAllCollections()

    fun getAnimeById(id: Long): Flow<AnimeEntity?> = animeDao.getAnimeById(id)

    suspend fun getAnimeByIdOnce(id: Long): AnimeEntity? = animeDao.getAnimeByIdOnce(id)

    suspend fun getAllAnimeList(): List<AnimeEntity> = animeDao.getAllAnimeList()

    suspend fun insertAnime(anime: AnimeEntity): Long = withContext(Dispatchers.IO) {
        animeDao.insertAnime(anime.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateAnime(anime: AnimeEntity) = withContext(Dispatchers.IO) {
        animeDao.updateAnime(anime.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteAnime(anime: AnimeEntity) = withContext(Dispatchers.IO) {
        val coverPath = anime.coverPath
        animeDao.deleteAnime(anime)
        // Check if cover path is internal and not used by any other anime
        if (!coverPath.isNullOrBlank() && coverPath.startsWith(context.filesDir.absolutePath)) {
            val allList = animeDao.getAllAnimeList()
            val isUsedElsewhere = allList.any { it.coverPath == coverPath }
            if (!isUsedElsewhere) {
                try {
                    File(coverPath).delete()
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun toggleFavorite(animeId: Long) = withContext(Dispatchers.IO) {
        val anime = animeDao.getAnimeByIdOnce(animeId) ?: return@withContext
        val updated = anime.copy(
            isFavorite = !anime.isFavorite,
            updatedAt = System.currentTimeMillis()
        )
        animeDao.updateAnime(updated)
    }

    suspend fun updateEpisodeProgress(animeId: Long, newProgress: Int) = withContext(Dispatchers.IO) {
        val anime = animeDao.getAnimeByIdOnce(animeId) ?: return@withContext
        val validProgress = newProgress.coerceAtLeast(0)
        val isNowCompleted = anime.totalEpisodes > 0 && validProgress >= anime.totalEpisodes
        val updatedStatus = if (isNowCompleted && anime.status != AnimeStatus.COMPLETED) {
            AnimeStatus.COMPLETED
        } else {
            anime.status
        }
        val completionDate = if (isNowCompleted && anime.completionDate == null) {
            System.currentTimeMillis()
        } else anime.completionDate

        val updated = anime.copy(
            watchedEpisodes = validProgress,
            status = updatedStatus,
            completionDate = completionDate,
            updatedAt = System.currentTimeMillis()
        )
        animeDao.updateAnime(updated)
    }

    /**
     * Copy user-picked image into private application storage to ensure permanent availability
     */
    suspend fun saveCoverImage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
            val fileName = "cover_${UUID.randomUUID()}.jpg"
            val destFile = File(coversDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Decode sample to avoid extreme memory allocation
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                // read stream
                val bytes = inputStream.readBytes()
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

                var inSampleSize = 1
                val maxDim = 1920
                if (options.outHeight > maxDim || options.outWidth > maxDim) {
                    val halfHeight = options.outHeight / 2
                    val halfWidth = options.outWidth / 2
                    while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                        inSampleSize *= 2
                    }
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
                    ?: return@withContext null

                FileOutputStream(destFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                bitmap.recycle()
            }
            destFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    // Collections
    fun getCollectionById(id: Long): Flow<CollectionEntity?> = collectionDao.getCollectionById(id)

    suspend fun getCollectionByIdOnce(id: Long): CollectionEntity? = collectionDao.getCollectionByIdOnce(id)

    fun getAnimeForCollection(collectionId: Long): Flow<List<AnimeEntity>> =
        collectionDao.getAnimeForCollection(collectionId)

    suspend fun createCollection(name: String, description: String = "", coverPath: String? = null): Long =
        withContext(Dispatchers.IO) {
            collectionDao.insertCollection(
                CollectionEntity(
                    name = name,
                    description = description,
                    coverPath = coverPath,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

    suspend fun updateCollection(collection: CollectionEntity) = withContext(Dispatchers.IO) {
        collectionDao.updateCollection(collection.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteCollection(collection: CollectionEntity) = withContext(Dispatchers.IO) {
        collectionDao.deleteCollection(collection)
    }

    suspend fun addAnimeToCollection(collectionId: Long, animeId: Long) = withContext(Dispatchers.IO) {
        val currentCrossRefs = collectionDao.getCrossRefsForCollection(collectionId)
        val nextOrder = (currentCrossRefs.maxOfOrNull { it.sortOrder } ?: 0) + 1
        collectionDao.addAnimeToCollection(
            CollectionAnimeCrossRef(
                collectionId = collectionId,
                animeId = animeId,
                sortOrder = nextOrder
            )
        )
    }

    suspend fun removeAnimeFromCollection(collectionId: Long, animeId: Long) = withContext(Dispatchers.IO) {
        collectionDao.removeAnimeFromCollection(collectionId, animeId)
    }

    suspend fun reorderAnimeInCollection(collectionId: Long, orderedAnimeIds: List<Long>) = withContext(Dispatchers.IO) {
        orderedAnimeIds.forEachIndexed { index, animeId ->
            collectionDao.updateCrossRef(
                CollectionAnimeCrossRef(
                    collectionId = collectionId,
                    animeId = animeId,
                    sortOrder = index
                )
            )
        }
    }

    // Statistics computation using real Room database data
    fun getStatistics(): Flow<AnimeStatistics> = allAnime.map { list ->
        calculateStatistics(list)
    }

    suspend fun getStatisticsOnce(): AnimeStatistics = withContext(Dispatchers.IO) {
        calculateStatistics(animeDao.getAllAnimeList())
    }

    private fun calculateStatistics(list: List<AnimeEntity>): AnimeStatistics {
        if (list.isEmpty()) return AnimeStatistics()

        var completed = 0
        var watching = 0
        var planToWatch = 0
        var onHold = 0
        var dropped = 0
        var totalEpisodesWatched = 0

        val ratedList = mutableListOf<Pair<AnimeEntity, Float>>()
        val genreMap = mutableMapOf<String, Int>()
        val ratingDist = mutableMapOf<Int, Int>().apply {
            for (i in 0..10) put(i, 0)
        }
        val monthlyMap = mutableMapOf<String, Int>()
        val monthFormat = SimpleDateFormat("MMM yyyy", Locale.US)

        list.forEach { anime ->
            when (anime.status) {
                AnimeStatus.COMPLETED -> completed++
                AnimeStatus.WATCHING -> watching++
                AnimeStatus.PLAN_TO_WATCH -> planToWatch++
                AnimeStatus.ON_HOLD -> onHold++
                AnimeStatus.DROPPED -> dropped++
            }
            totalEpisodesWatched += anime.watchedEpisodes

            // Genres
            anime.genres.forEach { genre ->
                val trimmed = genre.trim()
                if (trimmed.isNotEmpty()) {
                    genreMap[trimmed] = (genreMap[trimmed] ?: 0) + 1
                }
            }

            // Ratings (strictly exclude unrated)
            anime.personalRating?.let { rating ->
                ratedList.add(anime to rating)
                val rounded = rating.toInt().coerceIn(0, 10)
                ratingDist[rounded] = (ratingDist[rounded] ?: 0) + 1
            }

            // Monthly completion
            if (anime.status == AnimeStatus.COMPLETED && anime.completionDate != null) {
                val monthKey = monthFormat.format(Date(anime.completionDate))
                monthlyMap[monthKey] = (monthlyMap[monthKey] ?: 0) + 1
            }
        }

        val averageRating = if (ratedList.isNotEmpty()) {
            ratedList.map { it.second }.average().toFloat()
        } else null

        val highest = ratedList.maxByOrNull { it.second }
        val lowest = ratedList.minByOrNull { it.second }

        // Timeline items (top 15 recent events)
        val timelineItems = list
            .sortedByDescending { it.updatedAt }
            .take(15)
            .map { anime ->
                val eventType = when {
                    anime.status == AnimeStatus.COMPLETED -> "Completed"
                    anime.personalRating != null -> "Rated ${"%.1f".format(anime.personalRating)}/10"
                    anime.status == AnimeStatus.WATCHING -> "Watching"
                    else -> "Added to Library"
                }
                TimelineItem(
                    animeId = anime.id,
                    title = anime.title,
                    coverPath = anime.coverPath,
                    eventType = eventType,
                    timestamp = anime.updatedAt,
                    rating = anime.personalRating
                )
            }

        return AnimeStatistics(
            totalAnime = list.size,
            completedCount = completed,
            watchingCount = watching,
            planToWatchCount = planToWatch,
            onHoldCount = onHold,
            droppedCount = dropped,
            averageRating = averageRating,
            ratedCount = ratedList.size,
            highestRatedTitle = highest?.first?.title,
            highestRatedScore = highest?.second,
            lowestRatedTitle = lowest?.first?.title,
            lowestRatedScore = lowest?.second,
            totalEpisodesWatched = totalEpisodesWatched,
            genreDistribution = genreMap.toList().sortedByDescending { it.second }.toMap(),
            ratingDistribution = ratingDist,
            monthlyCompletions = monthlyMap,
            timeline = timelineItems
        )
    }

    // Sort & filter helpers
    companion object {
        fun sortAnimeList(list: List<AnimeEntity>, sortOption: SortOption): List<AnimeEntity> {
            return when (sortOption) {
                SortOption.HIGHEST_RATED -> list.sortedWith(
                    compareByDescending<AnimeEntity> { it.personalRating ?: -1f }
                        .thenByDescending { it.createdAt }
                )
                SortOption.LOWEST_RATED -> list.sortedWith(
                    compareBy<AnimeEntity> { it.personalRating ?: 99f }
                        .thenByDescending { it.createdAt }
                )
                SortOption.RECENTLY_ADDED -> list.sortedByDescending { it.createdAt }
                SortOption.OLDEST_ADDED -> list.sortedBy { it.createdAt }
                SortOption.ALPHABETICAL -> list.sortedBy { it.title.lowercase(Locale.ROOT) }
                SortOption.RECENTLY_UPDATED -> list.sortedByDescending { it.updatedAt }
                SortOption.RECENTLY_COMPLETED -> list.sortedWith(
                    compareByDescending<AnimeEntity> { it.completionDate ?: 0L }
                        .thenByDescending { it.updatedAt }
                )
            }
        }
    }
}

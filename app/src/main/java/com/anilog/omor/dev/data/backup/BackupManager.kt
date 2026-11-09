package com.anilog.omor.dev.data.backup

import android.content.Context
import android.net.Uri
import com.anilog.omor.dev.data.database.AppDatabase
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.database.entity.CollectionAnimeCrossRef
import com.anilog.omor.dev.data.database.entity.CollectionEntity
import com.anilog.omor.dev.domain.model.AnimeStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

enum class RestoreMode {
    MERGE,
    REPLACE
}

data class BackupValidationResult(
    val isValid: Boolean,
    val schemaVersion: Int = 1,
    val animeCount: Int = 0,
    val collectionCount: Int = 0,
    val imageCount: Int = 0,
    val errorMessage: String? = null
)

class BackupManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val animeDao = database.animeDao()
    private val collectionDao = database.collectionDao()

    /**
     * Export full ZIP backup containing manifest.json and image files
     */
    suspend fun createFullZipBackup(outputUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val animeList = animeDao.getAllAnimeList()
            val collections = collectionDao.getAllCollectionsList()
            val crossRefs = collectionDao.getAllCrossRefs()

            val manifestJson = buildManifestJson(animeList, collections, crossRefs)

            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    // 1. Add manifest.json
                    val manifestEntry = ZipEntry("manifest.json")
                    zipOut.putNextEntry(manifestEntry)
                    zipOut.write(manifestJson.toByteArray(StandardCharsets.UTF_8))
                    zipOut.closeEntry()

                    // 2. Add cover images
                    val coversDir = File(context.filesDir, "covers")
                    if (coversDir.exists() && coversDir.isDirectory) {
                        val referencedFiles = animeList.mapNotNull { it.coverPath }
                            .map { File(it).name }
                            .toSet()

                        coversDir.listFiles()?.forEach { file ->
                            if (file.isFile && referencedFiles.contains(file.name)) {
                                val coverEntry = ZipEntry("covers/${file.name}")
                                zipOut.putNextEntry(coverEntry)
                                FileInputStream(file).use { fis ->
                                    fis.copyTo(zipOut)
                                }
                                zipOut.closeEntry()
                            }
                        }
                    }
                }
            }
            Result.success(animeList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export metadata-only JSON
     */
    suspend fun createJsonMetadataExport(outputUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val animeList = animeDao.getAllAnimeList()
            val collections = collectionDao.getAllCollectionsList()
            val crossRefs = collectionDao.getAllCrossRefs()

            val jsonString = buildManifestJson(animeList, collections, crossRefs)

            context.contentResolver.openOutputStream(outputUri)?.use { os ->
                OutputStreamWriter(os, StandardCharsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                }
            }
            Result.success(animeList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export CSV anime list with UTF-8 BOM, Bangla & punctuation safe quoting
     */
    suspend fun createCsvExport(outputUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val animeList = animeDao.getAllAnimeList()
            context.contentResolver.openOutputStream(outputUri)?.use { os ->
                // Write UTF-8 BOM so Excel & spreadsheet apps read Bangla/Unicode cleanly
                os.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                OutputStreamWriter(os, StandardCharsets.UTF_8).use { writer ->
                    // Header
                    writer.write("ID,Title,Rating,Status,WatchedEpisodes,TotalEpisodes,Genres,Tags,ReleaseYear,Favorite,Review,Notes\n")
                    animeList.forEach { anime ->
                        val ratingStr = anime.personalRating?.let { "%.1f".format(it) } ?: ""
                        val row = listOf(
                            anime.id.toString(),
                            escapeCsv(anime.title),
                            ratingStr,
                            escapeCsv(anime.status.displayName),
                            anime.watchedEpisodes.toString(),
                            anime.totalEpisodes.toString(),
                            escapeCsv(anime.genres.joinToString(";")),
                            escapeCsv(anime.tags.joinToString(";")),
                            anime.releaseYear?.toString() ?: "",
                            if (anime.isFavorite) "Yes" else "No",
                            escapeCsv(anime.review),
                            escapeCsv(anime.notes)
                        ).joinToString(",")
                        writer.write(row + "\n")
                    }
                }
            }
            Result.success(animeList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun escapeCsv(text: String): String {
        val replaced = text.replace("\"", "\"\"")
        return "\"$replaced\""
    }

    private fun buildManifestJson(
        animeList: List<AnimeEntity>,
        collections: List<CollectionEntity>,
        crossRefs: List<CollectionAnimeCrossRef>
    ): String {
        val root = JSONObject()
        root.put("app", "AniLog")
        root.put("version", 1)
        root.put("exportTimestamp", System.currentTimeMillis())

        val animeArray = JSONArray()
        animeList.forEach { anime ->
            val obj = JSONObject()
            obj.put("id", anime.id)
            obj.put("title", anime.title)
            obj.put("originalTitle", anime.originalTitle)
            obj.put("coverFileName", anime.coverPath?.let { File(it).name })
            if (anime.personalRating != null) {
                obj.put("personalRating", anime.personalRating.toDouble())
            }
            obj.put("review", anime.review)
            obj.put("notes", anime.notes)
            val genresArr = JSONArray()
            anime.genres.forEach { genresArr.put(it) }
            obj.put("genres", genresArr)
            val tagsArr = JSONArray()
            anime.tags.forEach { tagsArr.put(it) }
            obj.put("tags", tagsArr)
            if (anime.releaseYear != null) obj.put("releaseYear", anime.releaseYear)
            obj.put("totalEpisodes", anime.totalEpisodes)
            obj.put("watchedEpisodes", anime.watchedEpisodes)
            obj.put("status", anime.status.name)
            if (anime.startDate != null) obj.put("startDate", anime.startDate)
            if (anime.completionDate != null) obj.put("completionDate", anime.completionDate)
            obj.put("isFavorite", anime.isFavorite)
            obj.put("createdAt", anime.createdAt)
            obj.put("updatedAt", anime.updatedAt)
            animeArray.put(obj)
        }
        root.put("anime", animeArray)

        val collectionsArray = JSONArray()
        collections.forEach { col ->
            val obj = JSONObject()
            obj.put("id", col.id)
            obj.put("name", col.name)
            obj.put("description", col.description)
            obj.put("coverFileName", col.coverPath?.let { File(it).name })
            obj.put("createdAt", col.createdAt)
            obj.put("updatedAt", col.updatedAt)
            collectionsArray.put(obj)
        }
        root.put("collections", collectionsArray)

        val crossRefsArray = JSONArray()
        crossRefs.forEach { ref ->
            val obj = JSONObject()
            obj.put("collectionId", ref.collectionId)
            obj.put("animeId", ref.animeId)
            obj.put("sortOrder", ref.sortOrder)
            crossRefsArray.put(obj)
        }
        root.put("crossRefs", crossRefsArray)

        return root.toString(2)
    }

    /**
     * Validate an incoming backup file (ZIP or JSON) before committing
     */
    suspend fun validateBackup(uri: Uri): BackupValidationResult = withContext(Dispatchers.IO) {
        try {
            val isZip = isZipFile(uri)
            if (isZip) {
                var manifestFound = false
                var manifestContent: String? = null
                var imageCount = 0

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                        var entry = zipIn.nextEntry
                        while (entry != null) {
                            if (entry.name == "manifest.json") {
                                manifestFound = true
                                manifestContent = zipIn.readBytes().toString(StandardCharsets.UTF_8)
                            } else if (entry.name.startsWith("covers/") && !entry.isDirectory) {
                                imageCount++
                            }
                            zipIn.closeEntry()
                            entry = zipIn.nextEntry
                        }
                    }
                }

                if (!manifestFound || manifestContent == null) {
                    return@withContext BackupValidationResult(
                        isValid = false,
                        errorMessage = "Invalid backup archive: missing manifest.json"
                    )
                }

                val json = JSONObject(manifestContent!!)
                val animeArray = json.optJSONArray("anime") ?: JSONArray()
                val collectionsArray = json.optJSONArray("collections") ?: JSONArray()
                val version = json.optInt("version", 1)

                BackupValidationResult(
                    isValid = true,
                    schemaVersion = version,
                    animeCount = animeArray.length(),
                    collectionCount = collectionsArray.length(),
                    imageCount = imageCount
                )
            } else {
                // Pure JSON metadata import
                val content = context.contentResolver.openInputStream(uri)?.use {
                    it.readBytes().toString(StandardCharsets.UTF_8)
                } ?: return@withContext BackupValidationResult(isValid = false, errorMessage = "Empty file")

                val json = JSONObject(content)
                val animeArray = json.optJSONArray("anime") ?: JSONArray()
                val collectionsArray = json.optJSONArray("collections") ?: JSONArray()
                val version = json.optInt("version", 1)

                BackupValidationResult(
                    isValid = true,
                    schemaVersion = version,
                    animeCount = animeArray.length(),
                    collectionCount = collectionsArray.length(),
                    imageCount = 0
                )
            }
        } catch (e: Exception) {
            BackupValidationResult(
                isValid = false,
                errorMessage = "Validation error: ${e.localizedMessage ?: "Corrupted file"}"
            )
        }
    }

    /**
     * Restore backup with MERGE or REPLACE mode
     */
    suspend fun restoreBackup(uri: Uri, mode: RestoreMode): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val isZip = isZipFile(uri)
            var manifestContent: String? = null
            val extractedCovers = mutableMapOf<String, String>() // fileName -> localAbsolutePath

            val coversDir = File(context.filesDir, "covers").apply { mkdirs() }

            if (isZip) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                        var entry = zipIn.nextEntry
                        while (entry != null) {
                            // Prevent Zip Slip / Path Traversal vulnerabilities
                            val targetFile = File(coversDir, File(entry.name).name)
                            val canonicalTarget = targetFile.canonicalPath
                            if (!canonicalTarget.startsWith(coversDir.canonicalPath)) {
                                throw SecurityException("Path traversal attempt in ZIP: ${entry.name}")
                            }

                            if (entry.name == "manifest.json") {
                                manifestContent = zipIn.readBytes().toString(StandardCharsets.UTF_8)
                            } else if (entry.name.startsWith("covers/") && !entry.isDirectory) {
                                val fileName = File(entry.name).name
                                val destFile = File(coversDir, fileName)
                                FileOutputStream(destFile).use { fos ->
                                    zipIn.copyTo(fos)
                                }
                                extractedCovers[fileName] = destFile.absolutePath
                            }
                            zipIn.closeEntry()
                            entry = zipIn.nextEntry
                        }
                    }
                }
            } else {
                manifestContent = context.contentResolver.openInputStream(uri)?.use {
                    it.readBytes().toString(StandardCharsets.UTF_8)
                }
            }

            if (manifestContent == null) {
                return@withContext Result.failure(IllegalArgumentException("Manifest data not found in backup"))
            }

            val json = JSONObject(manifestContent!!)
            val animeArray = json.optJSONArray("anime") ?: JSONArray()
            val collectionsArray = json.optJSONArray("collections") ?: JSONArray()
            val crossRefsArray = json.optJSONArray("crossRefs") ?: JSONArray()

            // Execute transaction: if REPLACE, wipe clean first
            if (mode == RestoreMode.REPLACE) {
                collectionDao.deleteAllCrossRefs()
                collectionDao.deleteAllCollections()
                animeDao.deleteAll()
            }

            val existingAnimeList = if (mode == RestoreMode.MERGE) animeDao.getAllAnimeList() else emptyList()
            val existingTitles = existingAnimeList.map { it.title.trim().lowercase() }.toSet()

            var importedCount = 0
            val animeIdMapping = mutableMapOf<Long, Long>() // oldId -> newId

            for (i in 0 until animeArray.length()) {
                val obj = animeArray.getJSONObject(i)
                val oldId = obj.optLong("id", 0L)
                val title = obj.getString("title")

                if (mode == RestoreMode.MERGE && existingTitles.contains(title.trim().lowercase())) {
                    // Skip duplicate in merge mode
                    continue
                }

                val coverFileName = if (obj.has("coverFileName") && !obj.isNull("coverFileName")) obj.optString("coverFileName") else null
                val localCoverPath = if (!coverFileName.isNullOrBlank()) {
                    extractedCovers[coverFileName] ?: File(coversDir, coverFileName).takeIf { it.exists() }?.absolutePath
                } else null

                val genres = mutableListOf<String>()
                obj.optJSONArray("genres")?.let { arr ->
                    for (g in 0 until arr.length()) genres.add(arr.getString(g))
                }

                val tags = mutableListOf<String>()
                obj.optJSONArray("tags")?.let { arr ->
                    for (t in 0 until arr.length()) tags.add(arr.getString(t))
                }

                val anime = AnimeEntity(
                    id = if (mode == RestoreMode.REPLACE) oldId else 0L,
                    title = title,
                    originalTitle = obj.optString("originalTitle", ""),
                    coverPath = localCoverPath,
                    personalRating = if (obj.has("personalRating")) obj.getDouble("personalRating").toFloat() else null,
                    review = obj.optString("review", ""),
                    notes = obj.optString("notes", ""),
                    genres = genres,
                    tags = tags,
                    releaseYear = if (obj.has("releaseYear")) obj.getInt("releaseYear") else null,
                    totalEpisodes = obj.optInt("totalEpisodes", 0),
                    watchedEpisodes = obj.optInt("watchedEpisodes", 0),
                    status = AnimeStatus.fromString(obj.optString("status", "PLAN_TO_WATCH")),
                    startDate = if (obj.has("startDate")) obj.getLong("startDate") else null,
                    completionDate = if (obj.has("completionDate")) obj.getLong("completionDate") else null,
                    isFavorite = obj.optBoolean("isFavorite", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )

                val newId = animeDao.insertAnime(anime)
                animeIdMapping[oldId] = newId
                importedCount++
            }

            // Restore collections
            val collectionIdMapping = mutableMapOf<Long, Long>()
            for (i in 0 until collectionsArray.length()) {
                val obj = collectionsArray.getJSONObject(i)
                val oldId = obj.optLong("id", 0L)
                val name = obj.getString("name")

                val col = CollectionEntity(
                    id = if (mode == RestoreMode.REPLACE) oldId else 0L,
                    name = name,
                    description = obj.optString("description", ""),
                    coverPath = null,
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
                val newId = collectionDao.insertCollection(col)
                collectionIdMapping[oldId] = newId
            }

            // Restore collection cross references
            for (i in 0 until crossRefsArray.length()) {
                val obj = crossRefsArray.getJSONObject(i)
                val oldColId = obj.getLong("collectionId")
                val oldAnimeId = obj.getLong("animeId")
                val sortOrder = obj.optInt("sortOrder", 0)

                val targetColId = collectionIdMapping[oldColId] ?: if (mode == RestoreMode.REPLACE) oldColId else null
                val targetAnimeId = animeIdMapping[oldAnimeId] ?: if (mode == RestoreMode.REPLACE) oldAnimeId else null

                if (targetColId != null && targetAnimeId != null) {
                    collectionDao.addAnimeToCollection(
                        CollectionAnimeCrossRef(
                            collectionId = targetColId,
                            animeId = targetAnimeId,
                            sortOrder = sortOrder
                        )
                    )
                }
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isZipFile(uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val header = ByteArray(4)
                val bytesRead = stream.read(header)
                bytesRead == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() && header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
            } ?: false
        } catch (_: Exception) {
            false
        }
    }
}

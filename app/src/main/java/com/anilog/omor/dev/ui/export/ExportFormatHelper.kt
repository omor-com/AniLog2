package com.anilog.omor.dev.ui.export

import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.util.RatingFormatHelper
import org.json.JSONArray
import org.json.JSONObject

enum class ExportType(val displayName: String, val description: String) {
    FORMAT_A("Format A — Names Only", "Clean list of anime titles (one per line)"),
    FORMAT_B("Format B — Names & Ratings", "Titles with exact custom decimal ratings"),
    FORMAT_C("Format C — Names, Ratings & Reviews", "Titles, exact ratings, and personal reviews"),
    FORMAT_D("Format D — Structured JSON", "Complete JSON export supporting full round-trip backup");
}

object ExportFormatHelper {

    fun generateExportText(animeList: List<AnimeEntity>, type: ExportType): String {
        return when (type) {
            ExportType.FORMAT_A -> generateFormatA(animeList)
            ExportType.FORMAT_B -> generateFormatB(animeList)
            ExportType.FORMAT_C -> generateFormatC(animeList)
            ExportType.FORMAT_D -> generateFormatD(animeList)
        }
    }

    // Format A: Names Only
    fun generateFormatA(list: List<AnimeEntity>): String {
        return list.joinToString("\n") { it.title.trim() }
    }

    // Format B: Names and Ratings (Preserves exact decimal values)
    fun generateFormatB(list: List<AnimeEntity>): String {
        return list.joinToString("\n") { anime ->
            val ratingStr = anime.personalRating?.let { RatingFormatHelper.formatRating(it) } ?: "Unrated"
            "${anime.title.trim()} | Rating: $ratingStr"
        }
    }

    // Format C: Names, Ratings, and Reviews
    fun generateFormatC(list: List<AnimeEntity>): String {
        return list.joinToString("\n\n") { anime ->
            val ratingStr = anime.personalRating?.let { RatingFormatHelper.formatRating(it) } ?: "Unrated"
            val reviewStr = if (anime.review.isNotBlank()) anime.review.trim() else "No review logged"
            "Title: ${anime.title.trim()}\nRating: $ratingStr\nReview: $reviewStr"
        }
    }

    // Format D: Complete Structured JSON
    fun generateFormatD(list: List<AnimeEntity>): String {
        val root = JSONObject()
        root.put("app", "AniLog")
        root.put("schemaVersion", 2)
        root.put("exportTimestamp", System.currentTimeMillis())

        val array = JSONArray()
        list.forEach { anime ->
            val obj = JSONObject()
            obj.put("id", anime.id)
            obj.put("title", anime.title)
            obj.put("originalTitle", anime.originalTitle)
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

            array.put(obj)
        }
        root.put("anime", array)

        return root.toString(2)
    }
}

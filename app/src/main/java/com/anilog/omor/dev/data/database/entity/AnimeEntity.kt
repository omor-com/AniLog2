package com.anilog.omor.dev.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.anilog.omor.dev.domain.model.AnimeStatus

@Entity(
    tableName = "anime",
    indices = [
        Index(value = ["title"]),
        Index(value = ["personalRating"]),
        Index(value = ["status"]),
        Index(value = ["isFavorite"]),
        Index(value = ["createdAt"])
    ]
)
data class AnimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val originalTitle: String = "",
    val coverPath: String? = null,
    val personalRating: Float? = null, // Supports half points (0.0 to 10.0), null = unrated
    val review: String = "",
    val notes: String = "",
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val releaseYear: Int? = null,
    val totalEpisodes: Int = 0,
    val watchedEpisodes: Int = 0,
    val status: AnimeStatus = AnimeStatus.PLAN_TO_WATCH,
    val startDate: Long? = null,
    val completionDate: Long? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

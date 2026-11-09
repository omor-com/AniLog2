package com.anilog.omor.dev.domain.model

data class TimelineItem(
    val animeId: Long,
    val title: String,
    val coverPath: String?,
    val eventType: String, // "Completed", "Added", "Rated"
    val timestamp: Long,
    val rating: Float?
)

data class AnimeStatistics(
    val totalAnime: Int = 0,
    val completedCount: Int = 0,
    val watchingCount: Int = 0,
    val planToWatchCount: Int = 0,
    val onHoldCount: Int = 0,
    val droppedCount: Int = 0,
    val averageRating: Float? = null, // Strictly excludes unrated!
    val ratedCount: Int = 0,
    val highestRatedTitle: String? = null,
    val highestRatedScore: Float? = null,
    val lowestRatedTitle: String? = null,
    val lowestRatedScore: Float? = null,
    val totalEpisodesWatched: Int = 0,
    val genreDistribution: Map<String, Int> = emptyMap(),
    val ratingDistribution: Map<Int, Int> = emptyMap(), // 0..10
    val monthlyCompletions: Map<String, Int> = emptyMap(),
    val timeline: List<TimelineItem> = emptyList()
)

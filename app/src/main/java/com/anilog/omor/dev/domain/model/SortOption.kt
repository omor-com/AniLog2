package com.anilog.omor.dev.domain.model

enum class SortOption(val displayName: String) {
    HIGHEST_RATED("Highest Rated"),
    LOWEST_RATED("Lowest Rated"),
    RECENTLY_ADDED("Recently Added"),
    OLDEST_ADDED("Oldest Added"),
    ALPHABETICAL("Alphabetical (A-Z)"),
    RECENTLY_UPDATED("Recently Updated"),
    RECENTLY_COMPLETED("Recently Completed");

    companion object {
        fun fromString(value: String?): SortOption {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: HIGHEST_RATED
        }
    }
}

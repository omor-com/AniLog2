package com.anilog.omor.dev.domain.model

enum class AnimeStatus(val displayName: String) {
    PLAN_TO_WATCH("Plan to Watch"),
    WATCHING("Watching"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold"),
    DROPPED("Dropped");

    companion object {
        fun fromString(value: String?): AnimeStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: PLAN_TO_WATCH
        }
    }
}

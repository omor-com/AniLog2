package com.anilog.omor.dev.domain.model

enum class CardTemplate(val displayName: String, val description: String) {
    PREMIUM_DARK("Premium Dark", "Deep charcoal theme with violet indigo glow and elegant serif typography"),
    MINIMAL_LIGHT("Minimal Light", "Clean white background with refined minimalist typography and soft borders"),
    CINEMATIC("Cinematic", "Dramatic poster layout with dimmed backdrop and widescreen anime framing"),
    COMPACT_REVIEW("Compact Review", "Dense, modern card optimized for quick review quotes and status tags"),
    TOP_RATED("Top-Rated Anime", "Gold-accented showcase card designed for masterpieces and high scores");

    companion object {
        fun fromString(value: String?): CardTemplate {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PREMIUM_DARK
        }
    }
}

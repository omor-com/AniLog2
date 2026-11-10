package com.anilog.omor.dev.domain.model

enum class CardAspectRatio(val displayName: String, val width: Int, val height: Int) {
    PORTRAIT("Portrait (4:5)", 1080, 1350),
    SQUARE("Square (1:1)", 1080, 1080),
    LANDSCAPE("Landscape (16:9)", 1200, 675);
}

enum class CardTemplate(val displayName: String, val description: String) {
    NEON_VIOLET("Neon Violet", "Black background, restrained purple glow, premium visual hierarchy"),
    CELESTIAL("Celestial Anime", "Cosmic background, elegant starfield lighting, layered cover presentation"),
    PREMIUM_COLLECTOR("Premium Collector", "Refined borders, metadata panels, polished collector-card layout"),
    ELEMENTAL("Elemental Card", "Original elemental gradients, visual energy, structured rating panels"),
    COLLECTIBLE_CARD("Collectible Card", "Collectible card composition with prominent artwork area, stat panels, decorative borders"),
    MINIMAL_PREMIUM("Minimal Premium", "Clean typography, generous spacing, and restrained decoration"),
    DARK_CINEMATIC("Dark Cinematic", "Cinematic contrast, layered backdrop, and dramatic lighting"),
    CUSTOM_REVIEW("Custom Review Card", "Spotlight on your personal review quote, rating, and cover"),
    RAFAEL_RANKED("Rafael Ranked Card", "Highlights personal rating, Rafael ranking position and tie-break badge");

    companion object {
        fun fromString(value: String?): CardTemplate {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NEON_VIOLET
        }
    }
}

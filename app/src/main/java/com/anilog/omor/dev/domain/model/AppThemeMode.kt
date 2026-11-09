package com.anilog.omor.dev.domain.model

enum class AppThemeMode(val displayName: String) {
    SYSTEM("Follow System"),
    LIGHT("Light Theme"),
    DARK("Dark Theme"),
    AMOLED("AMOLED Black");

    companion object {
        fun fromString(value: String?): AppThemeMode {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SYSTEM
        }
    }
}

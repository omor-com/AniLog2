package com.anilog.omor.dev.util

import java.math.BigDecimal
import java.math.RoundingMode

object RatingFormatHelper {

    fun formatRating(rating: Float?): String {
        if (rating == null) return "Unrated"
        val clamped = rating.coerceIn(0f, 10f)
        // Check if whole integer
        if (clamped % 1.0f == 0.0f) {
            return clamped.toInt().toString()
        }
        // Use BigDecimal to format cleanly without IEEE floating artifacts
        return try {
            val bd = BigDecimal(clamped.toDouble())
                .setScale(4, RoundingMode.HALF_UP)
                .stripTrailingZeros()
            bd.toPlainString()
        } catch (_: Exception) {
            "%.2f".format(clamped).trimEnd('0').trimEnd('.')
        }
    }

    fun formatRatingScore(rating: Float?): String {
        if (rating == null) return "Unrated"
        return "${formatRating(rating)}/10"
    }

    fun parseRatingInput(input: String): Float? {
        val trimmed = input.trim().replace(',', '.')
        if (trimmed.isBlank()) return null
        val number = trimmed.toFloatOrNull() ?: return null
        if (number < 0f || number > 10f || number.isNaN() || number.isInfinite()) return null
        return number
    }

    fun isValidRatingInput(input: String): Boolean {
        val trimmed = input.trim().replace(',', '.')
        if (trimmed.isBlank()) return false
        val number = trimmed.toFloatOrNull() ?: return false
        return number in 0f..10f && !number.isNaN() && !number.isInfinite()
    }
}

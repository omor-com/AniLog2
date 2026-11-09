package com.anilog.omor.dev.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anilog.omor.dev.ui.theme.StarGold

/**
 * Display ten-point personal rating with stars
 */
@Composable
fun StarRatingDisplay(
    rating: Float?,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
    showScoreText: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics {
            contentDescription = if (rating != null) "Personal rating: ${"%.1f".format(rating)} out of 10" else "Unrated"
        }
    ) {
        if (rating == null) {
            Text(
                text = "Unrated",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        } else {
            // Render 10 stars (5 stars representing 10 points or 10 stars directly)
            // With 10 points: 1 star = 1 full point (stars 1 to 10)
            val fullStars = rating.toInt().coerceIn(0, 10)
            val hasHalf = (rating - fullStars) >= 0.25f && (rating - fullStars) < 0.75f
            val totalRenderStars = 5 // To fit compactly on mobile cards, render 5 stars for score / 2 or 10 stars
            // On mobile cards, 5 stars mapped from 10 points (2 pts per star) or 10 miniature stars
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
                val scaled = (rating / 2f).coerceIn(0f, 5f)
                val full5 = scaled.toInt()
                val half5 = (scaled - full5) >= 0.25f

                for (i in 1..5) {
                    when {
                        i <= full5 -> Icon(Icons.Filled.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(starSize))
                        i == full5 + 1 && half5 -> Icon(Icons.Filled.StarHalf, contentDescription = null, tint = StarGold, modifier = Modifier.size(starSize))
                        else -> Icon(Icons.Filled.StarOutline, contentDescription = null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.size(starSize))
                    }
                }
            }

            if (showScoreText) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${"%.1f".format(rating)}/10",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = StarGold
                )
            }
        }
    }
}

/**
 * Interactive 10-point rating selector supporting half-point increments
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StarRatingInput(
    currentRating: Float?,
    onRatingChange: (Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.testTag("star_rating_input")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = if (currentRating != null) "Personal Rating: ${"%.1f".format(currentRating)} / 10.0" else "Personal Rating: Unrated",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (currentRating != null) StarGold else MaterialTheme.colorScheme.onSurface
            )

            if (currentRating != null) {
                AssistChip(
                    onClick = { onRatingChange(null) },
                    label = { Text("Clear (Unrated)") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Close, contentDescription = "Clear rating", modifier = Modifier.size(16.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("clear_rating_button")
                )
            }
        }

        // Quick rating score chips in half-point increments (0 to 10 in 0.5 steps)
        Text(
            text = "Select score (0.0 to 10.0, half-point increments):",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Preset score chips for fast selection
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            val commonScores = listOf(10.0f, 9.5f, 9.0f, 8.5f, 8.0f, 7.5f, 7.0f, 6.5f, 6.0f, 5.0f, 4.0f, 2.0f, 0.0f)
            commonScores.forEach { score ->
                val isSelected = currentRating != null && kotlin.math.abs(currentRating - score) < 0.01f
                AssistChip(
                    onClick = { onRatingChange(score) },
                    label = {
                        Text(
                            text = if (score == score.toInt().toFloat()) "${score.toInt()}" else "$score",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected) StarGold.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = if (isSelected) StarGold else MaterialTheme.colorScheme.onSurface
                    ),
                    border = if (isSelected) AssistChipDefaults.assistChipBorder(borderColor = StarGold, enabled = true) else null,
                    modifier = Modifier.testTag("rating_chip_${score}")
                )
            }
        }
    }
}

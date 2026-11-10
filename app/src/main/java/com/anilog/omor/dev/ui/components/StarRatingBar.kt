package com.anilog.omor.dev.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilog.omor.dev.R
import com.anilog.omor.dev.ui.theme.StarGold
import com.anilog.omor.dev.util.RatingFormatHelper

/**
 * Display exact decimal rating with stars and formatted score text
 */
@Composable
fun StarRatingDisplay(
    rating: Float?,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
    showScoreText: Boolean = true
) {
    val ratingText = RatingFormatHelper.formatRating(rating)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics {
            contentDescription = if (rating != null) "Personal rating: $ratingText out of 10" else "Unrated"
        }
    ) {
        if (rating == null) {
            Text(
                text = stringResource(R.string.unrated),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        } else {
            // 5 stars representing 10 points (2 points per star)
            val clamped = rating.coerceIn(0f, 10f)
            val scaled5 = clamped / 2f
            val fullStars = scaled5.toInt()
            val remainder = scaled5 - fullStars
            val hasHalf = remainder >= 0.25f && remainder < 0.75f
            val hasExtraFull = remainder >= 0.75f

            Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..5) {
                    when {
                        i <= fullStars || (i == fullStars + 1 && hasExtraFull) -> {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(starSize))
                        }
                        i == fullStars + 1 && hasHalf -> {
                            Icon(Icons.Filled.StarHalf, contentDescription = null, tint = StarGold, modifier = Modifier.size(starSize))
                        }
                        else -> {
                            Icon(Icons.Filled.StarOutline, contentDescription = null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.size(starSize))
                        }
                    }
                }
            }

            if (showScoreText) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$ratingText/10",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = StarGold
                )
            }
        }
    }
}

/**
 * Interactive rating selector supporting presets and arbitrary custom decimal ratings (0 to 10)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StarRatingInput(
    currentRating: Float?,
    onRatingChange: (Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomRatingDialog by remember { mutableStateOf(false) }
    var customInputText by remember { mutableStateOf(currentRating?.let { RatingFormatHelper.formatRating(it) } ?: "") }
    var customInputError by remember { mutableStateOf<String?>(null) }

    val formattedScore = RatingFormatHelper.formatRating(currentRating)

    Column(modifier = modifier.testTag("star_rating_input")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(
                text = if (currentRating != null) "${stringResource(R.string.personal_rating)}: $formattedScore / 10.0" else "${stringResource(R.string.personal_rating)}: ${stringResource(R.string.unrated)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (currentRating != null) StarGold else MaterialTheme.colorScheme.onSurface
            )

            if (currentRating != null) {
                AssistChip(
                    onClick = { onRatingChange(null) },
                    label = { Text(stringResource(R.string.clear_rating)) },
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

        // Custom Rating Button (Section 5.2)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = {
                    customInputText = currentRating?.let { RatingFormatHelper.formatRating(it) } ?: ""
                    customInputError = null
                    showCustomRatingDialog = true
                },
                modifier = Modifier.testTag("btn_custom_rating")
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.custom_rating),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Preset score chips for fast selection
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            val commonScores = listOf(10.0f, 9.5f, 9.0f, 8.5f, 8.0f, 7.5f, 7.0f, 6.5f, 6.0f, 5.0f, 4.0f, 2.0f, 0.0f)
            commonScores.forEach { score ->
                val isSelected = currentRating != null && kotlin.math.abs(currentRating - score) < 0.001f
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

    // Custom Rating Decimal Dialog
    if (showCustomRatingDialog) {
        val errorText = stringResource(R.string.invalid_rating_error)
        AlertDialog(
            onDismissRequest = { showCustomRatingDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.custom_rating),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.rating_range_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customInputText,
                        onValueChange = {
                            customInputText = it
                            customInputError = null
                        },
                        label = { Text("Rating (0.0 – 10.0)") },
                        placeholder = { Text("e.g. 7.123, 9.99, 8.7, 10") },
                        isError = customInputError != null,
                        supportingText = {
                            customInputError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("input_custom_rating_decimal")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = RatingFormatHelper.parseRatingInput(customInputText)
                        if (parsed != null) {
                            onRatingChange(parsed)
                            showCustomRatingDialog = false
                        } else {
                            customInputError = errorText
                        }
                    },
                    modifier = Modifier.testTag("btn_save_custom_rating")
                ) {
                    Text(stringResource(R.string.action_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomRatingDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

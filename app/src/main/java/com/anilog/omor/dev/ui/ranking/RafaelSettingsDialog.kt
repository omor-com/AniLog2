package com.anilog.omor.dev.ui.ranking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anilog.omor.dev.R
import com.anilog.omor.dev.data.repository.UserPreferencesRepository
import com.anilog.omor.dev.domain.ranking.RafaelRankedItem
import kotlinx.coroutines.launch

@Composable
fun RafaelSettingsDialog(
    preferencesRepository: UserPreferencesRepository,
    onDismiss: () -> Unit
) {
    val isEnabled by preferencesRepository.rafaelEnabled.collectAsStateWithLifecycle(true)
    val sigFavorite by preferencesRepository.rafaelSignalFavorite.collectAsStateWithLifecycle(true)
    val sigCompletion by preferencesRepository.rafaelSignalCompletion.collectAsStateWithLifecycle(true)
    val sigReview by preferencesRepository.rafaelSignalReviewDepth.collectAsStateWithLifecycle(true)
    val sigProgress by preferencesRepository.rafaelSignalProgress.collectAsStateWithLifecycle(true)
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.rafael_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.rafael_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Master Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.rafael_enable),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { scope.launch { preferencesRepository.setRafaelEnabled(it) } },
                        modifier = Modifier.testTag("switch_rafael_enable")
                    )
                }

                HorizontalDivider()

                Text(
                    text = "Tie-Break Signals / সম-রেটিং অগ্রাধিকার সংকেত:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                // Favorite Signal
                SignalToggleRow(
                    label = stringResource(R.string.rafael_signal_favorites),
                    checked = sigFavorite,
                    enabled = isEnabled,
                    onCheckedChange = {
                        scope.launch { preferencesRepository.setRafaelSignals(it, sigCompletion, sigReview, sigProgress) }
                    }
                )

                // Completion Signal
                SignalToggleRow(
                    label = stringResource(R.string.rafael_signal_completion),
                    checked = sigCompletion,
                    enabled = isEnabled,
                    onCheckedChange = {
                        scope.launch { preferencesRepository.setRafaelSignals(sigFavorite, it, sigReview, sigProgress) }
                    }
                )

                // Review Depth Signal
                SignalToggleRow(
                    label = stringResource(R.string.rafael_signal_review_depth),
                    checked = sigReview,
                    enabled = isEnabled,
                    onCheckedChange = {
                        scope.launch { preferencesRepository.setRafaelSignals(sigFavorite, sigCompletion, it, sigProgress) }
                    }
                )

                // Episode Progress Signal
                SignalToggleRow(
                    label = stringResource(R.string.rafael_signal_progress),
                    checked = sigProgress,
                    enabled = isEnabled,
                    onCheckedChange = {
                        scope.launch { preferencesRepository.setRafaelSignals(sigFavorite, sigCompletion, sigReview, it) }
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            preferencesRepository.resetRafaelPreferences()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("btn_reset_rafael_prefs")
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.rafael_reset_prefs))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun SignalToggleRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun RafaelRankingExplanationDialog(
    rankedItem: RafaelRankedItem,
    isBengali: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.rafael_explanation_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = rankedItem.anime.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rank #${rankedItem.rankPosition} • Rating: ${rankedItem.anime.personalRating ?: "Unrated"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = if (isBengali) rankedItem.explanationBn else rankedItem.explanationEn,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

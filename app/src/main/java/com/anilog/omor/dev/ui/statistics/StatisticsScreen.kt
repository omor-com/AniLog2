package com.anilog.omor.dev.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anilog.omor.dev.domain.model.TimelineItem
import com.anilog.omor.dev.ui.theme.StarGold
import com.anilog.omor.dev.ui.theme.StatusCompleted
import com.anilog.omor.dev.ui.theme.StatusDropped
import com.anilog.omor.dev.ui.theme.StatusOnHold
import com.anilog.omor.dev.ui.theme.StatusPlanToWatch
import com.anilog.omor.dev.ui.theme.StatusWatching
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateToAnimeDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.statistics.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Anime Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        if (stats.totalAnime == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Statistics Available",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add anime to your journal to see detailed insights, score distributions, and timeline tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("statistics_screen_content"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Key Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Total Anime",
                        value = "${stats.totalAnime}",
                        subtitle = "${stats.totalEpisodesWatched} eps watched",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Average Rating",
                        value = stats.averageRating?.let { "★ ${"%.2f".format(it)}" } ?: "—",
                        subtitle = "Across ${stats.ratedCount} rated anime",
                        isAccent = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Highest & Lowest Rated
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Highest Rated",
                        value = stats.highestRatedScore?.let { "${"%.1f".format(it)}/10" } ?: "—",
                        subtitle = stats.highestRatedTitle ?: "None",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Lowest Rated",
                        value = stats.lowestRatedScore?.let { "${"%.1f".format(it)}/10" } ?: "—",
                        subtitle = stats.lowestRatedTitle ?: "None",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Status Breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Watching Status Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val total = stats.totalAnime.toFloat()
                        StatusProgressBar(label = "Completed", count = stats.completedCount, total = total, color = StatusCompleted)
                        StatusProgressBar(label = "Watching", count = stats.watchingCount, total = total, color = StatusWatching)
                        StatusProgressBar(label = "Plan to Watch", count = stats.planToWatchCount, total = total, color = StatusPlanToWatch)
                        StatusProgressBar(label = "On Hold", count = stats.onHoldCount, total = total, color = StatusOnHold)
                        StatusProgressBar(label = "Dropped", count = stats.droppedCount, total = total, color = StatusDropped)
                    }
                }

                // Rating Distribution Histogram (0 to 10)
                if (stats.ratedCount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Personal Rating Distribution",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Shows frequency of scores across your 10-point ratings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            val maxFreq = stats.ratingDistribution.values.maxOrNull()?.coerceAtLeast(1) ?: 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                for (score in 1..10) {
                                    val count = stats.ratingDistribution[score] ?: 0
                                    val barFraction = count.toFloat() / maxFreq

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (count > 0) {
                                            Text(
                                                text = "$count",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(14.dp)
                                                .height((barFraction * 80).coerceAtLeast(4f).dp)
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(
                                                    if (score >= 9) StarGold else MaterialTheme.colorScheme.primary
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$score",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Genre Distribution
                if (stats.genreDistribution.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Top Genres",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val topGenres = stats.genreDistribution.entries.take(7)
                            val maxGenreCount = topGenres.firstOrNull()?.value?.toFloat() ?: 1f

                            topGenres.forEach { (genre, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = genre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.width(100.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    LinearProgressIndicator(
                                        progress = { (count / maxGenreCount).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Anime Journey Timeline
                if (stats.timeline.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Anime Journey Milestones",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
                            stats.timeline.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${item.eventType} • ${dateFormat.format(Date(item.timestamp))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    isAccent: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isAccent) StarGold else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatusProgressBar(
    label: String,
    count: Int,
    total: Float,
    color: androidx.compose.ui.graphics.Color
) {
    val fraction = if (total > 0f) (count / total).coerceIn(0f, 1f) else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(96.dp))
        LinearProgressIndicator(
            progress = { fraction },
            color = color,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp)
        )
    }
}

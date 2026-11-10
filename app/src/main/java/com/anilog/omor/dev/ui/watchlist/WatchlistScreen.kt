package com.anilog.omor.dev.ui.watchlist

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.domain.model.AnimeStatus
import com.anilog.omor.dev.ui.components.CoverImage
import com.anilog.omor.dev.ui.components.StarRatingDisplay
import com.anilog.omor.dev.ui.components.StatusBadge
import com.anilog.omor.dev.ui.home.EmptyFeedView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAddAnime: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Watchlist & Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Status Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimeStatus.entries.forEach { status ->
                    FilterChip(
                        selected = uiState.selectedStatus == status,
                        onClick = { viewModel.onStatusSelected(status) },
                        label = { Text(status.displayName) },
                        modifier = Modifier.testTag("watchlist_tab_${status.name.lowercase()}")
                    )
                }
            }

            // Anime Watchlist Items
            if (uiState.animeList.isEmpty()) {
                EmptyFeedView(
                    isSearching = true,
                    onAddClick = onNavigateToAddAnime,
                    onResetSearch = { viewModel.onStatusSelected(AnimeStatus.WATCHING) }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("watchlist_anime_list")
                ) {
                    items(uiState.animeList, key = { it.id }) { anime ->
                        WatchlistItemCard(
                            anime = anime,
                            onIncrement = { viewModel.incrementEpisode(anime) },
                            onDecrement = { viewModel.decrementEpisode(anime) },
                            onMarkCompleted = { viewModel.markAsCompleted(anime, setTodayAsCompletionDate = true) },
                            onClick = { onNavigateToDetail(anime.id) }
                        )
                    }
                }
            }
        }

        // Completion Dialog prompt
        uiState.pendingCompletionAnime?.let { anime ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissCompletionDialog() },
                title = { Text("Mark as Completed?") },
                text = {
                    Text("You reached ${anime.watchedEpisodes} episodes for \"${anime.title}\". Would you like to mark it as Completed and record today's date as completion date?")
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.markAsCompleted(anime, setTodayAsCompletionDate = true) },
                        modifier = Modifier.testTag("confirm_completion_button")
                    ) {
                        Text("Mark Completed")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissCompletionDialog() }) {
                        Text("Keep Watching")
                    }
                }
            )
        }
    }
}

@Composable
fun WatchlistItemCard(
    anime: AnimeEntity,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onMarkCompleted: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("watchlist_card_${anime.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Cover
                CoverImage(
                    coverPath = anime.coverPath,
                    modifier = Modifier
                        .width(72.dp)
                        .height(98.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 98.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = anime.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            StatusBadge(status = anime.status)
                            if (anime.releaseYear != null) {
                                Text(
                                    text = "• ${anime.releaseYear}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    StarRatingDisplay(
                        rating = anime.personalRating,
                        starSize = 13.dp,
                        showScoreText = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Episode Progress Bar
            val totalEps = anime.totalEpisodes
            val watched = anime.watchedEpisodes
            val progressFraction = if (totalEps > 0) (watched.toFloat() / totalEps).coerceIn(0f, 1f) else 0f

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (totalEps > 0) "Episode Progress: $watched / $totalEps eps" else "Episodes: $watched eps",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (totalEps > 0) {
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (totalEps > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Episode Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(
                        onClick = onDecrement,
                        enabled = watched > 0,
                        modifier = Modifier.size(38.dp).testTag("btn_decrement_${anime.id}")
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrement episode", modifier = Modifier.size(18.dp))
                    }

                    FilledTonalIconButton(
                        onClick = onIncrement,
                        enabled = totalEps == 0 || watched < totalEps,
                        modifier = Modifier.size(38.dp).testTag("btn_increment_${anime.id}")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Increment episode", modifier = Modifier.size(18.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (anime.status != AnimeStatus.COMPLETED) {
                        Button(
                            onClick = onMarkCompleted,
                            modifier = Modifier.height(38.dp).testTag("btn_complete_${anime.id}")
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Details", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

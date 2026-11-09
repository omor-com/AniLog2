package com.anilog.omor.dev.ui.anime

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anilog.omor.dev.ui.components.ConfirmDialog
import com.anilog.omor.dev.ui.components.CoverImage
import com.anilog.omor.dev.ui.components.StarRatingDisplay
import com.anilog.omor.dev.ui.components.StatusBadge
import com.anilog.omor.dev.ui.theme.FavoriteHeart
import com.anilog.omor.dev.ui.theme.StarGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AnimeDetailScreen(
    viewModel: AnimeDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToReviewCard: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    if (uiState.isLoading || uiState.anime == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val anime = uiState.anime!!

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier.testTag("detail_btn_favorite")
                    ) {
                        Icon(
                            imageVector = if (anime.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (anime.isFavorite) FavoriteHeart else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToEdit(anime.id) },
                        modifier = Modifier.testTag("detail_btn_edit")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Anime")
                    }
                    IconButton(
                        onClick = { viewModel.showDeleteConfirmation(true) },
                        modifier = Modifier.testTag("detail_btn_delete")
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Anime", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Hero Header with Cover & Quick Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                CoverImage(
                    coverPath = anime.coverPath,
                    modifier = Modifier
                        .width(130.dp)
                        .aspectRatio(0.72f)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.width(18.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = anime.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (anime.originalTitle.isNotBlank()) {
                        Text(
                            text = anime.originalTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    StatusBadge(status = anime.status)

                    // 10-Point Rating Display
                    StarRatingDisplay(
                        rating = anime.personalRating,
                        starSize = 18.dp,
                        showScoreText = true
                    )

                    if (anime.releaseYear != null) {
                        Text(
                            text = "Release Year: ${anime.releaseYear}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Actions: Generate Review Card, Add to Collection, Share Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigateToReviewCard(anime.id) },
                    modifier = Modifier.weight(1f).testTag("btn_generate_review_card")
                ) {
                    Icon(Icons.Filled.PhotoAlbum, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Review Card")
                }

                FilledTonalButton(
                    onClick = { viewModel.showAddToCollection(true) },
                    modifier = Modifier.weight(1f).testTag("btn_add_to_collection")
                ) {
                    Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Collection")
                }

                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            val scoreStr = anime.personalRating?.let { "★ ${"%.1f".format(it)}/10" } ?: "Unrated"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "AniLog Anime Review\n\n${anime.title} — $scoreStr\nStatus: ${anime.status.displayName}\n\n${anime.review}\n\nShared from AniLog Personal Anime Journal"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Anime Review"))
                    },
                    modifier = Modifier.testTag("btn_share_review_text")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Share Review Text")
                }
            }

            // Episode Progress Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Episode Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (anime.totalEpisodes > 0) "${anime.watchedEpisodes} / ${anime.totalEpisodes} eps" else "${anime.watchedEpisodes} eps",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (anime.totalEpisodes > 0) {
                        val fraction = (anime.watchedEpisodes.toFloat() / anime.totalEpisodes).coerceIn(0f, 1f)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
                    val dates = mutableListOf<String>()
                    anime.startDate?.let { dates.add("Started: ${dateFormat.format(Date(it))}") }
                    anime.completionDate?.let { dates.add("Completed: ${dateFormat.format(Date(it))}") }

                    if (dates.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dates.joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Personal Review Card (Unicode, Bangla, Emojis preserved exactly)
            if (anime.review.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Personal Review",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = anime.review,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Personal Notes
            if (anime.notes.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Personal Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = anime.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Genres & Tags
            if (anime.genres.isNotEmpty() || anime.tags.isNotEmpty()) {
                Column {
                    Text(
                        text = "Genres & Tags",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        anime.genres.forEach { genre ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(genre) }
                            )
                        }
                        anime.tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text("#$tag") }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Delete Dialog
        if (uiState.showDeleteDialog) {
            ConfirmDialog(
                title = "Delete Anime Entry?",
                message = "Are you sure you want to delete \"${anime.title}\"? This will permanently remove your rating, review, and notes.",
                confirmButtonText = "Delete",
                isDestructive = true,
                onConfirm = { viewModel.deleteAnime() },
                onDismiss = { viewModel.showDeleteConfirmation(false) }
            )
        }

        // Add to Collection Dialog
        if (uiState.showAddToCollectionDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showAddToCollection(false) },
                title = { Text("Add to Collection") },
                text = {
                    if (uiState.collections.isEmpty()) {
                        Text("No custom collections created yet. Go to Collections tab to create your first collection.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.collections.size) { i ->
                                val col = uiState.collections[i]
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.addToCollection(col.id) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = col.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.showAddToCollection(false) }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

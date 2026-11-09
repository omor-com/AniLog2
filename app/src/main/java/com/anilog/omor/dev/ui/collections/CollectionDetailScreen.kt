package com.anilog.omor.dev.ui.collections

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.ui.components.CoverImage
import com.anilog.omor.dev.ui.components.StarRatingDisplay
import com.anilog.omor.dev.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: Long,
    viewModel: CollectionsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAnimeDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val collection = uiState.collections.firstOrNull { it.id == collectionId }
    val animeInCollection by viewModel.getAnimeForCollection(collectionId).collectAsStateWithLifecycle(emptyList())

    val context = LocalContext.current
    var showAddAnimeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = collection?.name ?: "Collection",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (!collection?.description.isNullOrBlank()) {
                            Text(
                                text = collection!!.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (animeInCollection.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val text = buildString {
                                    appendLine("AniLog Collection: ${collection?.name}")
                                    if (!collection?.description.isNullOrBlank()) appendLine(collection!!.description)
                                    appendLine()
                                    animeInCollection.forEachIndexed { i, a ->
                                        val score = a.personalRating?.let { "★ ${"%.1f".format(it)}/10" } ?: "Unrated"
                                        appendLine("${i + 1}. ${a.title} — $score")
                                    }
                                    appendLine("\nCurated with AniLog Personal Anime Journal")
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Collection"))
                            },
                            modifier = Modifier.testTag("btn_share_collection")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share Collection")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAnimeDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_anime_to_collection")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Anime to Collection")
            }
        }
    ) { innerPadding ->
        if (animeInCollection.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Collection is Empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add anime from your library to this collection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddAnimeDialog = true },
                        modifier = Modifier.testTag("btn_empty_add_anime")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Anime")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(animeInCollection, key = { it.id }) { anime ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAnimeDetail(anime.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoverImage(
                                coverPath = anime.coverPath,
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(82.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = anime.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                StatusBadge(status = anime.status)
                                Spacer(modifier = Modifier.height(4.dp))
                                StarRatingDisplay(
                                    rating = anime.personalRating,
                                    starSize = 13.dp,
                                    showScoreText = true
                                )
                            }

                            IconButton(
                                onClick = { viewModel.removeAnimeFromCollection(collectionId, anime.id) },
                                modifier = Modifier.testTag("btn_remove_from_collection_${anime.id}")
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove from collection", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // Add Anime Dialog
        if (showAddAnimeDialog) {
            val availableToAdd = uiState.allAnime.filter { a -> animeInCollection.none { it.id == a.id } }
            AlertDialog(
                onDismissRequest = { showAddAnimeDialog = false },
                title = { Text("Select Anime to Add") },
                text = {
                    if (availableToAdd.isEmpty()) {
                        Text("All anime from your library are already in this collection.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableToAdd, key = { it.id }) { anime ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.addAnimeToCollection(collectionId, anime.id)
                                            showAddAnimeDialog = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CoverImage(
                                            coverPath = anime.coverPath,
                                            modifier = Modifier
                                                .width(42.dp)
                                                .height(58.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = anime.title,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                            StarRatingDisplay(rating = anime.personalRating, starSize = 12.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddAnimeDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

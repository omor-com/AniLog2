package com.anilog.omor.dev.ui.anime

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anilog.omor.dev.domain.model.AnimeStatus
import com.anilog.omor.dev.ui.components.CoverImage
import com.anilog.omor.dev.ui.components.StarRatingInput

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditAnimeScreen(
    animeId: Long?,
    viewModel: AddEditViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(animeId) {
        if (animeId != null && animeId > 0) {
            viewModel.loadAnime(animeId)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    // Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onCoverImagePicked(uri)
        }
    }

    var newGenreInput by remember { mutableStateOf("") }
    var newTagInput by remember { mutableStateOf("") }
    var searchQueryInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (animeId != null && animeId > 0) "Edit Anime Entry" else "Add New Anime",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openOnlineSearch() },
                        modifier = Modifier.testTag("btn_online_lookup")
                    ) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = "Online Metadata Lookup")
                    }
                    IconButton(
                        onClick = { viewModel.saveAnime() },
                        modifier = Modifier.testTag("btn_save_anime")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save Anime")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cover Image Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .aspectRatio(0.72f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("cover_image_picker"),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.coverPath != null) {
                        CoverImage(
                            coverPath = uiState.coverPath,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Add Cover", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Custom Anime Cover",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Select an image via Android Photo Picker. Stored securely on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("btn_choose_photo")
                    ) {
                        Text("Choose Artwork")
                    }
                }
            }

            // Title (Required)
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("Anime Title *") },
                isError = uiState.titleError != null,
                supportingText = {
                    uiState.titleError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_anime_title")
            )

            // Original Title (Optional)
            OutlinedTextField(
                value = uiState.originalTitle,
                onValueChange = { viewModel.onOriginalTitleChanged(it) },
                label = { Text("Original / Japanese Title (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Rating Selector
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    StarRatingInput(
                        currentRating = uiState.personalRating,
                        onRatingChange = { viewModel.onRatingChanged(it) }
                    )
                }
            }

            // Watching Status
            Column {
                Text(
                    text = "Watching Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimeStatus.entries.forEach { status ->
                        FilterChip(
                            selected = uiState.status == status,
                            onClick = { viewModel.onStatusChanged(status) },
                            label = { Text(status.displayName) },
                            modifier = Modifier.testTag("status_chip_${status.name.lowercase()}")
                        )
                    }
                }
            }

            // Episodes and Year
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.watchedEpisodes,
                    onValueChange = { viewModel.onWatchedEpisodesChanged(it) },
                    label = { Text("Watched") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_watched_episodes")
                )
                OutlinedTextField(
                    value = uiState.totalEpisodes,
                    onValueChange = { viewModel.onTotalEpisodesChanged(it) },
                    label = { Text("Total Eps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_total_episodes")
                )
                OutlinedTextField(
                    value = uiState.releaseYear,
                    onValueChange = { viewModel.onReleaseYearChanged(it) },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Personal Review (Bangla, Unicode, Multiline preserved exactly)
            OutlinedTextField(
                value = uiState.review,
                onValueChange = { viewModel.onReviewChanged(it) },
                label = { Text("Personal Review") },
                placeholder = { Text("Write your thoughts, impressions, or favorite quotes... (Bangla, English & emojis supported)") },
                minLines = 4,
                maxLines = 10,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_personal_review")
            )

            // Personal Notes
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onNotesChanged(it) },
                label = { Text("Personal Notes & Trivia") },
                placeholder = { Text("Where you watched, recommendations, private comments...") },
                minLines = 2,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            // Genres Selector & Tagging
            Column {
                Text(
                    text = "Genres",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.genres.forEach { genre ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.removeGenre(genre) },
                            label = { Text(genre) },
                            trailingIcon = {
                                Icon(Icons.Filled.Close, contentDescription = "Remove genre", modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newGenreInput,
                        onValueChange = { newGenreInput = it },
                        placeholder = { Text("Add custom genre (e.g. Shounen, Sci-Fi)...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newGenreInput.isNotBlank()) {
                                viewModel.addGenre(newGenreInput)
                                newGenreInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add genre")
                    }
                }
            }

            // Favorite Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add to Favorites",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Highlight this anime in your personal library and top feed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isFavorite,
                    onCheckedChange = { viewModel.onFavoriteChanged(it) },
                    modifier = Modifier.testTag("switch_favorite")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveAnime() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_bottom")
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Anime to Journal", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Online Search Dialog
        if (uiState.showOnlineSearchDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeOnlineSearch() },
                title = { Text("Search Online Anime Metadata") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    ) {
                        Text(
                            text = "Free public Jikan API. Previews title, cover, and episodes. Will never overwrite your personal review, rating, or notes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = searchQueryInput,
                                onValueChange = { searchQueryInput = it },
                                placeholder = { Text("Anime title...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.searchOnline(searchQueryInput) }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (uiState.isSearchingOnline) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (uiState.onlineSearchError != null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = uiState.onlineSearchError ?: "Lookup failed",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.onlineSearchResults) { item ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.applyOnlineMetadata(item) },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CoverImage(
                                                coverPath = item.imageUrl,
                                                modifier = Modifier
                                                    .width(48.dp)
                                                    .height(64.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${item.year ?: "—"} • ${if (item.episodes > 0) "${item.episodes} eps" else "Episodes TBA"}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (item.genres.isNotEmpty()) {
                                                    Text(
                                                        text = item.genres.take(3).joinToString(", "),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.closeOnlineSearch() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

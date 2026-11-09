package com.anilog.omor.dev.ui.comparison

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.ui.components.CoverImage
import com.anilog.omor.dev.ui.components.StarRatingDisplay
import com.anilog.omor.dev.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: CompareViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedA by remember { mutableStateOf(false) }
    var expandedB by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Compare Anime",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.allAnime.size < 2) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.CompareArrows,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Need at least 2 Anime",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add at least two anime entries to your journal to compare them side by side.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selectors row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dropdown A
                    ExposedDropdownMenuBox(
                        expanded = expandedA,
                        onExpandedChange = { expandedA = !expandedA },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.animeA?.title ?: "Select Anime",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Anime A") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedA) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedA,
                            onDismissRequest = { expandedA = false }
                        ) {
                            uiState.allAnime.forEach { a ->
                                DropdownMenuItem(
                                    text = { Text(a.title, maxLines = 1) },
                                    onClick = {
                                        viewModel.selectAnimeA(a.id)
                                        expandedA = false
                                    }
                                )
                            }
                        }
                    }

                    // Dropdown B
                    ExposedDropdownMenuBox(
                        expanded = expandedB,
                        onExpandedChange = { expandedB = !expandedB },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.animeB?.title ?: "Select Anime",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Anime B") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedB) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedB,
                            onDismissRequest = { expandedB = false }
                        ) {
                            uiState.allAnime.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text(b.title, maxLines = 1) },
                                    onClick = {
                                        viewModel.selectAnimeB(b.id)
                                        expandedB = false
                                    }
                                )
                            }
                        }
                    }
                }

                val animeA = uiState.animeA
                val animeB = uiState.animeB

                if (animeA != null && animeB != null) {
                    // Side-by-side Posters & Titles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompareHeaderCard(anime = animeA, modifier = Modifier.weight(1f))
                        CompareHeaderCard(anime = animeB, modifier = Modifier.weight(1f))
                    }

                    // Comparison Table
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CompareRow(
                                label = "Personal Rating",
                                valA = animeA.personalRating?.let { "★ ${"%.1f".format(it)}/10" } ?: "Not rated",
                                valB = animeB.personalRating?.let { "★ ${"%.1f".format(it)}/10" } ?: "Not rated"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            CompareRow(
                                label = "Status",
                                valA = animeA.status.displayName,
                                valB = animeB.status.displayName
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            CompareRow(
                                label = "Episode Progress",
                                valA = "${animeA.watchedEpisodes} / ${if (animeA.totalEpisodes > 0) animeA.totalEpisodes else "TBA"} eps",
                                valB = "${animeB.watchedEpisodes} / ${if (animeB.totalEpisodes > 0) animeB.totalEpisodes else "TBA"} eps"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            CompareRow(
                                label = "Release Year",
                                valA = animeA.releaseYear?.toString() ?: "Not specified",
                                valB = animeB.releaseYear?.toString() ?: "Not specified"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            CompareRow(
                                label = "Genres",
                                valA = if (animeA.genres.isNotEmpty()) animeA.genres.joinToString(", ") else "None specified",
                                valB = if (animeB.genres.isNotEmpty()) animeB.genres.joinToString(", ") else "None specified"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            CompareRow(
                                label = "Review Excerpt",
                                valA = if (animeA.review.isNotBlank()) "“${animeA.review.take(100)}...”" else "No personal review written",
                                valB = if (animeB.review.isNotBlank()) "“${animeB.review.take(100)}...”" else "No personal review written"
                            )
                        }
                    }

                    Text(
                        text = "AniLog Note: Ratings reflect your own personal opinions and journal entries, not community or objective scores.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CompareHeaderCard(
    anime: AnimeEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CoverImage(
                coverPath = anime.coverPath,
                modifier = Modifier
                    .width(90.dp)
                    .height(125.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = anime.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            StatusBadge(status = anime.status)
        }
    }
}

@Composable
fun CompareRow(
    label: String,
    valA: String,
    valB: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = valA,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = valB,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

package com.anilog.omor.dev.ui.library

import androidx.compose.ui.res.stringResource
import com.anilog.omor.dev.R

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anilog.omor.dev.domain.model.AnimeStatus
import com.anilog.omor.dev.domain.model.SortOption
import com.anilog.omor.dev.ui.components.AnimeListCard
import com.anilog.omor.dev.ui.home.EmptyFeedView
import com.anilog.omor.dev.ui.theme.FavoriteHeart
import com.anilog.omor.dev.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onNavigateToAnimeDetail: (Long) -> Unit,
    onNavigateToAddAnime: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_library),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Filter Sheet Trigger
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.testTag("library_filter_button")
                    ) {
                        Box {
                            Icon(Icons.Filled.FilterAlt, contentDescription = "Filters")
                            if (uiState.filterState.activeFilterCount > 0) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(16.dp)
                                        .testTag("filter_badge_count")
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            text = "${uiState.filterState.activeFilterCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sort Order
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("library_sort_button")
                        ) {
                            Icon(Icons.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.displayName,
                                            fontWeight = if (uiState.filterState.sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.filterState.sortOption == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.onSortChange(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.filterState.query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = { Text("Search by title, review, tags...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.filterState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("library_search_input")
            )

            // Horizontal Status & Filter bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorites Chip
                FilterChip(
                    selected = uiState.filterState.onlyFavorites,
                    onClick = { viewModel.toggleFavorites() },
                    leadingIcon = {
                        Icon(
                            imageVector = if (uiState.filterState.onlyFavorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (uiState.filterState.onlyFavorites) FavoriteHeart else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Favorites") },
                    modifier = Modifier.testTag("library_filter_favorites")
                )

                // All Status
                FilterChip(
                    selected = uiState.filterState.status == null,
                    onClick = { viewModel.onStatusChange(null) },
                    label = { Text("All") },
                    modifier = Modifier.testTag("library_filter_status_all")
                )

                // Status Chips
                AnimeStatus.entries.forEach { status ->
                    FilterChip(
                        selected = uiState.filterState.status == status,
                        onClick = {
                            viewModel.onStatusChange(if (uiState.filterState.status == status) null else status)
                        },
                        label = { Text(status.displayName) },
                        modifier = Modifier.testTag("library_filter_status_${status.name.lowercase()}")
                    )
                }

                if (uiState.filterState.activeFilterCount > 0) {
                    OutlinedButton(
                        onClick = { viewModel.resetFilters() },
                        modifier = Modifier.height(32.dp).testTag("library_reset_filters_button")
                    ) {
                        Text("Reset", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Results count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.animeList.size} anime found",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sorted by: ${uiState.filterState.sortOption.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Library List
            if (uiState.animeList.isEmpty()) {
                EmptyFeedView(
                    isSearching = uiState.filterState.activeFilterCount > 0 || uiState.filterState.query.isNotBlank(),
                    onAddClick = onNavigateToAddAnime,
                    onResetSearch = {
                        viewModel.resetFilters()
                        viewModel.onQueryChange("")
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("library_anime_list")
                ) {
                    items(uiState.animeList, key = { it.id }) { anime ->
                        AnimeListCard(
                            anime = anime,
                            onClick = { onNavigateToAnimeDetail(anime.id) },
                            onFavoriteToggle = { viewModel.toggleFavorite(anime.id) }
                        )
                    }
                }
            }
        }

        // Filter Bottom Sheet for Advanced Filters
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Filter Anime",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Minimum Rating Slider
                    Text(
                        text = "Minimum Rating: ${uiState.filterState.minRating?.let { "★ ${"%.1f".format(it)}" } ?: "Any"}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = uiState.filterState.minRating ?: 0f,
                        onValueChange = { viewModel.onMinRatingChange(if (it == 0f) null else it) },
                        valueRange = 0f..10f,
                        steps = 19,
                        modifier = Modifier.testTag("filter_rating_slider")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Genres Filter
                    if (uiState.availableGenres.isNotEmpty()) {
                        Text(
                            text = "Genre",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = uiState.filterState.selectedGenre == null,
                                onClick = { viewModel.onGenreChange(null) },
                                label = { Text("All Genres") }
                            )
                            uiState.availableGenres.forEach { genre ->
                                FilterChip(
                                    selected = uiState.filterState.selectedGenre == genre,
                                    onClick = {
                                        viewModel.onGenreChange(if (uiState.filterState.selectedGenre == genre) null else genre)
                                    },
                                    label = { Text(genre) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Release Year Filter
                    if (uiState.availableYears.isNotEmpty()) {
                        Text(
                            text = "Release Year",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = uiState.filterState.selectedYear == null,
                                onClick = { viewModel.onYearChange(null) },
                                label = { Text("All Years") }
                            )
                            uiState.availableYears.forEach { year ->
                                FilterChip(
                                    selected = uiState.filterState.selectedYear == year,
                                    onClick = {
                                        viewModel.onYearChange(if (uiState.filterState.selectedYear == year) null else year)
                                    },
                                    label = { Text("$year") }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.resetFilters()
                                showFilterSheet = false
                            }
                        ) {
                            Text("Reset All")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { showFilterSheet = false },
                            modifier = Modifier.testTag("apply_filters_button")
                        ) {
                            Text("Apply Filters")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

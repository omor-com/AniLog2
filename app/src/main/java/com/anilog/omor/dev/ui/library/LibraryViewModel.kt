package com.anilog.omor.dev.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.database.entity.CollectionEntity
import com.anilog.omor.dev.data.repository.AnimeRepository
import com.anilog.omor.dev.domain.model.AnimeStatus
import com.anilog.omor.dev.domain.model.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryFilterState(
    val query: String = "",
    val status: AnimeStatus? = null,
    val onlyFavorites: Boolean = false,
    val minRating: Float? = null, // e.g. 8.0f
    val selectedGenre: String? = null,
    val selectedYear: Int? = null,
    val selectedCollectionId: Long? = null,
    val sortOption: SortOption = SortOption.HIGHEST_RATED
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (status != null) count++
            if (onlyFavorites) count++
            if (minRating != null) count++
            if (selectedGenre != null) count++
            if (selectedYear != null) count++
            if (selectedCollectionId != null) count++
            return count
        }
}

data class LibraryUiState(
    val animeList: List<AnimeEntity> = emptyList(),
    val collections: List<CollectionEntity> = emptyList(),
    val availableGenres: List<String> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val filterState: LibraryFilterState = LibraryFilterState(),
    val isLoading: Boolean = false
)

class LibraryViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(LibraryFilterState())

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.allAnime,
        repository.allCollections,
        _filterState
    ) { allAnime, collections, filters ->
        // Extract genres and years for filter menus
        val allGenres = allAnime.flatMap { it.genres }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        val allYears = allAnime.mapNotNull { it.releaseYear }
            .distinct()
            .sortedDescending()

        var filtered = allAnime

        // Query search
        if (filters.query.isNotBlank()) {
            val q = filters.query.trim().lowercase()
            filtered = filtered.filter { anime ->
                anime.title.lowercase().contains(q) ||
                    anime.originalTitle.lowercase().contains(q) ||
                    anime.review.lowercase().contains(q) ||
                    anime.notes.lowercase().contains(q) ||
                    anime.genres.any { it.lowercase().contains(q) } ||
                    anime.tags.any { it.lowercase().contains(q) }
            }
        }

        // Status
        if (filters.status != null) {
            filtered = filtered.filter { it.status == filters.status }
        }

        // Favorites
        if (filters.onlyFavorites) {
            filtered = filtered.filter { it.isFavorite }
        }

        // Min rating
        if (filters.minRating != null) {
            filtered = filtered.filter { anime ->
                anime.personalRating != null && anime.personalRating >= filters.minRating
            }
        }

        // Genre
        if (filters.selectedGenre != null) {
            filtered = filtered.filter { anime ->
                anime.genres.any { it.equals(filters.selectedGenre, ignoreCase = true) }
            }
        }

        // Year
        if (filters.selectedYear != null) {
            filtered = filtered.filter { it.releaseYear == filters.selectedYear }
        }

        // Sort
        val sorted = AnimeRepository.sortAnimeList(filtered, filters.sortOption)

        LibraryUiState(
            animeList = sorted,
            collections = collections,
            availableGenres = allGenres,
            availableYears = allYears,
            filterState = filters,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState(isLoading = true)
    )

    fun onQueryChange(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun onStatusChange(status: AnimeStatus?) {
        _filterState.value = _filterState.value.copy(status = status)
    }

    fun toggleFavorites() {
        _filterState.value = _filterState.value.copy(onlyFavorites = !_filterState.value.onlyFavorites)
    }

    fun onMinRatingChange(rating: Float?) {
        _filterState.value = _filterState.value.copy(minRating = rating)
    }

    fun onGenreChange(genre: String?) {
        _filterState.value = _filterState.value.copy(selectedGenre = genre)
    }

    fun onYearChange(year: Int?) {
        _filterState.value = _filterState.value.copy(selectedYear = year)
    }

    fun onSortChange(sort: SortOption) {
        _filterState.value = _filterState.value.copy(sortOption = sort)
    }

    fun resetFilters() {
        _filterState.value = LibraryFilterState(query = _filterState.value.query)
    }

    fun toggleFavorite(animeId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(animeId)
        }
    }
}

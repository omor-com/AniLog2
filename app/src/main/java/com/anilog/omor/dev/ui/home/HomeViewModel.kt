package com.anilog.omor.dev.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.repository.AnimeRepository
import com.anilog.omor.dev.data.repository.UserPreferencesRepository
import com.anilog.omor.dev.domain.model.AnimeStatus
import com.anilog.omor.dev.domain.model.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val animeList: List<AnimeEntity> = emptyList(),
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val watchingCount: Int = 0,
    val averageRating: Float? = null,
    val searchQuery: String = "",
    val selectedStatus: AnimeStatus? = null,
    val sortOption: SortOption = SortOption.HIGHEST_RATED,
    val isGridView: Boolean = false,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: AnimeRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatus = MutableStateFlow<AnimeStatus?>(null)
    private val _sortOption = MutableStateFlow(SortOption.HIGHEST_RATED)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.allAnime,
        _searchQuery,
        _selectedStatus,
        _sortOption,
        preferencesRepository.isGridView
    ) { allList, query, statusFilter, sort, isGrid ->
        var filtered = allList

        // Search across title, originalTitle, review, notes, genres, tags
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter { anime ->
                anime.title.lowercase().contains(q) ||
                    anime.originalTitle.lowercase().contains(q) ||
                    anime.review.lowercase().contains(q) ||
                    anime.notes.lowercase().contains(q) ||
                    anime.genres.any { it.lowercase().contains(q) } ||
                    anime.tags.any { it.lowercase().contains(q) }
            }
        }

        // Status filter
        if (statusFilter != null) {
            filtered = filtered.filter { it.status == statusFilter }
        }

        // Sorting (Highest rated by default, tie breaker: most recently added)
        val sorted = AnimeRepository.sortAnimeList(filtered, sort)

        // Summary metrics
        val completed = allList.count { it.status == AnimeStatus.COMPLETED }
        val watching = allList.count { it.status == AnimeStatus.WATCHING }
        val ratedList = allList.mapNotNull { it.personalRating }
        val avgRating = if (ratedList.isNotEmpty()) ratedList.average().toFloat() else null

        HomeUiState(
            animeList = sorted,
            totalCount = allList.size,
            completedCount = completed,
            watchingCount = watching,
            averageRating = avgRating,
            searchQuery = query,
            selectedStatus = statusFilter,
            sortOption = sort,
            isGridView = isGrid,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusSelected(status: AnimeStatus?) {
        _selectedStatus.value = if (_selectedStatus.value == status) null else status
    }

    fun onSortOptionChanged(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleViewMode() {
        viewModelScope.launch {
            preferencesRepository.setGridView(!uiState.value.isGridView)
        }
    }

    fun toggleFavorite(animeId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(animeId)
        }
    }
}

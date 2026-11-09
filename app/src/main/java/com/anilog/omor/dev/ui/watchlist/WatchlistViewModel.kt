package com.anilog.omor.dev.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.repository.AnimeRepository
import com.anilog.omor.dev.domain.model.AnimeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WatchlistUiState(
    val animeList: List<AnimeEntity> = emptyList(),
    val selectedStatus: AnimeStatus = AnimeStatus.WATCHING,
    val pendingCompletionAnime: AnimeEntity? = null,
    val isLoading: Boolean = false
)

class WatchlistViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _selectedStatus = MutableStateFlow(AnimeStatus.WATCHING)
    private val _pendingCompletionAnime = MutableStateFlow<AnimeEntity?>(null)

    val uiState: StateFlow<WatchlistUiState> = combine(
        repository.allAnime,
        _selectedStatus,
        _pendingCompletionAnime
    ) { allAnime, status, pendingCompletion ->
        val filtered = allAnime
            .filter { it.status == status }
            .sortedByDescending { it.updatedAt }

        WatchlistUiState(
            animeList = filtered,
            selectedStatus = status,
            pendingCompletionAnime = pendingCompletion,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WatchlistUiState(isLoading = true)
    )

    fun onStatusSelected(status: AnimeStatus) {
        _selectedStatus.value = status
    }

    fun incrementEpisode(anime: AnimeEntity) {
        val next = anime.watchedEpisodes + 1
        val isReachingTotal = anime.totalEpisodes > 0 && next >= anime.totalEpisodes

        viewModelScope.launch {
            repository.updateEpisodeProgress(anime.id, next)
            if (isReachingTotal && anime.status != AnimeStatus.COMPLETED) {
                // Show completion prompt dialog
                _pendingCompletionAnime.value = anime.copy(watchedEpisodes = next)
            }
        }
    }

    fun decrementEpisode(anime: AnimeEntity) {
        if (anime.watchedEpisodes <= 0) return
        val prev = anime.watchedEpisodes - 1
        viewModelScope.launch {
            repository.updateEpisodeProgress(anime.id, prev)
        }
    }

    fun setDirectProgress(animeId: Long, progress: Int) {
        val valid = progress.coerceAtLeast(0)
        viewModelScope.launch {
            repository.updateEpisodeProgress(animeId, valid)
        }
    }

    fun markAsCompleted(anime: AnimeEntity, setTodayAsCompletionDate: Boolean) {
        viewModelScope.launch {
            val updated = anime.copy(
                status = AnimeStatus.COMPLETED,
                watchedEpisodes = if (anime.totalEpisodes > 0) anime.totalEpisodes else anime.watchedEpisodes,
                completionDate = if (setTodayAsCompletionDate) System.currentTimeMillis() else anime.completionDate,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateAnime(updated)
            _pendingCompletionAnime.value = null
        }
    }

    fun dismissCompletionDialog() {
        _pendingCompletionAnime.value = null
    }

    fun updateStatus(anime: AnimeEntity, newStatus: AnimeStatus) {
        viewModelScope.launch {
            val isNowCompleted = newStatus == AnimeStatus.COMPLETED
            val updated = anime.copy(
                status = newStatus,
                completionDate = if (isNowCompleted && anime.completionDate == null) System.currentTimeMillis() else anime.completionDate,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateAnime(updated)
        }
    }
}

package com.anilog.omor.dev.ui.anime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.database.entity.CollectionEntity
import com.anilog.omor.dev.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AnimeDetailUiState(
    val anime: AnimeEntity? = null,
    val collections: List<CollectionEntity> = emptyList(),
    val showDeleteDialog: Boolean = false,
    val showAddToCollectionDialog: Boolean = false,
    val isDeleted: Boolean = false,
    val isLoading: Boolean = false
)

class AnimeDetailViewModel(
    private val animeId: Long,
    private val repository: AnimeRepository
) : ViewModel() {

    private val _showDeleteDialog = MutableStateFlow(false)
    private val _showAddToCollectionDialog = MutableStateFlow(false)
    private val _isDeleted = MutableStateFlow(false)

    val uiState: StateFlow<AnimeDetailUiState> = combine(
        repository.getAnimeById(animeId),
        repository.allCollections,
        _showDeleteDialog,
        _showAddToCollectionDialog,
        _isDeleted
    ) { anime, collections, showDelete, showAddCollection, isDeleted ->
        AnimeDetailUiState(
            anime = anime,
            collections = collections,
            showDeleteDialog = showDelete,
            showAddToCollectionDialog = showAddCollection,
            isDeleted = isDeleted,
            isLoading = anime == null && !isDeleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnimeDetailUiState(isLoading = true)
    )

    fun toggleFavorite() {
        val currentAnime = uiState.value.anime ?: return
        viewModelScope.launch {
            repository.toggleFavorite(currentAnime.id)
        }
    }

    fun showDeleteConfirmation(show: Boolean) {
        _showDeleteDialog.value = show
    }

    fun showAddToCollection(show: Boolean) {
        _showAddToCollectionDialog.value = show
    }

    fun deleteAnime() {
        val currentAnime = uiState.value.anime ?: return
        viewModelScope.launch {
            repository.deleteAnime(currentAnime)
            _showDeleteDialog.value = false
            _isDeleted.value = true
        }
    }

    fun addToCollection(collectionId: Long) {
        val currentAnime = uiState.value.anime ?: return
        viewModelScope.launch {
            repository.addAnimeToCollection(collectionId, currentAnime.id)
            _showAddToCollectionDialog.value = false
        }
    }
}

package com.anilog.omor.dev.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.database.entity.CollectionEntity
import com.anilog.omor.dev.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionsUiState(
    val collections: List<CollectionEntity> = emptyList(),
    val allAnime: List<AnimeEntity> = emptyList(),
    val showCreateDialog: Boolean = false,
    val isLoading: Boolean = false
)

class CollectionsViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _showCreateDialog = MutableStateFlow(false)

    val uiState: StateFlow<CollectionsUiState> = combine(
        repository.allCollections,
        repository.allAnime,
        _showCreateDialog
    ) { collections, anime, showCreate ->
        CollectionsUiState(
            collections = collections,
            allAnime = anime,
            showCreateDialog = showCreate,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CollectionsUiState(isLoading = true)
    )

    fun showCreateDialog(show: Boolean) {
        _showCreateDialog.value = show
    }

    fun createCollection(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createCollection(name.trim(), description.trim())
            _showCreateDialog.value = false
        }
    }

    fun deleteCollection(collection: CollectionEntity) {
        viewModelScope.launch {
            repository.deleteCollection(collection)
        }
    }

    fun getAnimeForCollection(collectionId: Long) = repository.getAnimeForCollection(collectionId)

    fun removeAnimeFromCollection(collectionId: Long, animeId: Long) {
        viewModelScope.launch {
            repository.removeAnimeFromCollection(collectionId, animeId)
        }
    }

    fun addAnimeToCollection(collectionId: Long, animeId: Long) {
        viewModelScope.launch {
            repository.addAnimeToCollection(collectionId, animeId)
        }
    }

    fun updateCollection(collection: CollectionEntity) {
        viewModelScope.launch {
            repository.updateCollection(collection)
        }
    }
}

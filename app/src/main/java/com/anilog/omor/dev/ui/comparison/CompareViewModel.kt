package com.anilog.omor.dev.ui.comparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CompareUiState(
    val allAnime: List<AnimeEntity> = emptyList(),
    val animeA: AnimeEntity? = null,
    val animeB: AnimeEntity? = null
)

class CompareViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _selectedAnimeAId = MutableStateFlow<Long?>(null)
    private val _selectedAnimeBId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<CompareUiState> = combine(
        repository.allAnime,
        _selectedAnimeAId,
        _selectedAnimeBId
    ) { list, idA, idB ->
        val itemA = list.firstOrNull { it.id == idA } ?: list.getOrNull(0)
        val itemB = list.firstOrNull { it.id == idB } ?: list.getOrNull(1)

        CompareUiState(
            allAnime = list,
            animeA = itemA,
            animeB = itemB
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CompareUiState()
    )

    fun selectAnimeA(id: Long) {
        _selectedAnimeAId.value = id
    }

    fun selectAnimeB(id: Long) {
        _selectedAnimeBId.value = id
    }
}

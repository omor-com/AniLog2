package com.anilog.omor.dev.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.repository.AnimeRepository
import com.anilog.omor.dev.domain.model.AnimeStatistics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    val statistics: StateFlow<AnimeStatistics> = repository.getStatistics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnimeStatistics()
        )
}

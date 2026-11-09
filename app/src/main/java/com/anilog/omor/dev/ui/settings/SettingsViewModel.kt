package com.anilog.omor.dev.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.repository.UserPreferencesRepository
import com.anilog.omor.dev.domain.model.AppThemeMode
import com.anilog.omor.dev.domain.model.SortOption
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val defaultSort: SortOption = SortOption.HIGHEST_RATED,
    val isPasscodeEnabled: Boolean = false,
    val hasPasscodeSet: Boolean = false,
    val passcodeResetTimestamp: Long = 0L
)

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.themeMode,
        preferencesRepository.defaultSort,
        preferencesRepository.isPasscodeEnabled,
        preferencesRepository.passcodeHash,
        preferencesRepository.passcodeResetTimestamp
    ) { theme, sort, isPassEnabled, hash, resetTimestamp ->
        SettingsUiState(
            themeMode = theme,
            defaultSort = sort,
            isPasscodeEnabled = isPassEnabled,
            hasPasscodeSet = !hash.isNullOrBlank(),
            passcodeResetTimestamp = resetTimestamp
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun getRemainingCooldownSeconds(): Long {
        return preferencesRepository.getRemainingCooldownSeconds(uiState.value.passcodeResetTimestamp)
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setDefaultSort(sort: SortOption) {
        viewModelScope.launch {
            preferencesRepository.setDefaultSort(sort)
        }
    }

    fun setPasscode(pin: String?) {
        viewModelScope.launch {
            preferencesRepository.setPasscode(pin)
        }
    }

    fun togglePasscodeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPasscodeEnabled(enabled)
        }
    }
}

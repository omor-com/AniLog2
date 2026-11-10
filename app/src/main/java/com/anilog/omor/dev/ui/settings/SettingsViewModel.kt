package com.anilog.omor.dev.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.repository.UserPreferencesRepository
import com.anilog.omor.dev.domain.model.AppThemeMode
import com.anilog.omor.dev.domain.model.SortOption
import com.anilog.omor.dev.util.LocaleHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val defaultSort: SortOption = SortOption.RAFAEL_RANKING,
    val isPasscodeEnabled: Boolean = false,
    val hasPasscodeSet: Boolean = false,
    val recoveryDurationMinutes: Int = 5,
    val activeRecoveryDeadline: Long = 0L,
    val appLanguage: String = LocaleHelper.LANGUAGE_ENGLISH,
    val rafaelEnabled: Boolean = true
)

private data class GeneralPrefs(
    val theme: AppThemeMode,
    val sort: SortOption,
    val lang: String,
    val rafael: Boolean
)

private data class SecurityPrefs(
    val isPassEnabled: Boolean,
    val hash: String?,
    val duration: Int,
    val deadline: Long
)

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val generalFlow = combine(
        preferencesRepository.themeMode,
        preferencesRepository.defaultSort,
        preferencesRepository.appLanguage,
        preferencesRepository.rafaelEnabled
    ) { theme, sort, lang, rafael ->
        GeneralPrefs(theme, sort, lang, rafael)
    }

    private val securityFlow = combine(
        preferencesRepository.isPasscodeEnabled,
        preferencesRepository.passcodeHash,
        preferencesRepository.recoveryDurationMinutes,
        preferencesRepository.activeRecoveryDeadline
    ) { isPassEnabled, hash, duration, deadline ->
        SecurityPrefs(isPassEnabled, hash, duration, deadline)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        generalFlow,
        securityFlow
    ) { general, security ->
        SettingsUiState(
            themeMode = general.theme,
            defaultSort = general.sort,
            isPasscodeEnabled = security.isPassEnabled,
            hasPasscodeSet = !security.hash.isNullOrBlank(),
            recoveryDurationMinutes = security.duration,
            activeRecoveryDeadline = security.deadline,
            appLanguage = general.lang,
            rafaelEnabled = general.rafael
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

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

    fun setAppLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesRepository.setAppLanguage(languageCode)
        }
    }

    fun setRecoveryDurationMinutes(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setRecoveryDurationMinutes(minutes)
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

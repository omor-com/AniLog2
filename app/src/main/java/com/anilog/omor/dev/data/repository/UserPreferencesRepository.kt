package com.anilog.omor.dev.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anilog.omor.dev.domain.model.AppThemeMode
import com.anilog.omor.dev.domain.model.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.MessageDigest

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "anilog_user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PASSCODE_HASH = stringPreferencesKey("passcode_hash")
        val PASSCODE_ENABLED = booleanPreferencesKey("passcode_enabled")
        val PASSCODE_RESET_TIMESTAMP = androidx.datastore.preferences.core.longPreferencesKey("passcode_reset_timestamp")
        val DEFAULT_SORT = stringPreferencesKey("default_sort")
        val IS_GRID_VIEW = booleanPreferencesKey("is_grid_view")
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            AppThemeMode.fromString(preferences[PreferencesKeys.THEME_MODE])
        }

    val isPasscodeEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PASSCODE_ENABLED] ?: false
        }

    val passcodeHash: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PASSCODE_HASH]
        }

    val passcodeResetTimestamp: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PASSCODE_RESET_TIMESTAMP] ?: 0L
        }

    val defaultSort: Flow<SortOption> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            SortOption.fromString(preferences[PreferencesKeys.DEFAULT_SORT])
        }

    val isGridView: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_GRID_VIEW] ?: false
        }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setPasscode(pin: String?) {
        context.dataStore.edit { preferences ->
            if (pin.isNullOrBlank()) {
                preferences[PreferencesKeys.PASSCODE_HASH] = ""
                preferences[PreferencesKeys.PASSCODE_ENABLED] = false
            } else {
                preferences[PreferencesKeys.PASSCODE_HASH] = hashPin(pin)
                preferences[PreferencesKeys.PASSCODE_ENABLED] = true
                preferences[PreferencesKeys.PASSCODE_RESET_TIMESTAMP] = 0L
            }
        }
    }

    suspend fun resetForgottenPasscode() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PASSCODE_HASH] = ""
            preferences[PreferencesKeys.PASSCODE_ENABLED] = false
            preferences[PreferencesKeys.PASSCODE_RESET_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    fun getRemainingCooldownSeconds(resetTimestamp: Long): Long {
        if (resetTimestamp <= 0L) return 0L
        val elapsed = System.currentTimeMillis() - resetTimestamp
        val fiveMinutesMillis = 5 * 60 * 1000L // 300,000 ms
        val remainingMillis = fiveMinutesMillis - elapsed
        return if (remainingMillis > 0L) (remainingMillis / 1000L) else 0L
    }

    suspend fun setPasscodeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PASSCODE_ENABLED] = enabled
        }
    }

    suspend fun verifyPin(pin: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrBlank()) return true
        return hashPin(pin) == storedHash
    }

    suspend fun setDefaultSort(sortOption: SortOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_SORT] = sortOption.name
        }
    }

    suspend fun setGridView(isGrid: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_GRID_VIEW] = isGrid
        }
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

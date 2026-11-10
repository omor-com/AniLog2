package com.anilog.omor.dev.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anilog.omor.dev.domain.model.AppThemeMode
import com.anilog.omor.dev.domain.model.SortOption
import com.anilog.omor.dev.util.LocaleHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.MessageDigest

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "anilog_user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PASSCODE_HASH = stringPreferencesKey("passcode_hash")
        val PASSCODE_ENABLED = booleanPreferencesKey("passcode_enabled")
        val PASSCODE_RESET_TIMESTAMP = longPreferencesKey("passcode_reset_timestamp")
        val PASSCODE_RECOVERY_DURATION_MINUTES = intPreferencesKey("passcode_recovery_duration_minutes")
        val ACTIVE_RECOVERY_DEADLINE = longPreferencesKey("active_recovery_deadline")
        val DEFAULT_SORT = stringPreferencesKey("default_sort")
        val IS_GRID_VIEW = booleanPreferencesKey("is_grid_view")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val HAS_COMPLETED_LANGUAGE_SELECTION = booleanPreferencesKey("has_completed_language_selection")

        // Rafael ranking engine preferences
        val RAFAEL_ENABLED = booleanPreferencesKey("rafael_enabled")
        val RAFAEL_SIGNAL_FAVORITE = booleanPreferencesKey("rafael_signal_favorite")
        val RAFAEL_SIGNAL_COMPLETION = booleanPreferencesKey("rafael_signal_completion")
        val RAFAEL_SIGNAL_REVIEW_DEPTH = booleanPreferencesKey("rafael_signal_review_depth")
        val RAFAEL_SIGNAL_PROGRESS = booleanPreferencesKey("rafael_signal_progress")
        val RAFAEL_EXPLICIT_PREFERENCES = stringPreferencesKey("rafael_explicit_preferences")
    }

    // Language
    val appLanguage: Flow<String> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.APP_LANGUAGE] ?: LocaleHelper.LANGUAGE_ENGLISH }

    val hasCompletedLanguageSelection: Flow<Boolean> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.HAS_COMPLETED_LANGUAGE_SELECTION] ?: false }

    suspend fun setAppLanguage(languageCode: String) {
        val validCode = if (languageCode == LocaleHelper.LANGUAGE_BENGALI) LocaleHelper.LANGUAGE_BENGALI else LocaleHelper.LANGUAGE_ENGLISH
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LANGUAGE] = validCode
            preferences[PreferencesKeys.HAS_COMPLETED_LANGUAGE_SELECTION] = true
        }
        LocaleHelper.applyConfiguration(context, validCode)
    }

    suspend fun completeLanguageSelection(languageCode: String) {
        setAppLanguage(languageCode)
    }

    // Theme & Views
    val themeMode: Flow<AppThemeMode> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> AppThemeMode.fromString(preferences[PreferencesKeys.THEME_MODE]) }

    val isPasscodeEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.PASSCODE_ENABLED] ?: false }

    val passcodeHash: Flow<String?> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.PASSCODE_HASH] }

    val passcodeResetTimestamp: Flow<Long> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.PASSCODE_RESET_TIMESTAMP] ?: 0L }

    val recoveryDurationMinutes: Flow<Int> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> (preferences[PreferencesKeys.PASSCODE_RECOVERY_DURATION_MINUTES] ?: 5).coerceIn(5, 60) }

    val activeRecoveryDeadline: Flow<Long> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.ACTIVE_RECOVERY_DEADLINE] ?: 0L }

    val defaultSort: Flow<SortOption> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> SortOption.fromString(preferences[PreferencesKeys.DEFAULT_SORT]) }

    val isGridView: Flow<Boolean> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.IS_GRID_VIEW] ?: false }

    // Rafael Ranking Engine Settings
    val rafaelEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.RAFAEL_ENABLED] ?: true }

    val rafaelSignalFavorite: Flow<Boolean> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.RAFAEL_SIGNAL_FAVORITE] ?: true }

    val rafaelSignalCompletion: Flow<Boolean> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.RAFAEL_SIGNAL_COMPLETION] ?: true }

    val rafaelSignalReviewDepth: Flow<Boolean> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.RAFAEL_SIGNAL_REVIEW_DEPTH] ?: true }

    val rafaelSignalProgress: Flow<Boolean> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.RAFAEL_SIGNAL_PROGRESS] ?: true }

    val rafaelExplicitPreferences: Flow<String> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences -> preferences[PreferencesKeys.RAFAEL_EXPLICIT_PREFERENCES] ?: "" }

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
                preferences[PreferencesKeys.ACTIVE_RECOVERY_DEADLINE] = 0L
            }
        }
    }

    suspend fun setRecoveryDurationMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(5, 60)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PASSCODE_RECOVERY_DURATION_MINUTES] = clamped
        }
    }

    /**
     * Start enforced security waiting period for forgotten passcode.
     * Enforces deadline = now + duration (between 5 and 60 minutes).
     * Clears old passcode hash and immediately blocks access to protected content.
     */
    suspend fun startForgottenPasscodeRecovery(now: Long = System.currentTimeMillis()): Long {
        var deadline = 0L
        context.dataStore.edit { preferences ->
            val configuredMinutes = (preferences[PreferencesKeys.PASSCODE_RECOVERY_DURATION_MINUTES] ?: 5).coerceIn(5, 60)
            val durationMillis = configuredMinutes * 60 * 1000L
            deadline = now + durationMillis

            preferences[PreferencesKeys.PASSCODE_HASH] = ""
            preferences[PreferencesKeys.PASSCODE_ENABLED] = false
            preferences[PreferencesKeys.PASSCODE_RESET_TIMESTAMP] = now
            preferences[PreferencesKeys.ACTIVE_RECOVERY_DEADLINE] = deadline
        }
        return deadline
    }

    suspend fun completeRecovery() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_RECOVERY_DEADLINE] = 0L
        }
    }

    fun isRecoveryActive(deadline: Long, now: Long = System.currentTimeMillis()): Boolean {
        return deadline > 0L && now < deadline
    }

    fun getRemainingRecoverySeconds(deadline: Long, now: Long = System.currentTimeMillis()): Long {
        if (deadline <= 0L || now >= deadline) return 0L
        return (deadline - now) / 1000L
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

    // Rafael ranking engine configuration mutations
    suspend fun setRafaelEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.RAFAEL_ENABLED] = enabled }
    }

    suspend fun setRafaelSignals(
        favorite: Boolean,
        completion: Boolean,
        reviewDepth: Boolean,
        progress: Boolean
    ) {
        context.dataStore.edit {
            it[PreferencesKeys.RAFAEL_SIGNAL_FAVORITE] = favorite
            it[PreferencesKeys.RAFAEL_SIGNAL_COMPLETION] = completion
            it[PreferencesKeys.RAFAEL_SIGNAL_REVIEW_DEPTH] = reviewDepth
            it[PreferencesKeys.RAFAEL_SIGNAL_PROGRESS] = progress
        }
    }

    suspend fun recordRafaelPairwisePreference(higherAnimeId: Long, lowerAnimeId: Long) {
        context.dataStore.edit { prefs ->
            val existing = prefs[PreferencesKeys.RAFAEL_EXPLICIT_PREFERENCES] ?: ""
            val pairs = existing.split(",").filter { it.isNotBlank() }.toMutableList()
            // Format: A>B
            pairs.removeAll { it.startsWith("$higherAnimeId>$lowerAnimeId") || it.startsWith("$lowerAnimeId>$higherAnimeId") }
            pairs.add("$higherAnimeId>$lowerAnimeId")
            prefs[PreferencesKeys.RAFAEL_EXPLICIT_PREFERENCES] = pairs.joinToString(",")
        }
    }

    suspend fun resetRafaelPreferences() {
        context.dataStore.edit {
            it[PreferencesKeys.RAFAEL_EXPLICIT_PREFERENCES] = ""
            it[PreferencesKeys.RAFAEL_SIGNAL_FAVORITE] = true
            it[PreferencesKeys.RAFAEL_SIGNAL_COMPLETION] = true
            it[PreferencesKeys.RAFAEL_SIGNAL_REVIEW_DEPTH] = true
            it[PreferencesKeys.RAFAEL_SIGNAL_PROGRESS] = true
        }
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

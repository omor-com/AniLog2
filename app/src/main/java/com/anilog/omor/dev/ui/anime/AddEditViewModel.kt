package com.anilog.omor.dev.ui.anime

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.remote.JikanApiService
import com.anilog.omor.dev.data.remote.OnlineAnimeMetadata
import com.anilog.omor.dev.data.repository.AnimeRepository
import com.anilog.omor.dev.domain.model.AnimeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditUiState(
    val animeId: Long? = null,
    val title: String = "",
    val originalTitle: String = "",
    val coverPath: String? = null,
    val personalRating: Float? = null,
    val review: String = "",
    val notes: String = "",
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val releaseYear: String = "",
    val totalEpisodes: String = "",
    val watchedEpisodes: String = "0",
    val status: AnimeStatus = AnimeStatus.PLAN_TO_WATCH,
    val startDate: Long? = null,
    val completionDate: Long? = null,
    val isFavorite: Boolean = false,
    val titleError: String? = null,
    val isSaved: Boolean = false,
    // Online lookup state
    val isSearchingOnline: Boolean = false,
    val onlineSearchResults: List<OnlineAnimeMetadata> = emptyList(),
    val onlineSearchError: String? = null,
    val showOnlineSearchDialog: Boolean = false
)

class AddEditViewModel(
    private val repository: AnimeRepository,
    private val jikanApiService: JikanApiService = JikanApiService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    fun loadAnime(id: Long?) {
        if (id == null || id <= 0L) return
        viewModelScope.launch {
            val anime = repository.getAnimeByIdOnce(id) ?: return@launch
            _uiState.value = AddEditUiState(
                animeId = anime.id,
                title = anime.title,
                originalTitle = anime.originalTitle,
                coverPath = anime.coverPath,
                personalRating = anime.personalRating,
                review = anime.review,
                notes = anime.notes,
                genres = anime.genres,
                tags = anime.tags,
                releaseYear = anime.releaseYear?.toString() ?: "",
                totalEpisodes = if (anime.totalEpisodes > 0) anime.totalEpisodes.toString() else "",
                watchedEpisodes = anime.watchedEpisodes.toString(),
                status = anime.status,
                startDate = anime.startDate,
                completionDate = anime.completionDate,
                isFavorite = anime.isFavorite
            )
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = if (title.isBlank()) "Anime title is required" else null
        )
    }

    fun onOriginalTitleChanged(originalTitle: String) {
        _uiState.value = _uiState.value.copy(originalTitle = originalTitle)
    }

    fun onRatingChanged(rating: Float?) {
        _uiState.value = _uiState.value.copy(personalRating = rating)
    }

    fun onReviewChanged(review: String) {
        _uiState.value = _uiState.value.copy(review = review)
    }

    fun onNotesChanged(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun onStatusChanged(status: AnimeStatus) {
        _uiState.value = _uiState.value.copy(
            status = status,
            completionDate = if (status == AnimeStatus.COMPLETED && _uiState.value.completionDate == null) {
                System.currentTimeMillis()
            } else _uiState.value.completionDate
        )
    }

    fun onReleaseYearChanged(year: String) {
        val filtered = year.filter { it.isDigit() }.take(4)
        _uiState.value = _uiState.value.copy(releaseYear = filtered)
    }

    fun onTotalEpisodesChanged(total: String) {
        val filtered = total.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(totalEpisodes = filtered)
    }

    fun onWatchedEpisodesChanged(watched: String) {
        val filtered = watched.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(watchedEpisodes = filtered)
    }

    fun onFavoriteChanged(isFav: Boolean) {
        _uiState.value = _uiState.value.copy(isFavorite = isFav)
    }

    fun onStartDateChanged(date: Long?) {
        _uiState.value = _uiState.value.copy(startDate = date)
    }

    fun onCompletionDateChanged(date: Long?) {
        _uiState.value = _uiState.value.copy(completionDate = date)
    }

    fun onCoverImagePicked(uri: Uri) {
        viewModelScope.launch {
            val localPath = repository.saveCoverImage(uri)
            if (localPath != null) {
                _uiState.value = _uiState.value.copy(coverPath = localPath)
            }
        }
    }

    fun addGenre(genre: String) {
        val trimmed = genre.trim()
        if (trimmed.isNotBlank() && !_uiState.value.genres.contains(trimmed)) {
            _uiState.value = _uiState.value.copy(genres = _uiState.value.genres + trimmed)
        }
    }

    fun removeGenre(genre: String) {
        _uiState.value = _uiState.value.copy(genres = _uiState.value.genres - genre)
    }

    fun addTag(tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isNotBlank() && !_uiState.value.tags.contains(trimmed)) {
            _uiState.value = _uiState.value.copy(tags = _uiState.value.tags + trimmed)
        }
    }

    fun removeTag(tag: String) {
        _uiState.value = _uiState.value.copy(tags = _uiState.value.tags - tag)
    }

    // Online lookup
    fun openOnlineSearch() {
        _uiState.value = _uiState.value.copy(showOnlineSearchDialog = true, onlineSearchError = null)
        if (_uiState.value.title.isNotBlank()) {
            searchOnline(_uiState.value.title)
        }
    }

    fun closeOnlineSearch() {
        _uiState.value = _uiState.value.copy(showOnlineSearchDialog = false, onlineSearchResults = emptyList())
    }

    fun searchOnline(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearchingOnline = true, onlineSearchError = null)
            val result = jikanApiService.searchAnime(query)
            result.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    isSearchingOnline = false,
                    onlineSearchResults = list
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isSearchingOnline = false,
                    onlineSearchError = err.localizedMessage ?: "Failed to connect to online lookup"
                )
            }
        }
    }

    fun applyOnlineMetadata(metadata: OnlineAnimeMetadata) {
        // Apply metadata without overwriting personal rating, review, or notes!
        val currentGenres = _uiState.value.genres.toMutableList()
        metadata.genres.forEach { if (!currentGenres.contains(it)) currentGenres.add(it) }

        _uiState.value = _uiState.value.copy(
            title = if (_uiState.value.title.isBlank()) metadata.title else _uiState.value.title,
            originalTitle = if (_uiState.value.originalTitle.isBlank()) metadata.originalTitle else _uiState.value.originalTitle,
            coverPath = _uiState.value.coverPath ?: metadata.imageUrl,
            releaseYear = if (_uiState.value.releaseYear.isBlank() && metadata.year != null) metadata.year.toString() else _uiState.value.releaseYear,
            totalEpisodes = if (_uiState.value.totalEpisodes.isBlank() && metadata.episodes > 0) metadata.episodes.toString() else _uiState.value.totalEpisodes,
            genres = currentGenres,
            notes = if (_uiState.value.notes.isBlank() && !metadata.synopsis.isNullOrBlank()) "Synopsis: ${metadata.synopsis}" else _uiState.value.notes,
            showOnlineSearchDialog = false
        )
    }

    fun saveAnime(): Boolean {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(titleError = "Title is required")
            return false
        }

        val totalEps = state.totalEpisodes.toIntOrNull() ?: 0
        val watchedEps = (state.watchedEpisodes.toIntOrNull() ?: 0).coerceAtLeast(0)

        val entity = AnimeEntity(
            id = state.animeId ?: 0L,
            title = state.title.trim(),
            originalTitle = state.originalTitle.trim(),
            coverPath = state.coverPath,
            personalRating = state.personalRating,
            review = state.review,
            notes = state.notes,
            genres = state.genres,
            tags = state.tags,
            releaseYear = state.releaseYear.toIntOrNull(),
            totalEpisodes = totalEps,
            watchedEpisodes = watchedEps,
            status = state.status,
            startDate = state.startDate,
            completionDate = state.completionDate,
            isFavorite = state.isFavorite
        )

        viewModelScope.launch {
            if (state.animeId != null) {
                repository.updateAnime(entity)
            } else {
                repository.insertAnime(entity)
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
        return true
    }
}

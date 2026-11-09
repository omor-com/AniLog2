package com.anilog.omor.dev.ui.reviewcard

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.repository.AnimeRepository
import com.anilog.omor.dev.domain.model.CardTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class ReviewCardUiState(
    val anime: AnimeEntity? = null,
    val selectedTemplate: CardTemplate = CardTemplate.PREMIUM_DARK,
    val customExcerpt: String = "",
    val includeGenre: Boolean = true,
    val includeDate: Boolean = true,
    val previewBitmap: Bitmap? = null,
    val isRendering: Boolean = false,
    val savedFilePath: String? = null,
    val cachedImageUri: Uri? = null,
    val errorMessage: String? = null
)

class ReviewCardViewModel(
    private val animeId: Long,
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewCardUiState())
    val uiState: StateFlow<ReviewCardUiState> = _uiState.asStateFlow()

    fun loadAnime(context: Context) {
        viewModelScope.launch {
            val anime = repository.getAnimeByIdOnce(animeId) ?: return@launch
            val initialExcerpt = anime.review.ifBlank { anime.notes }
            _uiState.value = _uiState.value.copy(
                anime = anime,
                customExcerpt = initialExcerpt
            )
            renderPreview(context)
        }
    }

    fun selectTemplate(template: CardTemplate, context: Context) {
        _uiState.value = _uiState.value.copy(selectedTemplate = template)
        renderPreview(context)
    }

    fun onExcerptChanged(text: String, context: Context) {
        _uiState.value = _uiState.value.copy(customExcerpt = text)
        renderPreview(context)
    }

    fun toggleIncludeGenre(context: Context) {
        _uiState.value = _uiState.value.copy(includeGenre = !_uiState.value.includeGenre)
        renderPreview(context)
    }

    fun toggleIncludeDate(context: Context) {
        _uiState.value = _uiState.value.copy(includeDate = !_uiState.value.includeDate)
        renderPreview(context)
    }

    fun renderPreview(context: Context) {
        val anime = _uiState.value.anime ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRendering = true)
            try {
                val bitmap = ReviewCardRenderer.renderCard(
                    context = context,
                    anime = anime,
                    customExcerpt = _uiState.value.customExcerpt,
                    template = _uiState.value.selectedTemplate,
                    includeGenre = _uiState.value.includeGenre,
                    includeDate = _uiState.value.includeDate
                )
                // Cache bitmap URI for quick sharing
                val uri = ReviewCardRenderer.saveCardToCache(context, bitmap)
                _uiState.value = _uiState.value.copy(
                    previewBitmap = bitmap,
                    cachedImageUri = uri,
                    isRendering = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRendering = false,
                    errorMessage = e.localizedMessage
                )
            }
        }
    }

    fun saveCardPermanently(context: Context, onSaved: (File) -> Unit) {
        val bitmap = _uiState.value.previewBitmap ?: return
        viewModelScope.launch {
            try {
                val file = ReviewCardRenderer.saveCardPermanently(context, bitmap)
                _uiState.value = _uiState.value.copy(savedFilePath = file.absolutePath)
                onSaved(file)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save card: ${e.localizedMessage}")
            }
        }
    }
}

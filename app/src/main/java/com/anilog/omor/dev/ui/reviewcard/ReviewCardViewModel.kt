package com.anilog.omor.dev.ui.reviewcard

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.repository.AnimeRepository
import com.anilog.omor.dev.domain.model.CardAspectRatio
import com.anilog.omor.dev.domain.model.CardTemplate
import com.anilog.omor.dev.domain.ranking.RafaelRankingEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class ReviewCardUiState(
    val anime: AnimeEntity? = null,
    val customization: CardCustomization = CardCustomization(),
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
            val allList = repository.getAllAnimeList()
            val ranked = RafaelRankingEngine.rankAnime(allList)
            val rankPos = ranked.firstOrNull { it.anime.id == anime.id }?.rankPosition

            val initialExcerpt = anime.review.ifBlank { anime.notes }
            val custom = CardCustomization(
                template = CardTemplate.NEON_VIOLET,
                aspectRatio = CardAspectRatio.PORTRAIT,
                customExcerpt = initialExcerpt,
                rafaelRank = rankPos
            )
            _uiState.value = _uiState.value.copy(
                anime = anime,
                customization = custom
            )
            renderPreview(context)
        }
    }

    fun selectTemplate(template: CardTemplate, context: Context) {
        val updated = _uiState.value.customization.copy(template = template)
        _uiState.value = _uiState.value.copy(customization = updated)
        renderPreview(context)
    }

    fun selectAspectRatio(ratio: CardAspectRatio, context: Context) {
        val updated = _uiState.value.customization.copy(aspectRatio = ratio)
        _uiState.value = _uiState.value.copy(customization = updated)
        renderPreview(context)
    }

    fun onExcerptChanged(text: String, context: Context) {
        val updated = _uiState.value.customization.copy(customExcerpt = text)
        _uiState.value = _uiState.value.copy(customization = updated)
        renderPreview(context)
    }

    fun toggleCover(context: Context) {
        val cur = _uiState.value.customization
        _uiState.value = _uiState.value.copy(customization = cur.copy(showCover = !cur.showCover))
        renderPreview(context)
    }

    fun toggleRating(context: Context) {
        val cur = _uiState.value.customization
        _uiState.value = _uiState.value.copy(customization = cur.copy(showRating = !cur.showRating))
        renderPreview(context)
    }

    fun toggleReview(context: Context) {
        val cur = _uiState.value.customization
        _uiState.value = _uiState.value.copy(customization = cur.copy(showReview = !cur.showReview))
        renderPreview(context)
    }

    fun toggleRafaelRank(context: Context) {
        val cur = _uiState.value.customization
        _uiState.value = _uiState.value.copy(customization = cur.copy(showRafaelRank = !cur.showRafaelRank))
        renderPreview(context)
    }

    fun toggleBranding(context: Context) {
        val cur = _uiState.value.customization
        _uiState.value = _uiState.value.copy(customization = cur.copy(showBranding = !cur.showBranding))
        renderPreview(context)
    }

    fun toggleIncludeDate(context: Context) {
        val cur = _uiState.value.customization
        _uiState.value = _uiState.value.copy(customization = cur.copy(showDate = !cur.showDate))
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
                    customization = _uiState.value.customization
                )
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

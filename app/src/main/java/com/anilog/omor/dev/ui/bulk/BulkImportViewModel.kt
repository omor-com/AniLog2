package com.anilog.omor.dev.ui.bulk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.database.entity.AnimeEntity
import com.anilog.omor.dev.data.repository.AnimeRepository
import com.anilog.omor.dev.domain.model.AnimeStatus
import com.anilog.omor.dev.util.RatingFormatHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BulkImportItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String,
    val rating: Float? = null,
    val review: String = "",
    val isDuplicate: Boolean = false,
    val isSelected: Boolean = true
)

data class BulkImportUiState(
    val rawInputText: String = "",
    val parsedItems: List<BulkImportItem> = emptyList(),
    val isParsing: Boolean = false,
    val isImporting: Boolean = false,
    val importSuccessCount: Int? = null,
    val errorMessage: String? = null
)

class BulkImportViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BulkImportUiState())
    val uiState: StateFlow<BulkImportUiState> = _uiState.asStateFlow()

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(rawInputText = text, errorMessage = null)
    }

    fun parseInput() {
        val raw = _uiState.value.rawInputText
        if (raw.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter or paste anime titles")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isParsing = true)
            val existing = repository.getAllAnimeList()
            val existingTitleSet = existing.map { it.title.trim().lowercase() }.toSet()

            val items = parseRawText(raw, existingTitleSet)
            _uiState.value = _uiState.value.copy(
                isParsing = false,
                parsedItems = items
            )
        }
    }

    fun toggleItemSelection(id: String) {
        val updated = _uiState.value.parsedItems.map {
            if (it.id == id) it.copy(isSelected = !it.isSelected) else it
        }
        _uiState.value = _uiState.value.copy(parsedItems = updated)
    }

    fun updateItemTitle(id: String, newTitle: String) {
        val updated = _uiState.value.parsedItems.map {
            if (it.id == id) it.copy(title = newTitle) else it
        }
        _uiState.value = _uiState.value.copy(parsedItems = updated)
    }

    fun removeItem(id: String) {
        val updated = _uiState.value.parsedItems.filter { it.id != id }
        _uiState.value = _uiState.value.copy(parsedItems = updated)
    }

    fun selectAll(select: Boolean) {
        val updated = _uiState.value.parsedItems.map { it.copy(isSelected = select) }
        _uiState.value = _uiState.value.copy(parsedItems = updated)
    }

    fun selectNonDuplicatesOnly() {
        val updated = _uiState.value.parsedItems.map { it.copy(isSelected = !it.isDuplicate) }
        _uiState.value = _uiState.value.copy(parsedItems = updated)
    }

    fun commitImport() {
        val toImport = _uiState.value.parsedItems.filter { it.isSelected && it.title.isNotBlank() }
        if (toImport.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            val now = System.currentTimeMillis()

            withContext(Dispatchers.IO) {
                // Batch insert into Room database
                toImport.forEach { item ->
                    val entity = AnimeEntity(
                        id = 0L,
                        title = item.title.trim(),
                        personalRating = item.rating,
                        review = item.review,
                        status = AnimeStatus.PLAN_TO_WATCH,
                        createdAt = now,
                        updatedAt = now
                    )
                    repository.insertAnime(entity)
                }
            }

            _uiState.value = _uiState.value.copy(
                isImporting = false,
                importSuccessCount = toImport.size,
                parsedItems = emptyList(),
                rawInputText = ""
            )
        }
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(importSuccessCount = null)
    }

    companion object {
        fun parseRawText(raw: String, existingTitles: Set<String>): List<BulkImportItem> {
            val lines = raw.lines().map { it.trim() }.filter { it.isNotEmpty() }
            val items = mutableListOf<BulkImportItem>()

            var i = 0
            while (i < lines.size) {
                val line = lines[i]

                // Check Format C: Title: X\nRating: Y\nReview: Z
                if (line.startsWith("Title:", ignoreCase = true)) {
                    val title = line.substringAfter(":").trim()
                    var rating: Float? = null
                    var review = ""
                    var j = i + 1
                    while (j < lines.size && !lines[j].startsWith("Title:", ignoreCase = true)) {
                        val subLine = lines[j]
                        if (subLine.startsWith("Rating:", ignoreCase = true)) {
                            val rStr = subLine.substringAfter(":").trim()
                            rating = RatingFormatHelper.parseRatingInput(rStr)
                        } else if (subLine.startsWith("Review:", ignoreCase = true)) {
                            review = subLine.substringAfter(":").trim()
                        }
                        j++
                    }
                    if (title.isNotBlank()) {
                        val isDup = existingTitles.contains(title.lowercase())
                        items.add(BulkImportItem(title = title, rating = rating, review = review, isDuplicate = isDup))
                    }
                    i = j
                    continue
                }

                // Check Format B: Title | Rating: 8.7
                if (line.contains("| Rating:", ignoreCase = true) || line.contains("|", ignoreCase = true)) {
                    val parts = line.split("|")
                    val title = cleanTitlePrefix(parts[0].trim())
                    var rating: Float? = null
                    if (parts.size > 1) {
                        val ratingPart = parts[1].replace("Rating:", "", ignoreCase = true).trim()
                        rating = RatingFormatHelper.parseRatingInput(ratingPart)
                    }
                    if (title.isNotBlank()) {
                        val isDup = existingTitles.contains(title.lowercase())
                        items.add(BulkImportItem(title = title, rating = rating, isDuplicate = isDup))
                    }
                    i++
                    continue
                }

                // Standard single line / numbered / bulleted / tab-separated / CSV
                val tokens = if (line.contains("\t")) line.split("\t") else if (line.contains(",")) line.split(",") else listOf(line)
                val rawTitle = cleanTitlePrefix(tokens[0].trim().removeSurrounding("\""))
                val rating = if (tokens.size > 1) RatingFormatHelper.parseRatingInput(tokens[1].trim().removeSurrounding("\"")) else null

                if (rawTitle.isNotBlank()) {
                    val isDup = existingTitles.contains(rawTitle.lowercase())
                    items.add(BulkImportItem(title = rawTitle, rating = rating, isDuplicate = isDup))
                }
                i++
            }

            return items
        }

        private fun cleanTitlePrefix(raw: String): String {
            // Strip leading bullet or numbers like "1. ", "1) ", "- ", "* "
            return raw.replace(Regex("^[0-9]+[.)]\\s*"), "")
                .replace(Regex("^[-*•]\\s*"), "")
                .trim()
        }
    }
}

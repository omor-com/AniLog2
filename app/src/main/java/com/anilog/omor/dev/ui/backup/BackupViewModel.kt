package com.anilog.omor.dev.ui.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilog.omor.dev.data.backup.BackupManager
import com.anilog.omor.dev.data.backup.BackupValidationResult
import com.anilog.omor.dev.data.backup.RestoreMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val pendingValidationUri: Uri? = null,
    val validationResult: BackupValidationResult? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class BackupViewModel(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportFullZip(outputUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, successMessage = null, errorMessage = null)
            val result = backupManager.createFullZipBackup(outputUri)
            result.onSuccess { count ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    successMessage = "Successfully exported full ZIP backup with $count anime entries and artwork."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = "Export failed: ${err.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun exportJsonMetadata(outputUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, successMessage = null, errorMessage = null)
            val result = backupManager.createJsonMetadataExport(outputUri)
            result.onSuccess { count ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    successMessage = "Successfully exported JSON metadata for $count anime entries."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = "Metadata export failed: ${err.localizedMessage}"
                )
            }
        }
    }

    fun exportCsv(outputUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, successMessage = null, errorMessage = null)
            val result = backupManager.createCsvExport(outputUri)
            result.onSuccess { count ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    successMessage = "Successfully exported CSV list with $count anime entries (UTF-8)."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = "CSV export failed: ${err.localizedMessage}"
                )
            }
        }
    }

    fun onImportFileSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, errorMessage = null, successMessage = null)
            val validation = backupManager.validateBackup(uri)
            if (validation.isValid) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    pendingValidationUri = uri,
                    validationResult = validation
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    errorMessage = validation.errorMessage ?: "Invalid backup file"
                )
            }
        }
    }

    fun confirmRestore(mode: RestoreMode) {
        val uri = _uiState.value.pendingValidationUri ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isImporting = true,
                pendingValidationUri = null,
                validationResult = null
            )
            val result = backupManager.restoreBackup(uri, mode)
            result.onSuccess { count ->
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    successMessage = "Successfully restored $count anime entries in ${mode.name.lowercase()} mode."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    errorMessage = "Restore failed: ${err.localizedMessage}"
                )
            }
        }
    }

    fun cancelRestore() {
        _uiState.value = _uiState.value.copy(pendingValidationUri = null, validationResult = null)
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}

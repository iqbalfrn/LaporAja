package id.ac.kuliah.laporaja.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.domain.model.NewReport
import id.ac.kuliah.laporaja.domain.model.ReportCategory
import id.ac.kuliah.laporaja.domain.model.ReportEdit
import id.ac.kuliah.laporaja.domain.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val category: ReportCategory? = null,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val photoBytes: ByteArray? = null,
    val existingPhotoPath: String? = null,
    val statusLabel: String? = null,
    val categoryError: String? = null,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val locationError: String? = null,
    val photoError: String? = null,
    val isSubmitting: Boolean = false,
    val loadError: String? = null,
    val message: String? = null,
) {
    val canSubmit: Boolean get() = !isSubmitting && !isLoading && loadError == null
}

/**
 * S5 (buat) dan S7 (edit) memakai ViewModel yang sama; mode ditentukan ada tidaknya [reportId].
 * Foto hanya bisa dipilih saat membuat, karena kolom isi yang boleh diubah pemilik adalah judul,
 * deskripsi, kategori, dan lokasi (Subbab 7.9).
 */
class ReportFormViewModel(
    private val reportRepository: ReportRepository,
    private val session: SessionHolder,
    private val reportId: String?,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ReportFormUiState(isEditMode = reportId != null, isLoading = reportId != null),
    )
    val state: StateFlow<ReportFormUiState> = _state.asStateFlow()

    private var original = _state.value
    init {
        if (reportId != null) loadForEdit(reportId)
    }

    fun retryLoad() {
        val id = reportId ?: return
        _state.update { it.copy(isLoading = true, loadError = null) }
        loadForEdit(id)
    }

    private fun loadForEdit(id: String) {
        viewModelScope.launch {
            when (val result = reportRepository.detail(id)) {
                is AppResult.Success -> {
                    val report = result.data
                    val editable = report.canEditContent(session.currentUserId)
                    val prefilled = _state.value.copy(
                        isLoading = false,
                        category = report.category,
                        title = report.title,
                        description = report.description,
                        location = report.location,
                        existingPhotoPath = report.photoPath,
                        statusLabel = report.status.label,
                        loadError = if (editable) {
                            null
                        } else {
                            "Laporan ini tidak dapat diubah lagi"
                        },
                    )
                    original = prefilled
                    _state.value = prefilled
                }

                is AppResult.Failure -> _state.update {
                    it.copy(isLoading = false, loadError = ErrorMapper.message(result.error))
                }
            }
        }
    }

    fun onCategoryChange(value: ReportCategory) = _state.update {
        it.copy(category = value, categoryError = null)
    }

    fun onTitleChange(value: String) = _state.update {
        it.copy(title = value, titleError = null)
    }

    fun onDescriptionChange(value: String) = _state.update {
        it.copy(description = value, descriptionError = null)
    }

    fun onLocationChange(value: String) = _state.update {
        it.copy(location = value, locationError = null)
    }

    fun onPhotoPicked(bytes: ByteArray) = _state.update {
        it.copy(photoBytes = bytes, photoError = null)
    }

    fun onPhotoRejected(message: String) = _state.update { it.copy(photoError = message) }

    fun onPhotoRemoved() = _state.update { it.copy(photoBytes = null, photoError = null) }

    fun onMessageShown() = _state.update { it.copy(message = null) }

    fun isDirty(): Boolean = _state.value.hasUnsavedChanges(original)

    fun validate(current: ReportFormUiState): ReportFormUiState = current.copy(
        categoryError = FormValidator.category(current.category),
        titleError = FormValidator.title(current.title),
        descriptionError = FormValidator.reportDescription(current.description),
        locationError = FormValidator.location(current.location),
    )

    fun submit(onSaved: (String) -> Unit) {
        val current = _state.value
        val validated = validate(current)
        if (validated.categoryError != null || validated.titleError != null ||
            validated.descriptionError != null || validated.locationError != null
        ) {
            _state.value = validated
            return
        }
        val category = current.category ?: return
        if (current.isSubmitting) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            if (reportId == null) create(category, current, onSaved) else update(category, current, onSaved)
        }
    }

    private suspend fun create(
        category: ReportCategory,
        current: ReportFormUiState,
        onSaved: (String) -> Unit,
    ) {
        val input = NewReport(
            title = current.title.trim(),
            description = current.description.trim(),
            category = category,
            location = current.location.trim(),
        )
        when (val result = reportRepository.create(input, current.photoBytes)) {
            is AppResult.Success -> onSaved(SUCCESS_CREATE)
            is AppResult.Failure -> _state.update {
                it.copy(isSubmitting = false, message = ErrorMapper.message(result.error))
            }
        }
    }

    private suspend fun update(
        category: ReportCategory,
        current: ReportFormUiState,
        onSaved: (String) -> Unit,
    ) {
        val id = reportId ?: return
        val input = ReportEdit(
            title = current.title.trim(),
            description = current.description.trim(),
            category = category,
            location = current.location.trim(),
        )
        when (val result = reportRepository.updateContent(id, input)) {
            is AppResult.Success -> onSaved(SUCCESS_UPDATE)
            is AppResult.Failure -> _state.update {
                it.copy(isSubmitting = false, message = ErrorMapper.message(result.error))
            }
        }
    }

    private fun ReportFormUiState.hasUnsavedChanges(source: ReportFormUiState): Boolean =
        photoBytes != null || category != source.category || title != source.title ||
            description != source.description || location != source.location

    companion object {
        const val SUCCESS_CREATE = "Laporan terkirim, terima kasih sudah menjaga lingkungan"
        const val SUCCESS_UPDATE = "Perubahan laporan tersimpan"
    }
}

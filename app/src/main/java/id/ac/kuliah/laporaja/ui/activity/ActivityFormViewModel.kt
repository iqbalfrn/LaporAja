package id.ac.kuliah.laporaja.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.domain.model.ActivityCategory
import id.ac.kuliah.laporaja.domain.model.NewActivity
import id.ac.kuliah.laporaja.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivityFormUiState(
    val category: ActivityCategory? = null,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val scheduledAt: Long? = null,
    val quota: String = "",
    val posterBytes: ByteArray? = null,
    val categoryError: String? = null,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val locationError: String? = null,
    val scheduleError: String? = null,
    val quotaError: String? = null,
    val posterError: String? = null,
    val isSubmitting: Boolean = false,
    val message: String? = null,
) {
    val canSubmit: Boolean get() = !isSubmitting
}

/**
 * S11: form kegiatan khusus petugas. Tanggal dan jam dipilih lewat picker Material dan hasilnya
 * wajib masih di masa depan (Subbab 10.3).
 */
class ActivityFormViewModel(
    private val activityRepository: ActivityRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityFormUiState())
    val state: StateFlow<ActivityFormUiState> = _state.asStateFlow()

    fun onCategoryChange(value: ActivityCategory) = _state.update {
        it.copy(category = value, categoryError = null)
    }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value, titleError = null) }

    fun onDescriptionChange(value: String) = _state.update {
        it.copy(description = value, descriptionError = null)
    }

    fun onLocationChange(value: String) = _state.update { it.copy(location = value, locationError = null) }

    fun onQuotaChange(value: String) = _state.update {
        it.copy(quota = value.filter(Char::isDigit).take(QUOTA_DIGITS), quotaError = null)
    }

    fun onScheduleChange(epochMillis: Long) = _state.update {
        it.copy(scheduledAt = epochMillis, scheduleError = null)
    }

    fun onPosterPicked(bytes: ByteArray) = _state.update {
        it.copy(posterBytes = bytes, posterError = null)
    }

    fun onPosterRejected(reason: String) = _state.update { it.copy(posterError = reason) }

    fun onPosterRemoved() = _state.update { it.copy(posterBytes = null, posterError = null) }

    fun onMessageShown() = _state.update { it.copy(message = null) }

    private fun validated(current: ActivityFormUiState): ActivityFormUiState = current.copy(
        categoryError = FormValidator.activityCategory(current.category),
        titleError = FormValidator.title(current.title),
        descriptionError = FormValidator.activityDescription(current.description),
        locationError = FormValidator.location(current.location),
        scheduleError = if (current.scheduledAt == null) {
            SCHEDULE_MISSING
        } else {
            FormValidator.futureSchedule(current.scheduledAt, System.currentTimeMillis())
        },
        quotaError = FormValidator.quota(current.quota),
    )

    fun submit(onSaved: (String) -> Unit) {
        val current = _state.value
        if (current.isSubmitting) return
        val validated = validated(current)
        if (validated.hasErrors()) {
            _state.value = validated
            return
        }
        val category = current.category ?: return
        val scheduledAt = current.scheduledAt ?: return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val input = NewActivity(
                title = current.title.trim(),
                description = current.description.trim(),
                category = category,
                location = current.location.trim(),
                scheduledAt = scheduledAt,
                quota = current.quota.trim().toIntOrNull(),
            )
            when (val result = activityRepository.create(input, current.posterBytes)) {
                is AppResult.Success -> onSaved(SUCCESS_CREATE)
                is AppResult.Failure -> _state.update {
                    it.copy(isSubmitting = false, message = ErrorMapper.message(result.error))
                }
            }
        }
    }

    private fun ActivityFormUiState.hasErrors(): Boolean =
        listOf(categoryError, titleError, descriptionError, locationError, scheduleError, quotaError)
            .any { it != null }

    companion object {
        const val SCHEDULE_MISSING = "Pilih tanggal dan jam kegiatan"
        const val SUCCESS_CREATE = "Kegiatan terkirim dan tampil di katalog"
        private const val QUOTA_DIGITS = 3
    }
}

package id.ac.kuliah.laporaja.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.domain.model.ActivityItem
import id.ac.kuliah.laporaja.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivityDetailUiState(
    val isLoading: Boolean = true,
    val activity: ActivityItem? = null,
    val errorMessage: String? = null,
) {
    val isUpcoming: Boolean get() = activity?.isUpcoming() == true
}

/** S12: satu kegiatan, hanya baca. */
class ActivityDetailViewModel(
    private val activityRepository: ActivityRepository,
    private val activityId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityDetailUiState())
    val uiState: StateFlow<ActivityDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = activityRepository.detail(activityId)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, activity = result.data)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = ErrorMapper.message(result.error))
                }
            }
        }
    }

    companion object {
        const val ACTIVITY_NOT_FOUND = "Kegiatan tidak ditemukan"
    }
}

package id.ac.kuliah.laporaja.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.paging.PAGE_SIZE
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.domain.model.ActivityCategory
import id.ac.kuliah.laporaja.domain.model.ActivityItem
import id.ac.kuliah.laporaja.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivityListUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<ActivityItem> = emptyList(),
    val hasMore: Boolean = false,
    val errorMessage: String? = null,
    val isPetugas: Boolean = false,
)

/**
 * S10: katalog kegiatan. Filter kategori dikirim ke server supaya urutan dan halaman tetap benar,
 * bukan disaring setelah data masuk (Subbab 10.3).
 */
class ActivityListViewModel(
    private val activityRepository: ActivityRepository,
    private val session: SessionHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityListUiState(isLoading = true))
    val uiState: StateFlow<ActivityListUiState> = _uiState.asStateFlow()

    private var category: ActivityCategory? = null
    private var page = 0

    init {
        load(refresh = true)
    }

    fun onCategoryChanged(next: ActivityCategory?) {
        if (category == next) return
        category = next
        load(refresh = true)
    }

    fun load(refresh: Boolean) {
        viewModelScope.launch {
            val targetPage = if (refresh) 0 else page + 1
            _uiState.update {
                it.copy(
                    isPetugas = session.current?.isPetugas == true,
                    isLoading = refresh,
                    isLoadingMore = !refresh,
                    errorMessage = null,
                )
            }
            when (val result = activityRepository.list(category, targetPage)) {
                is AppResult.Success -> {
                    page = targetPage
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            items = if (refresh) result.data else it.items + result.data,
                            hasMore = result.data.size == PAGE_SIZE,
                        )
                    }
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = ErrorMapper.message(result.error),
                    )
                }
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        load(refresh = false)
    }
}

package id.ac.kuliah.laporaja.ui.report

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.paging.PAGE_SIZE
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.domain.model.Report
import id.ac.kuliah.laporaja.domain.model.StatusFilter
import id.ac.kuliah.laporaja.domain.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportListUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<Report> = emptyList(),
    val hasMore: Boolean = false,
    val errorMessage: String? = null,
    val isPetugas: Boolean = false,
)

/**
 * S4: daftar laporan milik sendiri (warga) atau semua laporan (petugas). Isi daftar ditentukan
 * kebijakan RLS server, bukan oleh filter di klien (Subbab 10.1).
 */
class ReportListViewModel(
    private val reportRepository: ReportRepository,
    private val session: SessionHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportListUiState(isLoading = true))
    val uiState: StateFlow<ReportListUiState> = _uiState.asStateFlow()

    var filter by mutableStateOf(StatusFilter.ALL)
        private set

    private var page = 0

    init {
        load(refresh = true)
    }

    fun onFilterChanged(next: StatusFilter) {
        if (filter == next) return
        filter = next
        load(refresh = true)
    }

    fun load(refresh: Boolean) {
        viewModelScope.launch {
            val isPetugas = session.current?.isPetugas == true
            _uiState.update {
                it.copy(
                    isPetugas = isPetugas,
                    isLoading = refresh,
                    isLoadingMore = !refresh,
                    errorMessage = null,
                )
            }
            val targetPage = if (refresh) 0 else page + 1
            when (val result = reportRepository.list(filter, targetPage)) {
                is AppResult.Success -> {
                    page = targetPage
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            items = if (refresh) result.data else state.items + result.data,
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

package id.ac.kuliah.laporaja.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.domain.model.Report
import id.ac.kuliah.laporaja.domain.model.ReportStatus
import id.ac.kuliah.laporaja.domain.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportDetailUiState(
    val isLoading: Boolean = true,
    val report: Report? = null,
    val errorMessage: String? = null,
    val isOwner: Boolean = false,
    val isPetugas: Boolean = false,
    val isSubmittingStatus: Boolean = false,
    val message: String? = null,
) {
    /** Tombol edit hanya untuk pemilik dan hanya pada status baru/ditolak (Subbab 10.1). */
    val canEditContent: Boolean get() = isOwner && report?.canEditStatus() == true

    val canChangeStatus: Boolean get() = isPetugas && report != null

    val transitions: List<ReportStatus> get() = report?.status?.allowedTransitions().orEmpty()
}

/**
 * S6: satu laporan lengkap beserta kelayakan tombol aksi. Hak dibaca dari state sesi dan kolom
 * milik laporan, tetapi server tetap pemutus akhir lewat RLS (Subbab 7.9).
 */
class ReportDetailViewModel(
    private val reportRepository: ReportRepository,
    private val session: SessionHolder,
    private val reportId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportDetailUiState())
    val state: StateFlow<ReportDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = reportRepository.detail(reportId)) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        report = result.data,
                        isOwner = result.data.isOwnedBy(session.currentUserId),
                        isPetugas = session.current?.isPetugas == true,
                        errorMessage = if (result.data.status == ReportStatus.UNKNOWN) {
                            REPORT_NOT_FOUND
                        } else {
                            null
                        },
                    )
                }

                is AppResult.Failure -> _state.update {
                    it.copy(
                        isLoading = false,
                        report = null,
                        errorMessage = if (result.error == AppError.NotFound) {
                            REPORT_NOT_FOUND
                        } else {
                            ErrorMapper.message(result.error)
                        },
                    )
                }
            }
        }
    }

    fun changeStatus(
        status: ReportStatus,
        note: String?,
        onChanged: (String) -> Unit,
    ) {
        if (_state.value.isSubmittingStatus) return
        _state.update { it.copy(isSubmittingStatus = true) }
        viewModelScope.launch {
            val trimmed = note?.trim()?.ifEmpty { null }
            when (val result = reportRepository.updateStatus(reportId, status, trimmed)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isSubmittingStatus = false, report = result.data) }
                    onChanged("Status laporan diperbarui menjadi ${status.label}")
                }

                is AppResult.Failure -> _state.update {
                    it.copy(isSubmittingStatus = false, message = ErrorMapper.message(result.error))
                }
            }
        }
    }

    fun onMessageShown() = _state.update { it.copy(message = null) }

    companion object {
        const val REPORT_NOT_FOUND = "Laporan tidak ditemukan"
    }
}

package id.ac.kuliah.laporaja.ui.response

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.session.ResponseHandoff
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.domain.model.ReportResponse
import id.ac.kuliah.laporaja.domain.repository.ResponseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResponseListUiState(
    val isLoading: Boolean = true,
    val items: List<ReportResponse> = emptyList(),
    val errorMessage: String? = null,
)

/**
 * Daftar tanggapan milik modul A4, ditanam di S6. Penambahan dilakukan tanpa memuat ulang daftar:
 * tanggapan dari S9 masuk lewat [ResponseHandoff] (Subbab 10.2).
 */
class ResponseViewModel(
    private val responseRepository: ResponseRepository,
    private val handoff: ResponseHandoff,
    private val reportId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(ResponseListUiState())
    val state: StateFlow<ResponseListUiState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            handoff.arrived.collect { response ->
                if (response != null && response.reportId == reportId) {
                    prepend(response)
                    handoff.clear()
                }
            }
        }
    }

    fun load() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = responseRepository.listByReport(reportId)) {
                is AppResult.Success -> _state.update {
                    it.copy(isLoading = false, items = result.data, errorMessage = null)
                }

                is AppResult.Failure -> _state.update {
                    it.copy(isLoading = false, errorMessage = ErrorMapper.message(result.error))
                }
            }
        }
    }

    private fun prepend(response: ReportResponse) {
        _state.update { state ->
            if (state.items.any { it.id == response.id }) state
            else state.copy(items = listOf(response) + state.items)
        }
    }
}

data class ResponseFormUiState(
    val message: String = "",
    val messageError: String? = null,
    val isSubmitting: Boolean = false,
    val info: String? = null,
) {
    val canSubmit: Boolean get() = !isSubmitting
}

/** S9: tulis tanggapan + pembatasan sederhana satu pesan per 10 detik di sisi klien. */
class ResponseFormViewModel(
    private val responseRepository: ResponseRepository,
    private val handoff: ResponseHandoff,
    private val session: SessionHolder,
    private val reportId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(ResponseFormUiState())
    val state: StateFlow<ResponseFormUiState> = _state.asStateFlow()

    private var lastSentAt = 0L

    fun onMessageChange(value: String) = _state.update {
        it.copy(message = value, messageError = null)
    }

    fun onInfoShown() = _state.update { it.copy(info = null) }

    fun submit(onSent: () -> Unit) {
        val current = _state.value
        val messageError = FormValidator.responseMessage(current.message)
        if (messageError != null) {
            _state.update { it.copy(messageError = messageError) }
            return
        }
        if (session.currentUserId == null) {
            _state.update { it.copy(info = ErrorMapper.message(AppError.Unauthorized)) }
            return
        }
        val now = System.currentTimeMillis()
        if (lastSentAt != 0L && now - lastSentAt < COOLDOWN_MILLIS) {
            _state.update { it.copy(info = COOLDOWN_MESSAGE) }
            return
        }
        if (current.isSubmitting) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            when (val result = responseRepository.add(reportId, current.message.trim())) {
                is AppResult.Success -> {
                    lastSentAt = System.currentTimeMillis()
                    handoff.publish(result.data)
                    onSent()
                }

                is AppResult.Failure -> _state.update {
                    it.copy(isSubmitting = false, info = ErrorMapper.message(result.error))
                }
            }
        }
    }

    companion object {
        private const val COOLDOWN_MILLIS = 10_000L
        const val COOLDOWN_MESSAGE = "Tunggu sepuluh detik sebelum mengirim tanggapan lagi"
    }
}

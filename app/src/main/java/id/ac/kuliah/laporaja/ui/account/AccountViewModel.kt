package id.ac.kuliah.laporaja.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.domain.model.UserProfile
import id.ac.kuliah.laporaja.domain.repository.AuthRepository
import id.ac.kuliah.laporaja.domain.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val user: UserProfile? = null,
    val isCountLoading: Boolean = true,
    val myReportCount: Int = 0,
    val countError: String? = null,
    val isLoggingOut: Boolean = false,
)

class AccountViewModel(
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository,
    private val sessionHolder: SessionHolder,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState(user = sessionHolder.current))
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // S13 ikut berubah begitu profil disentuh di S14 atau avatar diunggah (Subbab 9.2).
            sessionHolder.user.collect { profile -> _state.update { it.copy(user = profile) } }
        }
        loadReportCount()
    }

    fun loadReportCount() {
        _state.update { it.copy(isCountLoading = true, countError = null) }
        viewModelScope.launch {
            when (val result = reportRepository.countMine()) {
                is AppResult.Success -> _state.update {
                    it.copy(isCountLoading = false, myReportCount = result.data)
                }
                is AppResult.Failure -> _state.update {
                    it.copy(isCountLoading = false, countError = ErrorMapper.message(result.error))
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        if (_state.value.isLoggingOut) return
        _state.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}

package id.ac.kuliah.laporaja.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.domain.repository.AuthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplashUiState(val configError: String? = null)

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val isConfigured: Boolean,
) : ViewModel() {

    private val _state = MutableStateFlow(SplashUiState())
    val state: StateFlow<SplashUiState> = _state.asStateFlow()

    private var started = false

    fun start(onSignedIn: () -> Unit, onSignedOut: () -> Unit) {
        if (started) return
        started = true
        if (!isConfigured) {
            // Tanpa kredensial aplikasi tidak punya backend; tampilkan petunjuk, jangan crash.
            _state.update { it.copy(configError = ErrorMapper.message(AppError.NotConfigured)) }
            return
        }
        viewModelScope.launch {
            val profile = restoreWithinBrandDelay()
            if (profile != null) onSignedIn() else onSignedOut()
        }
    }

    /** Splash tidak boleh lebih singkat dari MIN_MILLIS dan tidak pernah melewati 1,5 detik. */
    private suspend fun restoreWithinBrandDelay() = coroutineScope {
        val pending = async {
            (authRepository.restoreSession() as? AppResult.Success)?.data
        }
        delay(MIN_MILLIS)
        pending.await()
    }

    companion object {
        private const val MIN_MILLIS = 450L
    }
}

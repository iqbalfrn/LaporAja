package id.ac.kuliah.laporaja.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isSubmitting: Boolean = false,
    val message: String? = null,
) {
    val canSubmit: Boolean get() = !isSubmitting
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionHolder: SessionHolder,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        // Arah dari NavGraph sudah login-expired; pesan hanya dipakai sekali di layar ini.
        val expired = sessionHolder.takeExpiredMessage()
        if (expired != null) _state.update { it.copy(message = expired) }
    }

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, passwordError = null) }
    }

    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    fun submit(onSuccess: () -> Unit) {
        val current = _state.value
        val emailError = FormValidator.email(current.email)
        val passwordError = if (current.password.isEmpty()) MESSAGE_FIELDS_REQUIRED else null
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        if (current.isSubmitting) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            when (val result = authRepository.login(current.email.trim().lowercase(), current.password)) {
                is AppResult.Success -> onSuccess()
                is AppResult.Failure -> _state.update {
                    it.copy(isSubmitting = false, message = messageFor(result.error))
                }
            }
        }
    }

    private fun messageFor(error: AppError): String = when (error) {
        AppError.NoInternet -> "Tidak ada koneksi internet, coba lagi"
        AppError.NotConfigured -> "Kredensial Supabase belum diisi, buka local.properties"
        else -> MESSAGE_LOGIN_FAILED
    }

    companion object {
        const val MESSAGE_LOGIN_FAILED = "Email atau password salah"
        const val MESSAGE_FIELDS_REQUIRED = "Email dan password harus diisi"
    }
}

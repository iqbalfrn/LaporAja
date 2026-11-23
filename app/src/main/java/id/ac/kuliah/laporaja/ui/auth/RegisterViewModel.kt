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

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val rtRw: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val rtRwError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,
    val isSubmitting: Boolean = false,
    val message: String? = null,
) {
    val isConfirmationVisible: Boolean get() = confirmPassword.isNotEmpty()
}

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val sessionHolder: SessionHolder,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    init {
        val expired = sessionHolder.takeExpiredMessage()
        if (expired != null) _state.update { it.copy(message = expired) }
    }

    fun onFullNameChange(value: String) = _state.update { it.copy(fullName = value, fullNameError = null) }

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, emailError = null) }

    fun onPhoneChange(value: String) = _state.update { it.copy(phone = value, phoneError = null) }

    fun onRtRwChange(value: String) = _state.update { it.copy(rtRw = value, rtRwError = null) }

    fun onPasswordChange(value: String) = _state.update { state ->
        state.copy(
            password = value,
            passwordError = null,
            confirmPasswordError = FormValidator.passwordMatch(value, state.confirmPassword)
                ?.takeIf { state.confirmPassword.isNotEmpty() },
        )
    }

    fun onConfirmPasswordChange(value: String) = _state.update {
        it.copy(confirmPassword = value, confirmPasswordError = null)
    }

    fun onTermsChange(accepted: Boolean) = _state.update {
        it.copy(termsAccepted = accepted, termsError = null)
    }

    fun onMessageShown() = _state.update { it.copy(message = null) }

    fun submit(onSuccess: () -> Unit) {
        val current = _state.value
        val errors = validate(current)
        if (errors.hasError) {
            _state.update { errors }
            return
        }
        if (current.isSubmitting) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            when (
                val result = authRepository.register(
                    fullName = current.fullName.trim(),
                    email = current.email.trim().lowercase(),
                    password = current.password,
                    phone = current.phone.trim().ifEmpty { null },
                    rtRw = current.rtRw.trim().ifEmpty { null },
                )
            ) {
                is AppResult.Success -> onSuccess()
                is AppResult.Failure -> _state.update {
                    it.copy(isSubmitting = false, message = messageFor(result.error))
                }
            }
        }
    }

    /**
     * Validasi lokal penuh sebelum permintaan dikirim: bila ada field yang salah, tidak ada
     * panggilan jaringan sama sekali (Subbab 9.1).
     */
    private fun validate(current: RegisterUiState): RegisterUiState = current.copy(
        fullNameError = FormValidator.fullName(current.fullName),
        emailError = FormValidator.email(current.email),
        phoneError = FormValidator.phone(current.phone),
        rtRwError = FormValidator.rtRw(current.rtRw),
        passwordError = FormValidator.password(current.password),
        confirmPasswordError = FormValidator.passwordMatch(current.password, current.confirmPassword),
        termsError = FormValidator.termsAccepted(current.termsAccepted),
    )

    private val RegisterUiState.hasError: Boolean
        get() = fullNameError != null || emailError != null || phoneError != null ||
            rtRwError != null || passwordError != null || confirmPasswordError != null ||
            termsError != null

    private fun messageFor(error: AppError): String = when (error) {
        AppError.EmailConfirmationRequired ->
            "Cek email kamu, bila tidak masuk silakan login karena email mungkin sudah terdaftar"
        AppError.NoInternet -> "Tidak ada koneksi internet, coba lagi"
        AppError.NotConfigured -> "Kredensial Supabase belum diisi, buka local.properties"
        else -> "Pendaftaran gagal, periksa kembali isian kamu"
    }
}

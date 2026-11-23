package id.ac.kuliah.laporaja.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val fullName: String = "",
    val phone: String = "",
    val rtRw: String = "",
    val roleLabel: String = "",
    val avatarPath: String? = null,
    val pendingAvatar: ByteArray? = null,
    val fullNameError: String? = null,
    val phoneError: String? = null,
    val rtRwError: String? = null,
    val imageError: String? = null,
    val isSubmitting: Boolean = false,
    val message: String? = null,
) {
    fun hasUnsavedChanges(original: EditProfileUiState): Boolean =
        pendingAvatar != null || fullName != original.fullName || phone != original.phone ||
            rtRw != original.rtRw
}

class EditProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionHolder: SessionHolder,
) : ViewModel() {

    private val loaded = sessionHolder.current
    private val original = EditProfileUiState(
        fullName = loaded?.fullName.orEmpty(),
        phone = loaded?.phone.orEmpty(),
        rtRw = loaded?.rtRw.orEmpty(),
        roleLabel = loaded?.role?.label.orEmpty(),
        avatarPath = loaded?.avatarPath,
    )

    private val _state = MutableStateFlow(original)
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    fun onFullNameChange(value: String) = _state.update {
        it.copy(fullName = value, fullNameError = null)
    }

    fun onPhoneChange(value: String) = _state.update { it.copy(phone = value, phoneError = null) }

    fun onRtRwChange(value: String) = _state.update { it.copy(rtRw = value, rtRwError = null) }

    fun onAvatarPicked(bytes: ByteArray) = _state.update {
        it.copy(pendingAvatar = bytes, imageError = null)
    }

    fun onAvatarRejected(message: String) = _state.update { it.copy(imageError = message) }

    fun onAvatarRemoved() = _state.update { it.copy(pendingAvatar = null, imageError = null) }

    fun onMessageShown() = _state.update { it.copy(message = null) }

    fun isDirty(): Boolean = _state.value.hasUnsavedChanges(original)

    fun save(onSaved: (String) -> Unit) {
        val current = _state.value
        val fullNameError = FormValidator.fullName(current.fullName)
        val phoneError = FormValidator.phone(current.phone)
        val rtRwError = FormValidator.rtRw(current.rtRw)
        if (fullNameError != null || phoneError != null || rtRwError != null) {
            _state.update {
                it.copy(fullNameError = fullNameError, phoneError = phoneError, rtRwError = rtRwError)
            }
            return
        }
        val userId = sessionHolder.currentUserId
        if (userId == null) {
            _state.update { it.copy(message = ErrorMapper.message(AppError.Unauthorized)) }
            return
        }
        if (current.isSubmitting) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val avatarBytes = current.pendingAvatar
            if (avatarBytes != null) {
                when (val uploaded = profileRepository.uploadAvatar(userId, avatarBytes)) {
                    is AppResult.Success -> _state.update { it.copy(pendingAvatar = null) }
                    is AppResult.Failure -> {
                        _state.update {
                            it.copy(isSubmitting = false, message = ErrorMapper.message(uploaded.error))
                        }
                        return@launch
                    }
                }
            }
            when (
                val saved = profileRepository.updateProfile(
                    id = userId,
                    fullName = current.fullName.trim(),
                    phone = current.phone.trim().ifEmpty { null },
                    rtRw = current.rtRw.trim().ifEmpty { null },
                )
            ) {
                is AppResult.Success -> onSaved(SUCCESS_PROFILE)
                is AppResult.Failure -> _state.update {
                    it.copy(isSubmitting = false, message = ErrorMapper.message(saved.error))
                }
            }
        }
    }

    companion object {
        const val SUCCESS_PROFILE = "Profil berhasil diperbarui"
    }
}

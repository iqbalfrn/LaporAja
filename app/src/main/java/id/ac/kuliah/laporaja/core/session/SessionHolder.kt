package id.ac.kuliah.laporaja.core.session

import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionHolder {

    private val _user = MutableStateFlow<UserProfile?>(null)
    val user: StateFlow<UserProfile?> = _user.asStateFlow()

    private val _unauthorizedTick = MutableStateFlow(0)
    val unauthorizedTick: StateFlow<Int> = _unauthorizedTick.asStateFlow()

    @Volatile
    private var expiredMessagePending = false

    val current: UserProfile? get() = _user.value

    val currentUserId: String? get() = _user.value?.id

    fun setUser(profile: UserProfile?) {
        _user.value = profile
    }

    fun replace(profile: UserProfile) {
        _user.value = profile
    }

    fun clear() {
        _user.value = null
        expiredMessagePending = false
    }

    fun signalUnauthorized() {
        expiredMessagePending = true
        _unauthorizedTick.value = _unauthorizedTick.value + 1
    }

    fun takeExpiredMessage(): String? {
        if (!expiredMessagePending) return null
        expiredMessagePending = false
        return ErrorMapper.message(AppError.Unauthorized)
    }
}

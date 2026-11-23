package id.ac.kuliah.laporaja.core.session

import id.ac.kuliah.laporaja.domain.model.ReportResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Jembatan satu kali baca dari S9 ke daftar tanggapan S6: tanggapan yang baru dikirim ditambahkan
 * ke state list tanpa memuat ulang seluruh daftar (Subbab 10.2).
 */
class ResponseHandoff {

    private val _arrived = MutableStateFlow<ReportResponse?>(null)
    val arrived: StateFlow<ReportResponse?> = _arrived.asStateFlow()

    fun publish(response: ReportResponse) {
        _arrived.value = response
    }

    fun clear() {
        _arrived.value = null
    }
}

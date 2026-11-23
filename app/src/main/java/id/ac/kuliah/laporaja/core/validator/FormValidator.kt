package id.ac.kuliah.laporaja.core.validator

import id.ac.kuliah.laporaja.domain.model.ActivityCategory
import id.ac.kuliah.laporaja.domain.model.ReportCategory

object FormValidator {

    const val NAME_MIN = 3
    const val NAME_MAX = 60
    const val PASSWORD_MIN = 8
    const val TITLE_MIN = 5
    const val TITLE_MAX = 80
    const val DESCRIPTION_MIN = 20
    const val DESCRIPTION_MAX = 1000
    const val LOCATION_MIN = 5
    const val LOCATION_MAX = 120
    const val RESPONSE_MIN = 3
    const val RESPONSE_MAX = 500
    const val ACTIVITY_DESCRIPTION_MIN = 30
    const val ACTIVITY_DESCRIPTION_MAX = 2000
    const val STATUS_NOTE_MAX = 300
    const val QUOTA_MIN = 1
    const val QUOTA_MAX = 999

    private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val rtRwPattern = Regex("^\\d{1,3}/\\d{1,3}$")

    fun fullName(value: String): String? = when {
        value.trim().length < NAME_MIN || value.trim().length > NAME_MAX ->
            "Masukkan nama lengkap minimal $NAME_MIN karakter"
        !value.trim().contains(' ') -> "Masukkan nama lengkap minimal $NAME_MIN karakter"
        else -> null
    }

    fun email(value: String): String? =
        if (emailPattern.matches(value.trim().lowercase())) null else "Format email belum benar"

    fun phone(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        return if (trimmed.length in 9..16 && trimmed.all(Char::isDigit)) null
        else "Nomor HP harus 9-16 digit angka"
    }

    fun rtRw(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        return if (trimmed.length <= 12 && rtRwPattern.matches(trimmed)) null
        else "Contoh format yang benar: 003/005"
    }

    fun password(value: String): String? = when {
        value.length < PASSWORD_MIN -> "Password minimal $PASSWORD_MIN karakter dan ada angkanya"
        value.none(Char::isDigit) -> "Password minimal $PASSWORD_MIN karakter dan ada angkanya"
        value.none(Char::isLetter) -> "Password minimal $PASSWORD_MIN karakter dan ada angkanya"
        else -> null
    }

    fun passwordMatch(password: String, confirmation: String): String? =
        if (password == confirmation) null else "Konfirmasi password tidak sama"

    fun termsAccepted(accepted: Boolean): String? =
        if (accepted) null else "Setujui syarat penggunaan terlebih dahulu"

    fun loginFields(email: String, password: String): String? =
        if (email.isNotBlank() && password.isNotBlank()) null else "Email dan password harus diisi"

    fun category(selected: ReportCategory?): String? =
        if (selected == null) "Pilih kategori laporan" else null

    fun activityCategory(selected: ActivityCategory?): String? =
        if (selected == null) "Pilih kategori kegiatan" else null

    fun title(value: String): String? =
        if (value.trim().length in TITLE_MIN..TITLE_MAX) null
        else "Judul minimal $TITLE_MIN dan maksimal $TITLE_MAX karakter"

    fun reportDescription(value: String): String? =
        if (value.trim().length in DESCRIPTION_MIN..DESCRIPTION_MAX) null
        else "Jelaskan masalah minimal $DESCRIPTION_MIN karakter"

    fun activityDescription(value: String): String? =
        if (value.trim().length in ACTIVITY_DESCRIPTION_MIN..ACTIVITY_DESCRIPTION_MAX) null
        else "Jelaskan kegiatan minimal $ACTIVITY_DESCRIPTION_MIN karakter"

    fun location(value: String): String? =
        if (value.trim().length in LOCATION_MIN..LOCATION_MAX) null
        else "Tulis lokasi atau patokan yang jelas"

    fun responseMessage(value: String): String? =
        if (value.trim().length in RESPONSE_MIN..RESPONSE_MAX) null
        else "Tulis tanggapan minimal $RESPONSE_MIN karakter"

    fun statusNote(value: String): String? =
        if (value.trim().length <= STATUS_NOTE_MAX) null
        else "Catatan maksimal $STATUS_NOTE_MAX karakter"

    fun quota(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        val parsed = trimmed.toIntOrNull()
        return if (parsed != null && parsed in QUOTA_MIN..QUOTA_MAX) null
        else "Kuota antara $QUOTA_MIN dan $QUOTA_MAX"
    }

    fun futureSchedule(epochMillis: Long, nowMillis: Long): String? =
        if (epochMillis > nowMillis) null else "Pilih tanggal yang belum berlalu"

    fun imageTooLarge(): String = "Ukuran gambar maksimal 12 MB"

    fun normalizedEmail(value: String): String = value.trim().lowercase()
}

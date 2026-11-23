package id.ac.kuliah.laporaja.domain.model

enum class UserRole(val value: String, val label: String) {
    WARGA("warga", "Warga"),
    PETUGAS("petugas", "Petugas"),
    ;

    companion object {
        fun from(raw: String?): UserRole = entries.firstOrNull { it.value == raw } ?: WARGA
    }
}

data class UserProfile(
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String?,
    val rtRw: String?,
    val role: UserRole,
    val avatarPath: String?,
    val createdAt: Long,
) {
    val isPetugas: Boolean get() = role == UserRole.PETUGAS

    fun initials(): String = initialsOf(fullName)
}

/**
 * Inisial nama untuk avatar tanpa foto. Dipakai UserProfile dan daftar tanggapan agar bentuk
 * identitas ringkas selalu sama di semua layar (Subbab 9.2).
 */
fun initialsOf(fullName: String): String {
    val words = fullName.trim().split(WHITESPACE).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> "${words[0].first()}${words[1].first()}".uppercase()
    }
}

private val WHITESPACE = Regex("\\s+")

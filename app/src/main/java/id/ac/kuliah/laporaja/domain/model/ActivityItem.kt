package id.ac.kuliah.laporaja.domain.model

enum class ActivityCategory(val value: String, val label: String) {
    KERJA_BAKTI("kerja_bakti", "Kerja Bakti"),
    EDUKASI("edukasi", "Edukasi"),
    BANK_SAMPAH("bank_sampah", "Bank Sampah"),
    KUNJUNGAN("kunjungan", "Kunjungan"),
    ;

    companion object {
        fun from(raw: String?): ActivityCategory = entries.firstOrNull { it.value == raw } ?: EDUKASI
    }
}

data class ActivityItem(
    val id: String,
    val title: String,
    val description: String,
    val category: ActivityCategory,
    val location: String,
    val scheduledAt: Long,
    val quota: Int?,
    val posterPath: String?,
    val isPublished: Boolean,
    val createdAt: Long,
    val creatorName: String?,
) {
    val hasPoster: Boolean get() = !posterPath.isNullOrBlank()

    fun isUpcoming(now: Long = System.currentTimeMillis()): Boolean = scheduledAt >= now
}

data class NewActivity(
    val title: String,
    val description: String,
    val category: ActivityCategory,
    val location: String,
    val scheduledAt: Long,
    val quota: Int?,
)

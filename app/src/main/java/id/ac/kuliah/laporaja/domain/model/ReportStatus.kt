package id.ac.kuliah.laporaja.domain.model

enum class ReportStatus(val value: String, val label: String) {
    BARU("baru", "Baru"),
    DIPROSES("diproses", "Diproses"),
    SELESAI("selesai", "Selesai"),
    DITOLAK("ditolak", "Ditolak"),
    UNKNOWN("unknown", "Tidak diketahui"),
    ;

    fun allowedTransitions(): List<ReportStatus> = when (this) {
        BARU -> listOf(DIPROSES, SELESAI, DITOLAK)
        DIPROSES -> listOf(SELESAI, DITOLAK, BARU)
        SELESAI -> listOf(DIPROSES)
        DITOLAK -> listOf(BARU)
        UNKNOWN -> emptyList()
    }

    fun canTransitionTo(next: ReportStatus): Boolean = next in allowedTransitions()

    companion object {
        fun from(raw: String?): ReportStatus = entries.firstOrNull { it.value == raw } ?: UNKNOWN
    }
}

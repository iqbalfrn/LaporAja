package id.ac.kuliah.laporaja.domain.model

/**
 * Chip filter pada S4. [ALL] mengirim query tanpa filter status, sisanya memfilter satu status.
 * Nama konstanta mengikuti kontrak Subbab 12.2.
 */
enum class StatusFilter(val label: String, val status: ReportStatus?) {
    ALL("Semua", null),
    BARU("Baru", ReportStatus.BARU),
    DIPROSES("Diproses", ReportStatus.DIPROSES),
    SELESAI("Selesai", ReportStatus.SELESAI),
    DITOLAK("Ditolak", ReportStatus.DITOLAK),
    ;

    companion object {
        fun from(raw: String?): StatusFilter = entries.firstOrNull { it.name == raw } ?: ALL
    }
}

package id.ac.kuliah.laporaja.domain.model

data class Report(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: ReportCategory,
    val location: String,
    val photoPath: String?,
    val status: ReportStatus,
    val statusNote: String?,
    val resolvedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val responseCount: Int,
) {
    val hasPhoto: Boolean get() = !photoPath.isNullOrBlank()

    fun isOwnedBy(userId: String?): Boolean = userId != null && userId == this.userId

    /** Isi laporan hanya boleh diubah pemilik saat status masih baru atau ditolak (Subbab 7.7). */
    fun canEditStatus(): Boolean = status == ReportStatus.BARU || status == ReportStatus.DITOLAK

    fun canEditContent(userId: String?): Boolean = isOwnedBy(userId) && canEditStatus()

    fun statusTimeline(): List<StatusStep> = listOf(
        StatusStep("Dibuat", reached = true, at = createdAt, note = null),
        StatusStep(
            label = "Diproses",
            reached = status == ReportStatus.DIPROSES || status == ReportStatus.SELESAI,
            at = if (status == ReportStatus.DIPROSES) updatedAt else 0L,
            note = if (status == ReportStatus.DIPROSES) statusNote else null,
        ),
        StatusStep(
            label = "Selesai",
            reached = status == ReportStatus.SELESAI,
            at = resolvedAt,
            note = if (status == ReportStatus.SELESAI) statusNote else null,
        ),
    ) + if (status == ReportStatus.DITOLAK) {
        listOf(StatusStep("Ditolak", reached = true, at = updatedAt, note = statusNote))
    } else {
        emptyList()
    }
}

data class StatusStep(
    val label: String,
    val reached: Boolean,
    val at: Long,
    val note: String?,
)

data class NewReport(
    val title: String,
    val description: String,
    val category: ReportCategory,
    val location: String,
)

data class ReportEdit(
    val title: String,
    val description: String,
    val category: ReportCategory,
    val location: String,
)

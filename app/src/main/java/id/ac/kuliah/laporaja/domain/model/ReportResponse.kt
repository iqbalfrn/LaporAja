package id.ac.kuliah.laporaja.domain.model

data class ReportResponse(
    val id: String,
    val reportId: String,
    val userId: String,
    val message: String,
    val isOfficial: Boolean,
    val createdAt: Long,
    val authorName: String,
    val authorRole: UserRole,
    val authorAvatarPath: String?,
)

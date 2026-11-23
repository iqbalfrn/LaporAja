package id.ac.kuliah.laporaja.domain.repository

import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.domain.model.ActivityCategory
import id.ac.kuliah.laporaja.domain.model.ActivityItem
import id.ac.kuliah.laporaja.domain.model.NewActivity
import id.ac.kuliah.laporaja.domain.model.NewReport
import id.ac.kuliah.laporaja.domain.model.Report
import id.ac.kuliah.laporaja.domain.model.ReportEdit
import id.ac.kuliah.laporaja.domain.model.ReportResponse
import id.ac.kuliah.laporaja.domain.model.ReportStatus
import id.ac.kuliah.laporaja.domain.model.StatusFilter
import id.ac.kuliah.laporaja.domain.model.UserProfile

interface AuthRepository {
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String?,
        rtRw: String?,
    ): AppResult<UserProfile>

    suspend fun login(email: String, password: String): AppResult<UserProfile>

    suspend fun restoreSession(): AppResult<UserProfile?>

    suspend fun logout(): AppResult<Unit>

    fun currentUser(): UserProfile?
}

interface ProfileRepository {
    suspend fun updateProfile(
        id: String,
        fullName: String,
        phone: String?,
        rtRw: String?,
    ): AppResult<UserProfile>

    /** Mengembalikan path relatif bucket, bukan URL penuh. */
    suspend fun uploadAvatar(id: String, bytes: ByteArray): AppResult<String>
}

interface ReportRepository {
    suspend fun create(input: NewReport, photoBytes: ByteArray?): AppResult<Report>

    suspend fun list(statusFilter: StatusFilter, page: Int): AppResult<List<Report>>

    suspend fun detail(id: String): AppResult<Report>

    suspend fun updateContent(id: String, input: ReportEdit): AppResult<Report>

    suspend fun updateStatus(
        id: String,
        status: ReportStatus,
        note: String?,
    ): AppResult<Report>

    /**
     * Jumlah laporan milik pengguna aktif untuk S13 (Subbab 9.2). Kontrak 12.3 hanya ditambah,
     * tidak ada tanda tangan lama yang berubah.
     */
    suspend fun countMine(): AppResult<Int>
}

interface ResponseRepository {
    suspend fun listByReport(reportId: String): AppResult<List<ReportResponse>>

    suspend fun add(reportId: String, message: String): AppResult<ReportResponse>
}

interface ActivityRepository {
    suspend fun list(category: ActivityCategory?, page: Int): AppResult<List<ActivityItem>>

    suspend fun detail(id: String): AppResult<ActivityItem>

    suspend fun create(input: NewActivity, posterBytes: ByteArray?): AppResult<ActivityItem>
}

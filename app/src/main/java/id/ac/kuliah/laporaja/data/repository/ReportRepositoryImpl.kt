package id.ac.kuliah.laporaja.data.repository

import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.image.ImageUploader
import id.ac.kuliah.laporaja.core.paging.PAGE_SIZE
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.result.getOrNull
import id.ac.kuliah.laporaja.core.result.map
import id.ac.kuliah.laporaja.core.result.orNotFound
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.data.mapper.ReportMapper
import id.ac.kuliah.laporaja.data.remote.JsonValues
import id.ac.kuliah.laporaja.data.remote.RemoteExecutor
import id.ac.kuliah.laporaja.data.remote.SupabaseBuckets
import id.ac.kuliah.laporaja.data.remote.SupabaseTables
import id.ac.kuliah.laporaja.data.remote.countOf
import id.ac.kuliah.laporaja.data.remote.dto.ReportDto
import id.ac.kuliah.laporaja.domain.model.NewReport
import id.ac.kuliah.laporaja.domain.model.Report
import id.ac.kuliah.laporaja.domain.model.ReportEdit
import id.ac.kuliah.laporaja.domain.model.ReportStatus
import id.ac.kuliah.laporaja.domain.model.StatusFilter
import id.ac.kuliah.laporaja.domain.repository.ReportRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject

internal class ReportRepositoryImpl(
    private val executor: RemoteExecutor,
    private val imageUploader: ImageUploader,
    private val sessionHolder: SessionHolder,
) : ReportRepository {

    override suspend fun create(input: NewReport, photoBytes: ByteArray?): AppResult<Report> {
        val userId = sessionHolder.currentUserId ?: return AppResult.Failure(AppError.Unauthorized)
        var photoPath: String? = null
        if (photoBytes != null) {
            // Berkas naik lebih dulu supaya tidak pernah ada path tanpa induk (Subbab 9.3).
            val path = ImageUploader.reportPhotoPath()
            val uploaded = imageUploader.upload(SupabaseBuckets.REPORT_PHOTOS, path, photoBytes)
            if (uploaded is AppResult.Failure) return AppResult.Failure(uploaded.error)
            photoPath = path
        }
        val inserted = executor.call { client ->
            val data = client.postgrest[SupabaseTables.REPORTS]
                .insert(ReportMapper.toInsertJson(input, userId, photoPath)) { select() }
                .data
            ReportDto.first(data)
        }
        if (inserted is AppResult.Failure) {
            imageUploader.deleteQuietly(SupabaseBuckets.REPORT_PHOTOS, photoPath)
            return AppResult.Failure(inserted.error)
        }
        val row = inserted.getOrNull()
        if (row == null) {
            imageUploader.deleteQuietly(SupabaseBuckets.REPORT_PHOTOS, photoPath)
            return AppResult.Failure(AppError.Server)
        }
        return AppResult.Success(ReportMapper.toDomain(row))
    }

    override suspend fun list(statusFilter: StatusFilter, page: Int): AppResult<List<Report>> =
        executor.call { client ->
            client.postgrest[SupabaseTables.REPORTS]
                .select(Columns.raw(SupabaseTables.reportProjection)) {
                    statusFilter.status?.let { status ->
                        filter { eq(SupabaseTables.Column.STATUS, status.value) }
                    }
                    order(SupabaseTables.Column.CREATED_AT, Order.DESCENDING)
                    range(page.toLong() * PAGE_SIZE until (page + 1).toLong() * PAGE_SIZE)
                }.data
        }.map { data -> ReportDto.list(data).map(ReportMapper::toDomain) }

    override suspend fun detail(id: String): AppResult<Report> =
        executor.call { client ->
            val data = client.postgrest[SupabaseTables.REPORTS]
                .select(Columns.raw(SupabaseTables.reportProjection)) {
                    filter { eq(SupabaseTables.Column.ID, id) }
                    limit(1)
                }.data
            ReportDto.first(data)
        }.orNotFound().map(ReportMapper::toDomain)

    override suspend fun updateContent(id: String, input: ReportEdit): AppResult<Report> =
        mutate(id, ReportMapper.toContentJson(input))

    /**
     * Jumlah laporan sendiri diambil lewat aggregate embed pada baris profiles agar tidak perlu
     * membaca daftar laporan penuh (Subbab 9.2).
     */
    override suspend fun countMine(): AppResult<Int> {
        val userId = sessionHolder.currentUserId ?: return AppResult.Failure(AppError.Unauthorized)
        return executor.call { client ->
            client.postgrest[SupabaseTables.PROFILES]
                .select(Columns.raw(SupabaseTables.ownReportCountProjection)) {
                    filter { eq(SupabaseTables.Column.ID, userId) }
                    limit(1)
                }.data
        }.map { data -> JsonValues.firstRow(data)?.countOf(SupabaseTables.REPORTS) ?: 0 }
    }

    override suspend fun updateStatus(
        id: String,
        status: ReportStatus,
        note: String?,
    ): AppResult<Report> = mutate(id, ReportMapper.toStatusJson(status, note))

    private suspend fun mutate(id: String, body: JsonObject): AppResult<Report> =
        executor.call { client ->
            val data = client.postgrest[SupabaseTables.REPORTS]
                .update(body) {
                    filter { eq(SupabaseTables.Column.ID, id) }
                    select()
                }.data
            ReportDto.first(data)
        }.orNotFound().map(ReportMapper::toDomain)
}

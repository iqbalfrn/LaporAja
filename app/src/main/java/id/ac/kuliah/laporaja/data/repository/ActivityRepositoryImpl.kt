package id.ac.kuliah.laporaja.data.repository

import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.image.ImageUploader
import id.ac.kuliah.laporaja.core.paging.PAGE_SIZE
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.result.getOrNull
import id.ac.kuliah.laporaja.core.result.map
import id.ac.kuliah.laporaja.core.result.orNotFound
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.data.mapper.ActivityMapper
import id.ac.kuliah.laporaja.data.remote.RemoteExecutor
import id.ac.kuliah.laporaja.data.remote.SupabaseBuckets
import id.ac.kuliah.laporaja.data.remote.SupabaseTables
import id.ac.kuliah.laporaja.data.remote.dto.ActivityDto
import id.ac.kuliah.laporaja.domain.model.ActivityCategory
import id.ac.kuliah.laporaja.domain.model.ActivityItem
import id.ac.kuliah.laporaja.domain.model.NewActivity
import id.ac.kuliah.laporaja.domain.repository.ActivityRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

internal class ActivityRepositoryImpl(
    private val executor: RemoteExecutor,
    private val imageUploader: ImageUploader,
    private val sessionHolder: SessionHolder,
) : ActivityRepository {

    override suspend fun list(category: ActivityCategory?, page: Int): AppResult<List<ActivityItem>> =
        executor.call { client ->
            client.postgrest[SupabaseTables.ACTIVITIES]
                .select(Columns.raw(SupabaseTables.activityProjection)) {
                    category?.let {
                        filter { eq(SupabaseTables.Column.CATEGORY, it.value) }
                    }
                    // Jadwal terbaru lebih dulu supaya halaman pertama memuat kegiatan mendatang,
                    // lalu urutan tampil disusun ulang mengikuti aturan Subbab 10.3.
                    order(SupabaseTables.Column.SCHEDULE_AT, Order.DESCENDING)
                    range(page.toLong() * PAGE_SIZE until (page + 1).toLong() * PAGE_SIZE)
                }.data
        }.map { data ->
            val items = ActivityDto.list(data).map(ActivityMapper::toDomain)
            val now = System.currentTimeMillis()
            items.filter { it.isUpcoming(now) }.sortedBy { it.scheduledAt } +
                items.filterNot { it.isUpcoming(now) }.sortedByDescending { it.createdAt }
        }

    override suspend fun detail(id: String): AppResult<ActivityItem> =
        executor.call { client ->
            val data = client.postgrest[SupabaseTables.ACTIVITIES]
                .select(Columns.raw(SupabaseTables.activityProjection)) {
                    filter { eq(SupabaseTables.Column.ID, id) }
                    limit(1)
                }.data
            ActivityDto.first(data)
        }.orNotFound().map(ActivityMapper::toDomain)

    override suspend fun create(input: NewActivity, posterBytes: ByteArray?): AppResult<ActivityItem> {
        val userId = sessionHolder.currentUserId ?: return AppResult.Failure(AppError.Unauthorized)
        var posterPath: String? = null
        if (posterBytes != null) {
            val path = ImageUploader.activityPosterPath()
            val uploaded = imageUploader.upload(SupabaseBuckets.ACTIVITY_POSTERS, path, posterBytes)
            if (uploaded is AppResult.Failure) return AppResult.Failure(uploaded.error)
            posterPath = path
        }
        val inserted = executor.call { client ->
            val data = client.postgrest[SupabaseTables.ACTIVITIES]
                .insert(ActivityMapper.toInsertJson(input, userId, posterPath)) { select() }
                .data
            ActivityDto.first(data)
        }
        if (inserted is AppResult.Failure) {
            imageUploader.deleteQuietly(SupabaseBuckets.ACTIVITY_POSTERS, posterPath)
            return AppResult.Failure(inserted.error)
        }
        val row = inserted.getOrNull()
        if (row == null) {
            imageUploader.deleteQuietly(SupabaseBuckets.ACTIVITY_POSTERS, posterPath)
            return AppResult.Failure(AppError.Server)
        }
        return AppResult.Success(ActivityMapper.toDomain(row))
    }
}

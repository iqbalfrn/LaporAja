package id.ac.kuliah.laporaja.data.mapper

import id.ac.kuliah.laporaja.core.time.AppTime
import id.ac.kuliah.laporaja.data.remote.SupabaseTables.Column
import id.ac.kuliah.laporaja.data.remote.dto.ActivityDto
import id.ac.kuliah.laporaja.domain.model.ActivityCategory
import id.ac.kuliah.laporaja.domain.model.ActivityItem
import id.ac.kuliah.laporaja.domain.model.NewActivity
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal object ActivityMapper {

    fun toDomain(dto: ActivityDto): ActivityItem = ActivityItem(
        id = dto.id,
        title = dto.title,
        description = dto.description,
        category = ActivityCategory.from(dto.category),
        location = dto.location,
        scheduledAt = AppTime.parse(dto.scheduleAt),
        quota = dto.quota,
        posterPath = dto.posterPath,
        isPublished = dto.isPublished,
        createdAt = AppTime.parse(dto.createdAt),
        creatorName = dto.creatorName,
    )

    fun toInsertJson(input: NewActivity, userId: String, posterPath: String?): JsonObject = buildJsonObject {
        put(Column.TITLE, input.title)
        put(Column.DESCRIPTION, input.description)
        put(Column.CATEGORY, input.category.value)
        put(Column.LOCATION, input.location)
        put(Column.SCHEDULE_AT, AppTime.toIsoUtc(input.scheduledAt))
        put(Column.QUOTA, input.quota)
        put(Column.POSTER_PATH, posterPath)
        put(Column.CREATED_BY, userId)
    }
}

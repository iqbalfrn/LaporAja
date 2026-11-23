package id.ac.kuliah.laporaja.data.mapper

import id.ac.kuliah.laporaja.core.time.AppTime
import id.ac.kuliah.laporaja.data.remote.SupabaseTables.Column
import id.ac.kuliah.laporaja.data.remote.dto.ReportDto
import id.ac.kuliah.laporaja.domain.model.NewReport
import id.ac.kuliah.laporaja.domain.model.Report
import id.ac.kuliah.laporaja.domain.model.ReportCategory
import id.ac.kuliah.laporaja.domain.model.ReportEdit
import id.ac.kuliah.laporaja.domain.model.ReportStatus
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal object ReportMapper {

    fun toDomain(dto: ReportDto): Report = Report(
        id = dto.id,
        userId = dto.userId,
        title = dto.title,
        description = dto.description,
        category = ReportCategory.from(dto.category),
        location = dto.location,
        photoPath = dto.photoPath,
        status = ReportStatus.from(dto.status),
        statusNote = dto.statusNote,
        resolvedAt = AppTime.parse(dto.resolvedAt),
        createdAt = AppTime.parse(dto.createdAt),
        updatedAt = AppTime.parse(dto.updatedAt),
        responseCount = dto.responseCount,
    )

    fun toInsertJson(input: NewReport, userId: String, photoPath: String?): JsonObject = buildJsonObject {
        put(Column.USER_ID, userId)
        put(Column.TITLE, input.title)
        put(Column.DESCRIPTION, input.description)
        put(Column.CATEGORY, input.category.value)
        put(Column.LOCATION, input.location)
        put(Column.PHOTO_PATH, photoPath)
    }

    fun toContentJson(input: ReportEdit): JsonObject = buildJsonObject {
        put(Column.TITLE, input.title)
        put(Column.DESCRIPTION, input.description)
        put(Column.CATEGORY, input.category.value)
        put(Column.LOCATION, input.location)
    }

    fun toStatusJson(status: ReportStatus, note: String?): JsonObject = buildJsonObject {
        put(Column.STATUS, status.value)
        put(Column.STATUS_NOTE, note)
        when (status) {
            ReportStatus.SELESAI -> put(Column.RESOLVED_AT, AppTime.toIsoUtc(AppTime.now()))
            else -> put(Column.RESOLVED_AT, null as String?)
        }
    }
}

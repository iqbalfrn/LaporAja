package id.ac.kuliah.laporaja.data.mapper

import id.ac.kuliah.laporaja.core.time.AppTime
import id.ac.kuliah.laporaja.data.remote.SupabaseTables.Column
import id.ac.kuliah.laporaja.data.remote.dto.ResponseDto
import id.ac.kuliah.laporaja.domain.model.ReportResponse
import id.ac.kuliah.laporaja.domain.model.UserRole
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal object ResponseMapper {

    fun toDomain(dto: ResponseDto): ReportResponse {
        val authorRole = UserRole.from(dto.authorRole)
        return ReportResponse(
            id = dto.id,
            reportId = dto.reportId,
            userId = dto.userId,
            message = dto.message,
            isOfficial = dto.isOfficial,
            createdAt = AppTime.parse(dto.createdAt),
            authorName = dto.authorName ?: "Warga",
            authorRole = authorRole,
            authorAvatarPath = dto.authorAvatarPath,
        )
    }

    fun toInsertJson(reportId: String, userId: String, message: String): JsonObject = buildJsonObject {
        put(Column.REPORT_ID, reportId)
        put(Column.USER_ID, userId)
        put(Column.MESSAGE, message)
    }
}

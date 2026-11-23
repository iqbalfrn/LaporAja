package id.ac.kuliah.laporaja.data.remote.dto

import id.ac.kuliah.laporaja.data.remote.JsonValues
import id.ac.kuliah.laporaja.data.remote.SupabaseTables.REPORT_RESPONSES
import id.ac.kuliah.laporaja.data.remote.SupabaseTables.Column
import id.ac.kuliah.laporaja.data.remote.countOf
import id.ac.kuliah.laporaja.data.remote.text
import kotlinx.serialization.json.JsonObject

data class ReportDto(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: String?,
    val location: String,
    val photoPath: String?,
    val status: String?,
    val statusNote: String?,
    val resolvedAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val responseCount: Int,
) {
    companion object {
        fun from(json: JsonObject): ReportDto? {
            val id = json.text(Column.ID) ?: return null
            return ReportDto(
                id = id,
                userId = json.text(Column.USER_ID).orEmpty(),
                title = json.text(Column.TITLE).orEmpty(),
                description = json.text(Column.DESCRIPTION).orEmpty(),
                category = json.text(Column.CATEGORY),
                location = json.text(Column.LOCATION).orEmpty(),
                photoPath = json.text(Column.PHOTO_PATH),
                status = json.text(Column.STATUS),
                statusNote = json.text(Column.STATUS_NOTE),
                resolvedAt = json.text(Column.RESOLVED_AT),
                createdAt = json.text(Column.CREATED_AT),
                updatedAt = json.text(Column.UPDATED_AT),
                responseCount = json.countOf(REPORT_RESPONSES),
            )
        }

        fun first(data: String?): ReportDto? = JsonValues.firstRow(data)?.let(::from)

        fun list(data: String?): List<ReportDto> = JsonValues.rows(data).mapNotNull(::from)
    }
}

package id.ac.kuliah.laporaja.data.remote.dto

import id.ac.kuliah.laporaja.data.remote.JsonValues
import id.ac.kuliah.laporaja.data.remote.SupabaseTables.PROFILES
import id.ac.kuliah.laporaja.data.remote.SupabaseTables.Column
import id.ac.kuliah.laporaja.data.remote.child
import id.ac.kuliah.laporaja.data.remote.flag
import id.ac.kuliah.laporaja.data.remote.number
import id.ac.kuliah.laporaja.data.remote.text
import kotlinx.serialization.json.JsonObject

data class ActivityDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String?,
    val location: String,
    val scheduleAt: String?,
    val quota: Int?,
    val posterPath: String?,
    val isPublished: Boolean,
    val createdAt: String?,
    val createdBy: String?,
    val creatorName: String?,
) {
    companion object {
        fun from(json: JsonObject): ActivityDto? {
            val id = json.text(Column.ID) ?: return null
            return ActivityDto(
                id = id,
                title = json.text(Column.TITLE).orEmpty(),
                description = json.text(Column.DESCRIPTION).orEmpty(),
                category = json.text(Column.CATEGORY),
                location = json.text(Column.LOCATION).orEmpty(),
                scheduleAt = json.text(Column.SCHEDULE_AT),
                quota = json.number(Column.QUOTA),
                posterPath = json.text(Column.POSTER_PATH),
                isPublished = json.flag(Column.IS_PUBLISHED),
                createdAt = json.text(Column.CREATED_AT),
                createdBy = json.text(Column.CREATED_BY),
                creatorName = json.child(PROFILES)?.text(Column.FULL_NAME),
            )
        }

        fun first(data: String?): ActivityDto? = JsonValues.firstRow(data)?.let(::from)

        fun list(data: String?): List<ActivityDto> = JsonValues.rows(data).mapNotNull(::from)
    }
}

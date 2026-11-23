package id.ac.kuliah.laporaja.data.remote.dto

import id.ac.kuliah.laporaja.data.remote.JsonValues
import id.ac.kuliah.laporaja.data.remote.SupabaseTables.PROFILES
import id.ac.kuliah.laporaja.data.remote.SupabaseTables.Column
import id.ac.kuliah.laporaja.data.remote.child
import id.ac.kuliah.laporaja.data.remote.flag
import id.ac.kuliah.laporaja.data.remote.text
import kotlinx.serialization.json.JsonObject

data class ResponseDto(
    val id: String,
    val reportId: String,
    val userId: String,
    val message: String,
    val isOfficial: Boolean,
    val createdAt: String?,
    val authorName: String?,
    val authorRole: String?,
    val authorAvatarPath: String?,
) {
    companion object {
        fun from(json: JsonObject): ResponseDto? {
            val id = json.text(Column.ID) ?: return null
            val author = json.child(PROFILES)
            return ResponseDto(
                id = id,
                reportId = json.text(Column.REPORT_ID).orEmpty(),
                userId = json.text(Column.USER_ID).orEmpty(),
                message = json.text(Column.MESSAGE).orEmpty(),
                isOfficial = json.flag(Column.IS_OFFICIAL),
                createdAt = json.text(Column.CREATED_AT),
                authorName = author?.text(Column.FULL_NAME),
                authorRole = author?.text(Column.ROLE),
                authorAvatarPath = author?.text(Column.AVATAR_PATH),
            )
        }

        fun list(data: String?): List<ResponseDto> = JsonValues.rows(data).mapNotNull(::from)

        fun first(data: String?): ResponseDto? = JsonValues.firstRow(data)?.let(::from)
    }
}

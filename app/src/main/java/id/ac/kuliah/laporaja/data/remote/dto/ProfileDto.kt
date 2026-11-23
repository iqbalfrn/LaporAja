package id.ac.kuliah.laporaja.data.remote.dto

import id.ac.kuliah.laporaja.data.remote.JsonValues
import id.ac.kuliah.laporaja.data.remote.SupabaseTables.Column
import id.ac.kuliah.laporaja.data.remote.text
import kotlinx.serialization.json.JsonObject

data class ProfileDto(
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String?,
    val rtRw: String?,
    val role: String?,
    val avatarPath: String?,
    val createdAt: String?,
) {
    companion object {
        fun from(json: JsonObject): ProfileDto? {
            val id = json.text(Column.ID) ?: return null
            return ProfileDto(
                id = id,
                email = json.text(Column.EMAIL).orEmpty(),
                fullName = json.text(Column.FULL_NAME).orEmpty(),
                phone = json.text(Column.PHONE),
                rtRw = json.text(Column.RT_RW),
                role = json.text(Column.ROLE),
                avatarPath = json.text(Column.AVATAR_PATH),
                createdAt = json.text(Column.CREATED_AT),
            )
        }

        fun first(data: String?): ProfileDto? = JsonValues.firstRow(data)?.let(::from)

        fun list(data: String?): List<ProfileDto> = JsonValues.rows(data).mapNotNull(::from)
    }
}

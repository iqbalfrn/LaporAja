package id.ac.kuliah.laporaja.data.mapper

import id.ac.kuliah.laporaja.core.time.AppTime
import id.ac.kuliah.laporaja.data.remote.SupabaseTables.Column
import id.ac.kuliah.laporaja.data.remote.dto.ProfileDto
import id.ac.kuliah.laporaja.domain.model.UserProfile
import id.ac.kuliah.laporaja.domain.model.UserRole
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal object ProfileMapper {

    fun toDomain(dto: ProfileDto): UserProfile = UserProfile(
        id = dto.id,
        email = dto.email,
        fullName = dto.fullName,
        phone = dto.phone,
        rtRw = dto.rtRw,
        role = UserRole.from(dto.role),
        avatarPath = dto.avatarPath,
        createdAt = AppTime.parse(dto.createdAt),
    )

    fun toUpdateJson(fullName: String, phone: String?, rtRw: String?): JsonObject = buildJsonObject {
        put(Column.FULL_NAME, fullName)
        put(Column.PHONE, phone)
        put(Column.RT_RW, rtRw)
    }

    fun toAvatarJson(avatarPath: String): JsonObject = buildJsonObject {
        put(Column.AVATAR_PATH, avatarPath)
    }
}

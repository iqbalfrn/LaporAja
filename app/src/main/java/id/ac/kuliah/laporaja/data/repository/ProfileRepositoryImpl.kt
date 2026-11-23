package id.ac.kuliah.laporaja.data.repository

import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.image.ImageUploader
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.result.flatMap
import id.ac.kuliah.laporaja.core.result.map
import id.ac.kuliah.laporaja.core.result.orNotFound
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.data.mapper.ProfileMapper
import id.ac.kuliah.laporaja.data.remote.RemoteExecutor
import id.ac.kuliah.laporaja.data.remote.SupabaseBuckets
import id.ac.kuliah.laporaja.data.remote.SupabaseTables
import id.ac.kuliah.laporaja.data.remote.dto.ProfileDto
import id.ac.kuliah.laporaja.domain.model.UserProfile
import id.ac.kuliah.laporaja.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

internal class ProfileRepositoryImpl(
    private val executor: RemoteExecutor,
    private val imageUploader: ImageUploader,
    private val sessionHolder: SessionHolder,
) : ProfileRepository {

    override suspend fun updateProfile(
        id: String,
        fullName: String,
        phone: String?,
        rtRw: String?,
    ): AppResult<UserProfile> = updateAndPublish(id) { client ->
        client.postgrest[SupabaseTables.PROFILES]
            .update(ProfileMapper.toUpdateJson(fullName, phone, rtRw)) {
                filter { eq(SupabaseTables.Column.ID, id) }
                select()
            }.data
    }

    override suspend fun uploadAvatar(id: String, bytes: ByteArray): AppResult<String> {
        if (id.isBlank()) return AppResult.Failure(AppError.Unauthorized)
        val path = ImageUploader.avatarPath(id)
        val uploaded = imageUploader.upload(SupabaseBuckets.AVATARS, path, bytes)
        if (uploaded is AppResult.Failure) return AppResult.Failure(uploaded.error)
        return updateAndPublish(id) { client ->
            client.postgrest[SupabaseTables.PROFILES]
                .update(ProfileMapper.toAvatarJson(path)) {
                    filter { eq(SupabaseTables.Column.ID, id) }
                    select()
                }.data
        }.map { path }
    }

    private suspend fun updateAndPublish(
        id: String,
        action: suspend (SupabaseClient) -> String,
    ): AppResult<UserProfile> =
        executor.call { client -> ProfileDto.first(action(client)) }
            .orNotFound()
            .flatMap { dto ->
                val profile = ProfileMapper.toDomain(dto)
                if (profile.id == id) sessionHolder.replace(profile)
                AppResult.Success(profile)
            }
}

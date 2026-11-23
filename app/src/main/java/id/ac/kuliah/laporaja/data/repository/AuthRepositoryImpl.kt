package id.ac.kuliah.laporaja.data.repository

import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.result.flatMap
import id.ac.kuliah.laporaja.core.result.map
import id.ac.kuliah.laporaja.core.result.orNotFound
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.data.mapper.ProfileMapper
import id.ac.kuliah.laporaja.data.remote.RemoteExecutor
import id.ac.kuliah.laporaja.data.remote.SupabaseTables
import id.ac.kuliah.laporaja.data.remote.dto.ProfileDto
import id.ac.kuliah.laporaja.domain.model.UserProfile
import id.ac.kuliah.laporaja.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class AuthRepositoryImpl(
    private val executor: RemoteExecutor,
    private val sessionHolder: SessionHolder,
) : AuthRepository {

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String?,
        rtRw: String?,
    ): AppResult<UserProfile> {
        val metadata = buildJsonObject {
            put(SupabaseTables.Column.FULL_NAME, fullName)
            put(SupabaseTables.Column.PHONE, phone)
            put(SupabaseTables.Column.RT_RW, rtRw)
        }
        val session = executor.call(notifyUnauthorized = false) { client ->
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = metadata
            }
            client.auth.currentSessionOrNull()?.user?.id
        }
        if (session is AppResult.Failure) return AppResult.Failure(session.error)
        // Tanpa sesi aktif berarti proyek masih mengaktifkan "Confirm email" (Subbab 9.1).
        val userId = (session as AppResult.Success).data
            ?: return AppResult.Failure(AppError.EmailConfirmationRequired)
        return loadProfile(userId)
    }

    override suspend fun login(email: String, password: String): AppResult<UserProfile> {
        val session = executor.call(notifyUnauthorized = false) { client ->
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            client.auth.currentSessionOrNull()?.user?.id
        }
        if (session is AppResult.Failure) return AppResult.Failure(rejectedLogin(session.error))
        val userId = (session as AppResult.Success).data ?: return AppResult.Failure(AppError.Unauthorized)
        return loadProfile(userId)
    }

    override suspend fun restoreSession(): AppResult<UserProfile?> {
        val stored = executor.call(notifyUnauthorized = false) { client ->
            awaitSessionLoad(client)
            client.auth.currentSessionOrNull()?.user?.id
        }
        if (stored is AppResult.Failure) return AppResult.Failure(stored.error)
        val userId = (stored as AppResult.Success).data ?: return AppResult.Success(null)
        return loadProfile(userId).map { profile -> profile as UserProfile? }
    }

    override suspend fun logout(): AppResult<Unit> {
        executor.call(notifyUnauthorized = false) { client -> client.auth.signOut() }
        sessionHolder.clear()
        return AppResult.Success(Unit)
    }

    override fun currentUser(): UserProfile? = sessionHolder.current

    private suspend fun loadProfile(userId: String): AppResult<UserProfile> =
        executor.call { client ->
            val data = client.postgrest[SupabaseTables.PROFILES]
                .select(Columns.raw(SupabaseTables.profileProjection)) {
                    filter { eq(SupabaseTables.Column.ID, userId) }
                }.data
            ProfileDto.first(data)
        }.orNotFound().flatMap { dto ->
            val profile = ProfileMapper.toDomain(dto)
            sessionHolder.setUser(profile)
            AppResult.Success(profile)
        }

    /**
     * Gagal login memakai satu pesan umum (Subbab 9.1) agar tidak membocorkan field mana
     * yang keliru; masalah jaringan tetap dilaporkan apa adanya.
     */
    private fun rejectedLogin(error: AppError): AppError = when (error) {
        AppError.NoInternet, AppError.Forbidden, AppError.Server, AppError.NotConfigured -> error
        else -> AppError.Unauthorized
    }

    private suspend fun awaitSessionLoad(client: SupabaseClient) {
        withTimeoutOrNull(SESSION_LOAD_TIMEOUT_MS) { client.auth.awaitInitialization() }
    }

    private companion object {
        const val SESSION_LOAD_TIMEOUT_MS = 1_200L
    }
}

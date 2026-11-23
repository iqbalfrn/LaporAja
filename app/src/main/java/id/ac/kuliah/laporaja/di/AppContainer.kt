package id.ac.kuliah.laporaja.di

import id.ac.kuliah.laporaja.BuildConfig
import id.ac.kuliah.laporaja.core.image.ImageUploader
import id.ac.kuliah.laporaja.core.image.PublicUrlBuilder
import id.ac.kuliah.laporaja.core.session.ResponseHandoff
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.data.remote.RemoteExecutor
import id.ac.kuliah.laporaja.data.repository.ActivityRepositoryImpl
import id.ac.kuliah.laporaja.data.repository.AuthRepositoryImpl
import id.ac.kuliah.laporaja.data.repository.ProfileRepositoryImpl
import id.ac.kuliah.laporaja.data.repository.ReportRepositoryImpl
import id.ac.kuliah.laporaja.data.repository.ResponseRepositoryImpl
import id.ac.kuliah.laporaja.domain.repository.ActivityRepository
import id.ac.kuliah.laporaja.domain.repository.AuthRepository
import id.ac.kuliah.laporaja.domain.repository.ProfileRepository
import id.ac.kuliah.laporaja.domain.repository.ReportRepository
import id.ac.kuliah.laporaja.domain.repository.ResponseRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Wadah manual seluruh dependensi (Subbab 6.1): tanpa Hilt, tanpa KAPT/KSP.
 * Kredensial dibaca dari BuildConfig; bila kosong seluruh panggilan data mengembalikan
 * galat konfigurasi, bukan crash.
 */
class AppContainer {

    val sessionHolder = SessionHolder()

    val responseHandoff = ResponseHandoff()

    val supabaseClient: SupabaseClient? by lazy { buildClient() }

    private val executor: RemoteExecutor by lazy { RemoteExecutor({ supabaseClient }, sessionHolder) }

    private val imageUploader: ImageUploader by lazy { ImageUploader(executor) }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(executor, sessionHolder)
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(executor, imageUploader, sessionHolder)
    }

    val reportRepository: ReportRepository by lazy {
        ReportRepositoryImpl(executor, imageUploader, sessionHolder)
    }

    val responseRepository: ResponseRepository by lazy {
        ResponseRepositoryImpl(executor, sessionHolder)
    }

    val activityRepository: ActivityRepository by lazy {
        ActivityRepositoryImpl(executor, imageUploader, sessionHolder)
    }

    val isConfigured: Boolean get() = supabaseClient != null

    private fun buildClient(): SupabaseClient? {
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY
        if (url.isBlank() || key.isBlank()) return null
        val client = runCatching {
            createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
                install(Auth)
                install(Postgrest)
                install(Storage)
            }
        }.getOrNull()
        PublicUrlBuilder.attach(client)
        return client
    }
}

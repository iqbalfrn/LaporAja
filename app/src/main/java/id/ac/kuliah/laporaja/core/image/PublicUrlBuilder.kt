package id.ac.kuliah.laporaja.core.image

import id.ac.kuliah.laporaja.data.remote.SupabaseBuckets
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.SupabaseClient

object PublicUrlBuilder {

    private var client: SupabaseClient? = null

    fun attach(supabaseClient: SupabaseClient?) {
        client = supabaseClient
    }

    fun reportPhoto(path: String?): String? = url(SupabaseBuckets.REPORT_PHOTOS, path)

    fun activityPoster(path: String?): String? = url(SupabaseBuckets.ACTIVITY_POSTERS, path)

    fun avatar(path: String?): String? = url(SupabaseBuckets.AVATARS, path)

    /**
     * Membentuk URL publik Storage dari path relatif bucket (Subbab 7.8).
     * Database hanya menyimpan path, jadi pemindahan bucket tidak butuh migrasi data.
     */
    fun url(bucket: String, path: String?): String? {
        if (path.isNullOrBlank()) return null
        val active = client ?: return null
        return runCatching { active.storage[bucket].publicUrl(path) }.getOrNull()
    }
}

package id.ac.kuliah.laporaja.core.image

import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.result.map
import io.github.jan.supabase.storage.storage
import id.ac.kuliah.laporaja.data.remote.RemoteExecutor
import java.util.UUID

/**
 * Satu mekanisme unggah gambar untuk foto laporan, poster kegiatan, dan foto profil (Subbab 9.3).
 */
internal class ImageUploader(private val executor: RemoteExecutor) {

    suspend fun upload(bucket: String, path: String, bytes: ByteArray): AppResult<Unit> =
        executor.call { client -> client.storage[bucket].upload(path, bytes) { upsert = true } }
            .map { }

    /** Penghapusan best-effort: kegagalan diabaikan dan tidak ditampilkan ke pengguna. */
    suspend fun deleteQuietly(bucket: String, path: String?) {
        if (path.isNullOrBlank()) return
        executor.call { client -> client.storage[bucket].delete(path) }
    }

    companion object {
        fun reportPhotoPath(): String = "reports/${UUID.randomUUID()}/${UUID.randomUUID()}.jpg"

        fun activityPosterPath(): String = "activities/${UUID.randomUUID()}/${UUID.randomUUID()}.jpg"

        fun avatarPath(userId: String): String =
            "users/$userId/avatar-${System.currentTimeMillis()}.jpg"
    }
}

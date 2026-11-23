package id.ac.kuliah.laporaja.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.result.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageCompressor {

    const val MAX_SOURCE_BYTES = 12L * 1024 * 1024
    private const val MAX_LONGEST_SIDE = 1280
    private const val JPEG_QUALITY = 80
    private val supportedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

    suspend fun compress(context: Context, uri: Uri): AppResult<ByteArray> {
        val resolver = context.applicationContext.contentResolver
        return withContext(Dispatchers.IO) {
            val mime = resolver.getType(uri)
            if (mime != null && mime !in supportedMimeTypes) return@withContext AppResult.Failure(AppError.Validation)
            val source = readBytes(resolver, uri) ?: return@withContext AppResult.Failure(AppError.NotFound)
            if (source.size.toLong() > MAX_SOURCE_BYTES) return@withContext AppResult.Failure(AppError.ImageTooLarge)
            val decoded = BitmapFactory.decodeByteArray(source, 0, source.size)
                ?: return@withContext AppResult.Failure(AppError.Validation)
            AppResult.Success(encode(decoded.longestSide(MAX_LONGEST_SIDE)))
        }
    }

    private fun readBytes(
        resolver: android.content.ContentResolver,
        uri: Uri,
    ): ByteArray? = try {
        resolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArrayOutputStream()
            input.copyTo(buffer)
            buffer.toByteArray()
        }
    } catch (_: Exception) {
        null
    }

    private fun Bitmap.longestSide(maxSide: Int): Bitmap {
        val longest = maxOf(width, height)
        if (longest <= maxSide) return this
        val factor = maxSide.toFloat() / longest.toFloat()
        val scaled = Bitmap.createScaledBitmap(
            this,
            (width * factor).toInt().coerceAtLeast(1),
            (height * factor).toInt().coerceAtLeast(1),
            true,
        )
        if (scaled !== this) recycle()
        return scaled
    }

    private fun encode(bitmap: Bitmap): ByteArray = ByteArrayOutputStream().use { buffer ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, buffer)
        buffer.toByteArray()
    }
}

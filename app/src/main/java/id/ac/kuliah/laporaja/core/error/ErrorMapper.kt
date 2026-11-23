package id.ac.kuliah.laporaja.core.error

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException

object ErrorMapper {

    fun classify(source: Throwable): AppError {
        val rest = findRestException(source)
        if (rest != null) return fromRest(rest)
        if (source is HttpRequestException || source is HttpRequestTimeoutException) return AppError.NoInternet
        return if (hasIoCause(source)) AppError.NoInternet else AppError.Unknown
    }

    fun message(error: AppError): String = when (error) {
        AppError.NoInternet -> "Tidak ada koneksi internet, coba lagi"
        AppError.Unauthorized -> "Sesi berakhir, silakan masuk kembali"
        AppError.EmailConfirmationRequired -> "Cek email kamu, bila tidak masuk silakan login karena email mungkin sudah terdaftar"
        AppError.NotFound -> "Data tidak ditemukan"
        AppError.Forbidden -> "Akun kamu belum punya hak untuk aksi ini"
        AppError.Validation -> "Ada isian yang belum sesuai, periksa kembali"
        AppError.ImageTooLarge -> "Ukuran gambar maksimal 12 MB"
        AppError.Server -> "Server menolak permintaan, coba lagi sebentar"
        AppError.Unknown -> "Terjadi kesalahan, coba lagi"
        AppError.NotConfigured -> "Kredensial Supabase belum diisi, buka local.properties"
    }

    fun loginFailed(): String = "Email atau password salah"

    fun isUnauthorized(error: AppError): Boolean = error == AppError.Unauthorized

    private fun fromRest(rest: RestException): AppError {
        val text = listOfNotNull(rest.message, rest.error, rest.description)
            .joinToString(" ")
            .lowercase()
        if (EMAIL_CONFIRMATION_KEYWORDS.any { text.contains(it) }) return AppError.EmailConfirmationRequired
        return when (rest.statusCode) {
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden
            404 -> AppError.NotFound
            400, 409 -> if (text.contains("row-level security") || text.contains("row level security")) {
                AppError.Forbidden
            } else {
                AppError.Validation
            }
            else -> AppError.Server
        }
    }

    private val EMAIL_CONFIRMATION_KEYWORDS =
        listOf("confirm your email", "confirmation required", "not yet confirmed", "email confirmation")

    private fun findRestException(source: Throwable?): RestException? {
        var current = source
        while (current != null) {
            if (current is RestException) return current
            current = current.cause
        }
        return null
    }

    private fun hasIoCause(source: Throwable?): Boolean {
        var current = source
        while (current != null) {
            if (current is IOException) return true
            current = current.cause
        }
        return false
    }
}

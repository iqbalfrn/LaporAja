package id.ac.kuliah.laporaja.data.remote

import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.session.SessionHolder
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Membungkus semua panggilan Supabase: satu titik try/catch, satu dispatcher,
 * satu penerjemah galat, dan satu pemicu sinyal sesi berakhir (Subbab 9.1 dan 13).
 */
internal class RemoteExecutor(
    private val clientProvider: () -> SupabaseClient?,
    private val sessionHolder: SessionHolder,
) {

    suspend fun <T> call(
        notifyUnauthorized: Boolean = true,
        action: suspend (SupabaseClient) -> T,
    ): AppResult<T> = try {
        val client = clientProvider() ?: return AppResult.Failure(AppError.NotConfigured)
        withContext(Dispatchers.IO) { AppResult.Success(action(client)) }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (source: Throwable) {
        val error = ErrorMapper.classify(source)
        if (notifyUnauthorized && ErrorMapper.isUnauthorized(error)) sessionHolder.signalUnauthorized()
        AppResult.Failure(error)
    }
}

package id.ac.kuliah.laporaja.data.repository

import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.result.map
import id.ac.kuliah.laporaja.core.result.orNotFound
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.data.mapper.ResponseMapper
import id.ac.kuliah.laporaja.data.remote.RemoteExecutor
import id.ac.kuliah.laporaja.data.remote.SupabaseTables
import id.ac.kuliah.laporaja.data.remote.dto.ResponseDto
import id.ac.kuliah.laporaja.domain.model.ReportResponse
import id.ac.kuliah.laporaja.domain.repository.ResponseRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

internal class ResponseRepositoryImpl(
    private val executor: RemoteExecutor,
    private val sessionHolder: SessionHolder,
) : ResponseRepository {

    override suspend fun listByReport(reportId: String): AppResult<List<ReportResponse>> =
        executor.call { client ->
            client.postgrest[SupabaseTables.REPORT_RESPONSES]
                .select(Columns.raw(SupabaseTables.responseProjection)) {
                    filter { eq(SupabaseTables.Column.REPORT_ID, reportId) }
                    order(SupabaseTables.Column.CREATED_AT, Order.DESCENDING)
                }.data
        }.map { data -> ResponseDto.list(data).map(ResponseMapper::toDomain) }

    override suspend fun add(reportId: String, message: String): AppResult<ReportResponse> {
        val userId = sessionHolder.currentUserId ?: return AppResult.Failure(AppError.Unauthorized)
        return executor.call { client ->
            val data = client.postgrest[SupabaseTables.REPORT_RESPONSES]
                .insert(ResponseMapper.toInsertJson(reportId, userId, message)) {
                    select(Columns.raw(SupabaseTables.responseProjection))
                }
                .data
            ResponseDto.first(data)
        }.orNotFound().map(ResponseMapper::toDomain)
    }
}

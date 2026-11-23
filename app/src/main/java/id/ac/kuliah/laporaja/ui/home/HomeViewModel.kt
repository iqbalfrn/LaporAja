package id.ac.kuliah.laporaja.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.domain.model.ActivityItem
import id.ac.kuliah.laporaja.domain.model.Report
import id.ac.kuliah.laporaja.domain.model.StatusFilter
import id.ac.kuliah.laporaja.domain.model.UserProfile
import id.ac.kuliah.laporaja.domain.repository.ActivityRepository
import id.ac.kuliah.laporaja.domain.repository.ReportRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val user: UserProfile? = null,
    val isPetugas: Boolean = false,
    val recentReports: List<Report> = emptyList(),
    val upcomingActivities: List<ActivityItem> = emptyList(),
) {
    val firstName: String get() = user?.fullName?.trim()?.split(" ")?.firstOrNull().orEmpty()
}

/**
 * S3: beranda ringan. Angkanya dihitung dari data nyata (laporan terbaru milik/semua laporan dan
 * kegiatan mendatang), tidak ada nilai tetap di klien (Subbab 5 nomor 4).
 */
class HomeViewModel(
    private val reportRepository: ReportRepository,
    private val activityRepository: ActivityRepository,
    private val session: SessionHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(user = session.current))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            session.user.collect { profile ->
                _uiState.update { it.copy(user = profile, isPetugas = profile?.isPetugas == true) }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val reportsResult: AppResult<List<Report>>
            val activitiesResult: AppResult<List<ActivityItem>>
            coroutineScope {
                val reports = async { reportRepository.list(StatusFilter.ALL, page = 0) }
                val activities = async { activityRepository.list(category = null, page = 0) }
                reportsResult = reports.await()
                activitiesResult = activities.await()
            }
            val failed = listOfNotNull(
                (reportsResult as? AppResult.Failure)?.let { ErrorMapper.message(it.error) },
                (activitiesResult as? AppResult.Failure)?.let { ErrorMapper.message(it.error) },
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isPetugas = session.current?.isPetugas == true,
                    recentReports = (reportsResult as? AppResult.Success)?.data.orEmpty().take(RECENT_LIMIT),
                    upcomingActivities = (activitiesResult as? AppResult.Success)
                        ?.data
                        ?.filter { item -> item.isUpcoming() }
                        ?.take(UPCOMING_LIMIT)
                        .orEmpty(),
                    errorMessage = if (reportsResult is AppResult.Failure && activitiesResult is AppResult.Failure) {
                        failed.firstOrNull()
                    } else {
                        null
                    },
                )
            }
        }
    }

    companion object {
        const val RECENT_LIMIT = 3
        const val UPCOMING_LIMIT = 2
    }
}

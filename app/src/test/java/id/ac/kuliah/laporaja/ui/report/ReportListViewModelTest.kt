package id.ac.kuliah.laporaja.ui.report

import id.ac.kuliah.laporaja.core.error.AppError
import id.ac.kuliah.laporaja.core.paging.PAGE_SIZE
import id.ac.kuliah.laporaja.core.result.AppResult
import id.ac.kuliah.laporaja.core.session.SessionHolder
import id.ac.kuliah.laporaja.domain.model.NewReport
import id.ac.kuliah.laporaja.domain.model.Report
import id.ac.kuliah.laporaja.domain.model.ReportCategory
import id.ac.kuliah.laporaja.domain.model.ReportEdit
import id.ac.kuliah.laporaja.domain.model.ReportStatus
import id.ac.kuliah.laporaja.domain.model.StatusFilter
import id.ac.kuliah.laporaja.domain.model.UserProfile
import id.ac.kuliah.laporaja.domain.model.UserRole
import id.ac.kuliah.laporaja.domain.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Perilaku paginasi dan tiga cabang daftar (loading, error, empty) sesuai Subbab 12.2.
 * Repository diganti tiruan tangan supaya tidak menyentuh jaringan maupun database.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportListViewModelTest {

    private class FakeReportRepository : ReportRepository {
        val requested = mutableListOf<Pair<StatusFilter, Int>>()
        var pages: Map<Int, List<Report>> = emptyMap()
        var failure: AppError? = null

        override suspend fun list(statusFilter: StatusFilter, page: Int): AppResult<List<Report>> {
            requested += statusFilter to page
            failure?.let { return AppResult.Failure(it) }
            return AppResult.Success(pages[page].orEmpty())
        }

        override suspend fun create(input: NewReport, photoBytes: ByteArray?): AppResult<Report> =
            AppResult.Failure(AppError.Unknown)

        override suspend fun detail(id: String): AppResult<Report> = AppResult.Failure(AppError.Unknown)

        override suspend fun updateContent(id: String, input: ReportEdit): AppResult<Report> =
            AppResult.Failure(AppError.Unknown)

        override suspend fun updateStatus(id: String, status: ReportStatus, note: String?): AppResult<Report> =
            AppResult.Failure(AppError.Unknown)

        override suspend fun countMine(): AppResult<Int> = AppResult.Failure(AppError.Unknown)
    }

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeReportRepository
    private lateinit var session: SessionHolder

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeReportRepository()
        session = SessionHolder()
        session.setUser(profile(UserRole.WARGA))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `halaman pertama penuh menandai masih ada laporan berikutnya`() = runTest(dispatcher) {
        repository.pages = mapOf(0 to reports(0 until PAGE_SIZE))
        val viewModel = ReportListViewModel(repository, session)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(listOf(StatusFilter.ALL to 0), repository.requested)
        assertEquals(PAGE_SIZE, state.items.size)
        assertTrue(state.hasMore)
        assertFalse(state.isLoading)
        assertFalse(state.isPetugas)
        assertNull(state.errorMessage)
    }

    @Test
    fun `muat lebih banyak menempel halaman berikut tanpa menghapus isi`() = runTest(dispatcher) {
        repository.pages = mapOf(0 to reports(0 until PAGE_SIZE), 1 to reports(PAGE_SIZE until PAGE_SIZE + 7))
        val viewModel = ReportListViewModel(repository, session)
        runCurrent()

        viewModel.loadMore()
        runCurrent()

        assertEquals(listOf(StatusFilter.ALL to 0, StatusFilter.ALL to 1), repository.requested)
        assertEquals(PAGE_SIZE + 7, viewModel.uiState.value.items.size)
        assertFalse("Halaman pendek berarti data habis", viewModel.uiState.value.hasMore)
    }

    @Test
    fun `daftar kosong tidak menawarkan muat lebih banyak`() = runTest(dispatcher) {
        repository.pages = mapOf(0 to emptyList())
        val viewModel = ReportListViewModel(repository, session)
        runCurrent()

        assertFalse(viewModel.uiState.value.hasMore)
        assertTrue(viewModel.uiState.value.items.isEmpty())

        viewModel.loadMore()
        runCurrent()
        assertEquals(1, repository.requested.size)
    }

    @Test
    fun `ganti filter mengulang dari halaman nol`() = runTest(dispatcher) {
        repository.pages = mapOf(0 to reports(0 until 3))
        val viewModel = ReportListViewModel(repository, session)
        runCurrent()

        viewModel.onFilterChanged(StatusFilter.DIPROSES)
        runCurrent()

        assertEquals(StatusFilter.DIPROSES, viewModel.filter)
        assertEquals(
            listOf(StatusFilter.ALL to 0, StatusFilter.DIPROSES to 0),
            repository.requested,
        )
        assertEquals(3, viewModel.uiState.value.items.size)

        // Filter yang sama tidak memicu query ulang ke server.
        viewModel.onFilterChanged(StatusFilter.DIPROSES)
        runCurrent()
        assertEquals(2, repository.requested.size)
    }

    @Test
    fun `gagal memuat pertama menampilkan pesan tanpa data tiruan`() = runTest(dispatcher) {
        repository.failure = AppError.NoInternet
        val viewModel = ReportListViewModel(repository, session)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.items.isEmpty())
        assertEquals("Tidak ada koneksi internet, coba lagi", state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `gagal memuat halaman berikutnya tidak menghapus daftar`() = runTest(dispatcher) {
        repository.pages = mapOf(0 to reports(0 until PAGE_SIZE))
        val viewModel = ReportListViewModel(repository, session)
        runCurrent()

        repository.failure = AppError.Forbidden
        viewModel.loadMore()
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(PAGE_SIZE, state.items.size)
        assertEquals("Akun kamu belum punya hak untuk aksi ini", state.errorMessage)
    }

    @Test
    fun `akun petugas memuat daftar semua laporan`() = runTest(dispatcher) {
        session.setUser(profile(UserRole.PETUGAS))
        repository.pages = mapOf(0 to reports(0 until 2))
        val viewModel = ReportListViewModel(repository, session)
        runCurrent()

        assertTrue(viewModel.uiState.value.isPetugas)
    }

    private fun profile(role: UserRole) = UserProfile(
        id = "user-1",
        email = "warga.demo@laporaja.test",
        fullName = "Sari Wulandari",
        phone = "081234567890",
        rtRw = "003/005",
        role = role,
        avatarPath = null,
        createdAt = 0L,
    )

    private fun reports(range: IntRange): List<Report> = range.map { index ->
        Report(
            id = "r-$index",
            userId = "user-1",
            title = "Laporan $index",
            description = "Deskripsi laporan nomor $index untuk pengujian daftar.",
            category = ReportCategory.SAMPAH,
            location = "Gang $index",
            photoPath = null,
            status = ReportStatus.BARU,
            statusNote = null,
            resolvedAt = 0L,
            createdAt = index.toLong(),
            updatedAt = index.toLong(),
            responseCount = 0,
        )
    }
}

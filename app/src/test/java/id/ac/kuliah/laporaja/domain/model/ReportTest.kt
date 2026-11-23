package id.ac.kuliah.laporaja.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Hak ubah pemilik dan alur status petugas (Subbab 7.7 dan 7.9). */
class ReportTest {

    private fun report(
        status: ReportStatus,
        userId: String = OWNER,
        note: String? = null,
    ) = Report(
        id = "r-1",
        userId = userId,
        title = "Sampah menumpuk di sudut gang",
        description = "Tumpukan sampah sudah tiga hari tidak diangkut dan mulai berbau.",
        category = ReportCategory.SAMPAH,
        location = "Gang Mawar RT 003",
        photoPath = "reports/r-1.jpg",
        status = status,
        statusNote = note,
        resolvedAt = if (status == ReportStatus.SELESAI) DUA else 0L,
        createdAt = SATU,
        updatedAt = DUA,
        responseCount = 2,
    )

    @Test
    fun `pemilik hanya bisa mengubah laporan berstatus baru atau ditolak`() {
        assertTrue(report(ReportStatus.BARU).canEditContent(OWNER))
        assertTrue(report(ReportStatus.DITOLAK).canEditContent(OWNER))
        assertFalse(report(ReportStatus.DIPROSES).canEditContent(OWNER))
        assertFalse(report(ReportStatus.SELESAI).canEditContent(OWNER))
    }

    @Test
    fun `bukan pemilik dan sesi kosong tidak punya hak ubah`() {
        assertFalse(report(ReportStatus.BARU).canEditContent(OTHER))
        assertFalse(report(ReportStatus.BARU).canEditContent(null))
        assertFalse(report(ReportStatus.BARU).isOwnedBy(null))
        assertTrue(report(ReportStatus.BARU).isOwnedBy(OWNER))
    }

    @Test
    fun `laporan dianggap punya foto hanya bila path nya terisi`() {
        assertTrue(report(ReportStatus.BARU).hasPhoto)
        assertFalse(report(ReportStatus.BARU).copy(photoPath = null).hasPhoto)
        assertFalse(report(ReportStatus.BARU).copy(photoPath = "   ").hasPhoto)
    }

    @Test
    fun `linimasa baru menampilkan dua langkah pertama saja`() {
        val steps = report(ReportStatus.BARU).statusTimeline()
        assertEquals(3, steps.size)
        assertEquals("Dibuat", steps[0].label)
        assertTrue(steps[0].reached)
        assertEquals(SATU, steps[0].at)
        assertFalse(steps[1].reached)
        assertFalse(steps[2].reached)
    }

    @Test
    fun `linimasa diproses menempel catatan pada langkah kedua`() {
        val steps = report(ReportStatus.DIPROSES, note = "Sudah dijadwalkan angkut").statusTimeline()
        assertTrue(steps[1].reached)
        assertEquals(DUA, steps[1].at)
        assertEquals("Sudah dijadwalkan angkut", steps[1].note)
        assertFalse(steps[2].reached)
        assertNull(steps[2].note)
    }

    @Test
    fun `linimasa selesai memakai waktu penyelesaian`() {
        val steps = report(ReportStatus.SELESAI, note = "Sudah diangkut").statusTimeline()
        assertTrue(steps[1].reached)
        assertEquals(0L, steps[1].at)
        assertTrue(steps[2].reached)
        assertEquals(DUA, steps[2].at)
        assertEquals("Sudah diangkut", steps[2].note)
    }

    @Test
    fun `linimasa ditolak menambah langkah keempat`() {
        val steps = report(ReportStatus.DITOLAK, note = "Lokasi tidak jelas").statusTimeline()
        assertEquals(4, steps.size)
        assertEquals("Ditolak", steps[3].label)
        assertTrue(steps[3].reached)
        assertEquals("Lokasi tidak jelas", steps[3].note)
    }

    @Test
    fun `transisi status mengikuti alur petugas`() {
        assertEquals(
            listOf(ReportStatus.DIPROSES, ReportStatus.SELESAI, ReportStatus.DITOLAK),
            ReportStatus.BARU.allowedTransitions(),
        )
        assertEquals(listOf(ReportStatus.SELESAI, ReportStatus.DITOLAK, ReportStatus.BARU), ReportStatus.DIPROSES.allowedTransitions())
        assertEquals(listOf(ReportStatus.DIPROSES), ReportStatus.SELESAI.allowedTransitions())
        assertEquals(listOf(ReportStatus.BARU), ReportStatus.DITOLAK.allowedTransitions())
        assertTrue(ReportStatus.BARU.canTransitionTo(ReportStatus.DIPROSES))
        assertFalse(ReportStatus.BARU.canTransitionTo(ReportStatus.BARU))
        assertFalse(ReportStatus.SELESAI.canTransitionTo(ReportStatus.DITOLAK))
        assertEquals(emptyList<ReportStatus>(), ReportStatus.UNKNOWN.allowedTransitions())
    }

    @Test
    fun `nilai status asing dibaca sebagai tidak diketahui`() {
        assertEquals(ReportStatus.BARU, ReportStatus.from("baru"))
        assertEquals(ReportStatus.SELESAI, ReportStatus.from("selesai"))
        assertEquals(ReportStatus.UNKNOWN, ReportStatus.from("menunggu"))
        assertEquals(ReportStatus.UNKNOWN, ReportStatus.from(null))
    }

    @Test
    fun `kategori peran dan filter punya nilai cadangan`() {
        assertEquals(ReportCategory.SAMPAH, ReportCategory.from("sampah"))
        assertEquals(ReportCategory.LAINNYA, ReportCategory.from("tidak_ada"))
        assertEquals(ReportCategory.LAINNYA, ReportCategory.from(null))
        assertEquals(UserRole.PETUGAS, UserRole.from("petugas"))
        assertEquals(UserRole.WARGA, UserRole.from("entah"))
        assertEquals(StatusFilter.SELESAI, StatusFilter.from("SELESAI"))
        assertEquals(StatusFilter.ALL, StatusFilter.from("TIDAK_ADA"))
        assertNull(StatusFilter.ALL.status)
        assertEquals(ReportStatus.DIPROSES, StatusFilter.DIPROSES.status)
    }

    @Test
    fun `inisial avatar memakai dua kata pertama`() {
        assertEquals("SW", initialsOf("Sari Wulandari"))
        assertEquals("SA", initialsOf("  sari  "))
        assertEquals("?", initialsOf("   "))
    }

    private companion object {
        const val OWNER = "user-1"
        const val OTHER = "user-2"
        const val SATU = 1_700_000_000_000L
        const val DUA = 1_700_086_400_000L
    }
}

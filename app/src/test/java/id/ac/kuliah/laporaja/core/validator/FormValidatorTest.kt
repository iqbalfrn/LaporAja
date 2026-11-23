package id.ac.kuliah.laporaja.core.validator

import id.ac.kuliah.laporaja.domain.model.ActivityCategory
import id.ac.kuliah.laporaja.domain.model.ReportCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Aturan validasi klien mengikuti batas yang sama dengan cek server (Subbab 6). */
class FormValidatorTest {

    @Test
    fun `email menerima alamat valid dan menolak yang cacat`() {
        assertNull(FormValidator.email("warga.demo@laporaja.test"))
        assertNull(FormValidator.email("  Warga.Dua@laporaja.test  "))
        assertEquals("Format email belum benar", FormValidator.email(""))
        assertEquals("Format email belum benar", FormValidator.email("warga"))
        assertEquals("Format email belum benar", FormValidator.email("warga@laporaja"))
        assertEquals("Format email belum benar", FormValidator.email("warga@@laporaja.test"))
    }

    @Test
    fun `email dinormalisasi sebelum dikirim`() {
        assertEquals("warga.demo@laporaja.test", FormValidator.normalizedEmail("  Warga.Demo@LaporAja.test "))
    }

    @Test
    fun `password butuh delapan karakter ada huruf dan angka`() {
        assertNull(FormValidator.password("LaporAja2026"))
        assertEquals(
            "Password minimal 8 karakter dan ada angkanya",
            FormValidator.password("LaporAja"),
        )
        assertEquals(
            "Password minimal 8 karakter dan ada angkanya",
            FormValidator.password("12345678"),
        )
        assertEquals(
            "Password minimal 8 karakter dan ada angkanya",
            FormValidator.password("abc"),
        )
    }

    @Test
    fun `konfirmasi password harus sama`() {
        assertNull(FormValidator.passwordMatch("LaporAja2026", "LaporAja2026"))
        assertEquals("Konfirmasi password tidak sama", FormValidator.passwordMatch("a1a1a1a1", "a1a1a1a2"))
    }

    @Test
    fun `nama lengkap perlu dua kata`() {
        assertNull(FormValidator.fullName("Sari Wulandari"))
        assertEquals("Masukkan nama lengkap minimal 3 karakter", FormValidator.fullName("Jo"))
        assertEquals("Masukkan nama lengkap minimal 3 karakter", FormValidator.fullName("Jono"))
        assertNotNull(FormValidator.fullName("a".repeat(FormValidator.NAME_MAX + 1)))
    }

    @Test
    fun `nomor hp opsional tapi harus angka jika diisi`() {
        assertNull(FormValidator.phone(""))
        assertNull(FormValidator.phone("081234567890"))
        assertEquals("Nomor HP harus 9-16 digit angka", FormValidator.phone("0812"))
        assertEquals("Nomor HP harus 9-16 digit angka", FormValidator.phone("0812-3456-7890"))
    }

    @Test
    fun `rt rw mengikuti pola angka tiga digit`() {
        assertNull(FormValidator.rtRw(""))
        assertNull(FormValidator.rtRw("003/005"))
        assertEquals("Contoh format yang benar: 003/005", FormValidator.rtRw("03-05"))
        assertEquals("Contoh format yang benar: 003/005", FormValidator.rtRw("0003/005"))
    }

    @Test
    fun `judul laporan punya batas bawah dan atas`() {
        assertNull(FormValidator.title("Sampah menumpuk di sudut gang"))
        assertEquals(
            "Judul minimal 5 dan maksimal 80 karakter",
            FormValidator.title("samp"),
        )
        assertEquals(
            "Judul minimal 5 dan maksimal 80 karakter",
            FormValidator.title("a".repeat(FormValidator.TITLE_MAX + 1)),
        )
    }

    @Test
    fun `deskripsi laporan dan kegiatan pakai batas berbeda`() {
        val duaPuluh = "s".repeat(FormValidator.DESCRIPTION_MIN)
        assertNull(FormValidator.reportDescription(duaPuluh))
        assertEquals(
            "Jelaskan masalah minimal 20 karakter",
            FormValidator.reportDescription("Sampah menumpuk."),
        )
        assertNull(FormValidator.activityDescription("k".repeat(FormValidator.ACTIVITY_DESCRIPTION_MIN)))
        assertEquals(
            "Jelaskan kegiatan minimal 30 karakter",
            FormValidator.activityDescription(duaPuluh),
        )
    }

    @Test
    fun `lokasi wajib jelas dan tanggapan minimal tiga karakter`() {
        assertNull(FormValidator.location("Belakang pos ronda"))
        assertEquals("Tulis lokasi atau patokan yang jelas", FormValidator.location("gang"))
        assertNull(FormValidator.responseMessage("Sudah dikerjakan ya"))
        assertEquals("Tulis tanggapan minimal 3 karakter", FormValidator.responseMessage("  "))
        assertNull(FormValidator.statusNote("Catatan singkat"))
        assertEquals(
            "Catatan maksimal 300 karakter",
            FormValidator.statusNote("x".repeat(FormValidator.STATUS_NOTE_MAX + 1)),
        )
    }

    @Test
    fun `kategori harus dipilih`() {
        assertNull(FormValidator.category(ReportCategory.SAMPAH))
        assertEquals("Pilih kategori laporan", FormValidator.category(null))
        assertNull(FormValidator.activityCategory(ActivityCategory.KERJA_BAKTI))
        assertEquals("Pilih kategori kegiatan", FormValidator.activityCategory(null))
    }

    @Test
    fun `kuota antara satu sampai sembilan ratus sembilan puluh sembilan`() {
        assertNull(FormValidator.quota(""))
        assertNull(FormValidator.quota("30"))
        assertEquals("Kuota antara 1 dan 999", FormValidator.quota("0"))
        assertEquals("Kuota antara 1 dan 999", FormValidator.quota("1000"))
        assertEquals("Kuota antara 1 dan 999", FormValidator.quota("tiga"))
    }

    @Test
    fun `jadwal harus di masa depan dan syarat harus dicentang`() {
        assertNull(FormValidator.futureSchedule(2_000L, 1_000L))
        assertEquals("Pilih tanggal yang belum berlalu", FormValidator.futureSchedule(1_000L, 1_000L))
        assertNull(FormValidator.termsAccepted(true))
        assertEquals("Setujui syarat penggunaan terlebih dahulu", FormValidator.termsAccepted(false))
        assertNull(FormValidator.loginFields("warga@laporaja.test", "LaporAja2026"))
        assertEquals("Email dan password harus diisi", FormValidator.loginFields("", "x"))
    }
}

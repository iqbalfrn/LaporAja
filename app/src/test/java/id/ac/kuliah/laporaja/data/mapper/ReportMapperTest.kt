package id.ac.kuliah.laporaja.data.mapper

import id.ac.kuliah.laporaja.data.remote.SupabaseTables.Column
import id.ac.kuliah.laporaja.data.remote.dto.ReportDto
import id.ac.kuliah.laporaja.data.remote.dto.ResponseDto
import id.ac.kuliah.laporaja.domain.model.NewReport
import id.ac.kuliah.laporaja.domain.model.ReportCategory
import id.ac.kuliah.laporaja.domain.model.ReportEdit
import id.ac.kuliah.laporaja.domain.model.ReportStatus
import id.ac.kuliah.laporaja.domain.model.UserRole
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** Baris PostgREST bisa datang tanpa kolom opsional; pemetaan tidak boleh menjatuhkan aplikasi (Subbab 12.4). */
class ReportMapperTest {

    private val json = """
        [
          {
            "id": "r-1",
            "user_id": "user-1",
            "title": "Sampah menumpuk di sudut gang",
            "description": "Sudah tiga hari tidak diangkut.",
            "category": "sampah",
            "location": "Gang Mawar RT 003",
            "photo_path": "reports/r-1.jpg",
            "status": "diproses",
            "status_note": "Sudah dijadwalkan",
            "resolved_at": null,
            "created_at": "2026-11-14T01:00:00+00:00",
            "updated_at": "2026-11-15T02:30:00+00:00",
            "report_responses": [{ "count": 3 }]
          },
          {
            "id": "r-2",
            "title": "Lampu jalan mati",
            "category": "tidak_ada",
            "status": "menunggu",
            "created_at": "2026-11-14 01:00:00"
          },
          { "title": "baris tanpa id harus dibuang" }
        ]
    """.trimIndent()

    @Test
    fun `baris lengkap termetak ke model domain`() {
        val rows = ReportDto.list(json)
        assertEquals(2, rows.size)
        val first = ReportMapper.toDomain(rows[0])
        assertEquals("r-1", first.id)
        assertEquals(ReportCategory.SAMPAH, first.category)
        assertEquals(ReportStatus.DIPROSES, first.status)
        assertEquals("Sudah dijadwalkan", first.statusNote)
        assertEquals(3, first.responseCount)
        assertTrue(first.hasPhoto)
        assertEquals(Instant.parse("2026-11-14T01:00:00Z").toEpochMilli(), first.createdAt)
        assertEquals(Instant.parse("2026-11-15T02:30:00Z").toEpochMilli(), first.updatedAt)
        assertEquals(0L, first.resolvedAt)
    }

    @Test
    fun `kolom yang hilang memakai nilai cadangan`() {
        val second = ReportMapper.toDomain(ReportDto.list(json)[1])
        assertEquals("", second.userId)
        assertEquals("", second.description)
        assertEquals(ReportCategory.LAINNYA, second.category)
        assertEquals(ReportStatus.UNKNOWN, second.status)
        assertNull(second.photoPath)
        assertNull(second.statusNote)
        assertEquals(0, second.responseCount)
        assertEquals(Instant.parse("2026-11-14T01:00:00Z").toEpochMilli(), second.createdAt)
    }

    @Test
    fun `satu objek saja tetap terbaca sebagai satu baris`() {
        val single = ReportDto.first("""{"id":"r-9","status":"selesai"}""")
        assertEquals("r-9", single?.id)
        assertEquals(ReportStatus.SELESAI, ReportMapper.toDomain(single!!).status)
        assertNull(ReportDto.first("""[{"title":"tanpa id"}]"""))
        assertTrue(ReportDto.list("bukan json").isEmpty())
        assertTrue(ReportDto.list(null).isEmpty())
    }

    @Test
    fun `sisipan laporan mengirim kolom milik tabel reports`() {
        val payload = ReportMapper.toInsertJson(
            input = NewReport(
                title = "Saluran air mampet",
                description = "Air menggenang sampai ke teras rumah warga.",
                category = ReportCategory.SALURAN,
                location = "Depan rumah nomor 12",
            ),
            userId = "user-1",
            photoPath = null,
        ).jsonObject
        assertEquals("user-1", payload[Column.USER_ID]?.jsonPrimitive?.content)
        assertEquals("saluran", payload[Column.CATEGORY]?.jsonPrimitive?.content)
        assertTrue(payload[Column.PHOTO_PATH] is JsonNull)
    }

    @Test
    fun `ubah isi tidak menyentuh kolom status`() {
        val payload = ReportMapper.toContentJson(
            ReportEdit("Judul baru", "Deskripsi baru yang cukup panjang", ReportCategory.JALAN, "Jalan Kenanga"),
        ).jsonObject
        assertEquals(4, payload.size)
        assertNull(payload[Column.STATUS])
        assertEquals("jalan", payload[Column.CATEGORY]?.jsonPrimitive?.content)
    }

    @Test
    fun `waktu penyelesaian hanya terisi saat status selesai`() {
        val selesai = ReportMapper.toStatusJson(ReportStatus.SELESAI, "Sudah diangkut").jsonObject
        assertEquals("selesai", selesai[Column.STATUS]?.jsonPrimitive?.content)
        assertTrue(selesai[Column.RESOLVED_AT] is JsonPrimitive)
        assertTrue((selesai[Column.RESOLVED_AT]!!.jsonPrimitive.content).endsWith("Z"))

        val dibuka = ReportMapper.toStatusJson(ReportStatus.BARU, null).jsonObject
        assertTrue(dibuka[Column.RESOLVED_AT] is JsonNull)
        assertTrue(dibuka[Column.STATUS_NOTE] is JsonNull)
    }

    @Test
    fun `pengirim tanggapan tanpa profil memakai nama warga`() {
        val dto = ResponseDto(
            id = "a-1",
            reportId = "r-1",
            userId = "user-2",
            message = "Sudah saya cek",
            isOfficial = true,
            createdAt = "2026-11-16T03:04:05+00:00",
            authorName = null,
            authorRole = null,
            authorAvatarPath = null,
        )
        val domain = ResponseMapper.toDomain(dto)
        assertEquals("Warga", domain.authorName)
        assertEquals(UserRole.WARGA, domain.authorRole)
        assertTrue(domain.isOfficial)
        assertEquals(Instant.parse("2026-11-16T03:04:05Z").toEpochMilli(), domain.createdAt)
    }
}

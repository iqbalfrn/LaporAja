package id.ac.kuliah.laporaja.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Syarat penggunaan (Subbab 10.1) ditulis satu kali di sini, dipakai oleh S15 dan dialog
 * persetujuan pada S2 supaya isinya tidak bercabang.
 */
object AppContent {

    val appName = "LaporAja"
    val version = "1.0"
    val tagline = "Kanal warga untuk melapor dan mengikuti kegiatan lingkungan RT/RW."

    val terms = listOf(
        "LaporAja dipakai untuk menyampaikan laporan kondisi lingkungan dan membaca agenda kegiatan di tingkat RT/RW.",
        "Isi laporan harus sesuai keadaan sebenarnya, memakai bahasa yang sopan, dan tidak memuat data pribadi orang lain.",
        "Foto yang diunggah sebaiknya milik sendiri atau boleh dipublikasikan, tanpa nomor polisi, wajah, atau dokumen pribadi.",
        "Nama dan inisial kamu tampil bersama laporan serta tanggapan supaya pengurus tahu siapa yang melapor.",
        "Nomor HP dan alamat detail hanya dipakai pengurus untuk koordinasi, tidak ditampilkan di layar mana pun.",
        "Petugas dapat mengubah status laporan, menambahkan tanggapan resmi, dan memposting kegiatan.",
        "Laporan yang sudah selesai atau ditolak tetap tersimpan sebagai arsip dan dapat dibuka kembali oleh petugas.",
        "Gunakan akun hanya atas nama sendiri. Sesi yang tidak dipakai akan berakhir sendiri dan kamu perlu masuk kembali.",
    )

    val credits = listOf(
        "Backend: Supabase (Auth, PostgREST, Storage) dengan Row Level Security aktif di semua tabel.",
        "Antarmuka: Jetpack Compose dan Material 3, tanpa library UI pihak ketiga.",
        "Data demo berisi nama, lokasi, dan nomor telepon fiktif buatan sendiri.",
    )
}

@Composable
fun TermsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Syarat penggunaan") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppContent.terms.forEach { item ->
                    Text(text = item, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        },
    )
}

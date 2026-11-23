package id.ac.kuliah.laporaja.ui.gate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.ac.kuliah.laporaja.ui.components.AppTopBar

/**
 * Layar pengalih untuk rute khusus petugas yang dibuka akun warga (Subbab 10.3).
 * Otorisasi tetap milik RLS; layar ini hanya mencegah tampilan form yang pasti ditolak server.
 */
@Composable
fun PetugasGateScreen(onBack: () -> Unit) {
    Scaffold(topBar = { AppTopBar(title = "Khusus Petugas", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Layar ini khusus petugas",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Hanya akun dengan peran petugas yang dapat memposting kegiatan. " +
                    "Peran ditetapkan pengurus lewat dasbor Supabase, bukan dari aplikasi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Kembali ke katalog kegiatan")
            }
        }
    }
}

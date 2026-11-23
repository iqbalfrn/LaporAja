package id.ac.kuliah.laporaja.ui.components

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.ac.kuliah.laporaja.core.error.ErrorMapper
import id.ac.kuliah.laporaja.core.image.ImageCompressor
import id.ac.kuliah.laporaja.core.result.AppResult
import kotlinx.coroutines.launch

/**
 * Pemilih gambar + pratinjau hasil kompresi, dipakai foto profil, foto laporan, dan poster
 * kegiatan (Subbab 9.3). Pratinjau selalu berkas yang akan diunggah, bukan berkas asal.
 */
@Composable
fun ImagePickerSection(
    label: String,
    photoBytes: ByteArray?,
    isBusy: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
    aspectRatioValue: Float = 16f / 9f,
    emptyHint: String = "Belum ada gambar terpilih",
    enabled: Boolean = true,
    onPicked: (ByteArray) -> Unit,
    onRejected: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            when (val result = ImageCompressor.compress(context, uri)) {
                is AppResult.Success -> onPicked(result.data)
                is AppResult.Failure -> onRejected(ErrorMapper.message(result.error))
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
        Surface(
            modifier = Modifier.fillMaxWidth().aspectRatio(aspectRatioValue),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    isBusy -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                        Text(
                            text = "Mengunggah foto...",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    photoBytes != null -> {
                        val bitmap = remember(photoBytes) {
                            BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Pratinjau $label",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            Text(
                                text = "Gambar tidak dapat dibaca, pilih ulang",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    else -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddAPhoto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                        )
                        Text(
                            text = emptyHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (error != null) {
            Text(text = error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        Column(horizontalAlignment = Alignment.Start) {
            OutlinedButton(
                onClick = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                enabled = enabled && !isBusy,
            ) {
                Text(if (photoBytes == null) "Pilih dari galeri" else "Ganti gambar")
            }
            if (photoBytes != null) {
                TextButton(onClick = onRemove, enabled = enabled && !isBusy) {
                    Text("Hapus gambar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

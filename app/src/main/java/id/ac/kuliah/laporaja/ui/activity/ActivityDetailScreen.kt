package id.ac.kuliah.laporaja.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.core.image.PublicUrlBuilder
import id.ac.kuliah.laporaja.core.time.AppTime
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.ErrorState
import id.ac.kuliah.laporaja.ui.components.LoadingState
import id.ac.kuliah.laporaja.ui.components.NoPhotoPlaceholder
import id.ac.kuliah.laporaja.ui.components.RemoteImage
import id.ac.kuliah.laporaja.ui.components.toIcon

/**
 * S12: detail kegiatan. Tidak ada tombol daftar peserta karena pendaftaran tidak ada dalam
 * lingkup produk (Subbab 10.3).
 */
@Composable
fun ActivityDetailScreen(
    activityId: String,
    onBack: () -> Unit,
) {
    val viewModel = appViewModel { ActivityDetailViewModel(it.activityRepository, activityId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { AppTopBar(title = "Detail Kegiatan", onBack = onBack) }) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { LoadingState(label = "Memuat kegiatan...") }

            state.activity == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(
                    message = state.errorMessage ?: ActivityDetailViewModel.ACTIVITY_NOT_FOUND,
                    onRetry = viewModel::load,
                )
            }

            else -> {
                val activity = state.activity
                if (activity != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        ActivityPoster(
                            posterPath = activity.posterPath,
                            title = activity.title,
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(text = activity.title, style = MaterialTheme.typography.headlineSmall)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                AssistChip(
                                    onClick = { },
                                    enabled = false,
                                    label = { Text(activity.category.label) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = activity.category.toIcon(),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                )
                                if (!activity.isUpcoming()) {
                                    Text(
                                        text = "Sudah berlangsung",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.outline,
                                    )
                                }
                            }
                            DetailLine(
                                icon = Icons.Outlined.Schedule,
                                text = AppTime.dateTimeLong(activity.scheduledAt),
                            )
                            DetailLine(icon = Icons.Outlined.Place, text = activity.location)
                            DetailLine(
                                icon = Icons.Outlined.Groups,
                                text = activity.quota?.let { "Kuota $it peserta" } ?: "Tanpa batas peserta",
                            )
                            HorizontalDivider()
                            Text(
                                text = activity.description,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            HorizontalDivider()
                            Text(
                                text = activity.creatorName?.let { "Diposting oleh $it" }
                                    ?: "Diposting oleh pengurus",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityPoster(posterPath: String?, title: String) {
    val url = PublicUrlBuilder.activityPoster(posterPath)
    if (url == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) { NoPhotoPlaceholder(label = "Kegiatan ini tanpa poster") }
    } else {
        RemoteImage(
            url = url,
            contentDescription = "Poster $title",
            aspectRatio = 4f / 3f,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DetailLine(
    icon: ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

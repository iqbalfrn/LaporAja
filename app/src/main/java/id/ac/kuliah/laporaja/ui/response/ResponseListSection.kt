package id.ac.kuliah.laporaja.ui.response

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.ac.kuliah.laporaja.core.image.PublicUrlBuilder
import id.ac.kuliah.laporaja.core.time.AppTime
import id.ac.kuliah.laporaja.domain.model.ReportResponse
import id.ac.kuliah.laporaja.domain.model.initialsOf
import id.ac.kuliah.laporaja.domain.model.UserRole
import id.ac.kuliah.laporaja.ui.components.EmptyState
import id.ac.kuliah.laporaja.ui.components.ErrorState
import id.ac.kuliah.laporaja.ui.components.LoadingState
import id.ac.kuliah.laporaja.ui.components.RemoteImage

/**
 * Section tanggapan milik modul A4 (Subbab 10.2). Ditanam sebagai composable terpisah di S6
 * supaya pemegang modul lain tidak menulis ke tabel ini.
 */
@Composable
fun ResponseListSection(
    state: ResponseListUiState,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Tanggapan",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        when {
            state.isLoading -> LoadingState(label = "Memuat tanggapan...")

            state.errorMessage != null -> ErrorState(message = state.errorMessage, onRetry = onRetry)

            state.items.isEmpty() -> EmptyState(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = "Belum ada tanggapan",
                message = "Belum ada tanggapan. Jadilah yang pertama menanggapi laporan ini.",
                modifier = Modifier.padding(vertical = 8.dp),
            )

            else -> state.items.forEachIndexed { index, response ->
                ResponseItem(response = response)
                if (index != state.items.lastIndex) HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ResponseItem(response: ReportResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        if (response.isOfficial) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AuthorAvatar(response = response)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = response.authorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (response.isOfficial) FontWeight.Bold else FontWeight.Normal,
                )
                if (response.isOfficial) {
                    Text(
                        text = "Resmi dari petugas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = response.message,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = AppTime.relative(response.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun AuthorAvatar(response: ReportResponse) {
    val container = if (response.authorRole == UserRole.PETUGAS) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val url = PublicUrlBuilder.avatar(response.authorAvatarPath)
    if (url != null) {
        RemoteImage(
            url = url,
            contentDescription = "Foto ${response.authorName}",
            aspectRatio = 1f,
            modifier = Modifier.size(36.dp).clip(CircleShape),
        )
        return
    }
    Surface(shape = CircleShape, color = container, modifier = Modifier.size(36.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initialsOf(response.authorName),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

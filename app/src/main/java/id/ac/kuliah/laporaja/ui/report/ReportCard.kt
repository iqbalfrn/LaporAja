package id.ac.kuliah.laporaja.ui.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.ac.kuliah.laporaja.core.image.PublicUrlBuilder
import id.ac.kuliah.laporaja.core.time.AppTime
import id.ac.kuliah.laporaja.domain.model.Report
import id.ac.kuliah.laporaja.ui.components.CategoryIcon
import id.ac.kuliah.laporaja.ui.components.NoPhotoPlaceholder
import id.ac.kuliah.laporaja.ui.components.RemoteImage
import id.ac.kuliah.laporaja.ui.components.StatusChip

/** Kartu laporan untuk S4 dan pratinjau S3 (Subbab 10.1). */
@Composable
fun ReportCard(
    report: Report,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportThumbnail(report)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                StatusChip(status = report.status)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    CategoryIcon(category = report.category, modifier = Modifier.size(16.dp))
                    Text(
                        text = report.category.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = report.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = AppTime.relative(report.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    ResponseCountBadge(count = report.responseCount)
                }
            }
        }
    }
}

@Composable
private fun ReportThumbnail(report: Report) {
    val url = PublicUrlBuilder.reportPhoto(report.photoPath)
    Surface(
        modifier = Modifier.width(96.dp).height(72.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (url == null) {
            NoPhotoPlaceholder(label = "Tanpa foto")
        } else {
            RemoteImage(
                url = url,
                contentDescription = "Foto laporan ${report.title}",
                aspectRatio = 4f / 3f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun ResponseCountBadge(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = if (count == 1) "1 tanggapan" else "$count tanggapan",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

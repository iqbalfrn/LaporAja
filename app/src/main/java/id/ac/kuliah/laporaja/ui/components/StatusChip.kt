package id.ac.kuliah.laporaja.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import id.ac.kuliah.laporaja.domain.model.ReportStatus
import id.ac.kuliah.laporaja.ui.theme.LocalStatusColors

/**
 * Badge status: ikon, teks, dan warna (Subbab 11.3). Status tetap terbaca tanpa warna
 * karena ikon dan label selalu ikut tampil (Subbab 11.1).
 */
@Composable
fun StatusChip(
    status: ReportStatus,
    modifier: Modifier = Modifier,
) {
    val colors = LocalStatusColors.current[status]
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = colors.container,
        contentColor = colors.content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = status.iconForStatus(),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Text(text = status.label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun ReportStatus.iconForStatus(): ImageVector = when (this) {
    ReportStatus.BARU -> Icons.AutoMirrored.Filled.HelpOutline
    ReportStatus.DIPROSES -> Icons.Filled.HourglassBottom
    ReportStatus.SELESAI -> Icons.Filled.CheckCircle
    ReportStatus.DITOLAK -> Icons.Filled.ErrorOutline
    ReportStatus.UNKNOWN -> Icons.Filled.ErrorOutline
}

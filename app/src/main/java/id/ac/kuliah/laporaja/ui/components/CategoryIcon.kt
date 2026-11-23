package id.ac.kuliah.laporaja.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import id.ac.kuliah.laporaja.domain.model.ActivityCategory
import id.ac.kuliah.laporaja.domain.model.ReportCategory

/** Ikon kategori laporan mengikuti katalog tetap Subbab 7.6. */
@Composable
fun CategoryIcon(
    category: ReportCategory,
    modifier: Modifier = Modifier,
    contentDescription: String? = category.label,
) {
    Icon(imageVector = category.toIcon(), contentDescription = contentDescription, modifier = modifier)
}

fun ReportCategory.toIcon(): ImageVector = when (this) {
    ReportCategory.SAMPAH -> Icons.Filled.Delete
    ReportCategory.SALURAN -> Icons.Filled.WaterDrop
    ReportCategory.JALAN -> Icons.Filled.Report
    ReportCategory.PENERANGAN -> Icons.Filled.Lightbulb
    ReportCategory.FASUM -> Icons.Filled.Park
    ReportCategory.LAINNYA -> Icons.Filled.MoreHoriz
}

/** Ikon kategori kegiatan (Subbab 10.3). */
fun ActivityCategory.toIcon(): ImageVector = when (this) {
    ActivityCategory.KERJA_BAKTI -> Icons.Outlined.Groups
    ActivityCategory.EDUKASI -> Icons.Outlined.Visibility
    ActivityCategory.BANK_SAMPAH -> Icons.Outlined.Recycling
    ActivityCategory.KUNJUNGAN -> Icons.Outlined.Campaign
}

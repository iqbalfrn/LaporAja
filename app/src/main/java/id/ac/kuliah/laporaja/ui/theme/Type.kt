package id.ac.kuliah.laporaja.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * Tipografi Material 3 bawaan dengan penyesuaian ukuran terkecil (Subbab 11.2):
 * body minimal 13 sp dan label chip 12 sp agar tetap terbaca di layar 5 inci.
 */
val LaporAjaTypography = Typography(
    bodySmall = TextStyle.Default.copy(fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle.Default.copy(fontSize = 12.sp, lineHeight = 16.sp),
)

package id.ac.kuliah.laporaja.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import id.ac.kuliah.laporaja.domain.model.ReportStatus

internal val LaporAjaLightScheme = lightColorScheme(
    primary = HijauDaun,
    onPrimary = PermukaanTerang,
    primaryContainer = HijauDaunTerang,
    onPrimaryContainer = HijauDaunGelap,
    secondary = Toska,
    onSecondary = PermukaanTerang,
    secondaryContainer = ToskaTerang,
    onSecondaryContainer = ToskaGelap,
    tertiary = Amber,
    onTertiary = PermukaanTerang,
    tertiaryContainer = AmberTerang,
    onTertiaryContainer = AmberGelap,
    error = MerahGalat,
    onError = PermukaanTerang,
    errorContainer = MerahGalatTerang,
    onErrorContainer = MerahGalatGelap,
    background = LatarTerang,
    surface = PermukaanTerang,
)

internal val LaporAjaDarkScheme = darkColorScheme(
    primary = HijauDaunTerang,
    onPrimary = HijauDaunGelap,
    primaryContainer = HijauDaun,
    onPrimaryContainer = HijauDaunTerang,
    secondary = ToskaTerang,
    onSecondary = ToskaGelap,
    secondaryContainer = Toska,
    onSecondaryContainer = ToskaTerang,
    tertiary = AmberTerang,
    onTertiary = AmberGelap,
    tertiaryContainer = Amber,
    onTertiaryContainer = AmberTerang,
    error = MerahGalatTerang,
    onError = MerahGalatGelap,
    errorContainer = MerahGalat,
    onErrorContainer = MerahGalatTerang,
    background = LatarGelap,
    surface = PermukaanGelap,
)

/**
 * Warna badge status. Disediakan lewat tema supaya composable tidak menulis nilai hex (Subbab 11.2).
 */
@Immutable
data class StatusColors(
    val container: Color,
    val content: Color,
)

@Immutable
data class LaporAjaStatusColors(
    val baru: StatusColors,
    val diproses: StatusColors,
    val selesai: StatusColors,
    val ditolak: StatusColors,
    val tidakJelas: StatusColors,
) {
    operator fun get(status: ReportStatus): StatusColors = when (status) {
        ReportStatus.BARU -> baru
        ReportStatus.DIPROSES -> diproses
        ReportStatus.SELESAI -> selesai
        ReportStatus.DITOLAK -> ditolak
        ReportStatus.UNKNOWN -> tidakJelas
    }
}

internal val LaporAjaLightStatusColors = LaporAjaStatusColors(
    baru = StatusColors(StatusBaruContainerTerang, StatusBaruIsiTerang),
    diproses = StatusColors(StatusDiprosesContainerTerang, StatusDiprosesIsiTerang),
    selesai = StatusColors(StatusSelesaiContainerTerang, StatusSelesaiIsiTerang),
    ditolak = StatusColors(StatusDitolakContainerTerang, StatusDitolakIsiTerang),
    tidakJelas = StatusColors(StatusTidakJelasContainerTerang, StatusTidakJelasIsiTerang),
)

internal val LaporAjaDarkStatusColors = LaporAjaStatusColors(
    baru = StatusColors(StatusBaruContainerGelap, StatusBaruIsiGelap),
    diproses = StatusColors(StatusDiprosesContainerGelap, StatusDiprosesIsiGelap),
    selesai = StatusColors(StatusSelesaiContainerGelap, StatusSelesaiIsiGelap),
    ditolak = StatusColors(StatusDitolakContainerGelap, StatusDitolakIsiGelap),
    tidakJelas = StatusColors(StatusTidakJelasContainerGelap, StatusTidakJelasIsiGelap),
)

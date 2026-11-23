package id.ac.kuliah.laporaja.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * dynamicColor tersedia (Android 12+) tetapi bawaannya dimatikan: identitas aplikasi
 * justru warna merek pada Subbab 11.2 dan hasil demo harus sama di semua perangkat.
 */
@Composable
fun LaporAjaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> LaporAjaDarkScheme
        else -> LaporAjaLightScheme
    }
    val statusColors = if (darkTheme) LaporAjaDarkStatusColors else LaporAjaLightStatusColors
    CompositionLocalProvider(LocalStatusColors provides statusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LaporAjaTypography,
            content = content,
        )
    }
}

val LocalStatusColors = staticCompositionLocalOf<LaporAjaStatusColors> { LaporAjaLightStatusColors }

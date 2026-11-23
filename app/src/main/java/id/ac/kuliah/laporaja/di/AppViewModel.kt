package id.ac.kuliah.laporaja.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/** Wadah dependensi tersedia untuk seluruh rute tanpa meneruskan parameter manual (Subbab 6.1). */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer belum disediakan oleh NavGraph")
}

/**
 * Factory manual untuk semua ViewModel: tanpa Hilt, tanpa anotasi.
 * Setiap layar memanggil ini dengan lambda pembuat ViewModel-nya.
 */
@Composable
inline fun <reified VM : ViewModel> appViewModel(noinline creator: (AppContainer) -> VM): VM {
    val container = LocalAppContainer.current
    val factory = remember(container) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = creator(container) as T
        }
    }
    return viewModel(factory = factory)
}

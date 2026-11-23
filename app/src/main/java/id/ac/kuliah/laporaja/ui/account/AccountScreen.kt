package id.ac.kuliah.laporaja.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.BuildConfig
import id.ac.kuliah.laporaja.core.image.PublicUrlBuilder
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.domain.model.UserProfile
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.ConfirmationDialog
import id.ac.kuliah.laporaja.ui.components.ErrorState
import id.ac.kuliah.laporaja.ui.components.RemoteImage
import id.ac.kuliah.laporaja.ui.navigation.AppBottomBar
import id.ac.kuliah.laporaja.ui.navigation.Routes

/** S13: identitas akun, jumlah laporan sendiri, dan pintu keluar (Subbab 9.2). */
@Composable
fun AccountScreen(
    notice: String?,
    onNoticeShown: () -> Unit,
    onSelectRoute: (String) -> Unit,
    onEditProfile: () -> Unit,
    onAbout: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val viewModel = appViewModel {
        AccountViewModel(it.authRepository, it.reportRepository, it.sessionHolder)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmLogout by remember { mutableStateOf(false) }

    LaunchedEffect(notice) {
        if (notice != null) {
            snackbarHostState.showSnackbar(notice)
            onNoticeShown()
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Akun Saya") },
        bottomBar = { AppBottomBar(selectedRoute = Routes.ACCOUNT, onSelectRoute = onSelectRoute) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            val user = state.user
            if (user == null) {
                // Sesi belum terbaca: layar ini hanya bisa dibuka setelah login, jadi tunggu state.
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                ProfileHeader(user)
                ReportCountSection(
                    isLoading = state.isCountLoading,
                    count = state.myReportCount,
                    error = state.countError,
                    onRetry = viewModel::loadReportCount,
                )
                HorizontalDivider()
                TextButton(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ListItem(
                        leadingContent = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        headlineContent = { Text("Edit profil") },
                        supportingContent = { Text("Nama, no. HP, RT/RW, dan foto profil") },
                    )
                }
                TextButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                        headlineContent = { Text("Tentang aplikasi") },
                        supportingContent = { Text("Versi, cara kerja, dan syarat penggunaan") },
                    )
                }
                TextButton(
                    onClick = { confirmLogout = true },
                    enabled = !state.isLoggingOut,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ListItem(
                        leadingContent = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
                        headlineContent = { Text("Keluar") },
                        supportingContent = { Text("Sesi di perangkat ini dihapus") },
                    )
                }
                if (state.isLoggingOut) {
                    Text(
                        text = "Menghapus sesi...",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
                // Hanya ada pada build debug: mempercepat berpindah akun warga/petugas saat demo.
                if (BuildConfig.DEBUG) {
                    HorizontalDivider()
                    TextButton(
                        onClick = { viewModel.logout(onLoggedOut) },
                        enabled = !state.isLoggingOut,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ListItem(
                            leadingContent = { Icon(Icons.Outlined.SwapHoriz, contentDescription = null) },
                            headlineContent = { Text("Ganti akun demo") },
                            supportingContent = { Text("Khusus build debug: langsung masuk tanpa dialog") },
                        )
                    }
                }
            }
        }
    }

    if (confirmLogout) {
        ConfirmationDialog(
            title = "Keluar dari LaporAja?",
            body = "Semua data tetap aman di server. Kamu perlu masuk kembali untuk melapor.",
            confirmLabel = "Keluar",
            onConfirm = {
                confirmLogout = false
                viewModel.logout(onLoggedOut)
            },
            onDismiss = { confirmLogout = false },
        )
    }
}

@Composable
private fun ProfileHeader(user: UserProfile) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Avatar(user)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = user.fullName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RoleBadge(user)
                Text(
                    text = user.rtRw?.let { "RT/RW $it" } ?: "RT/RW belum diisi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Avatar(user: UserProfile) {
    val url = PublicUrlBuilder.avatar(user.avatarPath)
    if (url == null) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = user.initials(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    } else {
        RemoteImage(
            url = url,
            contentDescription = "Foto profil ${user.fullName}",
            aspectRatio = 1f,
            modifier = Modifier.size(64.dp).clip(CircleShape),
        )
    }
}

@Composable
private fun RoleBadge(user: UserProfile) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = if (user.isPetugas) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        },
        contentColor = if (user.isPetugas) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onTertiaryContainer
        },
    ) {
        Text(
            text = user.role.label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ReportCountSection(
    isLoading: Boolean,
    count: Int,
    error: String?,
    onRetry: () -> Unit,
) {
    when {
        error != null -> ErrorState(message = error, onRetry = onRetry)
        isLoading -> Text(
            text = "Menghitung laporan kamu...",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        else -> Text(
            text = if (count == 1) "Kamu sudah mengirim 1 laporan" else "Kamu sudah mengirim $count laporan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
    }
}

package id.ac.kuliah.laporaja.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.core.time.AppTime
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.domain.model.ActivityItem
import id.ac.kuliah.laporaja.domain.model.Report
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.EmptyState
import id.ac.kuliah.laporaja.ui.components.ErrorState
import id.ac.kuliah.laporaja.ui.components.LoadingState
import id.ac.kuliah.laporaja.ui.components.toIcon
import id.ac.kuliah.laporaja.ui.navigation.AppBottomBar
import id.ac.kuliah.laporaja.ui.navigation.Routes
import id.ac.kuliah.laporaja.ui.report.ReportCard

/**
 * S3: beranda ringan berisi sapaan, peran, laporan terbaru, dan kegiatan berikutnya. Semua angka
 * dibaca dari database lewat repository, tidak ada nilai tetap (Subbab 5, 10.3).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSelectRoute: (String) -> Unit,
    onOpenReport: (String) -> Unit,
    onOpenActivity: (String) -> Unit,
    onOpenNewReport: () -> Unit,
) {
    val viewModel = appViewModel {
        HomeViewModel(it.reportRepository, it.activityRepository, it.sessionHolder)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar(title = "Beranda") },
        bottomBar = { AppBottomBar(selectedRoute = Routes.HOME, onSelectRoute = onSelectRoute) },
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { LoadingState(label = "Menyiapkan beranda...") }

            state.errorMessage != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(message = state.errorMessage.orEmpty(), onRetry = viewModel::load)
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GreetingCard(
                    firstName = state.firstName,
                    isPetugas = state.isPetugas,
                    onOpenNewReport = onOpenNewReport,
                    onOpenReports = { onSelectRoute(Routes.REPORTS) },
                )
                RecentReports(
                    reports = state.recentReports,
                    onOpenReport = onOpenReport,
                    onOpenAll = { onSelectRoute(Routes.REPORTS) },
                )
                UpcomingActivities(
                    activities = state.upcomingActivities,
                    onOpenActivity = onOpenActivity,
                    onOpenAll = { onSelectRoute(Routes.ACTIVITIES) },
                )
            }
        }
    }
}

@Composable
private fun GreetingCard(
    firstName: String,
    isPetugas: Boolean,
    onOpenNewReport: () -> Unit,
    onOpenReports: () -> Unit,
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (firstName.isBlank()) "Halo warga" else "Halo, $firstName",
                style = MaterialTheme.typography.titleLarge,
            )
            AssistChip(onClick = { }, enabled = false, label = { Text(if (isPetugas) "Petugas" else "Warga") })
            Text(
                text = if (isPetugas) {
                    "Kelola laporan warga dan jadwalkan kegiatan lingkungan."
                } else {
                    "Laporkan masalah lingkungan dan pantau perkembangannya di sini."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenNewReport) { Text("Buat Laporan") }
                OutlinedButton(onClick = onOpenReports) { Text("Lihat Laporan") }
            }
        }
    }
}

@Composable
private fun RecentReports(
    reports: List<Report>,
    onOpenReport: (String) -> Unit,
    onOpenAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Laporan terbaru", actionLabel = "Lihat semua", onAction = onOpenAll)
        if (reports.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Outlined.EventNote,
                title = "Belum ada laporan",
                message = "Laporan pertama yang dikirim akan tampil di sini.",
            )
        } else {
            reports.forEach { report ->
                ReportCard(report = report, onClick = { onOpenReport(report.id) })
            }
        }
    }
}

@Composable
private fun UpcomingActivities(
    activities: List<ActivityItem>,
    onOpenActivity: (String) -> Unit,
    onOpenAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Kegiatan berikutnya", actionLabel = "Katalog", onAction = onOpenAll)
        if (activities.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Outlined.EventNote,
                title = "Belum ada kegiatan",
                message = "Belum ada kegiatan. Cek lagi setelah pengurus memposting agenda berikutnya.",
            )
        } else {
            activities.forEach { activity ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenActivity(activity.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = activity.category.toIcon(),
                            contentDescription = activity.category.label,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activity.title,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = activity.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = AppTime.dateShort(activity.scheduledAt),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onAction) { Text(actionLabel) }
    }
    HorizontalDivider()
}

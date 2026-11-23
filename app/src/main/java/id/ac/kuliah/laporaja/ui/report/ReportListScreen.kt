package id.ac.kuliah.laporaja.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.domain.model.StatusFilter
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.EmptyState
import id.ac.kuliah.laporaja.ui.components.ErrorState
import id.ac.kuliah.laporaja.ui.components.LoadingState
import id.ac.kuliah.laporaja.ui.navigation.AppBottomBar
import id.ac.kuliah.laporaja.ui.navigation.Routes

/** S4: filter status, daftar laporan, dan pintu masuk form buat laporan (Subbab 10.1). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    notice: String?,
    onNoticeShown: () -> Unit,
    onSelectRoute: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    onOpenNew: () -> Unit,
) {
    val viewModel = appViewModel { ReportListViewModel(it.reportRepository, it.sessionHolder) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notice) {
        if (notice != null) {
            snackbarHostState.showSnackbar(notice)
            onNoticeShown()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(title = if (state.isPetugas) "Semua Laporan" else "Laporan Saya")
        },
        bottomBar = { AppBottomBar(selectedRoute = Routes.REPORTS, onSelectRoute = onSelectRoute) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenNew,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Buat Laporan") },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            StatusFilterRow(selected = viewModel.filter, onSelect = viewModel::onFilterChanged)
            PullToRefreshBox(
                isRefreshing = state.isLoading && state.items.isNotEmpty(),
                onRefresh = { viewModel.load(refresh = true) },
                modifier = Modifier.fillMaxSize(),
            ) {
                ReportListContent(
                    state = state,
                    onRetry = { viewModel.load(refresh = true) },
                    onLoadMore = viewModel::loadMore,
                    onOpenDetail = onOpenDetail,
                    onOpenNew = onOpenNew,
                )
            }
        }
    }
}

@Composable
fun StatusFilterRow(
    selected: StatusFilter,
    onSelect: (StatusFilter) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = StatusFilter.entries, key = { it.name }) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option.label) },
            )
        }
    }
}

/**
 * Isi daftar laporan dengan tiga cabang wajib: loading, error, dan empty (Subbab 12.2).
 * Dipakai S4 dan pratinjau ringkas di S3.
 */
@Composable
fun ReportListContent(
    state: ReportListUiState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onOpenNew: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    when {
        state.isLoading && state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingState(label = "Memuat laporan...")
        }

        state.errorMessage != null && state.items.isEmpty() -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ErrorState(message = state.errorMessage, onRetry = onRetry)
        }

        state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                icon = Icons.AutoMirrored.Outlined.Assignment,
                title = "Belum ada laporan",
                message = "Belum ada laporan pada filter ini. Tarik untuk memuat ulang atau kirim laporan pertama.",
                actionLabel = "Buat Laporan",
                onAction = onOpenNew,
            )
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = state.items, key = { it.id }) { report ->
                ReportCard(report = report, onClick = { onOpenDetail(report.id) })
            }
            if (state.hasMore) {
                item(key = "load-more") {
                    TextButton(
                        onClick = onLoadMore,
                        enabled = !state.isLoadingMore,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (state.isLoadingMore) "Memuat..." else "Muat lebih banyak")
                    }
                }
            }
            if (state.errorMessage != null) {
                item(key = "load-more-error") {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}

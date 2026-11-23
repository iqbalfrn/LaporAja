package id.ac.kuliah.laporaja.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.core.image.PublicUrlBuilder
import id.ac.kuliah.laporaja.core.time.AppTime
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.domain.model.ActivityCategory
import id.ac.kuliah.laporaja.domain.model.ActivityItem
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.EmptyState
import id.ac.kuliah.laporaja.ui.components.ErrorState
import id.ac.kuliah.laporaja.ui.components.LoadingState
import id.ac.kuliah.laporaja.ui.components.NoPhotoPlaceholder
import id.ac.kuliah.laporaja.ui.components.RemoteImage
import id.ac.kuliah.laporaja.ui.components.toIcon
import id.ac.kuliah.laporaja.ui.navigation.AppBottomBar
import id.ac.kuliah.laporaja.ui.navigation.Routes

/**
 * S10: katalog kegiatan dua kolom. Warga hanya membaca; tombol buat tampil untuk petugas saja,
 * dan rute activities/new dijaga terpisah (Subbab 10.3).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityListScreen(
    notice: String?,
    onNoticeShown: () -> Unit,
    onSelectRoute: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    onOpenNew: () -> Unit,
) {
    val viewModel = appViewModel { ActivityListViewModel(it.activityRepository, it.sessionHolder) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var category by remember { mutableStateOf<ActivityCategory?>(null) }

    LaunchedEffect(notice) {
        if (notice != null) {
            snackbarHostState.showSnackbar(notice)
            onNoticeShown()
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Kegiatan Warga") },
        bottomBar = { AppBottomBar(selectedRoute = Routes.ACTIVITIES, onSelectRoute = onSelectRoute) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.isPetugas) {
                ExtendedFloatingActionButton(
                    onClick = onOpenNew,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Kegiatan Baru") },
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            CategoryChipRow(
                selected = category,
                onSelect = {
                    category = it
                    viewModel.onCategoryChanged(it)
                },
            )
            PullToRefreshBox(
                isRefreshing = state.isLoading && state.items.isNotEmpty(),
                onRefresh = { viewModel.load(refresh = true) },
                modifier = Modifier.fillMaxSize(),
            ) {
                ActivityGrid(
                    state = state,
                    onRetry = { viewModel.load(refresh = true) },
                    onLoadMore = viewModel::loadMore,
                    onOpenDetail = onOpenDetail,
                )
            }
        }
    }
}

/** Chip "Semua" memakai nilai null supaya filter bisa dilepas lagi (Subbab 10.3). */
@Composable
private fun CategoryChipRow(
    selected: ActivityCategory?,
    onSelect: (ActivityCategory?) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "category-all") {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Semua") },
            )
        }
        items(items = ActivityCategory.entries, key = { it.name }) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option.label) },
            )
        }
    }
}

/** Grid dengan cabang loading/error/empty yang tidak boleh hilang (Subbab 12.2). */
@Composable
private fun ActivityGrid(
    state: ActivityListUiState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading && state.items.isEmpty() -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) { LoadingState(label = "Memuat kegiatan...") }

        state.errorMessage != null && state.items.isEmpty() -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ErrorState(message = state.errorMessage, onRetry = onRetry)
        }

        state.items.isEmpty() -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            EmptyState(
                icon = Icons.AutoMirrored.Outlined.EventNote,
                title = "Belum ada kegiatan",
                message = "Belum ada kegiatan. Cek lagi setelah pengurus memposting agenda berikutnya.",
                actionLabel = "Muat ulang",
                onAction = onRetry,
            )
        }

        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(count = state.items.size, key = { state.items[it].id }) { index ->
                val activity = state.items[index]
                ActivityCard(activity = activity, onClick = { onOpenDetail(activity.id) })
            }
            if (state.hasMore) {
                item(key = "load-more", span = { GridItemSpan(maxLineSpan) }) {
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
                item(key = "load-more-error", span = { GridItemSpan(maxLineSpan) }) {
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

/** Poster rasio 3:4 (gradasi + ikon bila kosong), badge kategori, judul 2 baris, lokasi, tanggal. */
@Composable
private fun ActivityCard(
    activity: ActivityItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column {
            val url = PublicUrlBuilder.activityPoster(activity.posterPath)
            if (url == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) { NoPhotoPlaceholder(label = "Tanpa poster") }
            } else {
                RemoteImage(
                    url = url,
                    contentDescription = "Poster ${activity.title}",
                    aspectRatio = 3f / 4f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = activity.category.toIcon(),
                        contentDescription = activity.category.label,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = activity.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.outline,
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
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

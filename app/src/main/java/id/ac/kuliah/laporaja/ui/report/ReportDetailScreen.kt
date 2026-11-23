package id.ac.kuliah.laporaja.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.core.image.PublicUrlBuilder
import id.ac.kuliah.laporaja.core.time.AppTime
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.domain.model.Report
import id.ac.kuliah.laporaja.domain.model.ReportStatus
import id.ac.kuliah.laporaja.domain.model.StatusStep
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.CategoryIcon
import id.ac.kuliah.laporaja.ui.components.ErrorState
import id.ac.kuliah.laporaja.ui.components.LabeledTextField
import id.ac.kuliah.laporaja.ui.components.LoadingState
import id.ac.kuliah.laporaja.ui.components.NoPhotoPlaceholder
import id.ac.kuliah.laporaja.ui.components.RemoteImage
import id.ac.kuliah.laporaja.ui.components.StatusChip
import id.ac.kuliah.laporaja.ui.response.ResponseListSection
import id.ac.kuliah.laporaja.ui.response.ResponseViewModel

/**
 * S6: detail laporan, timeline status, aksi sesuai peran, dan section tanggapan milik modul A4.
 * S8 (ubah status) dikerjakan sebagai dialog di layar ini (Subbab 10.1).
 */
@Composable
fun ReportDetailScreen(
    reportId: String,
    notice: String?,
    onNoticeShown: () -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRespond: () -> Unit,
) {
    val viewModel = appViewModel {
        ReportDetailViewModel(it.reportRepository, it.sessionHolder, reportId)
    }
    val responsesViewModel = appViewModel {
        ResponseViewModel(it.responseRepository, it.responseHandoff, reportId)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val responseState by responsesViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var statusDialogOpen by remember { mutableStateOf(false) }
    var statusNotice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(statusNotice) {
        val text = statusNotice ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        statusNotice = null
    }

    LaunchedEffect(state.message) {
        val text = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        viewModel.onMessageShown()
    }

    LaunchedEffect(notice) {
        if (notice != null) {
            snackbarHostState.showSnackbar(notice)
            onNoticeShown()
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Detail Laporan", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { LoadingState(label = "Memuat laporan...") }

            state.report == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(
                    message = state.errorMessage ?: ReportDetailViewModel.REPORT_NOT_FOUND,
                    onRetry = viewModel::load,
                )
            }

            else -> {
                val report = state.report
                if (report != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        ReportPhoto(report = report)
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(text = report.title, style = MaterialTheme.typography.headlineSmall)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                StatusChip(status = report.status)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    CategoryIcon(
                                        category = report.category,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = report.category.label,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Place,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.outline,
                                )
                                Text(text = report.location, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                text = "Dilaporkan ${AppTime.dateTime(report.createdAt)}" +
                                    "\nDiperbarui ${AppTime.dateTime(report.updatedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            HorizontalDivider()
                            StatusTimeline(steps = report.statusTimeline())
                            HorizontalDivider()
                            ReportActions(
                                canEdit = state.canEditContent,
                                canChangeStatus = state.canChangeStatus,
                                isBusy = state.isSubmittingStatus,
                                onEdit = onEdit,
                                onChangeStatus = { statusDialogOpen = true },
                                onRespond = onRespond,
                            )
                            HorizontalDivider()
                            ResponseListSection(
                                state = responseState,
                                onRetry = responsesViewModel::load,
                            )
                        }
                    }
                }

                if (statusDialogOpen) {
                    StatusChangeDialog(
                        options = state.transitions,
                        isBusy = state.isSubmittingStatus,
                        onDismiss = { statusDialogOpen = false },
                        onConfirm = { next, note ->
                            viewModel.changeStatus(next, note) { message ->
                                statusDialogOpen = false
                                statusNotice = message
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportPhoto(report: Report) {
    val url = PublicUrlBuilder.reportPhoto(report.photoPath)
    if (url == null) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().height(200.dp),
        ) {
            NoPhotoPlaceholder()
        }
    } else {
        RemoteImage(
            url = url,
            contentDescription = "Foto bukti ${report.title}",
            aspectRatio = 4f / 3f,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Timeline vertikal: langkah yang belum tercapai ditampilkan redup (Subbab 10.1). */
@Composable
private fun StatusTimeline(steps: List<StatusStep>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Perkembangan", style = MaterialTheme.typography.titleSmall)
        steps.forEach { step ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = if (step.reached) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (step.reached) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = if (step.at > 0L) {
                            "${step.label} • ${AppTime.dateTime(step.at)}"
                        } else {
                            step.label
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (step.reached) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    )
                    if (!step.note.isNullOrBlank()) {
                        Text(
                            text = step.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportActions(
    canEdit: Boolean,
    canChangeStatus: Boolean,
    isBusy: Boolean,
    onEdit: () -> Unit,
    onChangeStatus: () -> Unit,
    onRespond: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (canEdit) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text("Edit laporan")
            }
        }
        if (canChangeStatus) {
            Button(onClick = onChangeStatus, enabled = !isBusy, modifier = Modifier.fillMaxWidth()) {
                Text("Ubah status")
            }
        }
        OutlinedButton(onClick = onRespond, modifier = Modifier.fillMaxWidth()) {
            Text("Tambah tanggapan")
        }
    }
}

/** S8: hanya transisi sah dari Subbab 7.7; catatan wajib saat menolak. */
@Composable
private fun StatusChangeDialog(
    options: List<ReportStatus>,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ReportStatus, String?) -> Unit,
) {
    var selected by remember { mutableStateOf(options.firstOrNull()) }
    var note by remember { mutableStateOf("") }
    var noteError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah status laporan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (options.isEmpty()) {
                    Text(
                        text = "Status laporan tidak dikenal sehingga tidak ada perubahan yang sah",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RadioButton(
                            selected = selected == option,
                            onClick = { selected = option },
                            enabled = !isBusy,
                        )
                        Text(text = option.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                LabeledTextField(
                    label = "Catatan (opsional)",
                    value = note,
                    onValueChange = {
                        note = it
                        noteError = null
                    },
                    error = noteError,
                    supporting = "Wajib diisi saat menolak laporan",
                    maxLength = FormValidator.STATUS_NOTE_MAX,
                    minLines = 3,
                    enabled = !isBusy,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val choice = selected ?: return@TextButton
                    val tooLong = FormValidator.statusNote(note)
                    when {
                        choice == ReportStatus.DITOLAK && note.isBlank() ->
                            noteError = "Catatan wajib diisi saat menolak laporan"

                        tooLong != null -> noteError = tooLong

                        else -> onConfirm(choice, note)
                    }
                },
                enabled = !isBusy && options.isNotEmpty(),
            ) {
                Text(if (isBusy) "Menyimpan..." else "Simpan status")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) { Text("Batal") }
        },
    )
}

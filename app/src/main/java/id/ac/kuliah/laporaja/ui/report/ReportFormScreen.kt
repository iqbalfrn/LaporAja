package id.ac.kuliah.laporaja.ui.report

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.core.image.PublicUrlBuilder
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.domain.model.ReportCategory
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.CategoryDropdown
import id.ac.kuliah.laporaja.ui.components.ConfirmationDialog
import id.ac.kuliah.laporaja.ui.components.ErrorState
import id.ac.kuliah.laporaja.ui.components.ImagePickerSection
import id.ac.kuliah.laporaja.ui.components.LabeledTextField
import id.ac.kuliah.laporaja.ui.components.LoadingState
import id.ac.kuliah.laporaja.ui.components.NoPhotoPlaceholder
import id.ac.kuliah.laporaja.ui.components.RemoteImage
import id.ac.kuliah.laporaja.ui.components.SubmitButton

/**
 * S5 form buat laporan dan S7 form edit. Foto hanya bisa dipilih saat membuat laporan baru,
 * karena update pemilik hanya boleh menyentuh kolom isi (Subbab 7.9).
 */
@Composable
fun ReportFormScreen(
    reportId: String?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val viewModel = appViewModel {
        ReportFormViewModel(it.reportRepository, it.sessionHolder, reportId)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmDiscard by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onMessageShown()
    }

    val leave: () -> Unit = { if (viewModel.isDirty()) confirmDiscard = true else onBack() }
    BackHandler(enabled = state.canSubmit && viewModel.isDirty()) { confirmDiscard = true }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (state.isEditMode) "Edit Laporan" else "Buat Laporan",
                onBack = leave,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(
                label = "Memuat laporan...",
                modifier = Modifier.fillMaxSize().padding(padding),
            )

            state.loadError != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(message = state.loadError.orEmpty(), onRetry = viewModel::retryLoad)
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.statusLabel != null) {
                    Text(
                        text = "Status laporan: ${state.statusLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CategoryDropdown(
                    label = "Kategori",
                    options = ReportCategory.entries,
                    selected = state.category,
                    optionLabel = { it.label },
                    onSelect = viewModel::onCategoryChange,
                    error = state.categoryError,
                    enabled = state.canSubmit,
                )
                LabeledTextField(
                    label = "Judul",
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    error = state.titleError,
                    placeholder = "Contoh: Tumpukan sampah di sudut gang mawar",
                    maxLength = FormValidator.TITLE_MAX,
                    enabled = state.canSubmit,
                )
                LabeledTextField(
                    label = "Deskripsi",
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    error = state.descriptionError,
                    placeholder = "Jelaskan kondisi, sejak kapan, dan dampaknya",
                    supporting = "Minimal ${FormValidator.DESCRIPTION_MIN} karakter",
                    maxLength = FormValidator.DESCRIPTION_MAX,
                    minLines = 4,
                    enabled = state.canSubmit,
                    imeAction = ImeAction.Next,
                )
                LabeledTextField(
                    label = "Lokasi",
                    value = state.location,
                    onValueChange = viewModel::onLocationChange,
                    error = state.locationError,
                    placeholder = "Patokan yang mudah dicari",
                    maxLength = FormValidator.LOCATION_MAX,
                    enabled = state.canSubmit,
                    imeAction = ImeAction.Done,
                )
                if (state.isEditMode) {
                    ExistingPhoto(path = state.existingPhotoPath)
                } else {
                    ImagePickerSection(
                        label = "Foto bukti (opsional)",
                        photoBytes = state.photoBytes,
                        isBusy = false,
                        error = state.photoError,
                        enabled = state.canSubmit,
                        emptyHint = "Tambahkan foto supaya lokasi lebih jelas",
                        onPicked = viewModel::onPhotoPicked,
                        onRejected = viewModel::onPhotoRejected,
                        onRemove = viewModel::onPhotoRemoved,
                    )
                }
                SubmitButton(
                    label = if (state.isEditMode) "Simpan perubahan" else "Kirim laporan",
                    loading = state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.submit(onSaved) },
                )
                Text(
                    text = "Pastikan tidak ada data pribadi orang lain di dalam foto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }

    if (confirmDiscard) {
        ConfirmationDialog(
            title = "Buang laporan?",
            body = "Isian yang belum dikirim akan hilang.",
            confirmLabel = "Buang",
            onConfirm = {
                confirmDiscard = false
                onBack()
            },
            onDismiss = { confirmDiscard = false },
        )
    }
}

/** Foto yang sudah tersimpan di server; hanya ditampilkan, tidak dapat diganti. */
@Composable
private fun ExistingPhoto(path: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "Foto bukti", style = MaterialTheme.typography.titleSmall)
        val url = PublicUrlBuilder.reportPhoto(path)
        if (url == null) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                NoPhotoPlaceholder()
            }
        } else {
            RemoteImage(
                url = url,
                contentDescription = "Foto bukti laporan",
                aspectRatio = 16f / 9f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = "Foto tidak dapat diganti setelah laporan dibuat.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

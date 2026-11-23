package id.ac.kuliah.laporaja.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.core.time.AppTime
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.domain.model.ActivityCategory
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.CategoryDropdown
import id.ac.kuliah.laporaja.ui.components.ImagePickerSection
import id.ac.kuliah.laporaja.ui.components.LabeledTextField
import id.ac.kuliah.laporaja.ui.components.SubmitButton
import java.util.Calendar
import java.util.TimeZone

/**
 * S11: petugas membuat kegiatan. Tanggal dan jam diambil dari picker Material lalu digabung
 * menjadi satu titik waktu (Subbab 10.3).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFormScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val viewModel = appViewModel { ActivityFormViewModel(it.activityRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    var pickedDateUtc by remember { mutableStateOf<Long?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onMessageShown()
    }

    Scaffold(
        topBar = { AppTopBar(title = "Kegiatan Baru", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CategoryDropdown(
                label = "Kategori kegiatan",
                options = ActivityCategory.entries,
                selected = state.category,
                optionLabel = { it.label },
                onSelect = viewModel::onCategoryChange,
                error = state.categoryError,
                enabled = state.canSubmit,
            )
            LabeledTextField(
                label = "Judul kegiatan",
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                error = state.titleError,
                placeholder = "Contoh: Kerja bakti pembersihan saluran gang melati",
                maxLength = FormValidator.TITLE_MAX,
                enabled = state.canSubmit,
            )
            LabeledTextField(
                label = "Lokasi",
                value = state.location,
                onValueChange = viewModel::onLocationChange,
                error = state.locationError,
                placeholder = "Titik kumpul yang mudah dicari",
                maxLength = FormValidator.LOCATION_MAX,
                enabled = state.canSubmit,
            )
            ScheduleField(
                scheduledAt = state.scheduledAt,
                error = state.scheduleError,
                enabled = state.canSubmit,
                onPickDate = { showDatePicker = true },
            )
            LabeledTextField(
                label = "Deskripsi",
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                error = state.descriptionError,
                placeholder = "Jelaskan agenda, perlengkapan yang perlu dibawa, dan pembatasnya",
                supporting = "Minimal ${FormValidator.ACTIVITY_DESCRIPTION_MIN} karakter",
                maxLength = FormValidator.ACTIVITY_DESCRIPTION_MAX,
                minLines = 5,
                enabled = state.canSubmit,
            )
            LabeledTextField(
                label = "Kuota peserta (opsional)",
                value = state.quota,
                onValueChange = viewModel::onQuotaChange,
                error = state.quotaError,
                placeholder = "Contoh: 40",
                supporting = "Antara ${FormValidator.QUOTA_MIN} dan ${FormValidator.QUOTA_MAX}",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                enabled = state.canSubmit,
            )
            ImagePickerSection(
                label = "Poster kegiatan (opsional)",
                photoBytes = state.posterBytes,
                isBusy = state.isSubmitting,
                error = state.posterError,
                aspectRatioValue = 3f / 4f,
                emptyHint = "Poster membuat kegiatan lebih mudah dikenali",
                enabled = state.canSubmit,
                onPicked = viewModel::onPosterPicked,
                onRejected = viewModel::onPosterRejected,
                onRemove = viewModel::onPosterRemoved,
            )
            SubmitButton(
                label = "Publikasikan kegiatan",
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.submit(onSaved) },
            )
            Text(
                text = "Kegiatan yang terbit langsung terbaca seluruh warga.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val chosen = dateState.selectedDateMillis
                        if (chosen == null) {
                            showDatePicker = false
                        } else {
                            pickedDateUtc = chosen
                            showDatePicker = false
                            showTimePicker = true
                        }
                    },
                ) { Text("Lanjut ke jam") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            },
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(is24Hour = true)
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val date = pickedDateUtc
                        if (date != null) {
                            viewModel.onScheduleChange(
                                combineSchedule(date, timeState.hour, timeState.minute),
                            )
                        }
                        showTimePicker = false
                    },
                ) { Text("Gunakan jadwal ini") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Batal") }
            },
            title = { Text("Pilih jam mulai") },
        ) {
            TimePicker(state = timeState)
        }
    }
}

/** Label jadwal + tombol picker; pesan galat tampil di bawah seperti field lain. */
@Composable
private fun ScheduleField(
    scheduledAt: Long?,
    error: String?,
    enabled: Boolean,
    onPickDate: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "Jadwal kegiatan", style = MaterialTheme.typography.titleSmall)
        Text(
            text = scheduledAt?.let { AppTime.dateTimeLong(it) } ?: "Belum dipilih",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(
            onClick = onPickDate,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(imageVector = Icons.Outlined.Schedule, contentDescription = null)
            Text(
                text = if (scheduledAt == null) "Pilih tanggal dan jam" else "Ubah jadwal",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * DatePicker mengembalikan milidetik tengah hari UTC sedangkan jam dipilih pada waktu perangkat;
 * keduanya digabung lewat Calendar lokal agar jadwal sesuai niat petugas.
 */
private fun combineSchedule(dateUtcMillis: Long, hour: Int, minute: Int): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateUtcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH),
            utc.get(Calendar.DAY_OF_MONTH),
            hour,
            minute,
            0,
        )
    }.timeInMillis
}

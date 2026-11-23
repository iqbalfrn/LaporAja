package id.ac.kuliah.laporaja.ui.account

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.ConfirmationDialog
import id.ac.kuliah.laporaja.ui.components.ImagePickerSection
import id.ac.kuliah.laporaja.ui.components.LabeledTextField
import id.ac.kuliah.laporaja.ui.components.SubmitButton

/** S14: ubah identitas diri dan unggah foto profil (Subbab 9.2). */
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val viewModel = appViewModel { EditProfileViewModel(it.profileRepository, it.sessionHolder) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmDiscard by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onMessageShown()
    }

    BackHandler(enabled = viewModel.isDirty() && !state.isSubmitting) { confirmDiscard = true }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Edit Profil",
                onBack = { if (viewModel.isDirty()) confirmDiscard = true else onBack() },
            )
        },
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
            ImagePickerSection(
                label = "Foto profil",
                photoBytes = state.pendingAvatar,
                isBusy = state.isSubmitting,
                error = state.imageError,
                aspectRatioValue = 1f,
                emptyHint = "Belum ada foto. Biarkan kosong untuk memakai inisial nama.",
                enabled = !state.isSubmitting,
                onPicked = viewModel::onAvatarPicked,
                onRejected = viewModel::onAvatarRejected,
                onRemove = viewModel::onAvatarRemoved,
            )
            LabeledTextField(
                label = "Nama lengkap",
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                error = state.fullNameError,
                maxLength = FormValidator.NAME_MAX,
                enabled = !state.isSubmitting,
            )
            LabeledTextField(
                label = "No. HP (opsional)",
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                error = state.phoneError,
                supporting = "Hanya pengurus yang bisa membaca nomor ini",
                enabled = !state.isSubmitting,
                keyboardType = KeyboardType.Phone,
            )
            LabeledTextField(
                label = "RT/RW (opsional)",
                value = state.rtRw,
                onValueChange = viewModel::onRtRwChange,
                error = state.rtRwError,
                placeholder = "003/005",
                enabled = !state.isSubmitting,
            )
            LabeledTextField(
                label = "Peran",
                value = state.roleLabel,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                supporting = "Peran diatur oleh pengurus",
            )
            SubmitButton(
                label = "Simpan perubahan",
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.save(onSaved) },
            )
            Text(
                text = "Perubahan langsung tampil di Akun Saya tanpa membuka ulang aplikasi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }

    if (confirmDiscard) {
        ConfirmationDialog(
            title = "Buang perubahan?",
            body = "Perubahan pada profil kamu belum disimpan.",
            confirmLabel = "Buang",
            onConfirm = {
                confirmDiscard = false
                onBack()
            },
            onDismiss = { confirmDiscard = false },
        )
    }
}

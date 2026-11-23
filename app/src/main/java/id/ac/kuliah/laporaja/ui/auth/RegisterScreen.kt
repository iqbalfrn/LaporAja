package id.ac.kuliah.laporaja.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.ui.account.TermsDialog
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.LabeledTextField
import id.ac.kuliah.laporaja.ui.components.PasswordField
import id.ac.kuliah.laporaja.ui.components.SubmitButton

/** S2: pendaftaran akun warga baru (Subbab 9.1). */
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    val viewModel = appViewModel { RegisterViewModel(it.authRepository, it.sessionHolder) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTerms by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onMessageShown()
    }

    Scaffold(
        topBar = { AppTopBar(title = "Daftar Akun", onBack = onBack) },
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
            Text(
                text = "Isi data di bawah untuk membuat akun. Nama dan inisial kamu akan tampil di laporan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LabeledTextField(
                label = "Nama lengkap",
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                error = state.fullNameError,
                placeholder = "Nama panggilan warga",
                maxLength = FormValidator.NAME_MAX,
                enabled = !state.isSubmitting,
            )
            LabeledTextField(
                label = "Email",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                error = state.emailError,
                supporting = "Email diubah menjadi huruf kecil otomatis",
                enabled = !state.isSubmitting,
                keyboardType = KeyboardType.Email,
            )
            LabeledTextField(
                label = "No. HP (opsional)",
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                error = state.phoneError,
                placeholder = "08xxxxxxxxxx",
                supporting = "Tidak ditampilkan ke warga lain",
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
            PasswordField(
                label = "Password",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                error = state.passwordError,
                enabled = !state.isSubmitting,
            )
            if (state.isConfirmationVisible) {
                PasswordField(
                    label = "Konfirmasi password",
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    error = state.confirmPasswordError,
                    enabled = !state.isSubmitting,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { viewModel.submit(onSuccess) }),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Checkbox(
                    checked = state.termsAccepted,
                    onCheckedChange = viewModel::onTermsChange,
                    enabled = !state.isSubmitting,
                )
                Column {
                    Text(
                        text = "Saya menyetujui syarat penggunaan",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    val termsError = state.termsError
                    if (termsError != null) {
                        Text(
                            text = termsError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    TextButton(
                        onClick = { showTerms = true },
                        contentPadding = PaddingValues(0.dp, 4.dp, 0.dp, 0.dp),
                    ) {
                        Text(
                            text = "Baca syarat penggunaan",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
            SubmitButton(
                label = "Daftar",
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.submit(onSuccess) },
            )
            TextButton(onClick = onBack, enabled = !state.isSubmitting) {
                Text(
                    text = "Sudah punya akun? Masuk di sini",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }

    if (showTerms) TermsDialog(onDismiss = { showTerms = false })
}

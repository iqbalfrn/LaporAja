package id.ac.kuliah.laporaja.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.LabeledTextField
import id.ac.kuliah.laporaja.ui.components.PasswordField
import id.ac.kuliah.laporaja.ui.components.SubmitButton

/** S1: masuk dengan email dan password; galat server ditampilkan lewat Snackbar (Subbab 9.1). */
@Composable
fun LoginScreen(
    notice: String?,
    onNoticeShown: () -> Unit,
    onOpenRegister: () -> Unit,
    onSuccess: () -> Unit,
) {
    val viewModel = appViewModel { LoginViewModel(it.authRepository, it.sessionHolder) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onMessageShown()
    }

    // Pesan "Sesi berakhir" dititipkan lewat SavedStateHandle rute login (Subbab 9.1).
    LaunchedEffect(notice) {
        if (notice != null) {
            snackbarHostState.showSnackbar(notice)
            onNoticeShown()
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Masuk") },
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
                text = "Gunakan akun yang sudah terdaftar di data warga RT/RW kamu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LabeledTextField(
                label = "Email",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                error = state.emailError,
                enabled = !state.isSubmitting,
                keyboardType = KeyboardType.Email,
            )
            PasswordField(
                label = "Password",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                error = state.passwordError,
                enabled = !state.isSubmitting,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { viewModel.submit(onSuccess) }),
            )
            SubmitButton(
                label = "Masuk",
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.submit(onSuccess) },
            )
            TextButton(onClick = onOpenRegister, enabled = !state.isSubmitting) {
                Text(
                    text = "Belum punya akun? Daftar di sini",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(
                text = "Password minimal ${FormValidator.PASSWORD_MIN} karakter dengan campuran huruf dan angka.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

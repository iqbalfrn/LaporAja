package id.ac.kuliah.laporaja.ui.response

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.kuliah.laporaja.core.validator.FormValidator
import id.ac.kuliah.laporaja.di.appViewModel
import id.ac.kuliah.laporaja.ui.components.AppTopBar
import id.ac.kuliah.laporaja.ui.components.LabeledTextField
import id.ac.kuliah.laporaja.ui.components.SubmitButton

/** S9: kirim tanggapan atas laporan (Subbab 10.2). */
@Composable
fun ResponseFormScreen(
    reportId: String,
    onBack: () -> Unit,
    onSent: (String) -> Unit,
) {
    val viewModel = appViewModel {
        ResponseFormViewModel(it.responseRepository, it.responseHandoff, it.sessionHolder, reportId)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.info) {
        val info = state.info ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(info)
        viewModel.onInfoShown()
    }

    Scaffold(
        topBar = { AppTopBar(title = "Tambah Tanggapan", onBack = onBack) },
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
                text = "Tanggapan terlihat oleh warga lain dan pengurus. Jaga kalimat tetap sopan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LabeledTextField(
                label = "Tanggapan",
                value = state.message,
                onValueChange = viewModel::onMessageChange,
                error = state.messageError,
                supporting = "Minimal ${FormValidator.RESPONSE_MIN} karakter",
                maxLength = FormValidator.RESPONSE_MAX,
                minLines = 5,
                enabled = state.canSubmit,
            )
            SubmitButton(
                label = "Kirim tanggapan",
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                // Tombol mati sejak awal bila isian belum sah, sesuai kriteria penerimaan A4.
                enabled = FormValidator.responseMessage(state.message) == null,
                onClick = { viewModel.submit { onSent("Tanggapan terkirim") } },
            )
        }
    }
}

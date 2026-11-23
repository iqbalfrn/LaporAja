package id.ac.kuliah.laporaja.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Field dengan label, pesan galat inline, dan penghitung karakter (Subbab 11.3).
 * Dipakai semua form supaya pesan validasi selalu tampil di tempat yang sama.
 */
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    placeholder: String? = null,
    supporting: String? = null,
    maxLength: Int? = null,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = error != null,
        enabled = enabled,
        readOnly = readOnly,
        minLines = minLines,
        maxLines = if (minLines > 1) minLines + 4 else 1,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        supportingText = {
            val showCounter = maxLength != null && error == null
            when {
                error != null -> Text(text = error, color = MaterialTheme.colorScheme.error)
                supporting != null || showCounter -> Row {
                    if (supporting != null) {
                        Text(text = supporting, style = MaterialTheme.typography.bodySmall)
                    }
                    if (showCounter) {
                        Text(
                            text = "${value.length}/$maxLength",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        },
    )
}

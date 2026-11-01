package com.joao.gastos.ui.form

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joao.gastos.model.Category
import com.joao.gastos.model.MovementType
import com.joao.gastos.ui.form.ExpenseFormUiState.Companion.NOTE_LIMIT
import com.joao.gastos.ui.icon
import com.joao.gastos.ui.theme.GastosTheme
import com.joao.gastos.ui.toEpochDayLabel
import com.joao.gastos.ui.toUtcStartMillis
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormScreen(
    viewModel: ExpenseFormViewModel,
    onCancel: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val amountFocus = remember { FocusRequester() }

    LaunchedEffect(state.isEditing) {
        if (!state.isEditing && state is ExpenseFormUiState.Editing) {
            amountFocus.requestFocus()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditing) "Editar movimiento" else "Agregar movimiento",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Cancelar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        when (val current = state) {
            is ExpenseFormUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(padding).padding(24.dp), color = MaterialTheme.colorScheme.primary)
            }
            is ExpenseFormUiState.Editing -> {
                EditingForm(
                    state = current,
                    amountFocus = amountFocus,
                    onAmountChange = viewModel::onAmountChange,
                    onTypeChange = viewModel::onTypeChange,
                    onCategoryChange = viewModel::onCategoryChange,
                    onNoteChange = viewModel::onNoteChange,
                    onPickDate = { showDatePicker = true },
                    onSave = viewModel::save,
                    onCancel = onCancel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }

    val editing = state as? ExpenseFormUiState.Editing
    if (showDatePicker && editing != null) {
        ExpenseDatePicker(
            epochDay = editing.epochDay,
            onConfirm = { day ->
                viewModel.onDateChange(day)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditingForm(
    state: ExpenseFormUiState.Editing,
    amountFocus: FocusRequester,
    onAmountChange: (String) -> Unit,
    onTypeChange: (MovementType) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onNoteChange: (String) -> Unit,
    onPickDate: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currency = remember { NumberFormat.getCurrencyInstance().currency?.symbol.orEmpty() }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            MovementType.entries.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = state.type == type,
                    onClick = { onTypeChange(type) },
                    shape = SegmentedButtonDefaults.itemShape(index, MovementType.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Text(type.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        OutlinedTextField(
            value = state.amountText,
            onValueChange = onAmountChange,
            label = { Text("Importe") },
            prefix = { Text(currency) },
            textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            isError = state.amountError != null,
            supportingText = state.amountError?.let { message -> { Text(message) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(amountFocus),
        )
        Text("Categoría", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Category.forType(state.type).forEach { category ->
                FilterChip(
                    selected = state.category == category,
                    onClick = { onCategoryChange(category) },
                    label = { Text(category.label, style = MaterialTheme.typography.labelMedium) },
                    leadingIcon = {
                        Icon(category.icon(), contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = categoryChipColors(),
                    modifier = Modifier.height(32.dp),
                )
            }
        }
        OutlinedTextField(
            value = state.epochDay.toEpochDayLabel(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = onPickDate, modifier = Modifier.height(48.dp)) { Text("Cambiar fecha") }
        OutlinedTextField(
            value = state.note,
            onValueChange = onNoteChange,
            label = { Text("Nota") },
            supportingText = { Text("${state.note.length}/$NOTE_LIMIT") },
            minLines = 2,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.saveError != null) {
            Text(state.saveError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = onSave,
            enabled = !state.saving,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Text(if (state.saving) "Guardando" else "Guardar", style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Cancelar")
        }
    }
}

@Composable
private fun categoryChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
    iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDatePicker(
    epochDay: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = epochDay.toUtcStartMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay().toInt())
                    }
                    onDismiss()
                },
            ) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    ) {
        DatePicker(state = pickerState)
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun FormEmptyLightPreview() {
    GastosTheme {
        EditingForm(
            state = ExpenseFormUiState.Editing(),
            amountFocus = remember { FocusRequester() },
            onAmountChange = {},
            onTypeChange = {},
            onCategoryChange = {},
            onNoteChange = {},
            onPickDate = {},
            onSave = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FormEmptyDarkPreview() {
    GastosTheme {
        EditingForm(
            state = ExpenseFormUiState.Editing(),
            amountFocus = remember { FocusRequester() },
            onAmountChange = {},
            onTypeChange = {},
            onCategoryChange = {},
            onNoteChange = {},
            onPickDate = {},
            onSave = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun FormErrorPreview() {
    GastosTheme {
        EditingForm(
            state = ExpenseFormUiState.Editing(amountText = "12,5099", amountError = "Máximo 2 decimales"),
            amountFocus = remember { FocusRequester() },
            onAmountChange = {},
            onTypeChange = {},
            onCategoryChange = {},
            onNoteChange = {},
            onPickDate = {},
            onSave = {},
            onCancel = {},
        )
    }
}

package com.joao.gastos.ui.form

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joao.gastos.model.Category
import com.joao.gastos.model.MovementType
import com.joao.gastos.ui.form.ExpenseFormUiState.Companion.NOTE_LIMIT
import com.joao.gastos.ui.icon
import com.joao.gastos.ui.theme.GastosTheme
import com.joao.gastos.ui.theme.moneyColor
import com.joao.gastos.ui.toEpochDayLabel
import com.joao.gastos.ui.toUtcStartMillis
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
                        if (state.isEditing) "Editar movimiento" else "Registrar movimiento",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Cancelar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        when (val current = state) {
            is ExpenseFormUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.padding(padding).padding(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
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
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
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
    val noteFocus = remember { FocusRequester() }
    val amountColor = moneyColor(state.type)
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
        errorIndicatorColor = MaterialTheme.colorScheme.error,
        focusedTextColor = amountColor,
        unfocusedTextColor = amountColor,
        cursorColor = amountColor,
        focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        errorLabelColor = MaterialTheme.colorScheme.error,
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MovementType.entries.forEach { type ->
                TypeChoice(
                    type = type,
                    selected = state.type == type,
                    onClick = { onTypeChange(type) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        TextField(
            value = state.amountText,
            onValueChange = onAmountChange,
            label = { Text("Importe") },
            textStyle = MaterialTheme.typography.displaySmall.copy(color = amountColor),
            isError = state.amountError != null,
            supportingText = state.amountError?.let { message -> { Text(message) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { noteFocus.requestFocus() }),
            singleLine = true,
            colors = fieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(amountFocus),
        )
        Text(
            "Categoría",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Category.forType(state.type).forEach { category ->
                CategoryChoice(
                    category = category,
                    selected = state.category == category,
                    onClick = { onCategoryChange(category) },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(onClick = onPickDate, role = Role.Button),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Fecha", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = state.epochDay.toEpochDayLabel(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        TextField(
            value = state.note,
            onValueChange = onNoteChange,
            label = { Text("Nota") },
            supportingText = { Text("${state.note.length} de $NOTE_LIMIT") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (!state.saving) onSave() }),
            minLines = 2,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(noteFocus),
        )
        if (state.saveError != null) {
            Text(state.saveError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        }
        Button(
            onClick = onSave,
            enabled = !state.saving,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(if (state.saving) "Guardando" else "Guardar", style = MaterialTheme.typography.labelLarge)
        }
        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text("Cancelar", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TypeChoice(
    type: MovementType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val content = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .background(background, MaterialTheme.shapes.large)
            .clickable(onClick = onClick, role = Role.RadioButton)
            .semantics { this.selected = selected }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (type == MovementType.INGRESO) Icons.Rounded.Add else Icons.Rounded.Remove,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = type.label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = content,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun CategoryChoice(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val content = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .background(background, MaterialTheme.shapes.large)
            .clickable(onClick = onClick, role = Role.RadioButton)
            .semantics { this.selected = selected }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            category.icon(),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = content,
        )
        Text(
            text = category.label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = content,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

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
                modifier = Modifier.heightIn(min = 48.dp),
            ) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancelar") }
        },
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

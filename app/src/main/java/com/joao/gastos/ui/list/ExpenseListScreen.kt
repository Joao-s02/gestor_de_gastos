package com.joao.gastos.ui.list

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joao.gastos.R
import com.joao.gastos.model.Expense
import com.joao.gastos.model.Money
import com.joao.gastos.model.MovementType
import com.joao.gastos.ui.icon
import com.joao.gastos.ui.theme.GastosTheme
import com.joao.gastos.ui.theme.balanceCardColor
import com.joao.gastos.ui.theme.balanceCardContentColor
import com.joao.gastos.ui.theme.balanceColor
import com.joao.gastos.ui.theme.moneyColor
import com.joao.gastos.ui.toEpochDayLabel
import com.joao.gastos.ui.toMonthLabel
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseListViewModel,
    justSaved: Boolean,
    onSavedMessageShown: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = SnackbarHostState()
    var pendingConfirm by rememberSaveable { mutableStateOf<Long?>(null) }
    var showMonthSheet by rememberSaveable { mutableStateOf(false) }
    val expenseToConfirm = (state as? ExpenseListUiState.Ready)?.expenses?.find { it.id == pendingConfirm }

    LaunchedEffect(justSaved) {
        if (justSaved) {
            snackbarHostState.showSnackbar("Movimiento guardado")
            onSavedMessageShown()
        }
    }

    ExpenseListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAdd = onAdd,
        onEdit = onEdit,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onOpenMonth = { showMonthSheet = true },
        onDelete = { pendingConfirm = it },
        onRetry = viewModel::retry,
    )

    if (expenseToConfirm != null) {
        DeleteMovementDialog(
            onConfirm = {
                viewModel.confirmDelete(expenseToConfirm)
                pendingConfirm = null
            },
            onDismiss = { pendingConfirm = null },
        )
    }

    if (showMonthSheet) {
        MonthSheet(
            selected = state.month,
            onSelect = {
                viewModel.selectMonth(it)
                showMonthSheet = false
            },
            onDismiss = { showMonthSheet = false },
        )
    }
}

@Composable
private fun ExpenseListContent(
    state: ExpenseListUiState,
    snackbarHostState: SnackbarHostState,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenMonth: () -> Unit,
    onDelete: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MonthHeader(
                label = state.month.toMonthLabel(),
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
                onOpen = onOpenMonth,
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Button(
                    onClick = onAdd,
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .heightIn(min = 48.dp),
                ) {
                    Text("Registrar movimiento", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                is ExpenseListUiState.Loading -> LoadingState()
                is ExpenseListUiState.Empty -> EmptyState()
                is ExpenseListUiState.Error -> ErrorState(message = state.message, onRetry = onRetry)
                is ExpenseListUiState.Ready -> ReadyList(
                    state = state,
                    onEdit = onEdit,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@Composable
private fun MonthHeader(
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher),
            contentDescription = "Yesca",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(4.dp)
                .size(40.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .clickable(onClick = onOpen, role = Role.Button)
                .heightIn(min = 48.dp)
                .semantics { contentDescription = "$label, elegir mes" },
        )
        IconButton(onClick = onPrevious, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Mes anterior")
        }
        IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
private fun ReadyList(
    state: ExpenseListUiState.Ready,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            LedgerSummary(
                incomeCents = state.incomeCents,
                expenseCents = state.expenseCents,
                balanceCents = state.balanceCents,
            )
            Text(
                text = "Movimientos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            )
        }
        items(state.expenses, key = { it.id }) { expense ->
            SwipeToDeleteLine(
                expense = expense,
                onEdit = { onEdit(expense.id) },
                onDelete = { onDelete(expense.id) },
            )
        }
    }
}

@Composable
private fun LedgerSummary(
    incomeCents: Long,
    expenseCents: Long,
    balanceCents: Long,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(balanceCardColor(), MaterialTheme.shapes.large)
                .padding(20.dp),
        ) {
            Text(
                text = "Balance del mes",
                style = MaterialTheme.typography.bodyLarge,
                color = balanceCardContentColor(),
            )
            Text(
                text = Money.format(balanceCents),
                style = MaterialTheme.typography.displaySmall,
                color = balanceCardContentColor(),
            )
        }
        Spacer(Modifier.padding(top = 12.dp))
        TotalRow(label = "Ingresos", cents = incomeCents, type = MovementType.INGRESO)
        Spacer(Modifier.padding(top = 4.dp))
        TotalRow(label = "Gastos", cents = expenseCents, type = MovementType.GASTO)
    }
}

@Composable
private fun TotalRow(label: String, cents: Long, type: MovementType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (type == MovementType.INGRESO) Icons.Rounded.Add else Icons.Rounded.Remove,
                contentDescription = null,
                tint = moneyColor(type),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Text(
            text = signedAmount(type, cents),
            style = MaterialTheme.typography.bodyLarge,
            color = moneyColor(type),
            textAlign = TextAlign.End,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteLine(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete()
            false
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text("Borrar", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onError)
            }
        },
    ) {
        MovementLine(expense = expense, onEdit = onEdit, onDelete = onDelete)
    }
}

@Composable
private fun MovementLine(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val amountColor = moneyColor(expense.type)
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable(onClick = onEdit, role = Role.Button)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .semantics {
                    contentDescription = movementDescription(expense)
                    customActions = listOf(
                        CustomAccessibilityAction("Borrar movimiento", action = { onDelete(); true }),
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = expense.category.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp),
            ) {
                Text(
                    text = expense.category.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = expense.type.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = amountColor,
                )
                Text(
                    text = expense.epochDay.toEpochDayLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (expense.note.isNotBlank()) {
                    Text(
                        text = expense.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Row(
                modifier = Modifier.widthIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (expense.type == MovementType.INGRESO) Icons.Rounded.Add else Icons.Rounded.Remove,
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = Money.format(expense.amountCents),
                    style = MaterialTheme.typography.bodyLarge,
                    color = amountColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 16.dp))
    }
}

private fun movementDescription(expense: Expense): String {
    val note = if (expense.note.isBlank()) "" else ", ${expense.note}"
    return "${expense.category.label}, ${expense.type.label}, ${signedAmount(expense.type, expense.amountCents)}, ${expense.epochDay.toEpochDayLabel()}$note"
}

private fun signedAmount(type: MovementType, cents: Long): String {
    val sign = if (type == MovementType.INGRESO) "+" else "−"
    return sign + " " + Money.format(cents)
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            "Cargando movimientos",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Este mes no tiene movimientos.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            "Registra el primero con el botón de abajo.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        Button(
            onClick = onRetry,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .padding(top = 16.dp)
                .heightIn(min = 48.dp),
        ) {
            Text("Reintentar", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun DeleteMovementDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Borrar este movimiento?") },
        text = { Text("Esta acción no se puede deshacer.") },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Borrar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancelar")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthSheet(
    selected: YearMonth,
    onSelect: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var year by rememberSaveable { mutableIntStateOf(selected.year) }
    val locale = Locale.getDefault()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { year -= 1 }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Año anterior")
                }
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                IconButton(onClick = { year += 1 }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Año siguiente")
                }
            }
            Month.entries.chunked(3).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    row.forEach { month ->
                        val value = YearMonth.of(year, month)
                        val isSelected = value == selected
                        TextButton(
                            onClick = { onSelect(value) },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                contentColor = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .semantics { this.selected = isSelected },
                        ) {
                            Text(
                                text = month.getDisplayName(TextStyle.SHORT, locale)
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() },
                                style = if (isSelected) {
                                    MaterialTheme.typography.labelMedium
                                } else {
                                    MaterialTheme.typography.bodySmall
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewHost(content: @Composable () -> Unit) {
    GastosTheme { content() }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ListReadyLightPreview() {
    PreviewHost {
        ExpenseListContent(ExpenseListSamples.ready, SnackbarHostState(), {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListReadyDarkPreview() {
    PreviewHost {
        ExpenseListContent(ExpenseListSamples.ready, SnackbarHostState(), {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ListEmptyPreview() {
    PreviewHost {
        ExpenseListContent(ExpenseListSamples.empty, SnackbarHostState(), {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ListLoadingPreview() {
    PreviewHost {
        ExpenseListContent(ExpenseListSamples.loading, SnackbarHostState(), {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ListErrorPreview() {
    PreviewHost {
        ExpenseListContent(ExpenseListSamples.error, SnackbarHostState(), {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852, fontScale = 1.5f)
@Composable
private fun ListReadyLargeTextPreview() {
    PreviewHost {
        ExpenseListContent(ExpenseListSamples.ready, SnackbarHostState(), {}, {}, {}, {}, {}, {}, {})
    }
}

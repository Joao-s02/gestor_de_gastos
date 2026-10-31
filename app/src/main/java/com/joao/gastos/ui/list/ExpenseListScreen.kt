package com.joao.gastos.ui.list

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joao.gastos.model.Expense
import com.joao.gastos.model.Money
import com.joao.gastos.model.MovementType
import com.joao.gastos.ui.icon
import com.joao.gastos.ui.theme.GastosTheme
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

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = {
                    Text(
                        text = state.month.toMonthLabel(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.clickable(onClick = onOpenMonth),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onPreviousMonth, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Mes anterior")
                    }
                },
                actions = {
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Mes siguiente")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Agregar movimiento")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 8.dp),
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
private fun ReadyList(
    state: ExpenseListUiState.Ready,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            SummaryCard(
                incomeCents = state.incomeCents,
                expenseCents = state.expenseCents,
                balanceCents = state.balanceCents,
            )
            Spacer(Modifier.height(16.dp))
        }
        items(state.expenses, key = { it.id }) { expense ->
            SwipeToDeleteCard(
                expense = expense,
                onEdit = { onEdit(expense.id) },
                onDelete = { onDelete(expense.id) },
            )
        }
    }
}

@Composable
private fun SummaryCard(
    incomeCents: Long,
    expenseCents: Long,
    balanceCents: Long,
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Balance", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = Money.format(balanceCents),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor(balanceCents),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Ingresos", style = MaterialTheme.typography.labelMedium, color = moneyColor(MovementType.INGRESO))
                Text(
                    text = Money.format(incomeCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = moneyColor(MovementType.INGRESO),
                )
                Spacer(Modifier.height(8.dp))
                Text("Gastos", style = MaterialTheme.typography.labelMedium, color = moneyColor(MovementType.GASTO))
                Text(
                    text = Money.format(expenseCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = moneyColor(MovementType.GASTO),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteCard(
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
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Borrar movimiento",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        MovementCard(expense = expense, onEdit = onEdit, onDelete = onDelete)
    }
}

@Composable
private fun MovementCard(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val amountColor = moneyColor(expense.type)
    val prefix = if (expense.type == MovementType.INGRESO) "+" else "−"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onEdit)
            .semantics {
                contentDescription = "${expense.category.label}, ${expense.type.label}"
                customActions = listOf(CustomAccessibilityAction("Borrar movimiento", action = { onDelete(); true }))
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = expense.category.icon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 12.dp),
        ) {
            Text(expense.category.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = listOf(expense.epochDay.toEpochDayLabel(), expense.note).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = prefix + Money.format(expense.amountCents),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = amountColor,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("Cargando…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("Sin movimientos este mes", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Text(
            "Toca + para agregar el primero",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Rounded.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(48.dp),
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
        title = { Text("¿Borrar movimiento?") },
        text = { Text("Esta acción no se puede deshacer.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Borrar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
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
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp)) {
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
            Spacer(Modifier.height(16.dp))
            Month.entries.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    row.forEach { month ->
                        val value = YearMonth.of(year, month)
                        FilterChip(
                            selected = value == selected,
                            onClick = { onSelect(value) },
                            label = { Text(month.getDisplayName(TextStyle.SHORT, Locale.getDefault()), style = MaterialTheme.typography.labelMedium) },
                            modifier = Modifier.weight(1f).height(32.dp),
                        )
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

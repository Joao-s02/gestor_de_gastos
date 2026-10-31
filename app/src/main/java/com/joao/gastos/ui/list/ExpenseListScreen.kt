package com.joao.gastos.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joao.gastos.model.Category
import com.joao.gastos.model.Expense
import com.joao.gastos.model.Money
import com.joao.gastos.ui.theme.GastosTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseListViewModel,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = SnackbarHostState()
    var pendingConfirmId by rememberSaveable { mutableStateOf<Long?>(null) }
    val expenseToConfirm = (state as? ExpenseListUiState.Ready)
        ?.expenses
        ?.find { it.id == pendingConfirmId }

    LaunchedEffect(state.pendingDelete?.id) {
        val pending = state.pendingDelete ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Gasto eliminado",
            actionLabel = "Deshacer",
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDelete()
        } else {
            viewModel.clearPendingDelete()
        }
    }

    ExpenseListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAdd = onAdd,
        onEdit = onEdit,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onSelectCategory = viewModel::selectCategory,
        onDelete = { pendingConfirmId = it },
    )

    if (expenseToConfirm != null) {
        AlertDialog(
            onDismissRequest = { pendingConfirmId = null },
            title = { Text("Borrar gasto") },
            text = { Text("¿Borrar este gasto de ${Money.format(expenseToConfirm.amountCents)}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmDelete(expenseToConfirm)
                        pendingConfirmId = null
                    },
                ) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { pendingConfirmId = null }) { Text("Cancelar") }
            },
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
    onSelectCategory: (Category?) -> Unit,
    onDelete: (Long) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Gastos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir gasto")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            MonthHeader(
                month = state.month,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
            )
            CategoryFilters(
                selected = state.category,
                onSelect = onSelectCategory,
            )
            when (state) {
                is ExpenseListUiState.Loading -> LoadingMonth()
                is ExpenseListUiState.Empty -> {
                    Total(cents = 0)
                    EmptyMonth(
                        category = state.category,
                        onAdd = onAdd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    )
                }
                is ExpenseListUiState.Ready -> {
                    Total(cents = state.totalCents)
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.expenses, key = { it.id }) { expense ->
                            ExpenseRow(
                                expense = expense,
                                onEdit = { onEdit(expense.id) },
                                onDelete = { onDelete(expense.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Total(cents: Long) {
    Text(
        text = Money.format(cents),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun LoadingMonth() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
        }
        Text(text = month.label(), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
private fun CategoryFilters(
    selected: Category?,
    onSelect: (Category?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("Todas") },
        )
        Category.entries.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category.label) },
            )
        }
    }
}

@Composable
private fun EmptyMonth(
    category: Category?,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (category == null) {
                "No hay gastos en este mes"
            } else {
                "No hay gastos de ${category.label} en este mes"
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        TextButton(onClick = onAdd) { Text("Añadir gasto") }
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = Money.format(expense.amountCents), style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${expense.category.label} · ${expense.epochDay.toLocalDateLabel()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (expense.note.isNotBlank()) {
                Text(
                    text = expense.note,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = "Borrar gasto")
        }
    }
}

private fun YearMonth.label(locale: Locale = Locale.getDefault()): String {
    val raw = atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
    return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

private fun Int.toLocalDateLabel(locale: Locale = Locale.getDefault()): String {
    return LocalDate.ofEpochDay(toLong())
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
}

@Preview(showBackground = true)
@Composable
private fun ExpenseListReadyPreview() {
    GastosTheme {
        ExpenseListContent(
            state = ExpenseListSamples.ready,
            snackbarHostState = SnackbarHostState(),
            onAdd = {},
            onEdit = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onSelectCategory = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseListEmptyPreview() {
    GastosTheme {
        ExpenseListContent(
            state = ExpenseListSamples.empty,
            snackbarHostState = SnackbarHostState(),
            onAdd = {},
            onEdit = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onSelectCategory = {},
            onDelete = {},
        )
    }
}

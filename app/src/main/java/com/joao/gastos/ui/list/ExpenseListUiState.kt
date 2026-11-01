package com.joao.gastos.ui.list

import com.joao.gastos.model.Category
import com.joao.gastos.model.Expense
import com.joao.gastos.model.MovementType
import java.time.LocalDate
import java.time.YearMonth

sealed interface ExpenseListUiState {
    val month: YearMonth

    data class Loading(
        override val month: YearMonth,
    ) : ExpenseListUiState

    data class Empty(
        override val month: YearMonth,
    ) : ExpenseListUiState

    data class Error(
        override val month: YearMonth,
        val message: String,
    ) : ExpenseListUiState

    data class Ready(
        override val month: YearMonth,
        val expenses: List<Expense>,
        val incomeCents: Long,
        val expenseCents: Long,
        val balanceCents: Long,
    ) : ExpenseListUiState
}

object ExpenseListSamples {
    val month: YearMonth = YearMonth.of(2026, 10)

    val expenses: List<Expense> = listOf(
        Expense(
            id = 1,
            amountCents = 1_250,
            epochDay = LocalDate.of(2026, 10, 28).toEpochDay().toInt(),
            category = Category.Comida,
            note = "Menú del día",
            createdAt = 3,
            type = MovementType.GASTO,
        ),
        Expense(
            id = 2,
            amountCents = 240,
            epochDay = LocalDate.of(2026, 10, 27).toEpochDay().toInt(),
            category = Category.Transporte,
            note = "",
            createdAt = 2,
            type = MovementType.GASTO,
        ),
        Expense(
            id = 3,
            amountCents = 180_000,
            epochDay = LocalDate.of(2026, 10, 1).toEpochDay().toInt(),
            category = Category.Salario,
            note = "Nómina",
            createdAt = 1,
            type = MovementType.INGRESO,
        ),
    )

    val ready = ExpenseListUiState.Ready(
        month = month,
        expenses = expenses,
        incomeCents = 180_000,
        expenseCents = 1_490,
        balanceCents = 178_510,
    )

    val empty = ExpenseListUiState.Empty(month)
    val loading = ExpenseListUiState.Loading(month)
    val error = ExpenseListUiState.Error(month, "No se pudieron cargar los movimientos")
}

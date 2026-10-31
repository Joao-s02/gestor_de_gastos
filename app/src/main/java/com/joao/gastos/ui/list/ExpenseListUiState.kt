package com.joao.gastos.ui.list

import com.joao.gastos.model.Category
import com.joao.gastos.model.Expense
import java.time.LocalDate
import java.time.YearMonth

sealed interface ExpenseListUiState {
    val month: YearMonth
    val category: Category?
    val pendingDelete: Expense?

    data class Loading(
        override val month: YearMonth,
        override val category: Category?,
        override val pendingDelete: Expense? = null,
    ) : ExpenseListUiState

    data class Empty(
        override val month: YearMonth,
        override val category: Category?,
        override val pendingDelete: Expense? = null,
    ) : ExpenseListUiState

    data class Ready(
        override val month: YearMonth,
        override val category: Category?,
        val expenses: List<Expense>,
        val totalCents: Long,
        override val pendingDelete: Expense? = null,
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
        ),
        Expense(
            id = 2,
            amountCents = 240,
            epochDay = LocalDate.of(2026, 10, 27).toEpochDay().toInt(),
            category = Category.Transporte,
            note = "",
            createdAt = 2,
        ),
        Expense(
            id = 3,
            amountCents = 4_599,
            epochDay = LocalDate.of(2026, 10, 12).toEpochDay().toInt(),
            category = Category.Hogar,
            note = "Bombilla de la cocina que se fundió el martes por la noche",
            createdAt = 1,
        ),
    )

    val ready = ExpenseListUiState.Ready(
        month = month,
        category = null,
        expenses = expenses,
        totalCents = expenses.sumOf { it.amountCents },
    )

    val empty = ExpenseListUiState.Empty(
        month = month,
        category = Category.Ocio,
    )

    val loading = ExpenseListUiState.Loading(
        month = month,
        category = null,
    )
}

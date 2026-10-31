package com.joao.gastos.ui.form

import com.joao.gastos.model.Category
import java.time.LocalDate

data class ExpenseFormUiState(
    val amountText: String = "",
    val epochDay: Int = LocalDate.now().toEpochDay().toInt(),
    val category: Category = Category.Comida,
    val note: String = "",
    val amountError: String? = null,
    val noteError: String? = null,
    val isEditing: Boolean = false,
    val loading: Boolean = false,
)

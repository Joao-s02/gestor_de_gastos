package com.joao.gastos.ui.form

import com.joao.gastos.model.Category
import com.joao.gastos.model.MovementType
import java.time.LocalDate

sealed interface ExpenseFormUiState {
    val isEditing: Boolean

    data class Loading(
        override val isEditing: Boolean,
    ) : ExpenseFormUiState

    data class Editing(
        val amountText: String = "",
        val epochDay: Int = LocalDate.now().toEpochDay().toInt(),
        val type: MovementType = MovementType.GASTO,
        val category: Category = Category.Comida,
        val note: String = "",
        val amountError: String? = null,
        val saveError: String? = null,
        override val isEditing: Boolean = false,
        val saving: Boolean = false,
    ) : ExpenseFormUiState

    companion object {
        const val NOTE_LIMIT = 140
    }
}

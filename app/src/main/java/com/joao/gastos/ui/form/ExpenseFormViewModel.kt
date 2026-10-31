package com.joao.gastos.ui.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joao.gastos.data.ExpenseRepository
import com.joao.gastos.model.Category
import com.joao.gastos.model.Expense
import com.joao.gastos.model.Money
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseFormViewModel(
    private val repository: ExpenseRepository,
    private val expenseId: Long?,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val amount = savedState.getStateFlow(KEY_AMOUNT, "")
    private val day = savedState.getStateFlow(KEY_DAY, LocalDate.now().toEpochDay().toInt())
    private val categoryName = savedState.getStateFlow(KEY_CATEGORY, Category.Comida.name)
    private val note = savedState.getStateFlow(KEY_NOTE, "")
    private val amountError = MutableStateFlow<String?>(null)
    private val noteError = MutableStateFlow<String?>(null)
    private val loading = MutableStateFlow(expenseId != null && !savedState.contains(KEY_AMOUNT))
    private val saving = MutableStateFlow(false)

    private val _saved = MutableSharedFlow<Unit>()
    val saved = _saved

    val uiState: StateFlow<ExpenseFormUiState> = combine(
        combine(amount, day, categoryName, note) { amountText, epochDay, category, noteText ->
            FormFields(amountText, epochDay, category, noteText)
        },
        amountError,
        noteError,
        loading,
        saving,
    ) { fields, amountMessage, noteMessage, isLoading, isSaving ->
        ExpenseFormUiState(
            amountText = fields.amount,
            epochDay = fields.day,
            category = Category.valueOf(fields.category),
            note = fields.note,
            amountError = amountMessage,
            noteError = noteMessage,
            isEditing = expenseId != null,
            loading = isLoading,
            saving = isSaving,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseFormUiState(isEditing = expenseId != null, loading = loading.value))

    init {
        if (expenseId != null && !savedState.contains(KEY_AMOUNT)) {
            viewModelScope.launch {
                val expense = repository.get(expenseId)
                if (expense != null) {
                    savedState[KEY_AMOUNT] = Money.formatInput(expense.amountCents)
                    savedState[KEY_DAY] = expense.epochDay
                    savedState[KEY_CATEGORY] = expense.category.name
                    savedState[KEY_NOTE] = expense.note
                    savedState[KEY_CREATED_AT] = expense.createdAt
                }
                loading.value = false
            }
        }
    }

    fun onAmountChange(value: String) {
        savedState[KEY_AMOUNT] = value
        amountError.value = null
    }

    fun onDateChange(epochDay: Int) {
        savedState[KEY_DAY] = epochDay
    }

    fun onCategoryChange(category: Category) {
        savedState[KEY_CATEGORY] = category.name
    }

    fun onNoteChange(value: String) {
        savedState[KEY_NOTE] = value.take(MAX_NOTE)
        noteError.value = null
    }

    fun save() {
        if (saving.value) return
        val noteText = note.value.trim()
        if (noteText.length > MAX_NOTE) {
            noteError.value = "Máximo $MAX_NOTE caracteres"
            return
        }
        val amountMessage = Money.validationError(amount.value)
        if (amountMessage != null) {
            amountError.value = amountMessage
            return
        }
        val money = Money.parse(amount.value) ?: return
        saving.value = true
        viewModelScope.launch {
            val createdAt = savedState.get<Long>(KEY_CREATED_AT) ?: System.currentTimeMillis()
            repository.upsert(
                Expense(
                    id = expenseId ?: 0,
                    amountCents = money.cents,
                    epochDay = day.value,
                    category = Category.valueOf(categoryName.value),
                    note = noteText,
                    createdAt = createdAt,
                ),
            )
            _saved.emit(Unit)
        }
    }

    private data class FormFields(
        val amount: String,
        val day: Int,
        val category: String,
        val note: String,
    )

    private companion object {
        const val KEY_AMOUNT = "amount"
        const val KEY_DAY = "day"
        const val KEY_CATEGORY = "category"
        const val KEY_NOTE = "note"
        const val KEY_CREATED_AT = "createdAt"
        const val MAX_NOTE = 140
    }
}

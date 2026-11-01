package com.joao.gastos.ui.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joao.gastos.data.ExpenseRepository
import com.joao.gastos.model.Category
import com.joao.gastos.model.Expense
import com.joao.gastos.model.Money
import com.joao.gastos.model.MovementType
import com.joao.gastos.ui.form.ExpenseFormUiState.Companion.NOTE_LIMIT
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
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
    private val movementType = savedState.getStateFlow(KEY_TYPE, MovementType.GASTO.name)
    private val categoryName = savedState.getStateFlow(KEY_CATEGORY, Category.Comida.name)
    private val note = savedState.getStateFlow(KEY_NOTE, "")
    private val amountError = MutableStateFlow<String?>(null)
    private val saveError = MutableStateFlow<String?>(null)
    private val loading = MutableStateFlow(expenseId != null && !savedState.contains(KEY_AMOUNT))
    private val saving = MutableStateFlow(false)

    private val _saved = MutableSharedFlow<Unit>()
    val saved = _saved

    val uiState: StateFlow<ExpenseFormUiState> = combine(
        combine(amount, day, movementType, categoryName, note, ::FormFields),
        amountError,
        saveError,
        loading,
        saving,
    ) { fields, amountMessage, persistMessage, isLoading, isSaving ->
        if (isLoading) {
            ExpenseFormUiState.Loading(isEditing = expenseId != null)
        } else {
            ExpenseFormUiState.Editing(
                amountText = fields.amount,
                epochDay = fields.day,
                type = fields.type.toType(),
                category = fields.category.toCategory(),
                note = fields.note,
                amountError = amountMessage,
                saveError = persistMessage,
                isEditing = expenseId != null,
                saving = isSaving,
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialState(),
    )

    init {
        if (expenseId != null && !savedState.contains(KEY_AMOUNT)) {
            viewModelScope.launch {
                val expense = repository.get(expenseId)
                if (expense == null) {
                    saveError.value = "No se encontró el gasto"
                } else {
                    savedState[KEY_AMOUNT] = Money.formatInput(expense.amountCents)
                    savedState[KEY_DAY] = expense.epochDay
                    savedState[KEY_TYPE] = expense.type.name
                    savedState[KEY_CATEGORY] = expense.category.name
                    savedState[KEY_NOTE] = expense.note
                    savedState[KEY_CREATED_AT] = expense.createdAt
                    alignCategory(expense.type)
                }
                loading.value = false
            }
        }
    }

    fun onAmountChange(value: String) {
        savedState[KEY_AMOUNT] = value
        amountError.value = null
        saveError.value = null
    }

    fun onDateChange(epochDay: Int) {
        savedState[KEY_DAY] = epochDay
    }

    fun onTypeChange(type: MovementType) {
        savedState[KEY_TYPE] = type.name
        alignCategory(type)
    }

    fun onCategoryChange(category: Category) {
        savedState[KEY_CATEGORY] = category.name
    }

    fun onNoteChange(value: String) {
        savedState[KEY_NOTE] = value.take(NOTE_LIMIT)
    }

    fun save() {
        val editing = uiState.value as? ExpenseFormUiState.Editing ?: return
        if (editing.saving) return
        val amountMessage = Money.validationError(amount.value)
        if (amountMessage != null) {
            amountError.value = amountMessage
            return
        }
        val money = Money.parse(amount.value) ?: return
        saving.value = true
        saveError.value = null
        viewModelScope.launch {
            try {
                repository.upsert(
                    Expense(
                        id = expenseId ?: 0,
                        amountCents = money.cents,
                        epochDay = day.value,
                        category = categoryName.value.toCategory(),
                        type = movementType.value.toType(),
                        note = note.value.trim(),
                        createdAt = savedState.get<Long>(KEY_CREATED_AT) ?: System.currentTimeMillis(),
                    ),
                )
                _saved.emit(Unit)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                saveError.value = "No se pudo guardar"
            } finally {
                saving.value = false
            }
        }
    }

    private fun initialState(): ExpenseFormUiState {
        return if (loading.value) {
            ExpenseFormUiState.Loading(isEditing = expenseId != null)
        } else {
            ExpenseFormUiState.Editing(isEditing = expenseId != null)
        }
    }

    private fun alignCategory(type: MovementType) {
        val current = categoryName.value.toCategory()
        if (current.allows(type)) return
        savedState[KEY_CATEGORY] = Category.forType(type).first().name
    }

    private fun String.toCategory(): Category = Category.fromStored(this) ?: Category.Comida

    private fun String.toType(): MovementType = MovementType.fromStored(this) ?: MovementType.GASTO

    private data class FormFields(
        val amount: String,
        val day: Int,
        val type: String,
        val category: String,
        val note: String,
    )

    private companion object {
        const val KEY_AMOUNT = "amount"
        const val KEY_DAY = "day"
        const val KEY_TYPE = "type"
        const val KEY_CATEGORY = "category"
        const val KEY_NOTE = "note"
        const val KEY_CREATED_AT = "createdAt"
    }
}

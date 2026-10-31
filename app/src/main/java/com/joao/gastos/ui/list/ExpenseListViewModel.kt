package com.joao.gastos.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joao.gastos.data.ExpenseRepository
import com.joao.gastos.model.Category
import com.joao.gastos.model.Expense
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseListViewModel(
    private val repository: ExpenseRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val month = savedState.getStateFlow(KEY_MONTH, YearMonth.now().toString())
    private val categoryName = savedState.getStateFlow(KEY_CATEGORY, "")
    private val pendingDelete = MutableStateFlow<Expense?>(null)

    val uiState: StateFlow<ExpenseListUiState> = combine(
        month,
        categoryName,
        pendingDelete,
    ) { monthText, categoryText, pending ->
        Triple(YearMonth.parse(monthText), categoryText.toCategory(), pending)
    }.flatMapLatest { (visibleMonth, category, pending) ->
        repository.observe(visibleMonth, category).map { rows ->
            rows.toUiState(visibleMonth, category, pending)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ExpenseListUiState.Loading(
            month = YearMonth.parse(month.value),
            category = categoryName.value.toCategory(),
        ),
    )

    fun showPreviousMonth() = shiftMonth(-1)

    fun showNextMonth() = shiftMonth(1)

    fun selectCategory(category: Category?) {
        savedState[KEY_CATEGORY] = category?.name.orEmpty()
    }

    fun confirmDelete(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
            pendingDelete.value = expense
        }
    }

    fun undoDelete() {
        val expense = pendingDelete.value ?: return
        viewModelScope.launch {
            repository.upsert(expense)
            pendingDelete.value = null
        }
    }

    fun clearPendingDelete() {
        pendingDelete.value = null
    }

    private fun shiftMonth(months: Long) {
        val current = YearMonth.parse(month.value)
        savedState[KEY_MONTH] = current.plusMonths(months).toString()
    }

    private fun String.toCategory(): Category? = if (isEmpty()) null else Category.valueOf(this)

    private fun List<Expense>.toUiState(
        month: YearMonth,
        category: Category?,
        pending: Expense?,
    ): ExpenseListUiState {
        if (isEmpty()) return ExpenseListUiState.Empty(month, category, pending)
        return ExpenseListUiState.Ready(
            month = month,
            category = category,
            expenses = this,
            totalCents = sumOf { it.amountCents },
            pendingDelete = pending,
        )
    }

    private companion object {
        const val KEY_MONTH = "month"
        const val KEY_CATEGORY = "category"
    }
}

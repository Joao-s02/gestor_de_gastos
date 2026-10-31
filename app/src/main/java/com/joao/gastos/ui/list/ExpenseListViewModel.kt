package com.joao.gastos.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joao.gastos.data.ExpenseRepository
import com.joao.gastos.model.Expense
import com.joao.gastos.model.MovementType
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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
    private val retry = MutableStateFlow(0)

    val uiState: StateFlow<ExpenseListUiState> = combine(month, retry) { monthText, _ ->
        YearMonth.parse(monthText)
    }.flatMapLatest { visibleMonth ->
        repository.observe(visibleMonth, null)
            .map { rows -> rows.toUiState(visibleMonth) }
            .catch { emit(ExpenseListUiState.Error(visibleMonth, "No se pudieron cargar los movimientos")) }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ExpenseListUiState.Loading(YearMonth.parse(month.value)),
    )

    fun showPreviousMonth() = shiftMonth(-1)

    fun showNextMonth() = shiftMonth(1)

    fun selectMonth(value: YearMonth) {
        savedState[KEY_MONTH] = value.toString()
    }

    fun retry() {
        retry.value += 1
    }

    fun confirmDelete(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    private fun shiftMonth(months: Long) {
        val current = YearMonth.parse(month.value)
        savedState[KEY_MONTH] = current.plusMonths(months).toString()
    }

    private fun List<Expense>.toUiState(month: YearMonth): ExpenseListUiState {
        if (isEmpty()) return ExpenseListUiState.Empty(month)
        val income = filter { it.type == MovementType.INGRESO }.sumOf { it.amountCents }
        val spent = filter { it.type == MovementType.GASTO }.sumOf { it.amountCents }
        return ExpenseListUiState.Ready(
            month = month,
            expenses = this,
            incomeCents = income,
            expenseCents = spent,
            balanceCents = income - spent,
        )
    }

    private companion object {
        const val KEY_MONTH = "month"
    }
}

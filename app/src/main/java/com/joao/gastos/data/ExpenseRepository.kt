package com.joao.gastos.data

import com.joao.gastos.model.Category
import com.joao.gastos.model.Expense
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepository(private val dao: ExpenseDao) {
    fun observe(month: YearMonth, category: Category?): Flow<List<Expense>> {
        val fromDay = month.atDay(1).toEpochDay().toInt()
        val toDay = month.atEndOfMonth().toEpochDay().toInt()
        return dao.observe(fromDay, toDay, category?.name).map { rows -> rows.map { it.toModel() } }
    }

    suspend fun get(id: Long): Expense? = dao.get(id)?.toModel()

    suspend fun upsert(expense: Expense) {
        dao.upsert(expense.toEntity())
    }

    suspend fun delete(expense: Expense) {
        dao.delete(expense.id)
    }
}

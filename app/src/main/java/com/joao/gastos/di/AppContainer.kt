package com.joao.gastos.di

import android.content.Context
import com.joao.gastos.data.ExpenseRepository
import com.joao.gastos.data.GastosDatabase

class AppContainer(context: Context) {
    private val database = GastosDatabase.create(context)
    val repository = ExpenseRepository(database.expenseDao())
}

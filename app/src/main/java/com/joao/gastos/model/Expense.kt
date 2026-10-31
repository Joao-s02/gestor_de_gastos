package com.joao.gastos.model

data class Expense(
    val id: Long = 0,
    val amountCents: Long,
    val epochDay: Int,
    val category: Category,
    val note: String,
    val createdAt: Long,
)

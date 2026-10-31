package com.joao.gastos.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.joao.gastos.model.Category
import com.joao.gastos.model.Expense

@Entity(
    tableName = "expenses",
    indices = [Index("epochDay")],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val epochDay: Int,
    val category: String,
    val note: String,
    val createdAt: Long,
)

fun ExpenseEntity.toModel() = Expense(
    id = id,
    amountCents = amountCents,
    epochDay = epochDay,
    category = Category.valueOf(category),
    note = note,
    createdAt = createdAt,
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    amountCents = amountCents,
    epochDay = epochDay,
    category = category.name,
    note = note,
    createdAt = createdAt,
)

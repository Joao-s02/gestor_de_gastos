package com.joao.gastos.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.joao.gastos.model.Category
import com.joao.gastos.model.Expense
import com.joao.gastos.model.MovementType

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
    val type: String = MovementType.GASTO.name,
)

fun ExpenseEntity.toModel() = Expense(
    id = id,
    amountCents = amountCents,
    epochDay = epochDay,
    category = requireNotNull(Category.fromStored(category)) { "Categoría desconocida: $category" },
    note = note,
    createdAt = createdAt,
    type = MovementType.fromStored(type) ?: MovementType.GASTO,
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    amountCents = amountCents,
    epochDay = epochDay,
    category = category.name,
    note = note,
    createdAt = createdAt,
    type = type.name,
)

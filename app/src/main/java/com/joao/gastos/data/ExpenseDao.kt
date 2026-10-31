package com.joao.gastos.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query(
        """
        SELECT * FROM expenses
        WHERE epochDay BETWEEN :fromDay AND :toDay
        AND (:category IS NULL OR category = :category)
        ORDER BY epochDay DESC, createdAt DESC
        """,
    )
    fun observe(fromDay: Int, toDay: Int, category: String?): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun get(id: Long): ExpenseEntity?

    @Upsert
    suspend fun upsert(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: Long)
}

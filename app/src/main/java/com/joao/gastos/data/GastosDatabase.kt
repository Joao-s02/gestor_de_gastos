package com.joao.gastos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
abstract class GastosDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object {
        fun create(context: Context): GastosDatabase {
            return Room.databaseBuilder(context, GastosDatabase::class.java, "gastos.db").build()
        }
    }
}

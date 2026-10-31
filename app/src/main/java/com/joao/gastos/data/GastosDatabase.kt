package com.joao.gastos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ExpenseEntity::class], version = 2, exportSchema = false)
abstract class GastosDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN type TEXT NOT NULL DEFAULT 'GASTO'")
            }
        }

        fun create(context: Context): GastosDatabase {
            return Room.databaseBuilder(context, GastosDatabase::class.java, "gastos.db")
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}

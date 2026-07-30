package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ItemEntity::class, WarehouseLogEntity::class, WarehouseConfigEntity::class, UserEntity::class, WeeklyQuotaEntity::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun warehouseLogDao(): WarehouseLogDao
    abstract fun warehouseConfigDao(): WarehouseConfigDao
    abstract fun userDao(): UserDao
    abstract fun weeklyQuotaDao(): WeeklyQuotaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fivem_warehouse_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseConfigDao {
    @Query("SELECT * FROM warehouse_config WHERE id = 1")
    fun getConfig(): Flow<WarehouseConfigEntity?>

    @Query("SELECT * FROM warehouse_config WHERE id = 1")
    suspend fun getConfigDirect(): WarehouseConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: WarehouseConfigEntity)
}

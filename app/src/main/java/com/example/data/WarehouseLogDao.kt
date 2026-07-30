package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseLogDao {
    @Query("SELECT * FROM warehouse_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WarehouseLogEntity>>

    @Query("SELECT * FROM warehouse_logs WHERE actionType = :action ORDER BY timestamp DESC")
    fun getLogsByAction(action: String): Flow<List<WarehouseLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WarehouseLogEntity): Long

    @Query("UPDATE warehouse_logs SET discordSentSuccess = :success WHERE id = :logId")
    suspend fun updateDiscordStatus(logId: Long, success: Boolean)

    @Query("DELETE FROM warehouse_logs")
    suspend fun clearLogs()

    @Query("SELECT COUNT(*) FROM warehouse_logs")
    suspend fun getLogCount(): Int
}

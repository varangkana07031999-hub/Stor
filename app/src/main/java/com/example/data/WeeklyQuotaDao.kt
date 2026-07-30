package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyQuotaDao {
    @Query("SELECT * FROM weekly_quota_items ORDER BY id ASC")
    fun getAllQuotaItems(): Flow<List<WeeklyQuotaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotaItem(quota: WeeklyQuotaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotas: List<WeeklyQuotaEntity>)

    @Update
    suspend fun updateQuotaItem(quota: WeeklyQuotaEntity)

    @Delete
    suspend fun deleteQuotaItem(quota: WeeklyQuotaEntity)

    @Query("SELECT COUNT(*) FROM weekly_quota_items")
    suspend fun getQuotaCount(): Int

    @Query("DELETE FROM weekly_quota_items")
    suspend fun clearAllQuotaItems()
}

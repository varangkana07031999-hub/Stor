package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM warehouse_items ORDER BY itemName ASC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM warehouse_items WHERE id = :id")
    suspend fun getItemById(id: Int): ItemEntity?

    @Query("SELECT * FROM warehouse_items WHERE itemCode = :code")
    suspend fun getItemByCode(code: String): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Query("DELETE FROM warehouse_items WHERE id = :id")
    suspend fun deleteItem(id: Int)

    @Query("DELETE FROM warehouse_items")
    suspend fun deleteAllItems()

    @Query("SELECT COUNT(*) FROM warehouse_items")
    suspend fun getItemCount(): Int
}

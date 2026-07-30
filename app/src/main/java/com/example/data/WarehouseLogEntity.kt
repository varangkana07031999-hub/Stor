package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warehouse_logs")
data class WarehouseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String, // "DEPOSIT" (นำเข้า) or "WITHDRAW" (นำออก)
    val itemCode: String,
    val itemName: String,
    val amount: Int,
    val remainingStock: Int,
    val playerName: String,
    val playerCitizenId: String,
    val playerJob: String,
    val warehouseName: String,
    val notes: String = "",
    val discordSentSuccess: Boolean = false
)

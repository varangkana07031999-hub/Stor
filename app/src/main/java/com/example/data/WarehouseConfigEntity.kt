package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warehouse_config")
data class WarehouseConfigEntity(
    @PrimaryKey val id: Int = 1,
    val discordWebhookUrl: String = "",
    val isWebhookEnabled: Boolean = true,
    val serverName: String = "FiveM Thailand City RP",
    val defaultWarehouseName: String = "คลังแก๊ง Teletubbies",
    val isRealtimeSync: Boolean = true
)

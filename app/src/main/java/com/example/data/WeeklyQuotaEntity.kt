package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_quota_items")
data class WeeklyQuotaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemCode: String,
    val itemName: String,
    val targetAmount: Int,
    val unit: String = "ชิ้น",
    val finePerUnit: Double = 500.0,
    val penaltyType: String = "MONEY", // "MONEY", "ITEM", "ACTIVITY"
    val penaltyUnit: String = "บาท",
    val penaltyCustomNote: String = ""
)


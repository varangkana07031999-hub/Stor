package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warehouse_items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemCode: String,
    val itemName: String,
    val category: String,
    val currentQuantity: Int,
    val unit: String = "ชิ้น",
    val minQuantity: Int = 10,
    val weight: Double = 0.1,
    val unitPrice: Double = 100.0,
    val iconName: String = "box"
)

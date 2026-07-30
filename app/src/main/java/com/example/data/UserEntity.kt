package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val roleNameTh: String, val description: String) {
    ADMIN("ผู้ดูแลระบบ (Admin)", "สิทธิ์เต็มรูปแบบ: เพิ่ม, แก้ไข, ลบ, ดูรายงาน, ตั้งค่า, ล้างประวัติ"),
    STAFF("ผู้บันทึกข้อมูล (Staff)", "สิทธิ์ปฏิบัติงาน: นำเข้า, เบิกออก, เพิ่มไอเทม, ดูรายงาน"),
    VIEWER("ผู้ชมระบบ (Viewer)", "สิทธิ์อ่านอย่างเดียว: ดูคลังสินค้า, ดูประวัติ, ดูรายงาน")
}

@Entity(tableName = "warehouse_users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val citizenId: String,
    val role: String = UserRole.STAFF.name, // ADMIN, STAFF, VIEWER
    val job: String = "Civilian",
    val avatarColorHex: String = "#D0BCFF",
    val manualPrestigePoints: Int = 0,
    val avatarUrl: String = ""
) {
    fun getRoleEnum(): UserRole {
        return try {
            UserRole.valueOf(role)
        } catch (e: Exception) {
            UserRole.STAFF
        }
    }

    val canDeposit: Boolean
        get() = getRoleEnum() == UserRole.ADMIN || getRoleEnum() == UserRole.STAFF

    val canWithdraw: Boolean
        get() = getRoleEnum() == UserRole.ADMIN || getRoleEnum() == UserRole.STAFF

    val canAddItem: Boolean
        get() = getRoleEnum() == UserRole.ADMIN || getRoleEnum() == UserRole.STAFF

    val canEditItem: Boolean
        get() = getRoleEnum() == UserRole.ADMIN || getRoleEnum() == UserRole.STAFF

    val canDeleteItem: Boolean
        get() = getRoleEnum() == UserRole.ADMIN || getRoleEnum() == UserRole.STAFF

    val canClearLogs: Boolean
        get() = getRoleEnum() == UserRole.ADMIN

    val canManageConfig: Boolean
        get() = getRoleEnum() == UserRole.ADMIN

    val canManageUsers: Boolean
        get() = getRoleEnum() == UserRole.ADMIN
}

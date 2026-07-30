package com.example.data

import kotlinx.coroutines.flow.Flow

class WarehouseRepository(
    private val itemDao: ItemDao,
    private val logDao: WarehouseLogDao,
    private val configDao: WarehouseConfigDao,
    private val userDao: UserDao,
    private val weeklyQuotaDao: WeeklyQuotaDao,
    private val discordSender: DiscordWebhookSender = DiscordWebhookSender()
) {
    val allItems: Flow<List<ItemEntity>> = itemDao.getAllItems()
    val allLogs: Flow<List<WarehouseLogEntity>> = logDao.getAllLogs()
    val config: Flow<WarehouseConfigEntity?> = configDao.getConfig()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allWeeklyQuotas: Flow<List<WeeklyQuotaEntity>> = weeklyQuotaDao.getAllQuotaItems()

    suspend fun initDefaultDataIfEmpty() {
        if (itemDao.getItemCount() == 0) {
            val defaultItems = listOf(
                ItemEntity(itemCode = "bread", itemName = "ขนมปัง (Bread)", category = "อาหาร/เครื่องดื่ม", currentQuantity = 150, unit = "ชิ้น", minQuantity = 30, weight = 0.2, unitPrice = 50.0, iconName = "bread"),
                ItemEntity(itemCode = "water", itemName = "น้ำดื่ม (Water Bottle)", category = "อาหาร/เครื่องดื่ม", currentQuantity = 220, unit = "ขวด", minQuantity = 50, weight = 0.5, unitPrice = 30.0, iconName = "water"),
                ItemEntity(itemCode = "medkit", itemName = "กล่องพยาบาล (Medkit)", category = "อุปกรณ์การแพทย์", currentQuantity = 45, unit = "ชุด", minQuantity = 15, weight = 1.0, unitPrice = 500.0, iconName = "medkit"),
                ItemEntity(itemCode = "bandage", itemName = "ผ้าพันแผล (Bandage)", category = "อุปกรณ์การแพทย์", currentQuantity = 80, unit = "ม้วน", minQuantity = 20, weight = 0.1, unitPrice = 120.0, iconName = "bandage"),
                ItemEntity(itemCode = "repairkit", itemName = "ชุดซ่อมรถยนต์ (Repair Kit)", category = "อุปกรณ์เครื่องมือ", currentQuantity = 25, unit = "กล่อง", minQuantity = 10, weight = 2.5, unitPrice = 1500.0, iconName = "tool"),
                ItemEntity(itemCode = "lockpick", itemName = "เหล็กสะเดาะกลอน (Lockpick)", category = "อุปกรณ์เครื่องมือ", currentQuantity = 12, unit = "ชิ้น", minQuantity = 5, weight = 0.3, unitPrice = 800.0, iconName = "key"),
                ItemEntity(itemCode = "radio", itemName = "วิทยุสื่อสาร (Radio)", category = "อุปกรณ์เครื่องมือ", currentQuantity = 18, unit = "เครื่อง", minQuantity = 5, weight = 0.8, unitPrice = 2500.0, iconName = "radio"),
                ItemEntity(itemCode = "weapon_pistol", itemName = "ปืนพก 9mm (Pistol)", category = "อาวุธ/ยุทธภัณฑ์", currentQuantity = 8, unit = "กระบอก", minQuantity = 3, weight = 1.5, unitPrice = 15000.0, iconName = "weapon"),
                ItemEntity(itemCode = "ammo_9mm", itemName = "กระสุนปืน 9mm (9mm Ammo)", category = "อาวุธ/ยุทธภัณฑ์", currentQuantity = 350, unit = "นัด", minQuantity = 100, weight = 0.05, unitPrice = 50.0, iconName = "ammo"),
                ItemEntity(itemCode = "steel", itemName = "แผ่นเหล็กกล้า (Steel Plate)", category = "วัตถุดิบ/แร่", currentQuantity = 500, unit = "แผ่น", minQuantity = 100, weight = 2.0, unitPrice = 300.0, iconName = "metal")
            )
            itemDao.insertAll(defaultItems)
        }

        if (userDao.getUserCount() == 0) {
            val defaultUsers = listOf(
                UserEntity(username = "Admin_Somsak", citizenId = "081-234-5678", role = UserRole.ADMIN.name, job = "Chief Admin", avatarColorHex = "#D0BCFF"),
                UserEntity(username = "Staff_Somchai", citizenId = "089-876-5432", role = UserRole.STAFF.name, job = "Warehouse Officer", avatarColorHex = "#B2F2BB"),
                UserEntity(username = "Viewer_Somsri", citizenId = "086-111-2222", role = UserRole.VIEWER.name, job = "Auditor", avatarColorHex = "#F2B8B5")
            )
            userDao.insertAll(defaultUsers)
        }

        if (weeklyQuotaDao.getQuotaCount() == 0) {
            val defaultQuotas = listOf(
                WeeklyQuotaEntity(itemCode = "weed", itemName = "กัญชาแปรรูป (Dried Weed)", targetAmount = 100, unit = "ชิ้น", finePerUnit = 500.0, penaltyType = "MONEY", penaltyUnit = "บาท"),
                WeeklyQuotaEntity(itemCode = "steel", itemName = "แผ่นเหล็กกล้า (Steel Plate)", targetAmount = 50, unit = "แผ่น", finePerUnit = 2.0, penaltyType = "ITEM", penaltyUnit = "แผ่น", penaltyCustomNote = "ชดเชยแผ่นเหล็ก 2 เท่า"),
                WeeklyQuotaEntity(itemCode = "lockpick", itemName = "เหล็กสะเดาะกลอน (Lockpick)", targetAmount = 20, unit = "ชิ้น", finePerUnit = 1000.0, penaltyType = "MONEY", penaltyUnit = "บาท"),
                WeeklyQuotaEntity(itemCode = "repairkit", itemName = "ชุดซ่อมรถยนต์ (Repair Kit)", targetAmount = 10, unit = "กล่อง", finePerUnit = 1.0, penaltyType = "ACTIVITY", penaltyUnit = "รอบ", penaltyCustomNote = "วิ่งรอบฐานแก๊ง/ทำความสะอาดเซฟเฮาส์")
            )
            weeklyQuotaDao.insertAll(defaultQuotas)
        }

        if (configDao.getConfigDirect() == null) {
            configDao.saveConfig(WarehouseConfigEntity())
        }
    }

    suspend fun saveWeeklyQuota(quota: WeeklyQuotaEntity) {
        if (quota.id == 0) {
            weeklyQuotaDao.insertQuotaItem(quota)
        } else {
            weeklyQuotaDao.updateQuotaItem(quota)
        }
    }

    suspend fun deleteWeeklyQuota(quota: WeeklyQuotaEntity) {
        weeklyQuotaDao.deleteQuotaItem(quota)
    }

    suspend fun performItemTransaction(
        item: ItemEntity,
        actionType: String, // "DEPOSIT" or "WITHDRAW"
        amount: Int,
        playerName: String,
        playerCitizenId: String,
        playerJob: String,
        warehouseName: String,
        notes: String,
        sendDiscordImmediately: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ): Result<WarehouseLogEntity> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("จำนวนต้องมากกว่า 0"))
        }

        val isDeposit = actionType.equals("DEPOSIT", ignoreCase = true)
        val newQuantity = if (isDeposit) {
            item.currentQuantity + amount
        } else {
            if (item.currentQuantity < amount) {
                return Result.failure(IllegalStateException("จำนวนสินค้าในคลังไม่เพียงพอ (มีอยู่ ${item.currentQuantity} ${item.unit})"))
            }
            item.currentQuantity - amount
        }

        // Update Item Quantity
        val updatedItem = item.copy(currentQuantity = newQuantity)
        itemDao.updateItem(updatedItem)

        // Create Log
        var log = WarehouseLogEntity(
            timestamp = timestamp,
            actionType = if (isDeposit) "DEPOSIT" else "WITHDRAW",
            itemCode = item.itemCode,
            itemName = item.itemName,
            amount = amount,
            remainingStock = newQuantity,
            playerName = playerName,
            playerCitizenId = playerCitizenId,
            playerJob = playerJob,
            warehouseName = warehouseName,
            notes = notes,
            discordSentSuccess = false
        )

        val logId = logDao.insertLog(log)
        log = log.copy(id = logId)

        // Check Webhook Dispatch
        val currentConfig = configDao.getConfigDirect() ?: WarehouseConfigEntity()
        if (sendDiscordImmediately && currentConfig.isWebhookEnabled && currentConfig.discordWebhookUrl.isNotBlank()) {
            val discordResult = discordSender.sendWarehouseAlert(
                webhookUrl = currentConfig.discordWebhookUrl,
                actionType = log.actionType,
                itemName = log.itemName,
                itemCode = log.itemCode,
                amount = log.amount,
                remainingStock = log.remainingStock,
                playerName = log.playerName,
                playerCitizenId = log.playerCitizenId,
                playerJob = log.playerJob,
                warehouseName = log.warehouseName,
                notes = log.notes,
                serverName = currentConfig.serverName,
                timestamp = log.timestamp
            )

            val success = discordResult.isSuccess
            logDao.updateDiscordStatus(logId, success)
            log = log.copy(discordSentSuccess = success)
        }

        return Result.success(log)
    }

    suspend fun addNewItem(item: ItemEntity) {
        itemDao.insertItem(item)
    }

    suspend fun updateItem(item: ItemEntity) {
        itemDao.updateItem(item)
    }

    suspend fun deleteItem(id: Int) {
        itemDao.deleteItem(id)
    }

    suspend fun deleteAllItems() {
        itemDao.deleteAllItems()
    }

    suspend fun resendLogToDiscord(log: WarehouseLogEntity): Result<String> {
        val currentConfig = configDao.getConfigDirect() ?: return Result.failure(Exception("ยังไม่ได้ตั้งค่า Discord"))
        if (currentConfig.discordWebhookUrl.isBlank()) {
            return Result.failure(Exception("ยังไม่ได้ใส่ Discord Webhook URL"))
        }

        val res = discordSender.sendWarehouseAlert(
            webhookUrl = currentConfig.discordWebhookUrl,
            actionType = log.actionType,
            itemName = log.itemName,
            itemCode = log.itemCode,
            amount = log.amount,
            remainingStock = log.remainingStock,
            playerName = log.playerName,
            playerCitizenId = log.playerCitizenId,
            playerJob = log.playerJob,
            warehouseName = log.warehouseName,
            notes = log.notes,
            serverName = currentConfig.serverName
        )

        if (res.isSuccess) {
            logDao.updateDiscordStatus(log.id, true)
        }
        return res
    }

    suspend fun testDiscordWebhook(url: String): Result<String> {
        val currentConfig = configDao.getConfigDirect() ?: WarehouseConfigEntity()
        return discordSender.sendTestWebhook(url, currentConfig.serverName)
    }

    suspend fun sendFineAlert(
        targetPlayerName: String,
        targetCitizenId: String,
        offenseTitle: String,
        penaltyType: String,
        penaltyAmount: Int,
        itemUnit: String = "บาท",
        issuerAdminName: String = "ระบบ/ผู้ดูแลแก๊ง",
        notes: String = "",
        isPaidStatus: Boolean = false
    ): Result<String> {
        val currentConfig = configDao.getConfigDirect() ?: WarehouseConfigEntity()
        if (!currentConfig.isWebhookEnabled || currentConfig.discordWebhookUrl.isBlank()) {
            return Result.failure(Exception("ยังไม่ได้ตั้งค่า หรือปิดการใช้งาน Discord Webhook"))
        }
        return discordSender.sendFineAlert(
            webhookUrl = currentConfig.discordWebhookUrl,
            targetPlayerName = targetPlayerName,
            targetCitizenId = targetCitizenId,
            offenseTitle = offenseTitle,
            penaltyType = penaltyType,
            penaltyAmount = penaltyAmount,
            itemUnit = itemUnit,
            issuerAdminName = issuerAdminName,
            notes = notes,
            isPaidStatus = isPaidStatus,
            serverName = currentConfig.serverName
        )
    }

    suspend fun saveConfig(config: WarehouseConfigEntity) {
        configDao.saveConfig(config)
    }

    suspend fun clearLogs() {
        logDao.clearLogs()
    }

    // User Operations
    suspend fun addUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(id: Int) {
        userDao.deleteUser(id)
    }
}

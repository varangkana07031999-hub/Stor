package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ItemEntity
import com.example.data.UserEntity
import com.example.data.UserRole
import com.example.data.WarehouseConfigEntity
import com.example.data.WarehouseLogEntity
import com.example.data.WarehouseRepository
import com.example.data.WeeklyQuotaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WarehouseViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = WarehouseRepository(
        database.itemDao(),
        database.warehouseLogDao(),
        database.warehouseConfigDao(),
        database.userDao(),
        database.weeklyQuotaDao()
    )

    val config: StateFlow<WarehouseConfigEntity?> = repository.config.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WarehouseConfigEntity()
    )

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allWeeklyQuotas: StateFlow<List<WeeklyQuotaEntity>> = repository.allWeeklyQuotas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current active logged in user
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _inventoryViewMode = MutableStateFlow("TABLE") // "LIST" or "TABLE"
    val inventoryViewMode = _inventoryViewMode.asStateFlow()

    private val _reportTimeRange = MutableStateFlow("DAILY") // "DAILY", "WEEKLY", "MONTHLY", "ALL"
    val reportTimeRange = _reportTimeRange.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ทั้งหมด")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _logActionFilter = MutableStateFlow("ALL") // "ALL", "DEPOSIT", "WITHDRAW"
    val logActionFilter = _logActionFilter.asStateFlow()

    private val _logSearchQuery = MutableStateFlow("")
    val logSearchQuery = _logSearchQuery.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    private val _isTestingWebhook = MutableStateFlow(false)
    val isTestingWebhook = _isTestingWebhook.asStateFlow()

    private val _customCategories = MutableStateFlow(
        listOf("อาหาร/เครื่องดื่ม", "อุปกรณ์การแพทย์", "อุปกรณ์เครื่องมือ", "อาวุธ/ยุทธภัณฑ์", "วัตถุดิบ/แร่", "ของมีค่า", "ชุดเกราะ")
    )

    val rawItems: StateFlow<List<ItemEntity>> = repository.allItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories: StateFlow<List<String>> = combine(
        rawItems,
        _customCategories
    ) { items, custom ->
        val itemCats = items.map { it.category }.filter { it.isNotBlank() }
        val allCats = (listOf("ทั้งหมด") + (itemCats + custom).distinct()).filter { it.isNotBlank() }
        allCats
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("ทั้งหมด", "อาหาร/เครื่องดื่ม", "อุปกรณ์การแพทย์", "อุปกรณ์เครื่องมือ", "อาวุธ/ยุทธภัณฑ์", "วัตถุดิบ/แร่")
    )

    val rawLogs: StateFlow<List<WarehouseLogEntity>> = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredItems: StateFlow<List<ItemEntity>> = combine(
        repository.allItems,
        _searchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            val matchesCategory = (category == "ทั้งหมด") || item.category == category
            val matchesQuery = query.isBlank() ||
                    item.itemName.contains(query, ignoreCase = true) ||
                    item.itemCode.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredLogs: StateFlow<List<WarehouseLogEntity>> = combine(
        repository.allLogs,
        _logActionFilter,
        _logSearchQuery
    ) { logs, filter, query ->
        logs.filter { log ->
            val matchesFilter = (filter == "ALL") || log.actionType.equals(filter, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    log.itemName.contains(query, ignoreCase = true) ||
                    log.itemCode.contains(query, ignoreCase = true) ||
                    log.playerName.contains(query, ignoreCase = true) ||
                    log.playerCitizenId.contains(query, ignoreCase = true) ||
                    log.warehouseName.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfEmpty()
            repository.allUsers.collect { users ->
                if (_currentUser.value == null && users.isNotEmpty()) {
                    _currentUser.value = users.first()
                }
            }
        }
    }

    fun switchUser(user: UserEntity) {
        _currentUser.value = user
        _statusMessage.value = "สลับผู้ใช้งานเป็น [${user.username}] (${user.getRoleEnum().roleNameTh})"
    }

    fun setInventoryViewMode(mode: String) {
        _inventoryViewMode.value = mode
    }

    fun setReportTimeRange(range: String) {
        _reportTimeRange.value = range
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setLogActionFilter(filter: String) {
        _logActionFilter.value = filter
    }

    fun setLogSearchQuery(query: String) {
        _logSearchQuery.value = query
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun performTransaction(
        item: ItemEntity,
        actionType: String,
        amount: Int,
        playerName: String,
        playerCitizenId: String,
        playerJob: String,
        warehouseName: String,
        notes: String,
        sendDiscordImmediately: Boolean,
        timestamp: Long = System.currentTimeMillis(),
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                if (actionType == "DEPOSIT" && !user.canDeposit) {
                    _statusMessage.value = "คุณไม่มีสิทธิ์ในการนำสินค้าเข้าคลัง"
                    onComplete(false)
                    return@launch
                }
                if (actionType == "WITHDRAW" && !user.canWithdraw) {
                    _statusMessage.value = "คุณไม่มีสิทธิ์ในการเบิกสินค้าออกจากคลัง"
                    onComplete(false)
                    return@launch
                }
            }

            val result = repository.performItemTransaction(
                item = item,
                actionType = actionType,
                amount = amount,
                playerName = playerName,
                playerCitizenId = playerCitizenId,
                playerJob = playerJob,
                warehouseName = warehouseName,
                notes = notes,
                sendDiscordImmediately = sendDiscordImmediately,
                timestamp = timestamp
            )

            if (result.isSuccess) {
                val log = result.getOrNull()
                val actionMsg = if (actionType == "DEPOSIT") "นำเข้าคลัง" else "เบิกออกจากคลัง"
                var msg = "บันทึกรายการ $actionMsg (${item.itemName} x$amount) สำเร็จ"
                if (sendDiscordImmediately) {
                    msg += if (log?.discordSentSuccess == true) " + ส่ง Discord Webhook เรียบร้อย" else " (ส่ง Discord Webhook ไม่สำเร็จ)"
                }
                _statusMessage.value = msg
                onComplete(true)
            } else {
                _statusMessage.value = "เกิดข้อผิดพลาด: ${result.exceptionOrNull()?.message}"
                onComplete(false)
            }
        }
    }

    fun addNewItem(
        code: String,
        name: String,
        category: String,
        initialQty: Int,
        unit: String,
        minQty: Int,
        weight: Double,
        price: Double
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null && !user.canAddItem) {
                _statusMessage.value = "คุณไม่มีสิทธิ์เพิ่มสินค้าใหม่"
                return@launch
            }

            if (code.isBlank() || name.isBlank()) {
                _statusMessage.value = "โปรดระบุรหัสสินค้าและชื่อสินค้า"
                return@launch
            }
            val newItem = ItemEntity(
                itemCode = code.trim().lowercase(),
                itemName = name.trim(),
                category = category,
                currentQuantity = initialQty,
                unit = unit.ifBlank { "ชิ้น" },
                minQuantity = minQty,
                weight = weight,
                unitPrice = price,
                iconName = "box"
            )
            repository.addNewItem(newItem)
            _statusMessage.value = "เพิ่มสินค้าใหม่ [${newItem.itemName}] เข้าสู่คลังแล้ว"
        }
    }

    fun updateItem(item: ItemEntity) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null && !user.canEditItem) {
                _statusMessage.value = "คุณไม่มีสิทธิ์แก้ไขข้อมูลสินค้า"
                return@launch
            }
            repository.updateItem(item)
            _statusMessage.value = "อัปเดตข้อมูล [${item.itemName}] เรียบร้อยแล้ว"
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null && !user.canDeleteItem) {
                _statusMessage.value = "คุณไม่มีสิทธิ์ลบสินค้า"
                return@launch
            }
            repository.deleteItem(itemId)
            _statusMessage.value = "ลบรายการสินค้าเรียบร้อยแล้ว"
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null && !user.canDeleteItem) {
                _statusMessage.value = "คุณไม่มีสิทธิ์ลบสินค้า"
                return@launch
            }
            repository.deleteAllItems()
            _statusMessage.value = "ลบสินค้าทั้งหมดออกจากคลังเรียบร้อยแล้ว"
        }
    }

    fun addUser(username: String, citizenId: String, role: UserRole, job: String, avatarUrl: String = "") {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null && !user.canManageUsers) {
                _statusMessage.value = "คุณไม่มีสิทธิ์จัดการผู้ใช้งาน"
                return@launch
            }
            val newUser = UserEntity(
                username = username.trim(),
                citizenId = citizenId.trim(),
                role = role.name,
                job = job.trim(),
                avatarUrl = avatarUrl.trim(),
                avatarColorHex = when (role) {
                    UserRole.ADMIN -> "#D0BCFF"
                    UserRole.STAFF -> "#B2F2BB"
                    UserRole.VIEWER -> "#F2B8B5"
                }
            )
            repository.addUser(newUser)
            _statusMessage.value = "เพิ่มผู้ใช้งาน [${newUser.username}] (${role.roleNameTh}) แล้ว"
        }
    }

    fun updateUserRole(user: UserEntity, newRole: UserRole) {
        viewModelScope.launch {
            val currentUserVal = _currentUser.value
            if (currentUserVal != null && !currentUserVal.canManageUsers) {
                _statusMessage.value = "คุณไม่มีสิทธิ์เปลี่ยนบทบาทผู้ใช้"
                return@launch
            }
            val updated = user.copy(role = newRole.name)
            repository.updateUser(updated)
            if (_currentUser.value?.id == user.id) {
                _currentUser.value = updated
            }
            _statusMessage.value = "เปลี่ยนบทบาท [${user.username}] เป็น ${newRole.roleNameTh} เรียบร้อย"
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            val currentUserVal = _currentUser.value
            if (currentUserVal != null && !currentUserVal.canManageUsers) {
                _statusMessage.value = "คุณไม่มีสิทธิ์ลบผู้ใช้งาน"
                return@launch
            }
            repository.deleteUser(userId)
            _statusMessage.value = "ลบผู้ใช้งานเรียบร้อยแล้ว"
        }
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            val currentUserVal = _currentUser.value
            if (currentUserVal != null && !currentUserVal.canManageUsers && currentUserVal.id != user.id) {
                _statusMessage.value = "คุณไม่มีสิทธิ์แก้ไขข้อมูลผู้ใช้นี้"
                return@launch
            }
            repository.updateUser(user)
            if (_currentUser.value?.id == user.id) {
                _currentUser.value = user
            }
            _statusMessage.value = "อัปเดตข้อมูลผู้ใช้ [${user.username}] เรียบร้อยแล้ว"
        }
    }

    fun addCategory(newCategory: String) {
        val trimmed = newCategory.trim()
        if (trimmed.isBlank()) return
        if (!_customCategories.value.contains(trimmed)) {
            _customCategories.value = _customCategories.value + trimmed
            _statusMessage.value = "เพิ่มหมวดหมู่ [$trimmed] เรียบร้อยแล้ว"
        }
    }

    fun renameCategory(oldName: String, newName: String) {
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank() || oldName == trimmedNew) return
        viewModelScope.launch {
            val itemsToUpdate = rawItems.value.filter { it.category == oldName }
            itemsToUpdate.forEach { item ->
                repository.updateItem(item.copy(category = trimmedNew))
            }
            _customCategories.value = _customCategories.value.map { if (it == oldName) trimmedNew else it } + trimmedNew
            if (_selectedCategory.value == oldName) {
                _selectedCategory.value = trimmedNew
            }
            _statusMessage.value = "เปลี่ยนชื่อหมวดหมู่จาก [$oldName] เป็น [$trimmedNew] เรียบร้อยแล้ว"
        }
    }

    fun deleteCategory(categoryToDelete: String) {
        if (categoryToDelete == "ทั้งหมด") return
        viewModelScope.launch {
            val itemsToUpdate = rawItems.value.filter { it.category == categoryToDelete }
            itemsToUpdate.forEach { item ->
                repository.updateItem(item.copy(category = "อื่นๆ"))
            }
            _customCategories.value = _customCategories.value.filter { it != categoryToDelete }
            if (_selectedCategory.value == categoryToDelete) {
                _selectedCategory.value = "ทั้งหมด"
            }
            _statusMessage.value = "ลบหมวดหมู่ [$categoryToDelete] เรียบร้อยแล้ว"
        }
    }

    fun resendDiscordWebhook(log: WarehouseLogEntity) {
        viewModelScope.launch {
            val res = repository.resendLogToDiscord(log)
            if (res.isSuccess) {
                _statusMessage.value = "ส่ง Discord Webhook สำหรับรายการนี้สำเร็จ"
            } else {
                _statusMessage.value = "ส่ง Discord ล้มเหลว: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun testWebhook(url: String) {
        viewModelScope.launch {
            _isTestingWebhook.value = true
            val res = repository.testDiscordWebhook(url)
            _isTestingWebhook.value = false
            if (res.isSuccess) {
                _statusMessage.value = "✅ ทดสอบส่ง Webhook สำเร็จ! ได้รับข้อความใน Discord แล้ว"
            } else {
                _statusMessage.value = "❌ ทดสอบส่ง Webhook ล้มเหลว: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun saveConfig(config: WarehouseConfigEntity) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null && !user.canManageConfig) {
                _statusMessage.value = "คุณไม่มีสิทธิ์เปลี่ยนแปลงการตั้งค่าเซิร์ฟเวอร์"
                return@launch
            }
            repository.saveConfig(config)
            _statusMessage.value = "บันทึกการตั้งค่า Discord สำเร็จ"
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null && !user.canClearLogs) {
                _statusMessage.value = "คุณไม่มีสิทธิ์ล้างประวัติ (ต้องเป็น Admin เท่านั้น)"
                return@launch
            }
            repository.clearLogs()
            _statusMessage.value = "ล้างประวัติบันทึกข้อมูลเรียบร้อยแล้ว"
        }
    }

    fun saveWeeklyQuota(quota: WeeklyQuotaEntity) {
        viewModelScope.launch {
            repository.saveWeeklyQuota(quota)
            _statusMessage.value = "บันทึกรายการส่งของรายสัปดาห์เรียบร้อยแล้ว"
        }
    }

    fun sendFineNotification(
        targetPlayerName: String,
        targetCitizenId: String,
        offenseTitle: String,
        penaltyType: String,
        penaltyAmount: Int,
        itemUnit: String = "บาท",
        notes: String = "",
        isPaidStatus: Boolean = false,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val adminUser = _currentUser.value
            val issuerName = adminUser?.username ?: "ผู้ดูแลแก๊ง"
            val res = repository.sendFineAlert(
                targetPlayerName = targetPlayerName,
                targetCitizenId = targetCitizenId,
                offenseTitle = offenseTitle,
                penaltyType = penaltyType,
                penaltyAmount = penaltyAmount,
                itemUnit = itemUnit,
                issuerAdminName = issuerName,
                notes = notes,
                isPaidStatus = isPaidStatus
            )
            if (res.isSuccess) {
                _statusMessage.value = if (isPaidStatus) "✅ แจ้งเตือนการชำระค่าปรับเข้า Discord เรียบร้อย" else "🚨 แจ้งเตือนโดนค่าปรับเข้า Discord เรียบร้อย"
                onComplete(true, "ส่งเข้า Discord เรียบร้อยแล้ว")
            } else {
                val errMsg = res.exceptionOrNull()?.message ?: "ส่งแจ้งเตือนล้มเหลว"
                _statusMessage.value = "❌ ส่งแจ้งเตือน Discord ล้มเหลว: $errMsg"
                onComplete(false, errMsg)
            }
        }
    }

    fun deleteWeeklyQuota(quota: WeeklyQuotaEntity) {
        viewModelScope.launch {
            repository.deleteWeeklyQuota(quota)
            _statusMessage.value = "ลบรายการส่งของรายสัปดาห์เรียบร้อยแล้ว"
        }
    }
}

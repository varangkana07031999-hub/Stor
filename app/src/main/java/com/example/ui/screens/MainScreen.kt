package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ItemEntity
import com.example.data.UserRole
import com.example.ui.WarehouseViewModel
import com.example.ui.components.AddItemDialog
import com.example.ui.components.AppSettingsDialog
import com.example.ui.components.DepositWithdrawDialog
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: WarehouseViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val config by viewModel.config.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allWeeklyQuotas by viewModel.allWeeklyQuotas.collectAsStateWithLifecycle()
    val rawItems by viewModel.rawItems.collectAsStateWithLifecycle()
    val rawLogs by viewModel.rawLogs.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val filteredLogs by viewModel.filteredLogs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val inventoryViewMode by viewModel.inventoryViewMode.collectAsStateWithLifecycle()
    val reportTimeRange by viewModel.reportTimeRange.collectAsStateWithLifecycle()
    val logActionFilter by viewModel.logActionFilter.collectAsStateWithLifecycle()
    val logSearchQuery by viewModel.logSearchQuery.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isTestingWebhook by viewModel.isTestingWebhook.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Inventory, 1: Logs, 2: Reports, 3: Users, 4: Webhook

    var selectedItemForTransaction by remember { mutableStateOf<ItemEntity?>(null) }
    var transactionActionType by remember { mutableStateOf("DEPOSIT") }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAppSettingsDialog by remember { mutableStateOf(false) }
    var showEditWarehouseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.img_app_icon_1785091713953),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showEditWarehouseDialog = true }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = config?.defaultWarehouseName?.ifBlank { "คลังแก๊ง Teletubbies" } ?: "คลังแก๊ง Teletubbies",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "แก้ไขชื่อคลัง",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = config?.serverName ?: "FiveM Server",
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Right Side: Active User Badge & Gear Settings Icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            currentUser?.let { user ->
                                val roleEnum = user.getRoleEnum()
                                val roleColor = when (roleEnum) {
                                    UserRole.ADMIN -> CyberCyan
                                    UserRole.STAFF -> EmeraldGreen
                                    UserRole.VIEWER -> CrimsonRed
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(roleColor.copy(alpha = 0.18f))
                                        .border(1.dp, roleColor, RoundedCornerShape(20.dp))
                                        .clickable { selectedTab = 3 }
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(user = user, size = 18.dp, fallbackBgColor = roleColor)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${user.username} (${roleEnum.name})",
                                        color = roleColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { showAppSettingsDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "ตั้งค่าแอป",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDarkBg,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SlateSurface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Tab 0: Inventory
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Inventory, contentDescription = "คลังสินค้า") },
                    label = { Text("คลังสินค้า", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                // Tab 1: Logs
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (filteredLogs.isNotEmpty()) {
                                    Badge(containerColor = EmeraldGreen, contentColor = Color.Black) {
                                        Text("${filteredLogs.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.History, contentDescription = "ประวัติ")
                        }
                    },
                    label = { Text("ประวัติ", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                // Tab 2: Reports & Analytics
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.Analytics, contentDescription = "รายงานสรุป") },
                    label = { Text("รายงานสรุป", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                // Tab 3: User Management & Roles
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(imageVector = Icons.Default.Group, contentDescription = "ผู้ใช้ & สิทธิ์") },
                    label = { Text("สิทธิ์ผู้ใช้", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                // Tab 4: Discord & DB Config
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Webhook") },
                    label = { Text("Webhook", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DiscordBlurple,
                        indicatorColor = DiscordBlurple,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = SlateDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> InventoryScreen(
                    items = filteredItems,
                    currentUser = currentUser,
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    viewMode = inventoryViewMode,
                    categoriesList = categories,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onCategorySelect = { viewModel.setSelectedCategory(it) },
                    onViewModeChange = { viewModel.setInventoryViewMode(it) },
                    onDepositClick = { item ->
                        selectedItemForTransaction = item
                        transactionActionType = "DEPOSIT"
                    },
                    onWithdrawClick = { item ->
                        selectedItemForTransaction = item
                        transactionActionType = "WITHDRAW"
                    },
                    onEditItemClick = { updatedItem ->
                        viewModel.updateItem(updatedItem)
                    },
                    onDeleteItemClick = { item ->
                        viewModel.deleteItem(item.id)
                    },
                    onAddNewItemClick = { showAddItemDialog = true },
                    onDeleteAllItemsClick = { viewModel.deleteAllItems() }
                )

                1 -> LogHistoryScreen(
                    logs = filteredLogs,
                    actionFilter = logActionFilter,
                    searchQuery = logSearchQuery,
                    onFilterSelect = { viewModel.setLogActionFilter(it) },
                    onSearchQueryChange = { viewModel.setLogSearchQuery(it) },
                    onResendDiscord = { log -> viewModel.resendDiscordWebhook(log) },
                    onClearLogs = { viewModel.clearAllLogs() }
                )

                2 -> ReportScreen(
                    items = rawItems,
                    logs = rawLogs,
                    users = allUsers,
                    weeklyQuotas = allWeeklyQuotas,
                    selectedTimeRange = reportTimeRange,
                    onTimeRangeSelect = { viewModel.setReportTimeRange(it) },
                    onSaveWeeklyQuota = { viewModel.saveWeeklyQuota(it) },
                    onDeleteWeeklyQuota = { viewModel.deleteWeeklyQuota(it) },
                    onUpdateUser = { viewModel.updateUser(it) },
                    onSendFineDiscordNotification = { targetName, targetCid, title, mode, amount, unit, isPaid ->
                        viewModel.sendFineNotification(
                            targetPlayerName = targetName,
                            targetCitizenId = targetCid,
                            offenseTitle = title,
                            penaltyType = mode,
                            penaltyAmount = amount,
                            itemUnit = unit,
                            isPaidStatus = isPaid
                        )
                    }
                )

                3 -> UserManagementScreen(
                    currentUser = currentUser,
                    users = allUsers,
                    weeklyQuotas = allWeeklyQuotas,
                    weeklyLogs = rawLogs,
                    onSwitchUser = { viewModel.switchUser(it) },
                    onAddUser = { name, cid, role, job, avatarUrl -> viewModel.addUser(name, cid, role, job, avatarUrl) },
                    onUpdateRole = { user, role -> viewModel.updateUserRole(user, role) },
                    onUpdateUser = { viewModel.updateUser(it) },
                    onDeleteUser = { userId -> viewModel.deleteUser(userId) }
                )

                4 -> SettingsScreen(
                    config = config,
                    isTestingWebhook = isTestingWebhook,
                    onSaveConfig = { viewModel.saveConfig(it) },
                    onTestWebhook = { viewModel.testWebhook(it) }
                )
            }
        }
    }

    // Transaction Dialog
    selectedItemForTransaction?.let { item ->
        DepositWithdrawDialog(
            item = item,
            initialActionType = transactionActionType,
            defaultWarehouseName = config?.defaultWarehouseName ?: "คลังแก๊ง Teletubbies",
            users = allUsers,
            onDismiss = { selectedItemForTransaction = null },
            onSubmit = { actionType, amount, playerName, citizenId, job, warehouseName, notes, sendDiscord, timestamp ->
                viewModel.performTransaction(
                    item = item,
                    actionType = actionType,
                    amount = amount,
                    playerName = playerName,
                    playerCitizenId = citizenId,
                    playerJob = job,
                    warehouseName = warehouseName,
                    notes = notes,
                    sendDiscordImmediately = sendDiscord,
                    timestamp = timestamp,
                    onComplete = { selectedItemForTransaction = null }
                )
            }
        )
    }

    if (showAddItemDialog) {
        AddItemDialog(
            availableCategories = categories,
            onDismiss = { showAddItemDialog = false },
            onAddItem = { code, name, category, qty, unit, minQty, weight, price ->
                viewModel.addNewItem(code, name, category, qty, unit, minQty, weight, price)
                showAddItemDialog = false
            }
        )
    }

    if (showAppSettingsDialog) {
        AppSettingsDialog(
            currentUser = currentUser,
            config = config,
            categories = categories,
            onDismiss = { showAppSettingsDialog = false },
            onUpdateUser = { viewModel.updateUser(it) },
            onSaveConfig = { viewModel.saveConfig(it) },
            onAddCategory = { viewModel.addCategory(it) },
            onRenameCategory = { oldName, newName -> viewModel.renameCategory(oldName, newName) },
            onDeleteCategory = { viewModel.deleteCategory(it) },
            onTestWebhook = { viewModel.testWebhook(it) },
            onOpenUserManagement = { selectedTab = 3 },
            onClearLogs = { viewModel.clearAllLogs() }
        )
    }

    if (showEditWarehouseDialog) {
        var tempWarehouseName by remember(config) {
            mutableStateOf(config?.defaultWarehouseName ?: "คลังแก๊ง Teletubbies")
        }
        AlertDialog(
            onDismissRequest = { showEditWarehouseDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("✏️ แก้ไขชื่อคลังสินค้า / คลังแก๊ง", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text("กรอกชื่อคลังใหม่เพื่อใช้แสดงในระบบและประวัติบันทึก (Log):", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempWarehouseName,
                        onValueChange = { tempWarehouseName = it },
                        label = { Text("ชื่อคลังสินค้า") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateCardBorder,
                            focusedContainerColor = SlateSurface,
                            unfocusedContainerColor = SlateSurface,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedConfig = (config ?: com.example.data.WarehouseConfigEntity()).copy(
                            defaultWarehouseName = tempWarehouseName.trim().ifBlank { "คลังแก๊ง Teletubbies" }
                        )
                        viewModel.saveConfig(updatedConfig)
                        showEditWarehouseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("บันทึกชื่อคลัง", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditWarehouseDialog = false }) {
                    Text("ยกเลิก", color = TextSecondary)
                }
            },
            containerColor = SlateCardBg,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

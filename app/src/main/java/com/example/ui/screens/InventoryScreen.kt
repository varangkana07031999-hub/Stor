package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ItemEntity
import com.example.data.UserEntity
import com.example.ui.components.ItemCard
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun InventoryScreen(
    items: List<ItemEntity>,
    currentUser: UserEntity?,
    searchQuery: String,
    selectedCategory: String,
    viewMode: String, // "LIST" or "TABLE"
    categoriesList: List<String> = emptyList(),
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onViewModeChange: (String) -> Unit,
    onDepositClick: (ItemEntity) -> Unit,
    onWithdrawClick: (ItemEntity) -> Unit,
    onEditItemClick: ((ItemEntity) -> Unit)? = null,
    onDeleteItemClick: (ItemEntity) -> Unit,
    onAddNewItemClick: () -> Unit,
    onDeleteAllItemsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val categories = remember(categoriesList) {
        val defaultList = listOf("ทั้งหมด", "อาหาร/เครื่องดื่ม", "อุปกรณ์การแพทย์", "อุปกรณ์เครื่องมือ", "อาวุธ/ยุทธภัณฑ์", "วัตถุดิบ/แร่")
        if (categoriesList.isNotEmpty()) (defaultList + categoriesList).distinct() else defaultList
    }

    val currencyFormat = DecimalFormat("$#,##0")
    val numberFormat = DecimalFormat("#,##0")

    var itemToDelete by remember { mutableStateOf<ItemEntity?>(null) }
    var itemToEdit by remember { mutableStateOf<ItemEntity?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    itemToEdit?.let { item ->
        com.example.ui.components.EditItemDialog(
            item = item,
            availableCategories = categories,
            onDismiss = { itemToEdit = null },
            onSaveItem = { updatedItem ->
                onEditItemClick?.invoke(updatedItem)
                itemToEdit = null
            }
        )
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = {
                Text(
                    text = "⚠️ ยืนยันลบสินค้าทั้งหมด",
                    color = CrimsonRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "คุณต้องการลบรายการสินค้าทั้งหมด (${items.size} รายการ) ออกจากคลังแก๊งใช่หรือไม่? การกระทำนี้ไม่สามารถย้อนกลับได้",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllItemsClick?.invoke()
                        showDeleteAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ยืนยันลบทั้งหมด", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteAllConfirm = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ยกเลิก", color = TextSecondary)
                }
            },
            containerColor = SlateCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    text = "🗑️ ยืนยันการลบสินค้า",
                    color = CrimsonRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "คุณต้องการลบรายการ [${item.itemName}] (รหัส: ${item.itemCode}) ออกจากคลังสินค้าใช่หรือไม่?",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteItemClick(item)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ยืนยันลบ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { itemToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ยกเลิก", color = TextSecondary)
                }
            },
            containerColor = SlateCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Hero Banner Graphic
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .border(1.dp, SlateCardBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_warehouse_1785091728191),
                    contentDescription = "Warehouse Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "📦 คลังแก๊ง Teletubbies (Teletubbies Stash)",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "ผู้ใช้งาน: ${currentUser?.username ?: "Guest"} (${currentUser?.getRoleEnum()?.roleNameTh ?: "Viewer"})",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Read-only indicator banner if user cannot deposit/withdraw
        if (currentUser?.canDeposit == false && currentUser?.canWithdraw == false) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CrimsonRed.copy(alpha = 0.15f))
                    .border(1.dp, CrimsonRed, RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "คุณเข้าใช้งานในสิทธิ์ [Viewer - อ่านอย่างเดียว] ไม่สามารถฝาก/เบิกสินค้าได้",
                        color = CrimsonRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Compact Search Bar & View Mode Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                placeholder = { Text("ค้นหาชื่อ, รหัส, หมวดหมู่...", color = TextSecondary, fontSize = 11.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "ค้นหา", tint = CyberCyan, modifier = Modifier.size(16.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "ล้าง", tint = TextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = SlateCardBorder,
                    focusedContainerColor = SlateCardBg,
                    unfocusedContainerColor = SlateCardBg
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            // View Mode Toggle Button (Grid vs Table)
            Box(
                modifier = Modifier
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SlateCardBg)
                    .border(1.dp, SlateCardBorder, RoundedCornerShape(10.dp))
                    .clickable { onViewModeChange(if (viewMode == "TABLE") "LIST" else "TABLE") }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (viewMode == "TABLE") Icons.Default.GridView else Icons.Default.TableChart,
                        contentDescription = "เปลี่ยนมุมมอง",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (viewMode == "TABLE") "การ์ด" else "ตาราง",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (currentUser?.canAddItem == true) {
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onAddNewItemClick,
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "เพิ่มสินค้า", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "เพิ่ม", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            if (currentUser?.canDeleteItem == true && items.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = { showDeleteAllConfirm = true },
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonRed.copy(alpha = 0.2f),
                        contentColor = CrimsonRed
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "ลบทั้งหมด", modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) CyberCyan else SlateCardBg)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) CyberCyan else SlateCardBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onCategorySelect(category) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.Black else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Item View Render
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "ไม่พบรายการ",
                        tint = TextSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ไม่พบรายการสินค้าในคลัง",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "ลองเปลี่ยนการค้นหา หรือ ให้ Admin/Staff กดปุ่ม '+ เพิ่ม'",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else if (viewMode == "TABLE") {
            // Detailed Table View
            InventoryTableView(
                items = items,
                currentUser = currentUser,
                onDepositClick = onDepositClick,
                onWithdrawClick = onWithdrawClick,
                onEditItemClick = { itemToEdit = it },
                onDeleteItemClick = { itemToDelete = it }
            )
        } else {
            // Card Grid/List View
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ItemCard(
                        item = item,
                        onDepositClick = { onDepositClick(item) },
                        onWithdrawClick = { onWithdrawClick(item) },
                        onEditClick = if (currentUser?.canDeposit == true) { { itemToEdit = item } } else null,
                        onDeleteClick = if (currentUser?.canDeleteItem == true) { { itemToDelete = item } } else null
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryTableView(
    items: List<ItemEntity>,
    currentUser: UserEntity?,
    onDepositClick: (ItemEntity) -> Unit,
    onWithdrawClick: (ItemEntity) -> Unit,
    onEditItemClick: (ItemEntity) -> Unit,
    onDeleteItemClick: (ItemEntity) -> Unit
) {
    val currencyFormat = DecimalFormat("$#,##0")
    val numberFormat = DecimalFormat("#,##0")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            // Table Header Row
            Row(
                modifier = Modifier
                    .background(SlateSurface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("รหัสไอเทม", width = 110)
                TableHeaderCell("ชื่อสินค้า", width = 150)
                TableHeaderCell("หมวดหมู่", width = 120)
                TableHeaderCell("จำนวนคงเหลือ", width = 110)
                TableHeaderCell("ราคา/หน่วย", width = 100)
                TableHeaderCell("มูลค่ารวม ($)", width = 110)
                TableHeaderCell("สถานะสต็อก", width = 100)
                TableHeaderCell("การจัดการ", width = 130)
            }

            // Table Data Rows
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    val isLowStock = item.currentQuantity <= item.minQuantity
                    val totalItemValue = item.currentQuantity * item.unitPrice

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, SlateCardBorder.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Code
                        Text(
                            text = item.itemCode,
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(110.dp)
                        )

                        // Name
                        Text(
                            text = item.itemName,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(150.dp)
                        )

                        // Category
                        Text(
                            text = item.category,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.width(120.dp)
                        )

                        // Stock Qty
                        Text(
                            text = "${numberFormat.format(item.currentQuantity)} ${item.unit}",
                            color = if (isLowStock) CrimsonRed else EmeraldGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.width(110.dp)
                        )

                        // Price / Unit
                        Text(
                            text = currencyFormat.format(item.unitPrice),
                            color = TextPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.width(100.dp)
                        )

                        // Total Value
                        Text(
                            text = currencyFormat.format(totalItemValue),
                            color = AmberOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(110.dp)
                        )

                        // Stock Status Pill
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLowStock) CrimsonRed.copy(alpha = 0.2f) else EmeraldGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLowStock) "⚠️ สต็อกต่ำ" else "ปกติ",
                                color = if (isLowStock) CrimsonRed else EmeraldGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Actions (Deposit / Withdraw / Edit / Delete)
                        Row(
                            modifier = Modifier.width(130.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (currentUser?.canDeposit == true) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldGreen)
                                        .clickable { onDepositClick(item) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "ฝาก", tint = Color.Black, modifier = Modifier.size(12.dp))
                                }
                            }

                            if (currentUser?.canWithdraw == true) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CrimsonRed)
                                        .clickable { onWithdrawClick(item) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "เบิก", tint = Color.Black, modifier = Modifier.size(12.dp))
                                }
                            }

                            if (currentUser?.canDeposit == true) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberCyan)
                                        .clickable { onEditItemClick(item) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "แก้ไข", tint = Color.Black, modifier = Modifier.size(12.dp))
                                }
                            }

                            if (currentUser?.canDeleteItem == true) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SlateCardBorder)
                                        .clickable { onDeleteItemClick(item) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "ลบ", tint = CrimsonRed, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(title: String, width: Int) {
    Text(
        text = title,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.width(width.dp)
    )
}

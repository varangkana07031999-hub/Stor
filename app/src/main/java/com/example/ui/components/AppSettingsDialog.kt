package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.UserEntity
import com.example.data.WarehouseConfigEntity
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppSettingsDialog(
    currentUser: UserEntity?,
    config: WarehouseConfigEntity?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onUpdateUser: (UserEntity) -> Unit,
    onSaveConfig: (WarehouseConfigEntity) -> Unit,
    onAddCategory: (String) -> Unit,
    onRenameCategory: (oldName: String, newName: String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onTestWebhook: (String) -> Unit,
    onOpenUserManagement: () -> Unit,
    onClearLogs: () -> Unit
) {
    var adminUsername by remember(currentUser) { mutableStateOf(currentUser?.username ?: "") }
    var adminCitizenId by remember(currentUser) { mutableStateOf(currentUser?.citizenId ?: "") }
    var adminJob by remember(currentUser) { mutableStateOf(currentUser?.job ?: "") }

    var webhookUrl by remember(config) { mutableStateOf(config?.discordWebhookUrl ?: "") }
    var serverName by remember(config) { mutableStateOf(config?.serverName ?: "Teletubbies City") }
    var defaultWarehouseName by remember(config) { mutableStateOf(config?.defaultWarehouseName ?: "คลังแก๊ง Teletubbies") }
    var isWebhookEnabled by remember(config) { mutableStateOf(config?.isWebhookEnabled ?: true) }

    var newCatName by remember { mutableStateOf("") }
    var categoryToEdit by remember { mutableStateOf<String?>(null) }
    var editCatNewName by remember { mutableStateOf("") }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var showClearLogsConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateCardBorder, RoundedCornerShape(22.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "ตั้งค่า",
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚙️ ศูนย์รวมตั้งค่าแอปทั้งหมด",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Edit Admin Profile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "👑 แก้ไขชื่อแอดมิน / ผู้ใช้งานปัจจุบัน", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "ชื่อแอดมิน (Display Name):", color = TextPrimary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = adminUsername,
                            onValueChange = { adminUsername = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SlateCardBorder,
                                focusedContainerColor = SlateSurface,
                                unfocusedContainerColor = SlateSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "เบอร์มือถือ:", color = TextPrimary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = adminCitizenId,
                                    onValueChange = { adminCitizenId = it },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyberCyan,
                                        unfocusedBorderColor = SlateCardBorder,
                                        focusedContainerColor = SlateSurface,
                                        unfocusedContainerColor = SlateSurface
                                    )
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "ตำแหน่ง:", color = TextPrimary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = adminJob,
                                    onValueChange = { adminJob = it },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyberCyan,
                                        unfocusedBorderColor = SlateCardBorder,
                                        focusedContainerColor = SlateSurface,
                                        unfocusedContainerColor = SlateSurface
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                currentUser?.let { usr ->
                                    if (adminUsername.isNotBlank()) {
                                        onUpdateUser(usr.copy(username = adminUsername.trim(), citizenId = adminCitizenId.trim(), job = adminJob.trim()))
                                    }
                                }
                            },
                            enabled = adminUsername.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                        ) {
                            Text("บันทึกชื่อแอดมิน (Save Admin Name)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: Category Management
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "📁 จัดการหมวดหมู่สินค้า", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "หมวดหมู่ทั้งหมดในระบบ (คลิก ✏️ แก้ไข หรือ 🗑️ ลบ):", color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        val catsFiltered = categories.filter { it != "ทั้งหมด" }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            catsFiltered.forEach { cat ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateSurface)
                                        .border(1.dp, SlateCardBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = cat, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "แก้ไข",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(14.dp).clickable {
                                            categoryToEdit = cat
                                            editCatNewName = cat
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "ลบ",
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(14.dp).clickable {
                                            categoryToDelete = cat
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newCatName,
                                onValueChange = { newCatName = it },
                                placeholder = { Text("พิมพ์ชื่อหมวดหมู่ใหม่...", color = TextSecondary, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).height(42.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = SlateCardBorder,
                                    focusedContainerColor = SlateSurface,
                                    unfocusedContainerColor = SlateSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (newCatName.isNotBlank()) {
                                        onAddCategory(newCatName)
                                        newCatName = ""
                                    }
                                },
                                enabled = newCatName.isNotBlank(),
                                modifier = Modifier.height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "เพิ่ม", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("เพิ่ม", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 3: Discord Webhook & Server
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DiscordBlurple.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = DiscordBlurple, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "🔔 แจ้งเตือน Discord Webhook", color = DiscordBlurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Switch(
                                checked = isWebhookEnabled,
                                onCheckedChange = { isWebhookEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DiscordBlurple)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "ชื่อคลังสินค้า / คลังแก๊ง (Default Warehouse Name):", color = TextPrimary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = defaultWarehouseName,
                            onValueChange = { defaultWarehouseName = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SlateCardBorder,
                                focusedContainerColor = SlateSurface,
                                unfocusedContainerColor = SlateSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "ชื่อเซิร์ฟเวอร์ FiveM / แก๊ง:", color = TextPrimary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = serverName,
                            onValueChange = { serverName = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DiscordBlurple,
                                unfocusedBorderColor = SlateCardBorder,
                                focusedContainerColor = SlateSurface,
                                unfocusedContainerColor = SlateSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Discord Webhook URL:", color = TextPrimary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = webhookUrl,
                            onValueChange = { webhookUrl = it },
                            placeholder = { Text("https://discord.com/api/webhooks/...", color = TextSecondary, fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DiscordBlurple,
                                unfocusedBorderColor = SlateCardBorder,
                                focusedContainerColor = SlateSurface,
                                unfocusedContainerColor = SlateSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onTestWebhook(webhookUrl) },
                                enabled = webhookUrl.isNotBlank(),
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DiscordBlurple),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscordBlurple)
                            ) {
                                Text("🧪 ทดสอบ Webhook", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val newCfg = (config ?: WarehouseConfigEntity()).copy(
                                        serverName = serverName.trim(),
                                        defaultWarehouseName = defaultWarehouseName.trim(),
                                        discordWebhookUrl = webhookUrl.trim(),
                                        isWebhookEnabled = isWebhookEnabled
                                    )
                                    onSaveConfig(newCfg)
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple, contentColor = Color.White)
                            ) {
                                Text("บันทึกการตั้งค่า", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 4: Gang Roster & Permissions
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = AmberOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = "👥 รายชื่อและสิทธิ์สมาชิกแก๊ง", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "เพิ่ม แก้ไข ลบ หรือเปลี่ยนสิทธิ์ Admin/Staff", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                onOpenUserManagement()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberOrange, contentColor = Color.Black)
                        ) {
                            Text("จัดการแก๊ง", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 5: Clear Logs Action
                if (currentUser?.canClearLogs == true) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CrimsonRed.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "🧹 ล้างประวัติบันทึกข้อมูล", color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "ลบประวัติการฝาก/เบิกทั้งหมดออก", color = TextSecondary, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { showClearLogsConfirm = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White)
                            ) {
                                Text("ล้างประวัติ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Category Edit Dialog
    categoryToEdit?.let { oldCat ->
        AlertDialog(
            onDismissRequest = { categoryToEdit = null },
            title = { Text("✏️ แก้ไขชื่อหมวดหมู่", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("เปลี่ยนชื่อหมวดหมู่ [$oldCat] เป็นชื่อใหม่:", color = TextPrimary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editCatNewName,
                        onValueChange = { editCatNewName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateCardBorder,
                            focusedContainerColor = SlateCardBg,
                            unfocusedContainerColor = SlateCardBg
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editCatNewName.isNotBlank()) {
                            onRenameCategory(oldCat, editCatNewName)
                            categoryToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text("บันทึกชื่อใหม่", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { categoryToEdit = null }) { Text("ยกเลิก", color = TextSecondary) }
            },
            containerColor = SlateSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Category Delete Dialog
    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("🗑️ ยืนยันลบหมวดหมู่", color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    text = "คุณต้องการลบหมวดหมู่ [$cat] ใช่หรือไม่? สินค้าในหมวดนี้จะถูกเปลี่ยนเป็นหมวด 'อื่นๆ' โดยอัตโนมัติ",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(cat)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White)
                ) {
                    Text("ยืนยันลบ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { categoryToDelete = null }) { Text("ยกเลิก", color = TextSecondary) }
            },
            containerColor = SlateSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Clear Logs Confirm Dialog
    if (showClearLogsConfirm) {
        AlertDialog(
            onDismissRequest = { showClearLogsConfirm = false },
            title = { Text("⚠️ ยืนยันล้างประวัติ", color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("คุณต้องการลบประวัติการฝาก/เบิกไอเทมทั้งหมดออกใช่หรือไม่?", color = TextPrimary, fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearLogs()
                        showClearLogsConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White)
                ) {
                    Text("ยืนยันล้างประวัติ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearLogsConfirm = false }) { Text("ยกเลิก", color = TextSecondary) }
            },
            containerColor = SlateSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

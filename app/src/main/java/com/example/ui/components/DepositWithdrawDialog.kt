package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import com.example.data.UserEntity
import com.example.data.ItemEntity
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DepositWithdrawDialog(
    item: ItemEntity,
    initialActionType: String, // "DEPOSIT" or "WITHDRAW"
    defaultWarehouseName: String,
    users: List<UserEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (actionType: String, amount: Int, playerName: String, citizenId: String, job: String, warehouseName: String, notes: String, sendDiscord: Boolean, timestamp: Long) -> Unit
) {
    val context = LocalContext.current
    var actionType by remember { mutableStateOf(initialActionType) }
    var amountText by remember { mutableStateOf("1") }
    
    val firstUser = users.firstOrNull()
    var playerName by remember { mutableStateOf(firstUser?.username ?: "สมชาย สายลุย") }
    var citizenId by remember { mutableStateOf(firstUser?.citizenId ?: "081-234-5678") }
    var selectedJob by remember { mutableStateOf(firstUser?.job ?: "สมาชิกแก๊ง") }
    var warehouseName by remember { mutableStateOf(defaultWarehouseName.ifBlank { "คลังแก๊ง Teletubbies" }) }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var notes by remember { mutableStateOf("") }
    var sendDiscord by remember { mutableStateOf(true) }

    val isDeposit = actionType == "DEPOSIT"
    val isFinePayment = actionType == "FINE_DEPOSIT"
    val themeColor = when {
        isFinePayment -> Color(0xFFFFB300) // Amber/Gold
        isDeposit -> EmeraldGreen
        else -> CrimsonRed
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm น.", Locale("th", "TH")) }

    val openDateTimePicker = {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        cal.set(Calendar.MINUTE, minute)
                        cal.set(Calendar.SECOND, 0)
                        selectedTimestamp = cal.timeInMillis
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val jobsList = listOf(
        "ตำรวจ (Police)",
        "หน่วยแพทย์ (EMS)",
        "ช่างซ่อมรถ (Mechanic)",
        "สภา/รัฐบาล (Government)",
        "ประชาชนทั่วไป (Civilian)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateCardBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when {
                                isFinePayment -> "💰 ส่งชำระค่าปรับเข้าคลัง"
                                isDeposit -> "📥 ฝากของเข้าคลัง FiveM"
                                else -> "📤 เบิกของออกจากคลัง"
                            },
                            color = themeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${item.itemName} (${item.itemCode})",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "ปิด",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Selector Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateCardBg)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (actionType == "DEPOSIT") EmeraldGreen else Color.Transparent)
                            .clickable { actionType = "DEPOSIT" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📥 ฝากเข้า",
                            color = if (actionType == "DEPOSIT") Color.Black else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (actionType == "WITHDRAW") CrimsonRed else Color.Transparent)
                            .clickable { actionType = "WITHDRAW" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📤 เบิกออก",
                            color = if (actionType == "WITHDRAW") Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (actionType == "FINE_DEPOSIT") Color(0xFFFFB300) else Color.Transparent)
                            .clickable { actionType = "FINE_DEPOSIT" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💰 ชำระค่าปรับ",
                            color = if (actionType == "FINE_DEPOSIT") Color.Black else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Counter Input
                Text(
                    text = "จำนวนที่ต้องการ${if (isDeposit) "ฝาก" else "เบิก"} (คงเหลือในคลัง: ${item.currentQuantity} ${item.unit}):",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            val current = amountText.toIntOrNull() ?: 1
                            if (current > 1) amountText = (current - 1).toString()
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SlateCardBg)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "ลบ", tint = TextPrimary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColor,
                            unfocusedBorderColor = SlateCardBorder,
                            focusedContainerColor = SlateCardBg,
                            unfocusedContainerColor = SlateCardBg
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val current = amountText.toIntOrNull() ?: 0
                            amountText = (current + 1).toString()
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SlateCardBg)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "บวก", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gang Member Quick Select
                if (users.isNotEmpty()) {
                    Text(
                        text = "👥 เลือกสมาชิกในแก๊ง (เลือกแล้วระบบกรอกให้อัตโนมัติ):",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        users.forEach { user ->
                            val isSelected = playerName == user.username
                            val userColor = try {
                                Color(android.graphics.Color.parseColor(user.avatarColorHex))
                            } catch (e: Exception) {
                                CyberCyan
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) userColor else SlateCardBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) Color.White else SlateCardBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        playerName = user.username
                                        citizenId = user.citizenId
                                        selectedJob = user.job
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.Black else userColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = user.username,
                                        color = if (isSelected) Color.Black else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Player Name Field
                Text(text = "ชื่อผู้ทำรายการ (Player Name):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone Number & Job Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "เบอร์มือถือ:", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = citizenId,
                            onValueChange = { citizenId = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SlateCardBorder,
                                focusedContainerColor = SlateCardBg,
                                unfocusedContainerColor = SlateCardBg
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(text = "ตำแหน่ง/สังกัด:", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = selectedJob,
                            onValueChange = { selectedJob = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SlateCardBorder,
                                focusedContainerColor = SlateCardBg,
                                unfocusedContainerColor = SlateCardBg
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Warehouse Name
                Text(text = "ชื่อคลังเก็บของ (Warehouse Stash Name):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = warehouseName,
                    onValueChange = { warehouseName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date & Time Picker
                Text(
                    text = if (isDeposit) "วัน/เวลาที่ทำรายการฝาก:" else "วัน/เวลาที่ทำรายการเบิก:",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateCardBg)
                            .border(1.dp, SlateCardBorder, RoundedCornerShape(8.dp))
                            .clickable { openDateTimePicker() }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormatter.format(Date(selectedTimestamp)),
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "เลือกวันเวลา",
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { selectedTimestamp = System.currentTimeMillis() },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SlateCardBorder),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Text("ปัจจุบัน", fontSize = 11.sp, color = CyberCyan)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                Text(text = "หมายเหตุ (Optional):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("เช่น เบิกเพื่อเข้าเวรระงับเหตุ", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Send Discord Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DiscordBlurple.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = sendDiscord,
                        onCheckedChange = { sendDiscord = it },
                        colors = CheckboxDefaults.colors(checkedColor = DiscordBlurple)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ส่งการแจ้งเตือนเข้า Discord Webhook ทันที",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        val amt = amountText.toIntOrNull() ?: 1
                        val finalNotes = if (isFinePayment && !notes.contains("ชำระค่าปรับ")) "[ชำระค่าปรับ] ${notes.trim()}".trim() else notes
                        onSubmit(
                            actionType,
                            amt,
                            playerName.ifBlank { "Unidentified" },
                            citizenId,
                            selectedJob,
                            warehouseName,
                            finalNotes,
                            sendDiscord,
                            selectedTimestamp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColor,
                        contentColor = if (isDeposit || isFinePayment) Color.Black else Color.White
                    )
                ) {
                    Text(
                        text = when {
                            isFinePayment -> "💰 ยืนยันส่งชำระค่าปรับ (Fine Settlement)"
                            isDeposit -> "ยืนยันนำของเข้าคลัง (Deposit)"
                            else -> "ยืนยันเบิกของออก (Withdraw)"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

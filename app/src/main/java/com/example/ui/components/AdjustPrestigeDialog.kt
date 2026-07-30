package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.PrestigeHelper
import com.example.data.UserEntity
import com.example.data.WarehouseLogEntity
import com.example.data.WeeklyQuotaEntity
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdjustPrestigeDialog(
    user: UserEntity,
    weeklyQuotas: List<WeeklyQuotaEntity> = emptyList(),
    weeklyLogs: List<WarehouseLogEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (updatedUser: UserEntity) -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val autoExceededPoints = remember(user, weeklyQuotas, weeklyLogs) {
        PrestigeHelper.calculateAutoExceededPoints(user, weeklyQuotas, weeklyLogs)
    }

    var manualPointsInput by remember(user) { mutableStateOf(user.manualPrestigePoints.toString()) }
    var noteInput by remember { mutableStateOf("") }

    val currentManual = manualPointsInput.toIntOrNull() ?: user.manualPrestigePoints
    val totalPreview = currentManual + autoExceededPoints
    val rankInfo = PrestigeHelper.getRankInfo(totalPreview)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, rankInfo.color.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "แต้มบารมี",
                            tint = rankInfo.color,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ จัดการแต้มบารมีสมาชิก",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // User Info Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(user = user, size = 42.dp, fallbackBgColor = rankInfo.color)

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = user.username,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "เบอร์: ${user.citizenId} • ${user.job}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(rankInfo.color.copy(alpha = 0.2f))
                                        .border(0.5.dp, rankInfo.color, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${rankInfo.fullLevelName} • ${formatter.format(totalPreview)} แต้ม",
                                        color = rankInfo.color,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Score Breakdown Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "🟣 แต้มส่งเกินเป้าอัตโนมัติ (Auto):", color = TextSecondary, fontSize = 11.sp)
                            Text(text = "+${formatter.format(autoExceededPoints)} แต้ม", color = Color(0xFFAB47BC), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "✏️ แต้มปรับมือ / พิเศษ (Manual):", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = if (currentManual >= 0) "+${formatter.format(currentManual)} แต้ม" else "${formatter.format(currentManual)} แต้ม",
                                color = if (currentManual >= 0) EmeraldGreen else CrimsonRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SlateCardBorder))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✨ รวมแต้มบารมีสุทธิ:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                text = "${formatter.format(totalPreview)} แต้ม",
                                color = rankInfo.color,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Adjustment Buttons
                Text(text = "⚡ ปุ่มลัดเพิ่ม/หัก แต้มปรับมือ (Manual Delta):", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(10, 50, 100).forEach { delta ->
                        OutlinedButton(
                            onClick = {
                                val newVal = currentManual + delta
                                manualPointsInput = newVal.toString()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Text("+${delta}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    listOf(10, 50).forEach { delta ->
                        OutlinedButton(
                            onClick = {
                                val newVal = currentManual - delta
                                manualPointsInput = newVal.toString()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Text("-${delta}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Manual Input
                Text(text = "กรอกแต้มปรับมือโดยตรง ( Manual Points):", color = TextPrimary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = manualPointsInput,
                    onValueChange = { input ->
                        // Allow digits and optional leading minus sign
                        if (input.isEmpty() || input == "-" || input.toIntOrNull() != null) {
                            manualPointsInput = input
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = rankInfo.color,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "หมายเหตุ / เหตุผลการปรับแต้ม (Optional):", color = TextPrimary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("เช่น โบนัสช่วยงานวอร์แก๊ง / กิจกรรมประจำสัปดาห์", color = TextSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = rankInfo.color,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        onSave(user.copy(manualPrestigePoints = currentManual))
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = rankInfo.color, contentColor = Color.Black)
                ) {
                    Text(text = "บันทึกแต้มบารมี (${formatter.format(totalPreview)} แต้ม)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

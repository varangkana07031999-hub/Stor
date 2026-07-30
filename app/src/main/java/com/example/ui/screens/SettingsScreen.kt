package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.example.data.WarehouseConfigEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    config: WarehouseConfigEntity?,
    isTestingWebhook: Boolean,
    onSaveConfig: (WarehouseConfigEntity) -> Unit,
    onTestWebhook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var webhookUrl by remember(config) { mutableStateOf(config?.discordWebhookUrl ?: "") }
    var isEnabled by remember(config) { mutableStateOf(config?.isWebhookEnabled ?: true) }
    var serverName by remember(config) { mutableStateOf(config?.serverName ?: "FiveM Thailand City RP") }
    var defaultWarehouse by remember(config) { mutableStateOf(config?.defaultWarehouseName ?: "คลังแก๊ง Teletubbies") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "⚙️ ตั้งค่า Discord Webhook Notifications",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "กำหนดค่าการส่งการแจ้งเตือนและประวัติกิจกรรมเข้า Discord",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Discord Webhook Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Discord Webhook",
                            tint = DiscordBlurple,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Discord Webhook Notification",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DiscordBlurple
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Discord Webhook URL:", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = webhookUrl,
                    onValueChange = { webhookUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://discord.com/api/webhooks/...", color = TextSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiscordBlurple,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "ชื่อเซิร์ฟเวอร์ FiveM (Server Name):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "ชื่อคลังเก็บของเริ่มต้น (Default Stash):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = defaultWarehouse,
                    onValueChange = { defaultWarehouse = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Test Webhook Button
                OutlinedButton(
                    onClick = { onTestWebhook(webhookUrl) },
                    enabled = webhookUrl.isNotBlank() && !isTestingWebhook,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DiscordBlurple),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscordBlurple)
                ) {
                    if (isTestingWebhook) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DiscordBlurple, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "กำลังทดสอบส่ง Webhook...", fontSize = 13.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "ทดสอบ", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "🧪 ทดสอบส่ง Discord Webhook", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button
        Button(
            onClick = {
                onSaveConfig(
                    WarehouseConfigEntity(
                        id = 1,
                        discordWebhookUrl = webhookUrl.trim(),
                        isWebhookEnabled = isEnabled,
                        serverName = serverName.trim(),
                        defaultWarehouseName = defaultWarehouse.trim(),
                        isRealtimeSync = true
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldGreen,
                contentColor = Color.Black
            )
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "บันทึก", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "บันทึกการตั้งค่าทั้งหมด (Save Settings)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WarehouseLogEntity
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogCard(
    log: WarehouseLogEntity,
    onResendDiscord: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDeposit = log.actionType.equals("DEPOSIT", ignoreCase = true)
    val isFinePayment = log.actionType.equals("FINE_DEPOSIT", ignoreCase = true) || log.actionType.equals("FINE_PAYMENT", ignoreCase = true) || log.notes.contains("ชำระค่าปรับ", ignoreCase = true)
    val isFine = log.actionType.equals("FINE", ignoreCase = true) || log.actionType.equals("PENALTY", ignoreCase = true)

    val actionColor = when {
        isFinePayment -> Color(0xFFFFB300) // Gold
        isFine -> CrimsonRed
        isDeposit -> EmeraldGreen
        else -> CrimsonRed
    }

    val actionTitle = when {
        isFinePayment -> "💰 ชำระค่าปรับ"
        isFine -> "🚨 โดนค่าปรับ"
        isDeposit -> "📥 นำเข้าคลัง"
        else -> "📤 เบิกออกจากคลัง"
    }

    val actionIcon = when {
        isFinePayment -> Icons.Default.CheckCircle
        isFine -> Icons.Default.Error
        isDeposit -> Icons.Default.ArrowDownward
        else -> Icons.Default.ArrowUpward
    }

    val amountSign = when {
        isFinePayment -> "💰 "
        isFine -> "🚨 "
        isDeposit -> "+"
        else -> "-"
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss น.", Locale("th", "TH"))
    val formattedTime = dateFormat.format(Date(log.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SlateCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateCardBg),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Action Badge + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(actionColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = actionTitle,
                            tint = actionColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = actionTitle,
                        color = actionColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = formattedTime,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Info: Item Name & Quantity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = log.itemName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "รหัส: ${log.itemCode} | คลัง: ${log.warehouseName}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$amountSign${log.amount} ชิ้น",
                        color = actionColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "คงเหลือ: ${log.remainingStock}",
                        color = CyberCyan,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player & Job Detail
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "ผู้ทำรายการ",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${log.playerName} (เบอร์: ${log.playerCitizenId.ifBlank { "N/A" }})",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (log.playerJob.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = "อาชีพ",
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = log.playerJob,
                            color = CyberCyan,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (log.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📝 หมายเหตุ: ${log.notes}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Discord Webhook Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (log.discordSentSuccess) DiscordBlurple.copy(alpha = 0.15f)
                        else CrimsonRed.copy(alpha = 0.1f)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (log.discordSentSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = "Discord Status",
                        tint = if (log.discordSentSuccess) DiscordBlurple else CrimsonRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (log.discordSentSuccess) "ส่งแจ้งเตือน Discord Webhook เรียบร้อย" else "ยังไม่ได้ส่ง / Webhook ล้มเหลว",
                        color = if (log.discordSentSuccess) TextPrimary else CrimsonRed,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onResendDiscord,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "ส่ง Discord ซ้ำ",
                        tint = DiscordBlurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

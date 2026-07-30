package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.ItemEntity
import com.example.data.UserEntity
import com.example.data.WarehouseLogEntity
import com.example.data.WeeklyQuotaEntity
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateCardBg
import com.example.data.PrestigeHelper
import com.example.ui.components.AdjustPrestigeDialog
import com.example.ui.components.UserAvatar
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Data model for tracking fine / penalty item payment progress per user
// Data model for custom individual offenses / penalties
data class CustomOffenseData(
    val id: String = java.util.UUID.randomUUID().toString(),
    val username: String,
    val title: String,          // e.g. "ทำรถแก๊งพัง"
    val penaltyType: String,     // "MONEY", "ITEM", "ACTIVITY"
    val penaltyAmount: Double,  // e.g. 5000.0
    val penaltyUnit: String,    // "บาท", "ชิ้น", "รอบ"
    val itemOrNote: String,     // e.g. "แผ่นเหล็กกล้า" or description
    var isPaid: Boolean = false
)

data class FinePaymentData(
    val user: UserEntity,
    val quota: WeeklyQuotaEntity,
    val missingQty: Int,
    val requiredPenalty: Double,
    val penaltyUnit: String,
    val penaltyType: String,
    val penaltyNote: String,
    val currentPaid: Double
)

@Composable
fun ReportScreen(
    items: List<ItemEntity>,
    logs: List<WarehouseLogEntity>,
    users: List<UserEntity>,
    weeklyQuotas: List<WeeklyQuotaEntity>,
    selectedTimeRange: String, // "DAILY", "WEEKLY", "MONTHLY", "YEARLY", "ALL"
    onTimeRangeSelect: (String) -> Unit,
    onSaveWeeklyQuota: (WeeklyQuotaEntity) -> Unit,
    onDeleteWeeklyQuota: (WeeklyQuotaEntity) -> Unit,
    onUpdateUser: ((UserEntity) -> Unit)? = null,
    onSendFineDiscordNotification: ((targetPlayerName: String, targetCitizenId: String, title: String, mode: String, amount: Int, unit: String, isPaid: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val formatter = DecimalFormat("#,##0")
    val currencyFormatter = DecimalFormat("฿#,##0.00")

    var selectedViewMode by remember { mutableStateOf("CHARTS") } // "CHARTS" or "WEEKLY_QUOTA"
    var chartDisplayType by remember { mutableStateOf("BAR") } // "BAR", "LINE", "PIE"
    var selectedPieTab by remember { mutableStateOf("CATEGORY") } // "CATEGORY", "MEMBER", "RATIO"
    var showQuotaConfigDialog by remember { mutableStateOf(false) }
    var quotaToEdit by remember { mutableStateOf<WeeklyQuotaEntity?>(null) }
    var showEditSingleQuotaDialog by remember { mutableStateOf(false) }
    var forceOverdueMode by remember { mutableStateOf(false) }
    val userFineOverrides = remember { mutableStateMapOf<String, Double>() }
    val userPenaltyNoteOverrides = remember { mutableStateMapOf<String, String>() }
    val userFineItemPaidMap = remember { mutableStateMapOf<String, Double>() }
    val customOffensesList = remember { mutableStateListOf<CustomOffenseData>() }
    var userToEditPenalty by remember { mutableStateOf<Pair<UserEntity, Double>?>(null) }
    var fineItemToPay by remember { mutableStateOf<FinePaymentData?>(null) }
    var userForPrestige by remember { mutableStateOf<UserEntity?>(null) }

    // Filter logs based on selected time range
    val now = System.currentTimeMillis()
    val timeLimit = when (selectedTimeRange) {
        "DAILY" -> now - (24 * 60 * 60 * 1000L)
        "WEEKLY" -> now - (7 * 24 * 60 * 60 * 1000L)
        "MONTHLY" -> now - (30L * 24 * 60 * 60 * 1000L)
        "YEARLY" -> now - (365L * 24 * 60 * 60 * 1000L)
        else -> 0L
    }

    val filteredLogs = logs.filter { it.timestamp >= timeLimit }
    val depositLogs = filteredLogs.filter { it.actionType.equals("DEPOSIT", ignoreCase = true) }
    val withdrawLogs = filteredLogs.filter { it.actionType.equals("WITHDRAW", ignoreCase = true) }

    val totalDepositedQty = depositLogs.sumOf { it.amount }
    val totalWithdrawnQty = withdrawLogs.sumOf { it.amount }

    val totalStockQty = items.sumOf { it.currentQuantity }
    val totalValuation = items.sumOf { it.currentQuantity * it.unitPrice }

    val categoryGroup = items.groupBy { it.category }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Screen Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📊 รายงานวิเคราะห์ & เช็คส่งของแก๊ง",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "กราฟสรุปภาพรวมคลังสินค้า และระบบเช็คส่งของสมาชิกรายอาทิตย์",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // View Mode Switcher Pills
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
                    .background(if (selectedViewMode == "CHARTS") CyberCyan else Color.Transparent)
                    .clickable { selectedViewMode = "CHARTS" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = if (selectedViewMode == "CHARTS") Color.Black else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "กราฟ & รายงานภาพรวม",
                        color = if (selectedViewMode == "CHARTS") Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedViewMode == "WEEKLY_QUOTA") EmeraldGreen else Color.Transparent)
                    .clickable { selectedViewMode = "WEEKLY_QUOTA" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null,
                        tint = if (selectedViewMode == "WEEKLY_QUOTA") Color.Black else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "เช็คส่งของรายอาทิตย์",
                        color = if (selectedViewMode == "WEEKLY_QUOTA") Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedViewMode == "CHARTS") {
            // MODE 1: CHARTS & OVERVIEW
            // Time Range Filter Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateCardBg)
                    .padding(4.dp)
            ) {
                val ranges = listOf(
                    "DAILY" to "รายวัน",
                    "WEEKLY" to "7 วัน",
                    "MONTHLY" to "รายเดือน",
                    "YEARLY" to "รายปี",
                    "ALL" to "ทั้งหมด"
                )

                ranges.forEach { (key, label) ->
                    val isSelected = selectedTimeRange == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CyberCyan else Color.Transparent)
                            .clickable { onTimeRangeSelect(key) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Metric Cards 2x2 Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Total Deposited
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "นำเข้าคลัง", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${formatter.format(totalDepositedQty)} ชิ้น", color = EmeraldGreen, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = "${depositLogs.size} รายการทำรายการ", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                // Card 2: Total Withdrawn
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CrimsonRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "เบิกออกจากคลัง", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${formatter.format(totalWithdrawnQty)} ชิ้น", color = CrimsonRed, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = "${withdrawLogs.size} รายการทำรายการ", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 3: Total Inventory Stock
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "คงเหลือรวม", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${formatter.format(totalStockQty)} ชิ้น", color = CyberCyan, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = "ทั้งหมด ${items.size} ชนิดสินค้า", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                // Card 4: Total Inventory Valuation
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberOrange.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, tint = AmberOrange, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "มูลค่ารวมในคลัง", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = currencyFormatter.format(totalValuation), color = AmberOrange, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = "คำนวณตามราคาต่อหน่วย", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Visual Custom Canvas Chart: Comparative Deposit vs Withdraw Chart (Bar / Line / Pie)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (chartDisplayType) {
                                    "LINE" -> Icons.Default.ShowChart
                                    "PIE" -> Icons.Default.PieChart
                                    else -> Icons.Default.BarChart
                                },
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "กราฟเปรียบเทียบการฝาก - เบิก", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        // Chart Legend
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(EmeraldGreen))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ฝาก", color = TextSecondary, fontSize = 10.sp)

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CrimsonRed))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("เบิก", color = TextSecondary, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chart Display Type Selector Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateSurface)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "BAR" to "📊 แท่ง",
                            "LINE" to "📈 เส้น",
                            "PIE" to "🍩 วงกลม"
                        ).forEach { (type, label) ->
                            val isSelected = chartDisplayType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CyberCyan else Color.Transparent)
                                    .clickable { chartDisplayType = type }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Build Chart Data Buckets based on selectedTimeRange
                    val chartData = remember(filteredLogs, selectedTimeRange) {
                        buildTransactionChartData(filteredLogs, selectedTimeRange)
                    }

                    if (chartData.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = SlateSurface.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, SlateCardBorder, RoundedCornerShape(12.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_empty_chart),
                                        contentDescription = "No chart data graphic",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Gradient overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        SlateCardBg.copy(alpha = 0.7f),
                                                        SlateCardBg
                                                    )
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(CyberCyan.copy(alpha = 0.25f))
                                                .border(1.dp, CyberCyan, CircleShape)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Analytics,
                                                    contentDescription = null,
                                                    tint = CyberCyan,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "ไม่พบประวัติการทำรายการ",
                                                    color = CyberCyan,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ยังไม่มีข้อมูลทำรายการฝาก-เบิกในช่วงเวลา $selectedTimeRange",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "ลองสลับช่วงเวลา (วันนี้ / สัปดาห์นี้ / เดือนนี้) หรือทำรายการเบิก-ฝากสินค้าในคลังเพื่อเริ่มต้นติดตามสถิติกราฟ",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    } else {
                        when (chartDisplayType) {
                            "LINE" -> CustomLineChart(chartData = chartData)
                            "PIE" -> {
                                val totDep = chartData.sumOf { it.depositAmount.toDouble() }
                                val totWit = chartData.sumOf { it.withdrawAmount.toDouble() }
                                val total = (totDep + totWit).coerceAtLeast(1.0)
                                val depPct = (totDep / total * 100).toFloat()
                                val witPct = (totWit / total * 100).toFloat()

                                val slices = listOf(
                                    PieChartSliceData("ยอดฝากเข้า", totDep, depPct, EmeraldGreen),
                                    PieChartSliceData("ยอดเบิกออก", totWit, witPct, CrimsonRed)
                                )
                                CustomPieChart(
                                    slices = slices,
                                    centerText = "${DecimalFormat("#0.0").format(depPct)}%",
                                    centerSubtext = "สัดส่วนการฝาก"
                                )
                            }
                            else -> CustomBarChart(chartData = chartData)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dedicated Pie/Donut Chart Comparison Analysis Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PieChart, contentDescription = null, tint = AmberOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "กราฟวงกลมวิเคราะห์สัดส่วน", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sub-tab Selector Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateSurface)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "CATEGORY" to "📦 หมวดหมู่",
                            "MEMBER" to "👥 สมาชิก",
                            "RATIO" to "💰 ฝาก VS เบิก"
                        ).forEach { (tab, label) ->
                            val isSelected = selectedPieTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AmberOrange else Color.Transparent)
                                    .clickable { selectedPieTab = tab }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val palette = listOf(
                        CyberCyan, EmeraldGreen, AmberOrange, CrimsonRed,
                        Color(0xFFAB47BC), Color(0xFF42A5F5), Color(0xFFFF7043), Color(0xFF26A69A)
                    )

                    when (selectedPieTab) {
                        "CATEGORY" -> {
                            val catSlices = remember(items, categoryGroup, totalStockQty) {
                                val list = mutableListOf<PieChartSliceData>()
                                var idx = 0
                                for ((catName, itemList) in categoryGroup) {
                                    val qty = itemList.sumOf { it.currentQuantity }.toDouble()
                                    val pct = if (totalStockQty > 0) (qty / totalStockQty * 100).toFloat() else 0f
                                    if (qty > 0) {
                                        list.add(
                                            PieChartSliceData(
                                                label = catName,
                                                value = qty,
                                                percentage = pct,
                                                color = palette[idx % palette.size]
                                            )
                                        )
                                        idx++
                                    }
                                }
                                list
                            }

                            if (catSlices.isEmpty()) {
                                Text(text = "ยังไม่มีสินค้าในคลัง", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(16.dp))
                            } else {
                                CustomPieChart(
                                    slices = catSlices,
                                    centerText = "${formatter.format(totalStockQty)}",
                                    centerSubtext = "รวมสินค้าชิ้น"
                                )
                            }
                        }

                        "MEMBER" -> {
                            val memberSlices = remember(filteredLogs, users) {
                                val logsList: List<WarehouseLogEntity> = filteredLogs
                                val userLogCounts: Map<String, List<WarehouseLogEntity>> = logsList.groupBy { log -> log.playerName }
                                val totalLogs = logsList.size.coerceAtLeast(1)
                                val list = mutableListOf<PieChartSliceData>()
                                var idx = 0
                                for ((uname, logList) in userLogCounts) {
                                    val count = logList.size.toDouble()
                                    val pct = (count / totalLogs * 100).toFloat()
                                    list.add(
                                        PieChartSliceData(
                                            label = uname,
                                            value = count,
                                            percentage = pct,
                                            color = palette[idx % palette.size]
                                        )
                                    )
                                    idx++
                                }
                                list.sortedByDescending { it.value }.take(5)
                            }

                            if (memberSlices.isEmpty()) {
                                Text(text = "ยังไม่มีประวัติกิจกรรมของสมาชิกในช่วงเวลานี้", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(16.dp))
                            } else {
                                CustomPieChart(
                                    slices = memberSlices,
                                    centerText = "${filteredLogs.size}",
                                    centerSubtext = "รายการทำ"
                                )
                            }
                        }

                        else -> { // "RATIO"
                            val totalQty = (totalDepositedQty + totalWithdrawnQty).coerceAtLeast(1)
                            val depPct = (totalDepositedQty.toFloat() / totalQty.toFloat() * 100f)
                            val witPct = (totalWithdrawnQty.toFloat() / totalQty.toFloat() * 100f)

                            val ratioSlices = listOf(
                                PieChartSliceData("นำเข้าคลัง (ฝาก)", totalDepositedQty.toDouble(), depPct, EmeraldGreen),
                                PieChartSliceData("เบิกออก (เบิก)", totalWithdrawnQty.toDouble(), witPct, CrimsonRed)
                            )

                            CustomPieChart(
                                slices = ratioSlices,
                                centerText = "${DecimalFormat("#0.0").format(depPct)}%",
                                centerSubtext = "สัดส่วนนำเข้า"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Spacer(modifier = Modifier.height(18.dp))

            // Breakdown By Category Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PieChart, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "สัดส่วนสินค้าแยกตามหมวดหมู่", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    categoryGroup.forEach { (categoryName, itemList) ->
                        val catStock = itemList.sumOf { it.currentQuantity }
                        val catValue = itemList.sumOf { it.currentQuantity * it.unitPrice }
                        val progress = if (totalStockQty > 0) catStock.toFloat() / totalStockQty.toFloat() else 0f

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = categoryName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${formatter.format(catStock)} ชิ้น (${currencyFormatter.format(catValue)})", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = CyberCyan,
                                trackColor = Color.Black.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top Item Activity List
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "สินค้าที่มีมูลค่าสูงสุดในคลัง (Top Valued Items)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val sortedByValuation = items.sortedByDescending { it.currentQuantity * it.unitPrice }.take(5)

                    sortedByValuation.forEachIndexed { index, item ->
                        val itemTotalValue = item.currentQuantity * item.unitPrice
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "${index + 1}", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = item.itemName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "รหัส: ${item.itemCode} • ${item.category}", color = TextSecondary, fontSize = 10.sp)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = currencyFormatter.format(itemTotalValue), color = AmberOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "${formatter.format(item.currentQuantity)} ${item.unit}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

        } else {
            // MODE 2: WEEKLY GANG QUOTA CHECKER & EDITOR
            val weeklyStartTimestamp = remember {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }

            val weeklyLogs = logs.filter { it.timestamp >= weeklyStartTimestamp && it.actionType.equals("DEPOSIT", ignoreCase = true) }

            // Automatic Overdue Cutoff Calculation (Past Sunday 23:59 or toggle enabled)
            val currentCal = Calendar.getInstance()
            val dayOfWeek = currentCal.get(Calendar.DAY_OF_WEEK)
            val isDeadlinePassed = forceOverdueMode || dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.MONDAY

            // Calculate gang-wide stats for header pills
            var completedUsersCount = 0
            var exceededUsersCount = 0
            var incompleteUsersCount = 0
            var totalGangExcessItems = 0L

            users.forEach { u ->
                val uDeposits = weeklyLogs.filter { log ->
                    log.playerName.equals(u.username, ignoreCase = true) ||
                            (log.playerCitizenId.isNotBlank() && log.playerCitizenId.equals(u.citizenId, ignoreCase = true))
                }
                val uDone = weeklyQuotas.isNotEmpty() && weeklyQuotas.all { q ->
                    val dep = uDeposits.filter {
                        it.itemCode.equals(q.itemCode, ignoreCase = true) || it.itemName.contains(q.itemName, ignoreCase = true)
                    }.sumOf { it.amount }
                    dep >= q.targetAmount
                }
                var uExcess = 0L
                weeklyQuotas.forEach { q ->
                    val dep = uDeposits.filter {
                        it.itemCode.equals(q.itemCode, ignoreCase = true) || it.itemName.contains(q.itemName, ignoreCase = true)
                    }.sumOf { it.amount }
                    if (dep > q.targetAmount) {
                        uExcess += (dep - q.targetAmount)
                    }
                }
                if (uDone) {
                    completedUsersCount++
                    if (uExcess > 0) {
                        exceededUsersCount++
                        totalGangExcessItems += uExcess
                    }
                } else {
                    incompleteUsersCount++
                }
            }

            // Calculate total gang fine
            var totalGangFine = 0.0
            users.forEach { u ->
                val uDeposits = weeklyLogs.filter { log ->
                    log.playerName.equals(u.username, ignoreCase = true) ||
                            (log.playerCitizenId.isNotBlank() && log.playerCitizenId.equals(u.citizenId, ignoreCase = true))
                }
                var autoFine = 0.0
                weeklyQuotas.forEach { q ->
                    val dep = uDeposits.filter {
                        it.itemCode.equals(q.itemCode, ignoreCase = true) || it.itemName.contains(q.itemName, ignoreCase = true)
                    }.sumOf { it.amount }
                    val missing = (q.targetAmount - dep).coerceAtLeast(0)
                    autoFine += missing * q.finePerUnit
                }
                val effFine = userFineOverrides[u.username] ?: autoFine
                totalGangFine += effFine
            }

            // Header Banner with Edit Quota Requirements Button & Fine Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Checklist, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "เช็ครายการส่งของรายสัปดาห์แก๊ง", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "เริ่มสัปดาห์: ${SimpleDateFormat("dd/MM/yyyy", Locale("th", "TH")).format(Date(weeklyStartTimestamp))}", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = { showQuotaConfigDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "แก้ไข", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ตั้งค่าส่งแก๊ง", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gang Quota Overview Stats Pills
                    val totalGangPrestige = remember(users, weeklyQuotas, weeklyLogs) {
                        users.sumOf { PrestigeHelper.getTotalPrestigePoints(it, weeklyQuotas, weeklyLogs) }
                    }
                    val avgPrestige = if (users.isNotEmpty()) totalGangPrestige / users.size else 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldGreen.copy(alpha = 0.15f))
                                .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🟢 ส่งครบแล้ว: $completedUsersCount / ${users.size} คน",
                                color = EmeraldGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFAB47BC).copy(alpha = 0.18f))
                                .border(1.dp, Color(0xFFAB47BC).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🟣 ทะลุเป้า: $exceededUsersCount คน (+${formatter.format(totalGangExcessItems)} ชิ้น)",
                                color = Color(0xFFAB47BC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.18f))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✨ บารมีรวมแก๊ง: ${formatter.format(totalGangPrestige)} แต้ม (เฉลี่ย ${formatter.format(avgPrestige)} แต้ม/คน)",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (incompleteUsersCount > 0) AmberOrange.copy(alpha = 0.15f) else TextSecondary.copy(alpha = 0.15f))
                                .border(1.dp, if (incompleteUsersCount > 0) AmberOrange.copy(alpha = 0.3f) else TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🟡 ค้างส่ง: $incompleteUsersCount คน",
                                color = if (incompleteUsersCount > 0) AmberOrange else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quota Items Summary Badges
                    Text(
                        text = "รายการส่งของประจำสัปดาห์ (${weeklyQuotas.size} รายการ):",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (weeklyQuotas.isEmpty()) {
                        Text(text = "ยังไม่ได้ตั้งค่ารายการส่งของแก๊ง กด 'ตั้งค่าส่งแก๊ง' เพื่อเพิ่มรายการ", color = AmberOrange, fontSize = 11.sp)
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            weeklyQuotas.forEach { quota ->
                                val penaltySummaryText = when (quota.penaltyType) {
                                    "ITEM" -> {
                                        val itemNote = quota.penaltyCustomNote.ifBlank { quota.itemName }
                                        "ชดเชย: $itemNote ${formatter.format(quota.finePerUnit.toLong())} ${quota.penaltyUnit}/ชิ้น"
                                    }
                                    "ACTIVITY" -> {
                                        val actNote = quota.penaltyCustomNote.ifBlank { "กิจกรรมแก๊ง" }
                                        "บทลงโทษ: $actNote ${formatter.format(quota.finePerUnit.toLong())} ${quota.penaltyUnit}/ชิ้น"
                                    }
                                    else -> "ปรับ ฿${formatter.format(quota.finePerUnit.toLong())}/ชิ้น"
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CyberCyan.copy(alpha = 0.15f))
                                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "📦 ${quota.itemName}: ${formatter.format(quota.targetAmount)} ${quota.unit} ($penaltySummaryText)",
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total Gang Fine Summary Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (totalGangFine > 0) CrimsonRed.copy(alpha = 0.15f) else EmeraldGreen.copy(alpha = 0.15f))
                            .border(1.dp, if (totalGangFine > 0) CrimsonRed.copy(alpha = 0.4f) else EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (totalGangFine > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (totalGangFine > 0) CrimsonRed else EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (totalGangFine > 0) "💸 รวมยอดค่าปรับค้างส่งแก๊งสัปดาห์นี้" else "🎉 สมาชิกทุกคนส่งของครบแล้ว!",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (isDeadlinePassed) "🔴 เลยกำหนดเวลาส่งของแล้ว" else "🟡 อยู่ระหว่างระยะเวลาส่งของ",
                                        color = if (isDeadlinePassed) CrimsonRed else AmberOrange,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Text(
                                text = "฿${formatter.format(totalGangFine.toLong())}",
                                color = if (totalGangFine > 0) CrimsonRed else EmeraldGreen,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Overdue Mode Toggle Switch Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "สลับสถานะเกินกำหนด 🔴 (จำลอง/เปิดระบบเกินกำหนด):", color = TextSecondary, fontSize = 11.sp)
                        Button(
                            onClick = { forceOverdueMode = !forceOverdueMode },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDeadlinePassed) CrimsonRed else SlateCardBorder,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (isDeadlinePassed) "🔴 เกินกำหนดเปิดอยู่" else "🟡 ปกติ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gang Members Quota Progress List
            Text(
                text = "👥 สถิติการส่งของของสมาชิกในแก๊ง (${users.size} คน):",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (users.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(text = "ยังไม่มีข้อมูลสมาชิกในระบบ (เพิ่มสมาชิกได้ที่แท็บ 'สิทธิ์ผู้ใช้')", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                users.forEach { user ->
                    val userColor = try {
                        Color(android.graphics.Color.parseColor(user.avatarColorHex))
                    } catch (e: Exception) {
                        CyberCyan
                    }

                    // Calculate quota deposit breakdown for this user
                    val userDeposits = weeklyLogs.filter { log ->
                        log.playerName.equals(user.username, ignoreCase = true) ||
                                (log.playerCitizenId.isNotBlank() && log.playerCitizenId.equals(user.citizenId, ignoreCase = true))
                    }

                    val isFullyCompleted = weeklyQuotas.isNotEmpty() && weeklyQuotas.all { quota ->
                        val userSum = userDeposits.filter {
                            it.itemCode.equals(quota.itemCode, ignoreCase = true) ||
                                    it.itemName.contains(quota.itemName, ignoreCase = true)
                        }.sumOf { it.amount }
                        userSum >= quota.targetAmount
                    }

                    // Calculate auto fine and missing items list
                    var calculatedAutoFine = 0.0
                    val missingItemList = mutableListOf<String>()
                    val userFineItems = mutableListOf<FinePaymentData>()

                    weeklyQuotas.forEach { quota ->
                        val currentDeposited = userDeposits.filter { log ->
                            log.itemCode.equals(quota.itemCode, ignoreCase = true) ||
                                    log.itemName.contains(quota.itemName, ignoreCase = true)
                        }.sumOf { it.amount }
                        val missingQty = (quota.targetAmount - currentDeposited).coerceAtLeast(0)
                        if (missingQty > 0) {
                            val fineRate = quota.finePerUnit
                            val totalQty = missingQty * fineRate
                            val pUnit = quota.penaltyUnit.ifBlank { if (quota.penaltyType == "MONEY") "บาท" else "ชิ้น" }
                            val key = "${user.username}_${quota.itemCode}"
                            val paidSoFar = userFineItemPaidMap[key] ?: 0.0

                            userFineItems.add(
                                FinePaymentData(
                                    user = user,
                                    quota = quota,
                                    missingQty = missingQty,
                                    requiredPenalty = totalQty,
                                    penaltyUnit = pUnit,
                                    penaltyType = quota.penaltyType,
                                    penaltyNote = quota.penaltyCustomNote,
                                    currentPaid = paidSoFar
                                )
                            )

                            when (quota.penaltyType) {
                                "ITEM" -> {
                                    val itemNote = quota.penaltyCustomNote.ifBlank { quota.itemName }
                                    missingItemList.add("📦 ${quota.itemName}: ขาด ${formatter.format(missingQty)} ${quota.unit} ➔ ชดเชยของ: $itemNote ${formatter.format(totalQty.toLong())} ${quota.penaltyUnit}")
                                }
                                "ACTIVITY" -> {
                                    val actNote = quota.penaltyCustomNote.ifBlank { "กิจกรรมแก๊ง" }
                                    missingItemList.add("🏃 ${quota.itemName}: ขาด ${formatter.format(missingQty)} ${quota.unit} ➔ บทลงโทษ: $actNote ${formatter.format(totalQty.toLong())} ${quota.penaltyUnit}")
                                }
                                else -> {
                                    calculatedAutoFine += totalQty
                                    missingItemList.add("💰 ${quota.itemName}: ขาด ${formatter.format(missingQty)} ${quota.unit} ➔ ค่าปรับ: ฿${formatter.format(totalQty.toLong())}")
                                }
                            }
                        }
                    }

                    val customNoteOverride = userPenaltyNoteOverrides[user.username]
                    val userFine = userFineOverrides[user.username] ?: calculatedAutoFine

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(1.dp, SlateCardBorder, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            val userCustomOffenses = customOffensesList.filter { it.username == user.username }
                            val hasUnpaidCustomOffenses = userCustomOffenses.any { !it.isPaid }
                            val hasUnpaidAutoFines = userFineItems.isNotEmpty() && userFineItems.any { it.currentPaid < it.requiredPenalty }

                            // User Info Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(user = user, size = 36.dp, fallbackBgColor = userColor)

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(text = user.username, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "เบอร์: ${user.citizenId} • ${user.job}", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }

                                // Calculate user excess items sum
                                var userTotalExcess = 0L
                                weeklyQuotas.forEach { q ->
                                    val dep = userDeposits.filter { log ->
                                        log.itemCode.equals(q.itemCode, ignoreCase = true) ||
                                                log.itemName.contains(q.itemName, ignoreCase = true)
                                    }.sumOf { it.amount }
                                    if (dep > q.targetAmount) {
                                        userTotalExcess += (dep - q.targetAmount)
                                    }
                                }
                                val hasExceededQuota = isFullyCompleted && userTotalExcess > 0

                                // Status Badge with 🚨 Overdue / Fine / 🟣 Exceeded Quota indicator
                                val statusText: String
                                val statusBg: Color
                                if (hasExceededQuota) {
                                    statusText = "🟣 ทะลุเป้า (+${formatter.format(userTotalExcess)})"
                                    statusBg = Color(0xFFAB47BC)
                                } else if (isFullyCompleted) {
                                    statusText = "🟢 ครบแล้ว 100%"
                                    statusBg = EmeraldGreen
                                } else if (isDeadlinePassed && userFineItems.isNotEmpty()) {
                                    statusText = "🚨🔴 เกินกำหนดส่ง (โดนค่าปรับ)"
                                    statusBg = CrimsonRed
                                } else if (hasUnpaidCustomOffenses || userFine > 0) {
                                    statusText = "⚠️ มีค่าปรับค้างส่ง"
                                    statusBg = CrimsonRed
                                } else {
                                    statusText = "🟡 กำลังส่ง"
                                    statusBg = AmberOrange
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(statusBg.copy(alpha = 0.2f))
                                        .border(1.dp, statusBg, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        color = statusBg,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Member Prestige Points Banner
                            val userTotalPrestige = PrestigeHelper.getTotalPrestigePoints(user, weeklyQuotas, weeklyLogs)
                            val userRank = PrestigeHelper.getRankInfo(userTotalPrestige)
                            val autoPrestige = PrestigeHelper.calculateAutoExceededPoints(user, weeklyQuotas, weeklyLogs)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(userRank.color.copy(alpha = 0.12f))
                                    .border(0.5.dp, userRank.color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = userRank.icon, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "แต้มบารมี: ${formatter.format(userTotalPrestige)} แต้ม (${userRank.fullLevelName}) [เกินเป้า +${formatter.format(autoPrestige)}]",
                                        color = userRank.color,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (onUpdateUser != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(userRank.color.copy(alpha = 0.2f))
                                            .border(0.5.dp, userRank.color, RoundedCornerShape(6.dp))
                                            .clickable { userForPrestige = user }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "⚡ ปรับแต้ม", color = userRank.color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Individual Quota Item Progress Bars
                            if (weeklyQuotas.isEmpty()) {
                                Text(text = "ไม่มีรายการกำหนดส่งของ", color = TextSecondary, fontSize = 11.sp)
                            } else {
                                weeklyQuotas.forEach { quota ->
                                    val currentDeposited = userDeposits.filter { log ->
                                        log.itemCode.equals(quota.itemCode, ignoreCase = true) ||
                                                log.itemName.contains(quota.itemName, ignoreCase = true)
                                    }.sumOf { it.amount }

                                    val progress = (currentDeposited.toFloat() / quota.targetAmount.toFloat()).coerceAtMost(1.0f)
                                    val itemDone = currentDeposited >= quota.targetAmount
                                    val itemExceeded = currentDeposited > quota.targetAmount
                                    val itemExcessQty = (currentDeposited - quota.targetAmount).coerceAtLeast(0)

                                    val itemIcon = if (itemExceeded) "🟣" else if (itemDone) "✅" else (if (isDeadlinePassed) "🚨🔴" else "⏳")
                                    val itemColor = if (itemExceeded) Color(0xFFAB47BC) else if (itemDone) EmeraldGreen else (if (isDeadlinePassed) CrimsonRed else AmberOrange)

                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = itemIcon,
                                                    fontSize = 11.sp
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = quota.itemName,
                                                    color = TextPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                if (itemExceeded) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFAB47BC).copy(alpha = 0.2f))
                                                            .border(0.5.dp, Color(0xFFAB47BC), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = "🟣 ทะลุเป้า +${formatter.format(itemExcessQty)} ${quota.unit}",
                                                            color = Color(0xFFAB47BC),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = if (itemExceeded) {
                                                    "${formatter.format(currentDeposited)} / ${formatter.format(quota.targetAmount)} ${quota.unit} (+${formatter.format(itemExcessQty)})"
                                                } else {
                                                    "${formatter.format(currentDeposited)} / ${formatter.format(quota.targetAmount)} ${quota.unit}"
                                                },
                                                color = itemColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = itemColor,
                                            trackColor = Color.Black.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            }

                            // Conditional Fine Breakdown Card (ถ้าไม่มีค่าปรับ หรือส่งครบแล้ว จะไม่แสดงกล่องค่าปรับเลย)
                            val showFineSection = !isFullyCompleted && (
                                (isDeadlinePassed && userFineItems.isNotEmpty()) ||
                                hasUnpaidAutoFines ||
                                hasUnpaidCustomOffenses ||
                                customNoteOverride != null ||
                                (userFineOverrides[user.username] ?: 0.0) > 0.0
                            )

                            if (showFineSection) {
                                val allFinesCleared = userFineItems.isNotEmpty() && userFineItems.all { it.currentPaid >= it.requiredPenalty } && !hasUnpaidCustomOffenses

                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (allFinesCleared) EmeraldGreen.copy(alpha = 0.1f) else (if (isDeadlinePassed) CrimsonRed.copy(alpha = 0.12f) else AmberOrange.copy(alpha = 0.1f)))
                                        .border(1.dp, if (allFinesCleared) EmeraldGreen.copy(alpha = 0.4f) else (if (isDeadlinePassed) CrimsonRed.copy(alpha = 0.35f) else AmberOrange.copy(alpha = 0.25f)), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (allFinesCleared) "✅ ชำระค่าปรับ/บทลงโทษครบถ้วน 100%:" else (if (isDeadlinePassed) "🚨🔴 รายการบทลงโทษ/ค่าปรับ (เกินกำหนดส่ง):" else "⚖️ รายการบทลงโทษ/ค่าปรับค้างชำระ:"),
                                                color = if (allFinesCleared) EmeraldGreen else (if (isDeadlinePassed) CrimsonRed else AmberOrange),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )

                                            if (allFinesCleared) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(EmeraldGreen.copy(alpha = 0.2f))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text("CLEAR 100%", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                }
                                            } else if (userFine > 0) {
                                                Text(
                                                    text = "฿${formatter.format(userFine.toLong())}",
                                                    color = if (isDeadlinePassed) CrimsonRed else AmberOrange,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        if (customNoteOverride != null) {
                                            Text(
                                                text = "📌 บทลงโทษพิเศษที่กำหนด: $customNoteOverride",
                                                color = CyberCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        } else if (userFineItems.isNotEmpty()) {
                                            userFineItems.forEach { fineItem ->
                                                val isCleared = fineItem.currentPaid >= fineItem.requiredPenalty
                                                val progress = if (fineItem.requiredPenalty > 0) (fineItem.currentPaid / fineItem.requiredPenalty).toFloat().coerceAtMost(1.0f) else 1.0f

                                                val titleText = when (fineItem.penaltyType) {
                                                    "ITEM" -> "📦 ${fineItem.quota.itemName} (ขาด ${fineItem.missingQty} ${fineItem.quota.unit}) ➔ ชดเชย: ${fineItem.penaltyNote.ifBlank { fineItem.quota.itemName }}"
                                                    "ACTIVITY" -> "🏃 ${fineItem.quota.itemName} (ขาด ${fineItem.missingQty} ${fineItem.quota.unit}) ➔ กิจกรรม: ${fineItem.penaltyNote.ifBlank { "กิจกรรมแก๊ง" }}"
                                                    else -> "💰 ${fineItem.quota.itemName} (ขาด ${fineItem.missingQty} ${fineItem.quota.unit}) ➔ ค่าปรับเงิน"
                                                }

                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 3.dp),
                                                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.weight(1f)
                                                            ) {
                                                                Text(
                                                                    text = if (isCleared) "✅" else (if (fineItem.currentPaid > 0) "⏳" else "🔴"),
                                                                    fontSize = 11.sp
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = titleText,
                                                                    color = TextPrimary,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }

                                                            Text(
                                                                text = if (fineItem.penaltyType == "MONEY") "฿${formatter.format(fineItem.currentPaid.toLong())} / ฿${formatter.format(fineItem.requiredPenalty.toLong())}" else "${formatter.format(fineItem.currentPaid.toLong())} / ${formatter.format(fineItem.requiredPenalty.toLong())} ${fineItem.penaltyUnit}",
                                                                color = if (isCleared) EmeraldGreen else AmberOrange,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        LinearProgressIndicator(
                                                            progress = { progress },
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(5.dp)
                                                                .clip(RoundedCornerShape(3.dp)),
                                                            color = if (isCleared) EmeraldGreen else AmberOrange,
                                                            trackColor = Color.Black.copy(alpha = 0.3f)
                                                        )

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.End
                                                        ) {
                                                            OutlinedButton(
                                                                onClick = { fineItemToPay = fineItem },
                                                                shape = RoundedCornerShape(6.dp),
                                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                                modifier = Modifier.height(24.dp)
                                                            ) {
                                                                Icon(imageVector = Icons.Default.Add, contentDescription = "ส่งชำระ", tint = CyberCyan, modifier = Modifier.size(10.dp))
                                                                Spacer(modifier = Modifier.width(2.dp))
                                                                Text(if (isCleared) "✏️ แก้ไขการส่งชำระ" else "➕ ส่งชำระ / เช็กจำนวน", color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Render Custom Offenses for Other Violations (ค่าปรับข้อหาทำผิดอื่นๆ)
                                        if (userCustomOffenses.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(text = "🚨 ค่าปรับข้อหาทำผิดอื่นๆ (รายบุคคล):", color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Spacer(modifier = Modifier.height(4.dp))

                                            for (offense in userCustomOffenses) {
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 3.dp),
                                                    colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = "🚨 ${offense.title}",
                                                                color = TextPrimary,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            val detailText = when (offense.penaltyType) {
                                                                "MONEY" -> "💰 ค่าปรับ: ฿${formatter.format(offense.penaltyAmount.toLong())}"
                                                                "ITEM" -> "📦 ชดเชย: ${offense.itemOrNote} ${formatter.format(offense.penaltyAmount.toLong())} ${offense.penaltyUnit}"
                                                                else -> "🏃 ทำโทษ: ${offense.itemOrNote} ${formatter.format(offense.penaltyAmount.toLong())} ${offense.penaltyUnit}"
                                                            }
                                                            Text(text = detailText, color = if (offense.isPaid) EmeraldGreen else AmberOrange, fontSize = 10.sp)
                                                        }

                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                            Button(
                                                                onClick = {
                                                                    val index = customOffensesList.indexOfFirst { it.id == offense.id }
                                                                    if (index >= 0) {
                                                                        customOffensesList[index] = customOffensesList[index].copy(isPaid = !offense.isPaid)
                                                                    }
                                                                },
                                                                colors = ButtonDefaults.buttonColors(
                                                                    containerColor = if (offense.isPaid) EmeraldGreen else AmberOrange,
                                                                    contentColor = Color.Black
                                                                ),
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                                modifier = Modifier.height(26.dp),
                                                                shape = RoundedCornerShape(6.dp)
                                                            ) {
                                                                Text(if (offense.isPaid) "✅ ชำระแล้ว" else "🔴 ส่งชำระ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            }

                                                            IconButton(
                                                                onClick = { customOffensesList.removeAll { it.id == offense.id } },
                                                                modifier = Modifier.size(26.dp)
                                                            ) {
                                                                Icon(Icons.Default.Delete, contentDescription = "ลบ", tint = CrimsonRed, modifier = Modifier.size(14.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            OutlinedButton(
                                                onClick = { userToEditPenalty = Pair(user, userFine) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "เพิ่มค่าปรับข้อหาอื่นๆ", tint = CyberCyan, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("🚨+ เพิ่มค่าปรับข้อหาทำผิดอื่นๆ (รายบุคคล)", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Dialog 1: Manage Weekly Quotas List
    if (showQuotaConfigDialog) {
        WeeklyQuotaConfigDialog(
            quotas = weeklyQuotas,
            availableItems = items,
            onDismiss = { showQuotaConfigDialog = false },
            onSaveQuota = { quota ->
                onSaveWeeklyQuota(quota)
            },
            onDeleteQuota = { quota ->
                onDeleteWeeklyQuota(quota)
            }
        )
    }

    // Dialog 2: Edit User Specific Penalty Override
    userToEditPenalty?.let { (user, currentFine) ->
        EditUserPenaltyDialog(
            user = user,
            currentFine = currentFine,
            currentCustomNote = userPenaltyNoteOverrides[user.username] ?: "",
            availableItems = items,
            onDismiss = { userToEditPenalty = null },
            onSavePenalty = { newFine, customNote ->
                if (customNote != null) {
                    userPenaltyNoteOverrides[user.username] = customNote
                } else {
                    userPenaltyNoteOverrides.remove(user.username)
                }
                userFineOverrides[user.username] = newFine
                userToEditPenalty = null
            },
            onAddCustomOffense = { offense ->
                customOffensesList.add(offense)
                val targetUser = users.find { it.username == offense.username }
                val citizenId = targetUser?.citizenId ?: "N/A"
                onSendFineDiscordNotification?.invoke(
                    offense.username,
                    citizenId,
                    offense.title,
                    offense.penaltyType,
                    offense.penaltyAmount.toInt(),
                    offense.penaltyUnit,
                    offense.isPaid
                )
                userToEditPenalty = null
            }
        )
    }

    // Dialog 3: Fulfill / Pay Fine Item Dialog
    fineItemToPay?.let { fineData ->
        FulfillFineItemDialog(
            fineData = fineData,
            onDismiss = { fineItemToPay = null },
            onSavePayment = { paid ->
                val key = "${fineData.user.username}_${fineData.quota.itemCode}"
                userFineItemPaidMap[key] = paid
                if (paid > 0) {
                    onSendFineDiscordNotification?.invoke(
                        fineData.user.username,
                        fineData.user.citizenId,
                        "ชำระค่าปรับโควตา: ${fineData.quota.itemName}",
                        "ITEM",
                        paid.toInt(),
                        "ชิ้น",
                        true
                    )
                }
                fineItemToPay = null
            }
        )
    }

    // Dialog 4: Adjust Prestige Points Dialog
    userForPrestige?.let { usr ->
        AdjustPrestigeDialog(
            user = usr,
            weeklyQuotas = weeklyQuotas,
            weeklyLogs = logs,
            onDismiss = { userForPrestige = null },
            onSave = { updatedUser ->
                onUpdateUser?.invoke(updatedUser)
                userForPrestige = null
            }
        )
    }
}

// Chart Data Builder
data class ChartBarData(
    val label: String,
    val depositAmount: Float,
    val withdrawAmount: Float
)

fun buildTransactionChartData(logs: List<WarehouseLogEntity>, timeRange: String): List<ChartBarData> {
    if (logs.isEmpty()) return emptyList()

    val result = mutableListOf<ChartBarData>()
    val sdf = when (timeRange) {
        "DAILY" -> SimpleDateFormat("HH:00", Locale("th", "TH"))
        "WEEKLY" -> SimpleDateFormat("EEE", Locale("th", "TH"))
        "MONTHLY" -> SimpleDateFormat("dd MMM", Locale("th", "TH"))
        "YEARLY" -> SimpleDateFormat("MMM", Locale("th", "TH"))
        else -> SimpleDateFormat("dd/MM", Locale("th", "TH"))
    }

    val grouped = logs.groupBy { sdf.format(Date(it.timestamp)) }

    grouped.entries.toList().takeLast(8).forEach { (label, logList) ->
        val dep = logList.filter { it.actionType.equals("DEPOSIT", ignoreCase = true) }.sumOf { it.amount }.toFloat()
        val wit = logList.filter { it.actionType.equals("WITHDRAW", ignoreCase = true) }.sumOf { it.amount }.toFloat()
        result.add(ChartBarData(label = label, depositAmount = dep, withdrawAmount = wit))
    }

    return result
}

// Custom Dual Bar Chart Rendered with Jetpack Compose Canvas
@Composable
fun CustomBarChart(chartData: List<ChartBarData>) {
    val maxVal = (chartData.flatMap { listOf(it.depositAmount, it.withdrawAmount) }.maxOrNull() ?: 10f).coerceAtLeast(10f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(vertical = 10.dp)
        ) {
            val width = size.width
            val height = size.height
            val barGroupWidth = width / chartData.size
            val barWidth = (barGroupWidth * 0.35f).coerceAtMost(24.dp.toPx())

            chartData.forEachIndexed { index, bar ->
                val groupLeft = index * barGroupWidth
                val groupCenter = groupLeft + (barGroupWidth / 2)

                // Deposit Bar (Emerald Green)
                val depHeight = (bar.depositAmount / maxVal) * height
                val depLeft = groupCenter - barWidth - 2.dp.toPx()
                val depTop = height - depHeight

                drawRect(
                    color = EmeraldGreen,
                    topLeft = Offset(depLeft, depTop),
                    size = Size(barWidth, depHeight)
                )

                // Withdraw Bar (Crimson Red)
                val witHeight = (bar.withdrawAmount / maxVal) * height
                val witLeft = groupCenter + 2.dp.toPx()
                val witTop = height - witHeight

                drawRect(
                    color = CrimsonRed,
                    topLeft = Offset(witLeft, witTop),
                    size = Size(barWidth, witHeight)
                )
            }
        }

        // X-Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            chartData.forEach { bar ->
                Text(
                    text = bar.label,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Custom Comparative Dual Line Chart with Glowing Slopes and Area Fill
@Composable
fun CustomLineChart(chartData: List<ChartBarData>) {
    if (chartData.isEmpty()) return

    val maxVal = (chartData.flatMap { listOf(it.depositAmount, it.withdrawAmount) }.maxOrNull() ?: 10f).coerceAtLeast(10f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(vertical = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val paddingX = 28.dp.toPx()
                val availableWidth = width - (paddingX * 2)
                val stepX = if (chartData.size > 1) availableWidth / (chartData.size - 1) else availableWidth

                // Background horizontal grid lines
                val gridColor = Color.White.copy(alpha = 0.08f)
                listOf(0f, 0.33f, 0.66f, 1f).forEach { ratio ->
                    val y = height * ratio
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Compute points
                val depPoints = chartData.mapIndexed { index, data ->
                    val x = paddingX + index * stepX
                    val y = height - ((data.depositAmount / maxVal) * (height - 20.dp.toPx())) - 10.dp.toPx()
                    Offset(x, y)
                }

                val witPoints = chartData.mapIndexed { index, data ->
                    val x = paddingX + index * stepX
                    val y = height - ((data.withdrawAmount / maxVal) * (height - 20.dp.toPx())) - 10.dp.toPx()
                    Offset(x, y)
                }

                // Deposit Area Gradient Fill
                if (depPoints.isNotEmpty()) {
                    val depFillPath = Path().apply {
                        moveTo(depPoints.first().x, height)
                        depPoints.forEach { lineTo(it.x, it.y) }
                        lineTo(depPoints.last().x, height)
                        close()
                    }
                    drawPath(
                        path = depFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(EmeraldGreen.copy(alpha = 0.30f), Color.Transparent)
                        )
                    )
                }

                // Withdraw Area Gradient Fill
                if (witPoints.isNotEmpty()) {
                    val witFillPath = Path().apply {
                        moveTo(witPoints.first().x, height)
                        witPoints.forEach { lineTo(it.x, it.y) }
                        lineTo(witPoints.last().x, height)
                        close()
                    }
                    drawPath(
                        path = witFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(CrimsonRed.copy(alpha = 0.20f), Color.Transparent)
                        )
                    )
                }

                // Deposit Line
                if (depPoints.size > 1) {
                    val depPath = Path().apply {
                        moveTo(depPoints.first().x, depPoints.first().y)
                        for (i in 1 until depPoints.size) {
                            val pPrev = depPoints[i - 1]
                            val pCurr = depPoints[i]
                            val controlX1 = pPrev.x + (pCurr.x - pPrev.x) / 2
                            val controlY1 = pPrev.y
                            val controlX2 = pPrev.x + (pCurr.x - pPrev.x) / 2
                            val controlY2 = pCurr.y
                            cubicTo(controlX1, controlY1, controlX2, controlY2, pCurr.x, pCurr.y)
                        }
                    }
                    drawPath(
                        path = depPath,
                        color = EmeraldGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Withdraw Line
                if (witPoints.size > 1) {
                    val witPath = Path().apply {
                        moveTo(witPoints.first().x, witPoints.first().y)
                        for (i in 1 until witPoints.size) {
                            val pPrev = witPoints[i - 1]
                            val pCurr = witPoints[i]
                            val controlX1 = pPrev.x + (pCurr.x - pPrev.x) / 2
                            val controlY1 = pPrev.y
                            val controlX2 = pPrev.x + (pCurr.x - pPrev.x) / 2
                            val controlY2 = pCurr.y
                            cubicTo(controlX1, controlY1, controlX2, controlY2, pCurr.x, pCurr.y)
                        }
                    }
                    drawPath(
                        path = witPath,
                        color = CrimsonRed,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Points & Nodes
                depPoints.forEach { pt ->
                    drawCircle(color = Color.Black, radius = 5.dp.toPx(), center = pt)
                    drawCircle(color = EmeraldGreen, radius = 3.5.dp.toPx(), center = pt)
                }

                witPoints.forEach { pt ->
                    drawCircle(color = Color.Black, radius = 5.dp.toPx(), center = pt)
                    drawCircle(color = CrimsonRed, radius = 3.5.dp.toPx(), center = pt)
                }
            }
        }

        // X-Axis Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            chartData.forEach { bar ->
                Text(
                    text = bar.label,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Data holder for Pie/Donut Chart slices
data class PieChartSliceData(
    val label: String,
    val value: Double,
    val percentage: Float,
    val color: Color
)

// Custom Donut/Pie Chart Rendered with Jetpack Compose Canvas
@Composable
fun CustomPieChart(
    slices: List<PieChartSliceData>,
    centerText: String = "",
    centerSubtext: String = ""
) {
    if (slices.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing Donut Ring Canvas
        Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 22.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeftOffset = Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(diameter, diameter)

                var startAngle = -90f
                slices.forEach { slice ->
                    val sweepAngle = (slice.percentage / 100f) * 360f
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle + 1f,
                            sweepAngle = (sweepAngle - 2f).coerceAtLeast(1f),
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            // Inner Ring Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (centerText.isNotBlank()) {
                    Text(
                        text = centerText,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                if (centerSubtext.isNotBlank()) {
                    Text(
                        text = centerSubtext,
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right side Legend List with Percentage Pills
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val formatter = DecimalFormat("#,##0")
            val pctFormatter = DecimalFormat("#0.0")

            slices.take(5).forEach { slice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SlateSurface.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(slice.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = slice.label,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatter.format(slice.value.toLong()),
                            color = TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(slice.color.copy(alpha = 0.2f))
                                .border(0.5.dp, slice.color, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = "${pctFormatter.format(slice.percentage)}%",
                                color = slice.color,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Dialog for Managing/Editing Weekly Quota Items & Flexible Penalties
@Composable
fun WeeklyQuotaConfigDialog(
    quotas: List<WeeklyQuotaEntity>,
    availableItems: List<ItemEntity>,
    onDismiss: () -> Unit,
    onSaveQuota: (WeeklyQuotaEntity) -> Unit,
    onDeleteQuota: (WeeklyQuotaEntity) -> Unit
) {
    val fmt = DecimalFormat("#,##0")
    var showForm by remember { mutableStateOf(false) }
    var editingQuota by remember { mutableStateOf<WeeklyQuotaEntity?>(null) }

    var selectedItemCode by remember { mutableStateOf(availableItems.firstOrNull()?.itemCode ?: "weed") }
    var itemNameInput by remember { mutableStateOf(availableItems.firstOrNull()?.itemName ?: "กัญชาแปรรูป (Dried Weed)") }
    var targetAmountInput by remember { mutableStateOf("50") }
    var unitInput by remember { mutableStateOf(availableItems.firstOrNull()?.unit ?: "ชิ้น") }
    var penaltyTypeInput by remember { mutableStateOf("MONEY") } // MONEY, ITEM, ACTIVITY
    var finePerUnitInput by remember { mutableStateOf("500") }
    var penaltyUnitInput by remember { mutableStateOf("บาท") }
    var penaltyCustomNoteInput by remember { mutableStateOf("") }

    fun openFormForEdit(quota: WeeklyQuotaEntity) {
        editingQuota = quota
        selectedItemCode = quota.itemCode
        itemNameInput = quota.itemName
        targetAmountInput = quota.targetAmount.toString()
        unitInput = quota.unit
        penaltyTypeInput = quota.penaltyType.ifBlank { "MONEY" }
        finePerUnitInput = quota.finePerUnit.toLong().toString()
        penaltyUnitInput = quota.penaltyUnit.ifBlank { if (quota.penaltyType == "MONEY") "บาท" else "ชิ้น" }
        penaltyCustomNoteInput = quota.penaltyCustomNote
        showForm = true
    }

    fun openFormForAdd() {
        editingQuota = null
        selectedItemCode = availableItems.firstOrNull()?.itemCode ?: "custom_${System.currentTimeMillis()}"
        itemNameInput = availableItems.firstOrNull()?.itemName ?: "ไอเทมส่งแก๊ง"
        targetAmountInput = "50"
        unitInput = availableItems.firstOrNull()?.unit ?: "ชิ้น"
        penaltyTypeInput = "MONEY"
        finePerUnitInput = "500"
        penaltyUnitInput = "บาท"
        penaltyCustomNoteInput = ""
        showForm = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, SlateCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ ตั้งค่ารายการส่งของ & บทลงโทษ",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "รายการส่งของ และอัตราค่าปรับ/บทลงโทษอัตโนมัติ:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                if (quotas.isEmpty()) {
                    Text(
                        text = "ยังไม่มีรายการส่งของ กด '+ เพิ่มรายการ' ด้านล่างเพื่อเริ่มตั้งค่า",
                        color = AmberOrange,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    quotas.forEach { quota ->
                        val penaltyLabel = when (quota.penaltyType) {
                            "ITEM" -> "📦 ชดเชยของ: ${quota.penaltyCustomNote.ifBlank { quota.itemName }} ${fmt.format(quota.finePerUnit.toLong())} ${quota.penaltyUnit}/ชิ้นที่ขาด"
                            "ACTIVITY" -> "🏃 กิจกรรม: ${quota.penaltyCustomNote.ifBlank { "กิจกรรมแก๊ง" }} ${fmt.format(quota.finePerUnit.toLong())} ${quota.penaltyUnit}/ชิ้นที่ขาด"
                            else -> "💰 ค่าปรับ: ฿${fmt.format(quota.finePerUnit.toLong())}/${quota.unit}ที่ขาด"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateCardBg)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = quota.itemName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(
                                    text = "เป้าหมาย: ${fmt.format(quota.targetAmount)} ${quota.unit} • $penaltyLabel",
                                    color = CyberCyan,
                                    fontSize = 10.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { openFormForEdit(quota) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "แก้ไข", tint = CyberCyan, modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = { onDeleteQuota(quota) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "ลบ/ลดรายการ", tint = CrimsonRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Add or Edit Quota Target Section
                if (!showForm) {
                    OutlinedButton(
                        onClick = { openFormForAdd() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "เพิ่ม", tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ เพิ่มรายการส่งของ / บทลงโทษใหม่", color = CyberCyan, fontSize = 12.sp)
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberCyan, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateCardBg)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (editingQuota != null) "✏️ แก้ไขรายการและบทลงโทษ:" else "➕ เพิ่มรายการส่งของและบทลงโทษใหม่:",
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = "ชื่อรายการส่งของ:", color = TextPrimary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = itemNameInput,
                                onValueChange = { itemNameInput = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = SlateCardBorder,
                                    focusedContainerColor = SlateCardBg,
                                    unfocusedContainerColor = SlateCardBg
                                )
                            )

                            if (availableItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "📦 เลือกรายการส่งของจากคลัง (ไม่ต้องพิมพ์เอง):", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    availableItems.forEach { item ->
                                        val isSelected = (selectedItemCode == item.itemCode || itemNameInput == item.itemName)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) CyberCyan.copy(alpha = 0.25f) else SlateSurface)
                                                .border(1.dp, if (isSelected) CyberCyan else SlateCardBorder, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedItemCode = item.itemCode
                                                    itemNameInput = item.itemName
                                                    unitInput = item.unit
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${if (isSelected) "✓ " else "+ "}${item.itemName} (${item.unit})",
                                                color = if (isSelected) CyberCyan else TextPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "เป้าหมาย/คน:", color = TextPrimary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = targetAmountInput,
                                        onValueChange = { targetAmountInput = it.filter { c -> c.isDigit() } },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = SlateCardBorder,
                                            focusedContainerColor = SlateCardBg,
                                            unfocusedContainerColor = SlateCardBg
                                        )
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "หน่วยส่งของ:", color = TextPrimary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = unitInput,
                                        onValueChange = { unitInput = it },
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

                            // Penalty Type Selector
                            Text(text = "เลือกประเภทบทลงโทษเมื่อขาดส่ง:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val types = listOf(
                                    "MONEY" to "💰 ค่าปรับเงิน",
                                    "ITEM" to "📦 สิ่งของชดเชย",
                                    "ACTIVITY" to "🏃 กิจกรรมแก๊ง"
                                )
                                types.forEach { (typeCode, label) ->
                                    val isSelected = penaltyTypeInput == typeCode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) CyberCyan else SlateSurface)
                                            .border(1.dp, if (isSelected) CyberCyan else SlateCardBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                penaltyTypeInput = typeCode
                                                if (typeCode == "MONEY") penaltyUnitInput = "บาท"
                                                else if (typeCode == "ITEM") penaltyUnitInput = "ชิ้น"
                                                else if (typeCode == "ACTIVITY") penaltyUnitInput = "รอบ"
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.Black else TextPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Dynamic penalty input fields based on selection
                            when (penaltyTypeInput) {
                                "MONEY" -> {
                                    Text(text = "อัตราเงินปรับต่อ 1 หน่วยที่ขาด (บาท):", color = TextPrimary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = finePerUnitInput,
                                        onValueChange = { finePerUnitInput = it.filter { c -> c.isDigit() } },
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
                                "ITEM" -> {
                                    Text(text = "ชื่อไอเทมสิ่งของที่ต้องชดเชย:", color = TextPrimary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = penaltyCustomNoteInput,
                                        onValueChange = { penaltyCustomNoteInput = it },
                                        placeholder = { Text("เช่น แผ่นเหล็กกล้า / ชุดซ่อม", color = TextSecondary, fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = SlateCardBorder,
                                            focusedContainerColor = SlateCardBg,
                                            unfocusedContainerColor = SlateCardBg
                                        )
                                    )

                                    if (availableItems.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "📦 เลือกไอเทมชดเชยจากคลัง (ไม่ต้องพิมพ์เอง):", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            availableItems.forEach { item ->
                                                val isSelected = penaltyCustomNoteInput == item.itemName
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) CyberCyan.copy(alpha = 0.25f) else SlateSurface)
                                                        .border(1.dp, if (isSelected) CyberCyan else SlateCardBorder, RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            penaltyCustomNoteInput = item.itemName
                                                            penaltyUnitInput = item.unit
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "${if (isSelected) "✓ " else "+ "}${item.itemName} (${item.unit})",
                                                        color = if (isSelected) CyberCyan else TextPrimary,
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "จำนวนชดเชยต่อ 1 ชิ้นที่ขาด:", color = TextPrimary, fontSize = 10.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = finePerUnitInput,
                                                onValueChange = { finePerUnitInput = it.filter { c -> c.isDigit() } },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = CyberCyan,
                                                    unfocusedBorderColor = SlateCardBorder,
                                                    focusedContainerColor = SlateCardBg,
                                                    unfocusedContainerColor = SlateCardBg
                                                )
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "หน่วยชดเชย:", color = TextPrimary, fontSize = 10.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = penaltyUnitInput,
                                                onValueChange = { penaltyUnitInput = it },
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
                                }
                                "ACTIVITY" -> {
                                    Text(text = "ชือกิจกรรมลงโทษ:", color = TextPrimary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = penaltyCustomNoteInput,
                                        onValueChange = { penaltyCustomNoteInput = it },
                                        placeholder = { Text("เช่น วิ่งรอบฐานแก๊ง / ทำความสะอาดเซฟเฮาส์", color = TextSecondary, fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = SlateCardBorder,
                                            focusedContainerColor = SlateCardBg,
                                            unfocusedContainerColor = SlateCardBg
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "จำนวนต่อ 1 ชิ้นที่ขาด:", color = TextPrimary, fontSize = 10.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = finePerUnitInput,
                                                onValueChange = { finePerUnitInput = it.filter { c -> c.isDigit() } },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = CyberCyan,
                                                    unfocusedBorderColor = SlateCardBorder,
                                                    focusedContainerColor = SlateCardBg,
                                                    unfocusedContainerColor = SlateCardBg
                                                )
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "หน่วยกิจกรรม:", color = TextPrimary, fontSize = 10.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = penaltyUnitInput,
                                                onValueChange = { penaltyUnitInput = it },
                                                placeholder = { Text("รอบ/ครั้ง/ชั่วโมง", color = TextSecondary, fontSize = 10.sp) },
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
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { showForm = false; editingQuota = null },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("ยกเลิก", color = TextSecondary, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        val amt = targetAmountInput.toIntOrNull() ?: 10
                                        val fineRate = finePerUnitInput.toDoubleOrNull() ?: 1.0
                                        val qToSave = WeeklyQuotaEntity(
                                            id = editingQuota?.id ?: 0,
                                            itemCode = editingQuota?.itemCode ?: selectedItemCode,
                                            itemName = itemNameInput.ifBlank { "ไอเทมส่งแก๊ง" },
                                            targetAmount = amt,
                                            unit = unitInput.ifBlank { "ชิ้น" },
                                            finePerUnit = fineRate,
                                            penaltyType = penaltyTypeInput,
                                            penaltyUnit = penaltyUnitInput.ifBlank { if (penaltyTypeInput == "MONEY") "บาท" else "ชิ้น" },
                                            penaltyCustomNote = penaltyCustomNoteInput
                                        )
                                        onSaveQuota(qToSave)
                                        showForm = false
                                        editingQuota = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                                ) {
                                    Text("บันทึก", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SlateCardBg, contentColor = TextPrimary)
                ) {
                    Text("ปิดหน้าต่าง")
                }
            }
        }
    }
}

// Dialog for editing user specific fine or penalty override
@Composable
fun EditUserPenaltyDialog(
    user: UserEntity,
    currentFine: Double,
    currentCustomNote: String,
    availableItems: List<ItemEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSavePenalty: (Double, String?) -> Unit,
    onAddCustomOffense: (CustomOffenseData) -> Unit = {}
) {
    var offenseTitleInput by remember { mutableStateOf("🚗 ทำรถแก๊งพัง/ไม่ซ่อม") }
    var fineInput by remember { mutableStateOf(if (currentFine > 0) currentFine.toLong().toString() else "5000") }
    var customNoteInput by remember { mutableStateOf(currentCustomNote) }
    var selectedMode by remember { mutableStateOf("MONEY") } // MONEY, ITEM, ACTIVITY

    val presetOffenses = listOf(
        "🚗 ทำรถแก๊งพัง/ไม่ซ่อม",
        "🚫 ฝ่าฝืนกฎแก๊ง/ไม่ฟังคำสั่ง",
        "⏰ ไม่เข้าประชุมแก๊ง/สาย",
        "⚔️ ขาดงานสงครามแก๊ง",
        "✍️ ระบุข้อหาอื่นๆ..."
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, SlateCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚨 เพิ่มค่าปรับข้อหาทำผิด (รายบุคคล)",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "สมาชิก: ${user.username} (${user.citizenId})",
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "เลือกข้อหาความผิด:", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    presetOffenses.forEach { preset ->
                        val isSelected = offenseTitleInput == preset || (preset.contains("ระบุข้อหา") && !presetOffenses.take(4).contains(offenseTitleInput))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CrimsonRed.copy(alpha = 0.2f) else SlateCardBg)
                                .border(1.dp, if (isSelected) CrimsonRed else SlateCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (preset.contains("ระบุข้อหา")) {
                                        offenseTitleInput = ""
                                    } else {
                                        offenseTitleInput = preset
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = preset, color = if (isSelected) CrimsonRed else TextPrimary, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                if (!presetOffenses.take(4).contains(offenseTitleInput)) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = offenseTitleInput,
                        onValueChange = { offenseTitleInput = it },
                        placeholder = { Text("พิมพ์ระบุข้อหาความผิดเอง...", color = TextSecondary, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateCardBorder,
                            focusedContainerColor = SlateCardBg,
                            unfocusedContainerColor = SlateCardBg
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "รูปแบบบทลงโทษ:", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("MONEY" to "💰 ปรับเงิน", "ITEM" to "📦 ชดเชยสิ่งของ", "ACTIVITY" to "🏃 กิจกรรม").forEach { (mode, label) ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyberCyan else SlateCardBg)
                                .border(1.dp, if (isSelected) CyberCyan else SlateCardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedMode = mode }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = label, color = if (isSelected) Color.Black else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedMode == "MONEY") {
                    Text(text = "จำนวนเงินค่าปรับ (บาท):", color = TextPrimary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = fineInput,
                        onValueChange = { fineInput = it.filter { c -> c.isDigit() } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateCardBorder,
                            focusedContainerColor = SlateCardBg,
                            unfocusedContainerColor = SlateCardBg
                        )
                    )
                } else if (selectedMode == "ITEM") {
                    Text(text = "เลือกไอเทมชดเชยจากคลัง หรือพิมพ์เอง:", color = TextPrimary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = customNoteInput,
                        onValueChange = { customNoteInput = it },
                        placeholder = { Text("ชื่อสิ่งของที่ต้องชดเชย", color = TextSecondary, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateCardBorder,
                            focusedContainerColor = SlateCardBg,
                            unfocusedContainerColor = SlateCardBg
                        )
                    )

                    if (availableItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            availableItems.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SlateCardBg)
                                        .border(1.dp, SlateCardBorder, RoundedCornerShape(6.dp))
                                        .clickable { customNoteInput = item.itemName }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("+ ${item.itemName}", color = CyberCyan, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "จำนวนชิ้นที่ต้องชดเชย:", color = TextPrimary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = fineInput,
                        onValueChange = { fineInput = it.filter { c -> c.isDigit() } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateCardBorder,
                            focusedContainerColor = SlateCardBg,
                            unfocusedContainerColor = SlateCardBg
                        )
                    )
                } else {
                    Text(text = "รายละเอียดกิจกรรมบทลงโทษ:", color = TextPrimary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customNoteInput,
                        onValueChange = { customNoteInput = it },
                        placeholder = { Text("เช่น ล้างรถแก๊ง, วิ่งรอบค่าย", color = TextSecondary, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateCardBorder,
                            focusedContainerColor = SlateCardBg,
                            unfocusedContainerColor = SlateCardBg
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val amt = fineInput.toDoubleOrNull() ?: 0.0
                        val title = offenseTitleInput.ifBlank { "ข้อหาทำผิดทั่วไป" }
                        val unit = if (selectedMode == "MONEY") "บาท" else if (selectedMode == "ITEM") "ชิ้น" else "รอบ"
                        val itemNote = if (selectedMode == "MONEY") "" else customNoteInput

                        onAddCustomOffense(
                            CustomOffenseData(
                                username = user.username,
                                title = title,
                                penaltyType = selectedMode,
                                penaltyAmount = amt,
                                penaltyUnit = unit,
                                itemOrNote = itemNote,
                                isPaid = false
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White)
                ) {
                    Text("🚨 บันทึกเพิ่มค่าปรับข้อหานี้", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = { onSavePenalty(0.0, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.2f), contentColor = EmeraldGreen)
                ) {
                    Text("🟢 ล้างบทลงโทษทั้งหมดของคนนี้ (ให้อภัย)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FulfillFineItemDialog(
    fineData: FinePaymentData,
    onDismiss: () -> Unit,
    onSavePayment: (Double) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    val remaining = (fineData.requiredPenalty - fineData.currentPaid).coerceAtLeast(0.0)
    val formatter = DecimalFormat("#,##0")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, SlateCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📥 ส่งชำระ / เช็กจำนวนค่าปรับ",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "👤 สมาชิก: ${fineData.user.username}",
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "📦 รายการที่ขาด: ${fineData.quota.itemName} (${formatter.format(fineData.missingQty)} ${fineData.quota.unit})",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                val penaltyLabel = when (fineData.penaltyType) {
                    "ITEM" -> "📦 ชดเชยสิ่งของ: ${fineData.penaltyNote.ifBlank { fineData.quota.itemName }}"
                    "ACTIVITY" -> "🏃 บทลงโทษกิจกรรม: ${fineData.penaltyNote.ifBlank { "กิจกรรมแก๊ง" }}"
                    else -> "💰 ค่าปรับเป็นเงิน"
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = penaltyLabel,
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Info Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateCardBg)
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "ยอดที่ต้องส่ง/ชำระทั้งหมด:", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "${formatter.format(fineData.requiredPenalty.toLong())} ${fineData.penaltyUnit}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "ส่ง/ชำระไปแล้ว:", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "${formatter.format(fineData.currentPaid.toLong())} ${fineData.penaltyUnit}",
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "คงเหลือที่ต้องส่ง/ชำระอีก:", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "${formatter.format(remaining.toLong())} ${fineData.penaltyUnit}",
                                color = CrimsonRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ระบุจำนวนที่ส่งชำระเพิ่ม (${fineData.penaltyUnit}):",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("ใส่จำนวน เช่น ${formatter.format(remaining.toLong())}", color = TextSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick add buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val addOptions = if (fineData.penaltyType == "MONEY") listOf(100, 500, 1000, 5000) else listOf(1, 5, 10, 20)
                    addOptions.forEach { opt ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SlateCardBg)
                                .border(1.dp, SlateCardBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    val current = amountInput.toDoubleOrNull() ?: 0.0
                                    amountInput = (current + opt).toLong().toString()
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+$opt", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onSavePayment(fineData.requiredPenalty) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black)
                ) {
                    Text("🟢 ชำระ / ส่งของครบ 100% (เคลียร์รายการนี้)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ยกเลิก", color = TextSecondary, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val addVal = amountInput.toDoubleOrNull() ?: 0.0
                            val newTotal = (fineData.currentPaid + addVal).coerceAtLeast(0.0).coerceAtMost(fineData.requiredPenalty)
                            onSavePayment(newTotal)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                    ) {
                        Text("บันทึกการส่งชำระ", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

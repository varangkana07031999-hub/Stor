package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.PrestigeHelper
import com.example.data.UserEntity
import com.example.data.UserRole
import com.example.data.WarehouseLogEntity
import com.example.data.WeeklyQuotaEntity
import com.example.ui.components.AdjustPrestigeDialog
import com.example.ui.components.AvatarPickerSection
import com.example.ui.components.UserAvatar
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
fun UserManagementScreen(
    currentUser: UserEntity?,
    users: List<UserEntity>,
    weeklyQuotas: List<WeeklyQuotaEntity> = emptyList(),
    weeklyLogs: List<WarehouseLogEntity> = emptyList(),
    onSwitchUser: (UserEntity) -> Unit,
    onAddUser: (username: String, citizenId: String, role: UserRole, job: String, avatarUrl: String) -> Unit,
    onUpdateRole: (UserEntity, UserRole) -> Unit,
    onUpdateUser: ((UserEntity) -> Unit)? = null,
    onDeleteUser: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var userForPrestige by remember { mutableStateOf<UserEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "👥 จัดการผู้ใช้ & สิทธิ์การใช้งาน",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "กำหนดบทบาท Admin, Staff, Viewer และควบคุมการเข้าถึงระบบ",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            if (currentUser?.canManageUsers == true) {
                Button(
                    onClick = { showAddUserDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "เพิ่มผู้ใช้", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "เพิ่มผู้ใช้", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Current Active User Profile Banner
        currentUser?.let { user ->
            val roleEnum = user.getRoleEnum()
            val badgeBg = when (roleEnum) {
                UserRole.ADMIN -> CyberCyan
                UserRole.STAFF -> EmeraldGreen
                UserRole.VIEWER -> CrimsonRed
            }
            val userTotalPoints = PrestigeHelper.getTotalPrestigePoints(user, weeklyQuotas, weeklyLogs)
            val rankInfo = PrestigeHelper.getRankInfo(userTotalPoints)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, badgeBg.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCardBg)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            UserAvatar(user = user, size = 32.dp, fallbackBgColor = badgeBg)

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.username,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = roleEnum.roleNameTh.split(" ")[0],
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "เบอร์: ${user.citizenId} • ${user.job}",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(text = "ผู้ใช้งานปัจจุบัน", color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { userToEdit = user },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "แก้ไขโปรไฟล์", tint = CyberCyan, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Compact Prestige Status Bar on Active User Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(rankInfo.color.copy(alpha = 0.1f))
                            .border(0.5.dp, rankInfo.color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = rankInfo.icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "แต้มบารมี: ${formatter.format(userTotalPoints)} แต้ม (${rankInfo.fullLevelName})",
                                color = rankInfo.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        if (currentUser.canManageUsers) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(rankInfo.color.copy(alpha = 0.2f))
                                    .border(0.5.dp, rankInfo.color, RoundedCornerShape(4.dp))
                                    .clickable { userForPrestige = user }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("⚡ ปรับแต้ม", color = rankInfo.color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prestige Points Leaderboard Card
        if (users.isNotEmpty()) {
            val sortedUsersByPrestige = remember(users, weeklyQuotas, weeklyLogs) {
                users.map { u ->
                    u to PrestigeHelper.getTotalPrestigePoints(u, weeklyQuotas, weeklyLogs)
                }.sortedByDescending { it.second }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCardBg)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🏆", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ตารางอันดับแต้มบารมีสมาชิกแก๊ง (Prestige Leaderboard)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "อัตโนมัติ + ปรับมือ",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        sortedUsersByPrestige.take(5).forEachIndexed { index, (user, points) ->
                            val rankInfo = PrestigeHelper.getRankInfo(points)
                            val medal = when (index) {
                                0 -> "🥇"
                                1 -> "🥈"
                                2 -> "🥉"
                                else -> "  ${index + 1}."
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (index == 0) Color(0xFFFFD700).copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = medal, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    UserAvatar(user = user, size = 26.dp, fallbackBgColor = rankInfo.color)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = user.username, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "(${rankInfo.fullLevelName})", color = rankInfo.color, fontSize = 10.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${formatter.format(points)} แต้ม",
                                        color = rankInfo.color,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                    if (currentUser?.canManageUsers == true) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { userForPrestige = user },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "ปรับแต้ม", tint = rankInfo.color, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role Permission Matrix Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ตารางสิทธิ์การใช้งาน (Role Matrix)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleMatrixRow(title = "👑 ผู้ดูแลระบบ (Admin)", desc = "ฝาก, เบิก, เพิ่ม, แก้ไข, ลบไอเทม, จัดการ User, ล้างประวัติ, ตั้งค่า", color = CyberCyan)
                    RoleMatrixRow(title = "📦 ผู้บันทึกข้อมูล (Staff)", desc = "ฝาก, เบิก, เพิ่ม, แก้ไข, ลบไอเทม, ดูรายงาน", color = EmeraldGreen)
                    RoleMatrixRow(title = "👁️ ผู้ชมระบบ (Viewer)", desc = "ดูคลังสินค้า, ดูประวัติ, ดูรายงานได้อย่างเดียว (Read-only)", color = CrimsonRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // All Users List Header
        Text(
            text = "📋 รายชื่อผู้ใช้ในระบบ (${users.size} คน)",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // User Cards
        users.forEach { user ->
            val roleEnum = user.getRoleEnum()
            val badgeColor = when (roleEnum) {
                UserRole.ADMIN -> CyberCyan
                UserRole.STAFF -> EmeraldGreen
                UserRole.VIEWER -> CrimsonRed
            }
            val isCurrent = user.id == currentUser?.id
            val totalUserPrestige = PrestigeHelper.getTotalPrestigePoints(user, weeklyQuotas, weeklyLogs)
            val userRank = PrestigeHelper.getRankInfo(totalUserPrestige)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .border(1.dp, if (isCurrent) CyberCyan else SlateCardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCardBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(user = user, size = 36.dp, fallbackBgColor = badgeColor)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = user.username,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "เบอร์: ${user.citizenId} • ${user.job}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Role Selector Pills / Action Buttons
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (currentUser?.canManageUsers == true) {
                                IconButton(
                                    onClick = { userToEdit = user },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "แก้ไข", tint = CyberCyan, modifier = Modifier.size(16.dp))
                                }
                            }

                            if (!isCurrent) {
                                OutlinedButton(
                                    onClick = { onSwitchUser(user) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "สลับผู้ใช้", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "สลับสิทธิ์", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CyberCyan)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "ใช้งานอยู่", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Prestige Points Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(userRank.color.copy(alpha = 0.1f))
                            .border(0.5.dp, userRank.color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = userRank.icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "บารมี: ${formatter.format(totalUserPrestige)} แต้ม (${userRank.fullLevelName})",
                                color = userRank.color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (currentUser?.canManageUsers == true) {
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Role Change Row (Admin Only)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "บทบาท: ${roleEnum.roleNameTh}", color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        if (currentUser?.canManageUsers == true && !isCurrent) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                UserRole.values().forEach { role ->
                                    val selected = role == roleEnum
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selected) badgeColor else Color.Transparent)
                                            .border(1.dp, if (selected) badgeColor else SlateCardBorder, RoundedCornerShape(6.dp))
                                            .clickable { onUpdateRole(user, role) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = role.name,
                                            color = if (selected) Color.Black else TextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { userToDelete = user },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "ลบ", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onAdd = { name, cid, role, job, avatarUrl ->
                onAddUser(name, cid, role, job, avatarUrl)
                showAddUserDialog = false
            }
        )
    }

    userToEdit?.let { usr ->
        EditUserDialog(
            user = usr,
            onDismiss = { userToEdit = null },
            onSave = { updatedUser ->
                onUpdateUser?.invoke(updatedUser)
                userToEdit = null
            }
        )
    }

    userForPrestige?.let { usr ->
        AdjustPrestigeDialog(
            user = usr,
            weeklyQuotas = weeklyQuotas,
            weeklyLogs = weeklyLogs,
            onDismiss = { userForPrestige = null },
            onSave = { updatedUser ->
                onUpdateUser?.invoke(updatedUser)
                userForPrestige = null
            }
        )
    }

    userToDelete?.let { usr ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = {
                Text(
                    text = "🗑️ ยืนยันลบสมาชิกแก๊ง",
                    color = CrimsonRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "คุณต้องการลบสมาชิก [${usr.username}] (เบอร์: ${usr.citizenId}) ออกจากระบบแก๊งใช่หรือไม่?",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(usr.id)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ยืนยันลบสมาชิก", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToDelete = null }) {
                    Text("ยกเลิก", color = TextSecondary)
                }
            },
            containerColor = SlateSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun EditUserDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSave: (UserEntity) -> Unit
) {
    var username by remember(user) { mutableStateOf(user.username) }
    var citizenId by remember(user) { mutableStateOf(user.citizenId) }
    var job by remember(user) { mutableStateOf(user.job) }
    var avatarUrl by remember(user) { mutableStateOf(user.avatarUrl) }
    var selectedRole by remember(user) { mutableStateOf(user.getRoleEnum()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateCardBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✏️ แก้ไขข้อมูลสมาชิกแก๊ง",
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "ชื่อแอดมิน / ชื่อสมาชิก (Username):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "เบอร์มือถือ (Phone Number):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = citizenId,
                    onValueChange = { citizenId = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "ตำแหน่ง (Job):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = job,
                    onValueChange = { job = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                AvatarPickerSection(
                    currentAvatarUrl = avatarUrl,
                    onAvatarSelected = { avatarUrl = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "บทบาท / สิทธิ์การใช้งาน:", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UserRole.values().forEach { role ->
                        val selected = selectedRole == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) CyberCyan else SlateCardBg)
                                .border(1.dp, if (selected) CyberCyan else SlateCardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedRole = role }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = role.roleNameTh,
                                color = if (selected) Color.Black else TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (username.isNotBlank() && citizenId.isNotBlank()) {
                            onSave(
                                user.copy(
                                    username = username.trim(),
                                    citizenId = citizenId.trim(),
                                    job = job.trim(),
                                    avatarUrl = avatarUrl.trim(),
                                    role = selectedRole.name
                                )
                            )
                        }
                    },
                    enabled = username.isNotBlank() && citizenId.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text(text = "บันทึกข้อมูล (Save Member)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun RoleMatrixRow(title: String, desc: String, color: Color) {
    Column {
        Text(text = title, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = desc, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onAdd: (username: String, citizenId: String, role: UserRole, job: String, avatarUrl: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var citizenId by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("Police Officer") }
    var avatarUrl by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STAFF) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateCardBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👤 เพิ่มผู้ใช้ใหม่ในระบบ FiveM",
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "ชื่อผู้ใช้ (Username/Character Name):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("เช่น Staff_Keng", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "เบอร์มือถือ:", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = citizenId,
                    onValueChange = { citizenId = it },
                    placeholder = { Text("เช่น 081-234-5678", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "ตำแหน่ง (Job):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = job,
                    onValueChange = { job = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateCardBorder,
                        focusedContainerColor = SlateCardBg,
                        unfocusedContainerColor = SlateCardBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                AvatarPickerSection(
                    currentAvatarUrl = avatarUrl,
                    onAvatarSelected = { avatarUrl = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "กำหนดบทบาท (Role Permission):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserRole.values().forEach { role ->
                        val isSel = role == selectedRole
                        val color = when (role) {
                            UserRole.ADMIN -> CyberCyan
                            UserRole.STAFF -> EmeraldGreen
                            UserRole.VIEWER -> CrimsonRed
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) color else SlateCardBg)
                                .border(1.dp, if (isSel) color else SlateCardBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedRole = role }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = role.name,
                                color = if (isSel) Color.Black else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (username.isNotBlank() && citizenId.isNotBlank()) {
                            onAdd(username, citizenId, selectedRole, job, avatarUrl)
                        }
                    },
                    enabled = username.isNotBlank() && citizenId.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text(text = "บันทึกผู้ใช้ใหม่ (Save User)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

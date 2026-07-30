package com.example.data

import androidx.compose.ui.graphics.Color

data class PrestigeRankInfo(
    val level: Int,
    val title: String,
    val icon: String,
    val color: Color
) {
    val fullLevelName: String get() = "Level $level: $title"
}

object PrestigeHelper {
    fun getRankInfo(totalPoints: Int): PrestigeRankInfo {
        return when {
            totalPoints >= 1000 -> PrestigeRankInfo(3, "สุลต่าน", "👑", Color(0xFFFFD700))
            totalPoints >= 100  -> PrestigeRankInfo(2, "เสาหลัก", "🌟", Color(0xFFFFB300))
            totalPoints >= 0    -> PrestigeRankInfo(0, "หน้าใหม่", "🔰", Color(0xFF00E5FF))
            totalPoints >= -4   -> PrestigeRankInfo(-1, "ขอทาน", "🪵", Color(0xFFFF9800))
            totalPoints >= -9   -> PrestigeRankInfo(-2, "คนล้างส้วม", "🧹", Color(0xFFFF5722))
            else                -> PrestigeRankInfo(-3, "บัญชีดำ", "🚫", Color(0xFFFF5252))
        }
    }

    /**
     * Auto calculate bonus prestige points from total excess items deposited across weekly quotas.
     * Every 1 item deposited beyond weekly target = +1 auto prestige point.
     */
    fun calculateAutoExceededPoints(
        user: UserEntity,
        weeklyQuotas: List<WeeklyQuotaEntity>,
        weeklyLogs: List<WarehouseLogEntity>
    ): Int {
        val userDeposits = weeklyLogs.filter { log ->
            log.actionType.equals("DEPOSIT", ignoreCase = true) &&
            (log.playerName.equals(user.username, ignoreCase = true) ||
            (log.playerCitizenId.isNotBlank() && log.playerCitizenId.equals(user.citizenId, ignoreCase = true)))
        }
        var totalExcess = 0L
        weeklyQuotas.forEach { quota ->
            val dep = userDeposits.filter { log ->
                log.itemCode.equals(quota.itemCode, ignoreCase = true) ||
                        log.itemName.contains(quota.itemName, ignoreCase = true)
            }.sumOf { it.amount }
            if (dep > quota.targetAmount) {
                totalExcess += (dep - quota.targetAmount)
            }
        }
        return totalExcess.toInt()
    }

    fun getTotalPrestigePoints(
        user: UserEntity,
        weeklyQuotas: List<WeeklyQuotaEntity>,
        weeklyLogs: List<WarehouseLogEntity>
    ): Int {
        val autoBonus = calculateAutoExceededPoints(user, weeklyQuotas, weeklyLogs)
        return user.manualPrestigePoints + autoBonus
    }
}

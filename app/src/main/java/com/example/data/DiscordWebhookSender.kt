package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class DiscordWebhookSender {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun sendWarehouseAlert(
        webhookUrl: String,
        actionType: String, // "DEPOSIT" or "WITHDRAW"
        itemName: String,
        itemCode: String,
        amount: Int,
        remainingStock: Int,
        playerName: String,
        playerCitizenId: String,
        playerJob: String,
        warehouseName: String,
        notes: String,
        serverName: String,
        timestamp: Long = System.currentTimeMillis()
    ): Result<String> = withContext(Dispatchers.IO) {
        if (webhookUrl.isBlank() || (!webhookUrl.startsWith("http://") && !webhookUrl.startsWith("https://"))) {
            return@withContext Result.failure(IllegalArgumentException("Webhook URL ไม่ถูกต้อง"))
        }

        try {
            val isDeposit = actionType.equals("DEPOSIT", ignoreCase = true)
            val isFinePayment = actionType.equals("FINE_DEPOSIT", ignoreCase = true) || actionType.equals("FINE_PAYMENT", ignoreCase = true) || notes.contains("ชำระค่าปรับ", ignoreCase = true)
            val isFine = actionType.equals("FINE", ignoreCase = true) || actionType.equals("PENALTY", ignoreCase = true) || notes.contains("โดนค่าปรับ", ignoreCase = true)

            val actionTitle = when {
                isFinePayment -> "💰 [ส่งชำระค่าปรับเข้าคลัง] $itemName ($itemCode)"
                isFine -> "🚨 [แจ้งเตือนโดนค่าปรับ / บทลงโทษ] $itemName ($itemCode)"
                isDeposit -> "📥 [นำเข้าคลัง] $itemName ($itemCode)"
                else -> "📤 [เบิกออกจากคลัง] $itemName ($itemCode)"
            }

            val colorDecimal = when {
                isFinePayment -> 16758016 // Amber/Gold (0xFFA000)
                isFine -> 15158332 // Crimson Red (0xE74C3C)
                isDeposit -> 3066993 // Green (0x2ECC71)
                else -> 15158332 // Crimson Red
            }

            val amountText = when {
                isFinePayment -> "💰 ชำระ $amount ชิ้น"
                isFine -> "🚨 ปรับ $amount ชิ้น"
                isDeposit -> "+$amount ชิ้น"
                else -> "-$amount ชิ้น"
            }

            val transactionDate = Date(timestamp)
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val formattedIsoTime = isoFormat.format(transactionDate)

            val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("th", "TH"))
            val displayTime = displayFormat.format(transactionDate)

            val rootJson = JSONObject()
            rootJson.put("username", "Teletubbies Stash Bot")
            rootJson.put("avatar_url", "https://cdn-icons-png.flaticon.com/512/2897/2897785.png")

            val embedJson = JSONObject()
            embedJson.put("title", actionTitle)
            embedJson.put("description", "ระบบบันทึกประวัติเข้า-ออกคลังแก๊ง Teletubbies ($serverName)")
            embedJson.put("color", colorDecimal)

            val fieldsArray = JSONArray()

            // Field 1: Player Name & Phone Number
            val fieldPlayer = JSONObject()
            fieldPlayer.put("name", "👤 ผู้ทำรายการ")
            fieldPlayer.put("value", "$playerName (เบอร์: ${playerCitizenId.ifBlank { "N/A" }})")
            fieldPlayer.put("inline", true)
            fieldsArray.put(fieldPlayer)

            // Field 2: Job / Department
            val fieldJob = JSONObject()
            fieldJob.put("name", "💼 ตำแหน่ง/สังกัด")
            fieldJob.put("value", playerJob.ifBlank { "ประชาชนทั่วไป (Civilian)" })
            fieldJob.put("inline", true)
            fieldsArray.put(fieldJob)

            // Field 3: Warehouse Name
            val fieldWarehouse = JSONObject()
            fieldWarehouse.put("name", "🏢 คลังเก็บของ")
            fieldWarehouse.put("value", warehouseName.ifBlank { "คลังหลัก" })
            fieldWarehouse.put("inline", false)
            fieldsArray.put(fieldWarehouse)

            // Field 4: Amount
            val fieldAmount = JSONObject()
            fieldAmount.put("name", if (isDeposit) "➕ จำนวนที่ฝาก" else "➖ จำนวนที่เบิก")
            fieldAmount.put("value", amountText)
            fieldAmount.put("inline", true)
            fieldsArray.put(fieldAmount)

            // Field 5: Remaining Stock
            val fieldStock = JSONObject()
            fieldStock.put("name", "📊 สต็อกคงเหลือล่าสุด")
            fieldStock.put("value", "$remainingStock ชิ้น")
            fieldStock.put("inline", true)
            fieldsArray.put(fieldStock)

            // Field 6: Notes
            if (notes.isNotBlank()) {
                val fieldNotes = JSONObject()
                fieldNotes.put("name", "📝 หมายเหตุ")
                fieldNotes.put("value", notes)
                fieldNotes.put("inline", false)
                fieldsArray.put(fieldNotes)
            }

            // Field 7: Date Time
            val fieldTime = JSONObject()
            fieldTime.put("name", "🕒 เวลาทำรายการ")
            fieldTime.put("value", displayTime)
            fieldTime.put("inline", false)
            fieldsArray.put(fieldTime)

            embedJson.put("fields", fieldsArray)

            val footerJson = JSONObject()
            footerJson.put("text", "$serverName • Live Warehouse Log")
            footerJson.put("icon_url", "https://cdn-icons-png.flaticon.com/512/2897/2897785.png")
            embedJson.put("footer", footerJson)
            embedJson.put("timestamp", formattedIsoTime)

            val embedsArray = JSONArray()
            embedsArray.put(embedJson)
            rootJson.put("embeds", embedsArray)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 204) {
                    Result.success("ส่งแจ้งเตือนเข้า Discord Webhook เรียบร้อยแล้ว")
                } else {
                    Result.failure(Exception("Discord Error Code: ${response.code} ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendTestWebhook(webhookUrl: String, serverName: String): Result<String> = withContext(Dispatchers.IO) {
        if (webhookUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("โปรดระบุ Webhook URL"))
        }

        try {
            val rootJson = JSONObject()
            rootJson.put("username", "Teletubbies Stash Bot")
            rootJson.put("avatar_url", "https://cdn-icons-png.flaticon.com/512/2897/2897785.png")

            val embedJson = JSONObject()
            embedJson.put("title", "✅ ทดสอบการเชื่อมต่อ Discord Webhook สำเร็จ")
            embedJson.put("description", "ระบบรายการส่งของเข้า-ออกคลังแก๊ง Teletubbies ($serverName) เชื่อมต่อสมบูรณ์!")
            embedJson.put("color", 65535) // Cyan

            val fieldsArray = JSONArray()
            val fieldStatus = JSONObject()
            fieldStatus.put("name", "สถานะระบบ")
            fieldStatus.put("value", "🟢 พร้อมใช้งานเรียลไทม์")
            fieldStatus.put("inline", true)
            fieldsArray.put(fieldStatus)

            embedJson.put("fields", fieldsArray)
            rootJson.put("embeds", JSONArray().put(embedJson))

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 204) {
                    Result.success("เชื่อมต่อ Discord Webhook สำเร็จ!")
                } else {
                    Result.failure(Exception("HTTP Error ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFineAlert(
        webhookUrl: String,
        targetPlayerName: String,
        targetCitizenId: String,
        offenseTitle: String,
        penaltyType: String, // "MONEY", "ITEM", "ACTIVITY"
        penaltyAmount: Int,
        itemUnit: String = "บาท",
        issuerAdminName: String = "ระบบ/ผู้ดูแลแก๊ง",
        notes: String = "",
        isPaidStatus: Boolean = false,
        serverName: String = "FiveM Thailand City RP",
        timestamp: Long = System.currentTimeMillis()
    ): Result<String> = withContext(Dispatchers.IO) {
        if (webhookUrl.isBlank() || (!webhookUrl.startsWith("http://") && !webhookUrl.startsWith("https://"))) {
            return@withContext Result.failure(IllegalArgumentException("Webhook URL ไม่ถูกต้อง"))
        }

        try {
            val title = if (isPaidStatus) "✅ [แจ้งเตือนชำระค่าปรับเรียบร้อย]" else "🚨 [แจ้งเตือนโดนค่าปรับ / บทลงโทษรายบุคคล]"
            val colorDecimal = if (isPaidStatus) 3066993 else 15158332 // Green or Crimson Red

            val transactionDate = Date(timestamp)
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val formattedIsoTime = isoFormat.format(transactionDate)

            val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("th", "TH"))
            val displayTime = displayFormat.format(transactionDate)

            val rootJson = JSONObject()
            rootJson.put("username", "Teletubbies Fine Bot")
            rootJson.put("avatar_url", "https://cdn-icons-png.flaticon.com/512/2897/2897785.png")

            val embedJson = JSONObject()
            embedJson.put("title", title)
            embedJson.put("description", "ระบบบันทึกค่าปรับและบทลงโทษสมาชิกแก๊ง Teletubbies ($serverName)")
            embedJson.put("color", colorDecimal)

            val fieldsArray = JSONArray()

            // Field 1: Target Member
            val fieldPlayer = JSONObject()
            fieldPlayer.put("name", "👤 สมาชิกที่รับบทลงโทษ")
            fieldPlayer.put("value", "$targetPlayerName (เบอร์: ${targetCitizenId.ifBlank { "N/A" }})")
            fieldPlayer.put("inline", true)
            fieldsArray.put(fieldPlayer)

            // Field 2: Issuer Admin
            val fieldAdmin = JSONObject()
            fieldAdmin.put("name", "👮 ผู้ลงโทษ / ออกใบสั่ง")
            fieldAdmin.put("value", issuerAdminName.ifBlank { "หัวหน้า/ผู้ดูแลแก๊ง" })
            fieldAdmin.put("inline", true)
            fieldsArray.put(fieldAdmin)

            // Field 3: Offense Title
            val fieldOffense = JSONObject()
            fieldOffense.put("name", "🚨 ข้อหาความผิด")
            fieldOffense.put("value", offenseTitle.ifBlank { "ฝ่าฝืนกฎระเบียบแก๊ง" })
            fieldOffense.put("inline", false)
            fieldsArray.put(fieldOffense)

            // Field 4: Penalty Details
            val penaltyText = when (penaltyType.uppercase()) {
                "MONEY" -> "💰 ค่าปรับเงิน: ฿$penaltyAmount บาท"
                "ITEM" -> "📦 ชดเชยสิ่งของ: $penaltyAmount $itemUnit"
                "ACTIVITY" -> "🏃 กิจกรรมบทลงโทษ: $penaltyAmount $itemUnit"
                else -> "⚠️ บทลงโทษ: $penaltyAmount $itemUnit"
            }
            val fieldPenalty = JSONObject()
            fieldPenalty.put("name", "⚖️ อัตราโทษ/ค่าปรับ")
            fieldPenalty.put("value", penaltyText)
            fieldPenalty.put("inline", true)
            fieldsArray.put(fieldPenalty)

            // Field 5: Status
            val fieldStatus = JSONObject()
            fieldStatus.put("name", "📌 สถานะชำระ")
            fieldStatus.put("value", if (isPaidStatus) "🟢 ชำระค่าปรับครบถ้วนแล้ว" else "🚨🔴 ยังไม่ได้ชำระ (ค้างชำระ)")
            fieldStatus.put("inline", true)
            fieldsArray.put(fieldStatus)

            if (notes.isNotBlank()) {
                val fieldNotes = JSONObject()
                fieldNotes.put("name", "📝 หมายเหตุเพิ่มเติม")
                fieldNotes.put("value", notes)
                fieldNotes.put("inline", false)
                fieldsArray.put(fieldNotes)
            }

            val fieldTime = JSONObject()
            fieldTime.put("name", "🕒 วันเวลาที่ออกใบสั่ง")
            fieldTime.put("value", displayTime)
            fieldTime.put("inline", false)
            fieldsArray.put(fieldTime)

            embedJson.put("fields", fieldsArray)

            val footerJson = JSONObject()
            footerJson.put("text", "$serverName • Gang Fine & Penalty Alert System")
            footerJson.put("icon_url", "https://cdn-icons-png.flaticon.com/512/2897/2897785.png")
            embedJson.put("footer", footerJson)
            embedJson.put("timestamp", formattedIsoTime)

            rootJson.put("embeds", JSONArray().put(embedJson))

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 204) {
                    Result.success("ส่งแจ้งเตือนโดนค่าปรับเข้า Discord Webhook เรียบร้อยแล้ว")
                } else {
                    Result.failure(Exception("Discord Error Code: ${response.code} ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

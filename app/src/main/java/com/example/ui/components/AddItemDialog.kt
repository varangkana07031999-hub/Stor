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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    availableCategories: List<String> = listOf("อาหาร/เครื่องดื่ม", "อุปกรณ์การแพทย์", "อุปกรณ์เครื่องมือ", "อาวุธ/ยุทธภัณฑ์", "วัตถุดิบ/แร่", "ของมีค่า", "ชุดเกราะ"),
    onAddItem: (code: String, name: String, category: String, qty: Int, unit: String, minQty: Int, weight: Double, price: Double) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    
    val categoryList = remember(availableCategories) {
        val defaultList = listOf("อาหาร/เครื่องดื่ม", "อุปกรณ์การแพทย์", "อุปกรณ์เครื่องมือ", "อาวุธ/ยุทธภัณฑ์", "วัตถุดิบ/แร่", "ของมีค่า", "ชุดเกราะ")
        (availableCategories.filter { it != "ทั้งหมด" } + defaultList).distinct()
    }

    var category by remember { mutableStateOf(categoryList.firstOrNull() ?: "อุปกรณ์เครื่องมือ") }
    var isCustomCategory by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("50") }
    var unit by remember { mutableStateOf("ชิ้น") }
    var minQtyText by remember { mutableStateOf("10") }
    var weightText by remember { mutableStateOf("0.5") }
    var priceText by remember { mutableStateOf("100") }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ เพิ่มสินค้าใหม่เข้าคลัง",
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "ปิด",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "รหัสไอเทม (Item Code):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    placeholder = { Text("เช่น item_bandage_super", color = TextSecondary) },
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

                Text(text = "ชื่อแสดงภาษาไทย (Display Name):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("เช่น ผ้าพันแผลขั้นสูง", color = TextSecondary) },
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

                Text(text = "เลือกหมวดหมู่ (คลิกเลือกได้เลย):", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categoryList.forEach { cat ->
                        val isSelected = !isCustomCategory && category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyberCyan else SlateCardBg)
                                .border(1.dp, if (isSelected) CyberCyan else SlateCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    isCustomCategory = false
                                    category = cat
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCustomCategory) CyberCyan else SlateCardBg)
                            .border(1.dp, if (isCustomCategory) CyberCyan else SlateCardBorder, RoundedCornerShape(8.dp))
                            .clickable { isCustomCategory = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+ พิมพ์เอง",
                            color = if (isCustomCategory) Color.Black else CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isCustomCategory) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customCategoryText,
                        onValueChange = { customCategoryText = it },
                        placeholder = { Text("พิมพ์ชื่อหมวดหมู่ใหม่...", color = TextSecondary, fontSize = 12.sp) },
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

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "จำนวนเริ่มต้น:", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it },
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
                        Text(text = "ราคาต่อหน่วย ($):", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "หน่วยนับ:", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
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
                        Text(text = "แจ้งเตือนต่ำกว่า:", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = minQtyText,
                            onValueChange = { minQtyText = it },
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

                Spacer(modifier = Modifier.height(18.dp))

                val finalCategory = if (isCustomCategory) customCategoryText.trim() else category

                Button(
                    onClick = {
                        if (code.isNotBlank() && name.isNotBlank() && finalCategory.isNotBlank()) {
                            onAddItem(
                                code.trim(),
                                name.trim(),
                                finalCategory,
                                qtyText.toIntOrNull() ?: 0,
                                unit.trim(),
                                minQtyText.toIntOrNull() ?: 10,
                                weightText.toDoubleOrNull() ?: 0.5,
                                priceText.toDoubleOrNull() ?: 100.0
                            )
                        }
                    },
                    enabled = code.isNotBlank() && name.isNotBlank() && finalCategory.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "บันทึกข้อมูลสินค้า (Save Item)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

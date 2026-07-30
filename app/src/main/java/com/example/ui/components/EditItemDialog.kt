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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.ItemEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditItemDialog(
    item: ItemEntity,
    onDismiss: () -> Unit,
    availableCategories: List<String> = listOf("อาหาร/เครื่องดื่ม", "อุปกรณ์การแพทย์", "อุปกรณ์เครื่องมือ", "อาวุธ/ยุทธภัณฑ์", "วัตถุดิบ/แร่", "ของมีค่า", "ชุดเกราะ"),
    onSaveItem: (updatedItem: ItemEntity) -> Unit
) {
    var code by remember(item) { mutableStateOf(item.itemCode) }
    var name by remember(item) { mutableStateOf(item.itemName) }

    val categoryList = remember(availableCategories, item) {
        val defaultList = listOf("อาหาร/เครื่องดื่ม", "อุปกรณ์การแพทย์", "อุปกรณ์เครื่องมือ", "อาวุธ/ยุทธภัณฑ์", "วัตถุดิบ/แร่", "ของมีค่า", "ชุดเกราะ")
        (listOf(item.category) + availableCategories.filter { it != "ทั้งหมด" } + defaultList).distinct()
    }

    var category by remember(item) { mutableStateOf(item.category) }
    var isCustomCategory by remember(item) { mutableStateOf(!categoryList.contains(item.category)) }
    var customCategoryText by remember(item) { mutableStateOf(if (isCustomCategory) item.category else "") }

    var unit by remember(item) { mutableStateOf(item.unit) }
    var minQtyText by remember(item) { mutableStateOf(item.minQuantity.toString()) }
    var weightText by remember(item) { mutableStateOf(item.weight.toString()) }
    var priceText by remember(item) { mutableStateOf(item.unitPrice.toString()) }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✏️ แก้ไขข้อมูลสินค้า",
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
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

                Text(text = "รหัสไอเทม (Item Code):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
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

                Text(text = "ชื่อสินค้า (Item Name):", color = TextPrimary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "น้ำหนัก/ชิ้น:", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
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
                        val updated = item.copy(
                            itemCode = code.trim(),
                            itemName = name.trim(),
                            category = finalCategory,
                            unit = unit.trim(),
                            minQuantity = minQtyText.toIntOrNull() ?: item.minQuantity,
                            weight = weightText.toDoubleOrNull() ?: item.weight,
                            unitPrice = priceText.toDoubleOrNull() ?: item.unitPrice
                        )
                        onSaveItem(updated)
                    },
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
                        text = "บันทึกการแก้ไข (Save Changes)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

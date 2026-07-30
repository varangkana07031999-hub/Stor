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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.UserEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

val PRESET_GANG_AVATARS = listOf(
    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=150&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=150&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1633332755192-727a05c4013d?w=150&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80"
)

@Composable
fun UserAvatar(
    user: UserEntity,
    size: Dp = 44.dp,
    fallbackBgColor: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    val avatarUrl = user.avatarUrl.trim()
    if (avatarUrl.isNotBlank()) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "รูปโปรไฟล์ของ ${user.username}",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(1.5.dp, fallbackBgColor, CircleShape)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(fallbackBgColor.copy(alpha = 0.2f))
                .border(1.5.dp, fallbackBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.username.take(2).uppercase(),
                color = fallbackBgColor,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38f).sp
            )
        }
    }
}

@Composable
fun AvatarPickerSection(
    currentAvatarUrl: String,
    onAvatarSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = "🖼️ รูปโปรไฟล์สมาชิก (Profile Image):", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        // Avatar Preview Row
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (currentAvatarUrl.isNotBlank()) {
                AsyncImage(
                    model = currentAvatarUrl,
                    contentDescription = "Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(2.dp, CyberCyan, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.2f))
                        .border(2.dp, CyberCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = CyberCyan)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = "เลือกรูปจากสำเร็จรูป หรือวาง URL รูปภาพ:", color = TextSecondary, fontSize = 11.sp)
                if (currentAvatarUrl.isNotBlank()) {
                    Text(
                        text = "ล้างรูปโปรไฟล์",
                        color = Color(0xFFFF5252),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onAvatarSelected("") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Presets List
        Text(text = "รูปโปรไฟล์สำเร็จรูป (Preset Avatars):", color = TextSecondary, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(PRESET_GANG_AVATARS) { url ->
                val isSelected = currentAvatarUrl == url
                AsyncImage(
                    model = url,
                    contentDescription = "Preset Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) CyberCyan else SlateCardBorder,
                            shape = CircleShape
                        )
                        .clickable { onAvatarSelected(url) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // URL Field
        OutlinedTextField(
            value = currentAvatarUrl,
            onValueChange = onAvatarSelected,
            placeholder = { Text("https://example.com/my-photo.jpg", color = TextSecondary, fontSize = 11.sp) },
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
}

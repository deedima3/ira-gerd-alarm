package com.example.cutesyalarm.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cutesyalarm.model.Alarm
import com.example.cutesyalarm.ui.theme.CuteColors
import com.example.cutesyalarm.ui.theme.getScaleFactor

@Composable
fun AlarmCard(
    alarm: Alarm,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = getScaleFactor()
    val cardScale by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.98f,
        label = "scale"
    )
    
    val cardColor = when {
        !isEnabled -> CuteColors.Cream
        alarm.category.name == "MEDICINE" -> CuteColors.SecondaryPink
        else -> CuteColors.LightMint
    }
    
    // Responsive padding
    val padding = (16 * scale).dp
    val iconSize = (48 * scale).dp
    val iconTextSize = if (scale < 0.9) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .shadow(
                elevation = if (isEnabled) (8 * scale).dp else (2 * scale).dp,
                shape = RoundedCornerShape((24 * scale).dp),
                spotColor = Color(alarm.color).copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape((24 * scale).dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .background(Color(alarm.color).copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = alarm.icon,
                        style = iconTextSize
                    )
                }
                
                Column {
                    Text(
                        text = alarm.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isEnabled) CuteColors.TextPrimary else CuteColors.TextLight
                        )
                    )
                    Text(
                        text = alarm.category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isEnabled) CuteColors.TextSecondary else CuteColors.TextLight
                        )
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)
            ) {
                // Time display
                Text(
                    text = String.format("%02d:%02d", alarm.time.hour, alarm.time.minute),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color(alarm.color) else CuteColors.TextLight
                    )
                )
                
                // Toggle switch
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.scale(scale.coerceIn(0.7f, 1.0f)),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(alarm.color),
                        checkedTrackColor = Color(alarm.color).copy(alpha = 0.5f),
                        uncheckedThumbColor = CuteColors.TextLight,
                        uncheckedTrackColor = CuteColors.Cream
                    )
                )
            }
        }
    }
}
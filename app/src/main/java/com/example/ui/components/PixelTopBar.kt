package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraSettings
import com.example.model.FlashMode
import com.example.model.HdrMode
import com.example.ui.theme.PixelBlack
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelOverlay
import com.example.ui.theme.PixelSurfaceContainer
import com.example.ui.theme.PixelWhite
import com.example.ui.theme.PixelYellow

@Composable
fun PixelTopBar(
    settings: CameraSettings,
    onOpenSettings: () -> Unit,
    onOpenConfigDialog: () -> Unit,
    onAddConfigClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // Left side: Flash and HDR indicators
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash badge
            Surface(
                shape = CircleShape,
                color = PixelSurfaceContainer.copy(alpha = 0.85f),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onOpenSettings() }
                    .testTag("flash_status_badge")
            ) {
                Box(modifier = Modifier.padding(7.dp)) {
                    val flashIcon = when (settings.flashMode) {
                        FlashMode.ON -> Icons.Default.FlashOn
                        FlashMode.AUTO -> Icons.Default.FlashAuto
                        FlashMode.OFF -> Icons.Default.FlashOff
                    }
                    val flashColor = if (settings.flashMode == FlashMode.ON) PixelYellow else PixelWhite
                    Icon(
                        imageVector = flashIcon,
                        contentDescription = "Flash Status",
                        tint = flashColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // HDR+ badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (settings.hdrMode != HdrMode.OFF) PixelYellow.copy(alpha = 0.2f) else PixelSurfaceContainer.copy(alpha = 0.85f),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenSettings() }
                    .testTag("hdr_status_badge")
            ) {
                Text(
                    text = settings.hdrMode.badge,
                    color = if (settings.hdrMode != HdrMode.OFF) PixelYellow else PixelWhite.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }

        // Center: Swipe down pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PixelSurfaceContainer.copy(alpha = 0.88f),
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenSettings() }
                .testTag("swipe_down_pill")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Swipe Down for Settings",
                    tint = PixelWhite,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Settings",
                    color = PixelWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Right side: "+ Add Config" button and Active LMC XML Config chip
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prominent button to add/import custom XML config files
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PixelYellow,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onAddConfigClick() }
                    .testTag("add_config_top_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Config",
                        tint = PixelBlack,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "+ Config",
                        color = PixelBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Active Config Chip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PixelSurfaceContainer.copy(alpha = 0.85f),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onOpenConfigDialog() }
                    .testTag("active_config_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "LMC Config",
                        tint = PixelYellow,
                        modifier = Modifier.size(13.dp)
                    )
                    val configDisplayName = settings.activeConfig.name
                        .replace(".xml", "")
                        .replace("LMC8.4_", "")
                        .take(9)
                    Text(
                        text = configDisplayName,
                        color = PixelWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

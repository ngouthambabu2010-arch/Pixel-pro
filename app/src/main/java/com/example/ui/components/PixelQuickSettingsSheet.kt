package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.model.AspectRatioMode
import com.example.model.CameraSettings
import com.example.model.FlashMode
import com.example.model.GridType
import com.example.model.HdrMode
import com.example.model.LmcConfig
import com.example.model.TimerMode
import com.example.ui.theme.PixelBlack
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelOverlay
import com.example.ui.theme.PixelSurfaceContainer
import com.example.ui.theme.PixelSurfaceDark
import com.example.ui.theme.PixelSurfaceVariant
import com.example.ui.theme.PixelTextSecondary
import com.example.ui.theme.PixelWhite
import com.example.ui.theme.PixelYellow

@Composable
fun PixelQuickSettingsSheet(
    visible: Boolean,
    settings: CameraSettings,
    onDismiss: () -> Unit,
    onHdrChange: (HdrMode) -> Unit,
    onFlashChange: (FlashMode) -> Unit,
    onTimerChange: (TimerMode) -> Unit,
    onRatioChange: (AspectRatioMode) -> Unit,
    onGridChange: (GridType) -> Unit,
    onOpenConfigManager: () -> Unit,
    onOpenAddConfigDialog: () -> Unit = {},
    onSelectConfig: (LmcConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PixelSurfaceDark.copy(alpha = 0.95f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, PixelBorder.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_settings_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = PixelYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Camera Settings",
                                color = PixelWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Settings",
                                tint = PixelTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // HDR+ Setting Row
                    SettingSectionTitle(title = "HDR+ PROCESSING")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HdrOptionPill(
                            label = "Off",
                            isSelected = settings.hdrMode == HdrMode.OFF,
                            onClick = { onHdrChange(HdrMode.OFF) },
                            modifier = Modifier.weight(1f)
                        )
                        HdrOptionPill(
                            label = "HDR+ Auto",
                            isSelected = settings.hdrMode == HdrMode.HDR_PLUS,
                            onClick = { onHdrChange(HdrMode.HDR_PLUS) },
                            modifier = Modifier.weight(1f)
                        )
                        HdrOptionPill(
                            label = "HDR+ Enhanced",
                            isSelected = settings.hdrMode == HdrMode.HDR_ENHANCED,
                            onClick = { onHdrChange(HdrMode.HDR_ENHANCED) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Flash Row
                    SettingSectionTitle(title = "FLASH")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FlashOptionPill(
                            label = "Off",
                            icon = Icons.Default.FlashOff,
                            isSelected = settings.flashMode == FlashMode.OFF,
                            onClick = { onFlashChange(FlashMode.OFF) },
                            modifier = Modifier.weight(1f)
                        )
                        FlashOptionPill(
                            label = "Auto",
                            icon = Icons.Default.FlashAuto,
                            isSelected = settings.flashMode == FlashMode.AUTO,
                            onClick = { onFlashChange(FlashMode.AUTO) },
                            modifier = Modifier.weight(1f)
                        )
                        FlashOptionPill(
                            label = "On",
                            icon = Icons.Default.FlashOn,
                            isSelected = settings.flashMode == FlashMode.ON,
                            onClick = { onFlashChange(FlashMode.ON) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Timer Row & Ratio Row in 2 columns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Timer
                        Column(modifier = Modifier.weight(1f)) {
                            SettingSectionTitle(title = "TIMER")
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TimerMode.values().forEach { timer ->
                                    SmallOptionPill(
                                        label = timer.label,
                                        isSelected = settings.timerMode == timer,
                                        onClick = { onTimerChange(timer) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Ratio
                        Column(modifier = Modifier.weight(1f)) {
                            SettingSectionTitle(title = "RATIO")
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                AspectRatioMode.values().take(3).forEach { ratio ->
                                    SmallOptionPill(
                                        label = ratio.label,
                                        isSelected = settings.aspectRatio == ratio,
                                        onClick = { onRatioChange(ratio) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grid Setting Row
                    SettingSectionTitle(title = "GRID")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GridType.values().forEach { grid ->
                            SmallOptionPill(
                                label = grid.label,
                                isSelected = settings.gridType == grid,
                                onClick = { onGridChange(grid) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = PixelBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // LMC XML Config Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            SettingSectionTitle(title = "LMC8.4 XML CONFIG PROFILE")
                            Text(
                                text = "Color science, noise reduction & sharpness matrix",
                                color = PixelTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onOpenAddConfigDialog,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PixelYellow,
                                    contentColor = PixelBlack
                                ),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("add_custom_config_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Add Config", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onOpenConfigManager,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("manage_configs_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = PixelWhite,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Manage", color = PixelWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Config quick select list
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LmcConfig.defaultConfigs.take(3).forEach { config ->
                            val isSelected = settings.activeConfig.id == config.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PixelYellow.copy(alpha = 0.15f) else PixelSurfaceContainer,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, PixelYellow) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectConfig(config) }
                                    .testTag("quick_config_${config.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = config.name,
                                            color = if (isSelected) PixelYellow else PixelWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Sat: ${config.saturation}x • Sharp: ${config.edgeStrength}x • NR: ${config.noiseReductionLevel}",
                                            color = PixelTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PixelYellow,
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = PixelBlack,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Swipe up handle indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDismiss() }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Collapse settings",
                            tint = PixelTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSectionTitle(title: String) {
    Text(
        text = title,
        color = PixelTextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun HdrOptionPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PixelYellow else PixelSurfaceContainer,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) PixelBlack else PixelWhite,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FlashOptionPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PixelYellow else PixelSurfaceContainer,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PixelBlack else PixelWhite,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = if (isSelected) PixelBlack else PixelWhite,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SmallOptionPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) PixelYellow else PixelSurfaceContainer,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) PixelBlack else PixelWhite,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

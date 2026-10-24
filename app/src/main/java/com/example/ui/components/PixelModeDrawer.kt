package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Panorama
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraMode
import com.example.ui.theme.PixelBlack
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelOverlay
import com.example.ui.theme.PixelSurfaceContainer
import com.example.ui.theme.PixelSurfaceDark
import com.example.ui.theme.PixelTextSecondary
import com.example.ui.theme.PixelWhite
import com.example.ui.theme.PixelYellow

data class ExtendedModeItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val targetMode: CameraMode
)

@Composable
fun PixelModeDrawer(
    visible: Boolean,
    currentMode: CameraMode,
    onModeSelect: (CameraMode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modesList = listOf(
        ExtendedModeItem("night_sight", "Night Sight", "Stars & low light", Icons.Default.Bedtime, CameraMode.NIGHT_SIGHT),
        ExtendedModeItem("portrait", "Portrait", "Natural bokeh blur", Icons.Default.Face, CameraMode.PORTRAIT),
        ExtendedModeItem("camera", "Camera", "Standard HDR+ photos", Icons.Default.CameraAlt, CameraMode.CAMERA),
        ExtendedModeItem("video", "Video", "Stabilized recording", Icons.Default.Videocam, CameraMode.VIDEO),
        ExtendedModeItem("astro", "Astrophotography", "Long exposure sky", Icons.Default.Bedtime, CameraMode.NIGHT_SIGHT),
        ExtendedModeItem("panorama", "Panorama", "Wide horizontal views", Icons.Default.Panorama, CameraMode.CAMERA),
        ExtendedModeItem("photosphere", "Photo Sphere", "360° virtual capture", Icons.Default.Public, CameraMode.CAMERA),
        ExtendedModeItem("slowmo", "Slow Motion", "High frame rate capture", Icons.Default.SlowMotionVideo, CameraMode.VIDEO),
        ExtendedModeItem("timelapse", "Time Lapse", "Accelerated scene clips", Icons.Default.Timelapse, CameraMode.VIDEO),
        ExtendedModeItem("lens", "Google Lens", "Translate & identify", Icons.Default.Search, CameraMode.CAMERA)
    )

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PixelSurfaceDark.copy(alpha = 0.96f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, PixelBorder.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mode_drawer_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Pull down bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDismiss() }
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = PixelBorder,
                            modifier = Modifier.size(width = 40.dp, height = 4.dp)
                        ) {}
                    }

                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Camera Modes",
                                color = PixelWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select or switch capturing mode",
                                color = PixelTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Modes",
                                tint = PixelTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Modes Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(340.dp)
                    ) {
                        items(modesList) { item ->
                            val isSelected = item.targetMode == currentMode && (item.id == "night_sight" || item.id == "portrait" || item.id == "camera" || item.id == "video")
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) PixelYellow.copy(alpha = 0.2f) else PixelSurfaceContainer,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PixelYellow) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        onModeSelect(item.targetMode)
                                        onDismiss()
                                    }
                                    .testTag("drawer_mode_${item.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) PixelYellow else PixelSurfaceDark,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = null,
                                                tint = if (isSelected) PixelBlack else PixelWhite,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = item.title,
                                            color = if (isSelected) PixelYellow else PixelWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = item.subtitle,
                                            color = PixelTextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
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

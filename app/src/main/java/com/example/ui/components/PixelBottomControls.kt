package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import android.view.HapticFeedbackConstants
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraMode
import com.example.model.CameraSettings
import com.example.ui.theme.PixelBlack
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelRed
import com.example.ui.theme.PixelShutterInner
import com.example.ui.theme.PixelShutterOuter
import com.example.ui.theme.PixelSurfaceContainer
import com.example.ui.theme.PixelTextSecondary
import com.example.ui.theme.PixelWhite
import com.example.ui.theme.PixelYellow

@Composable
fun PixelBottomControls(
    settings: CameraSettings,
    latestThumbnail: Bitmap?,
    onShutterClick: () -> Unit,
    onSwitchCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onModeSelected: (CameraMode) -> Unit,
    onZoomSelected: (Float) -> Unit,
    onOpenConfigDialog: () -> Unit,
    onAddConfigClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PixelBlack.copy(alpha = 0.88f))
            .padding(bottom = 24.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Zoom Level Selector Pill
        ZoomSelectorPill(
            currentZoom = settings.zoomLevel,
            onZoomSelected = onZoomSelected
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Mode Carousel (Night Sight, Portrait, Camera, Video, Modes)
        ModeCarousel(
            activeMode = settings.activeMode,
            onModeSelected = onModeSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Shutter Control Row with authentic LMC Double-tap zone
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                // Double tap the background area near the shutter button to pop up LMC XML dialog
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            onOpenConfigDialog()
                        }
                    )
                }
                .testTag("shutter_control_container")
        ) {
            // Left: Switch Camera Button
            Surface(
                shape = CircleShape,
                color = PixelSurfaceContainer.copy(alpha = 0.9f),
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .clickable { onSwitchCamera() }
                    .testTag("switch_camera_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = PixelWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Center: Pixel Shutter Button with Progress Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(86.dp),
                contentAlignment = Alignment.Center
            ) {
                // If capturing (e.g. Night Sight stacking), show circular progress
                if (settings.isCapturing) {
                    CircularProgressIndicator(
                        progress = { settings.captureProgress },
                        color = PixelYellow,
                        strokeWidth = 4.dp,
                        modifier = Modifier
                            .size(86.dp)
                            .testTag("shutter_progress_indicator")
                    )
                }

                // Outer white ring
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(4.dp, PixelShutterOuter),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Inner circle
                        val innerSize = if (settings.isCapturing) 56.dp else 66.dp
                        val innerColor = if (settings.activeMode == CameraMode.VIDEO) {
                            PixelRed
                        } else {
                            PixelShutterInner
                        }
                        val innerShape = if (settings.activeMode == CameraMode.VIDEO && settings.isCapturing) {
                            RoundedCornerShape(8.dp)
                        } else {
                            CircleShape
                        }

                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val shutterScale by animateFloatAsState(targetValue = if (isPressed) 0.88f else 1.0f)
                        val view = LocalView.current

                        Surface(
                            shape = innerShape,
                            color = innerColor,
                            modifier = Modifier
                                .size(innerSize)
                                .scale(shutterScale)
                                .clip(innerShape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    enabled = !settings.isCapturing
                                ) {
                                    try {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    } catch (_: Exception) {}
                                    onShutterClick()
                                }
                                .testTag("shutter_button")
                        ) {
                            if (settings.activeMode == CameraMode.VIDEO && settings.isCapturing) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop Recording",
                                        tint = PixelWhite,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right: Gallery Entrance Button
            Surface(
                shape = CircleShape,
                color = PixelSurfaceContainer.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(2.dp, PixelBorder),
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .clickable { onOpenGallery() }
                    .testTag("gallery_entrance_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (latestThumbnail != null) {
                        Image(
                            bitmap = latestThumbnail.asImageBitmap(),
                            contentDescription = "Latest Photo",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery Entrance",
                            tint = PixelWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Action row: Button to add own config files & LMC double-tap trigger
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Button to add our own config files
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PixelYellow,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAddConfigClick() }
                    .testTag("bottom_add_config_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Config",
                        tint = PixelBlack,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Config File",
                        color = PixelBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick load / config manager chip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PixelSurfaceContainer.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PixelBorder.copy(alpha = 0.6f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenConfigDialog() }
                    .testTag("bottom_load_config_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Config Manager",
                        tint = PixelYellow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Configs / Tune",
                        color = PixelWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomSelectorPill(
    currentZoom: Float,
    onZoomSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoomOptions = listOf(0.5f, 1.0f, 2.0f, 5.0f)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = PixelSurfaceContainer.copy(alpha = 0.75f),
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .testTag("zoom_selector_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            zoomOptions.forEach { zoom ->
                val isSelected = kotlin.math.abs(currentZoom - zoom) < 0.1f
                val label = if (zoom == 0.5f) ".5" else if (zoom == 1.0f) "1x" else "${zoom.toInt()}x"

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) PixelYellow else Color.Transparent,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onZoomSelected(zoom) }
                        .testTag("zoom_btn_$label")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            color = if (isSelected) PixelBlack else PixelWhite,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeCarousel(
    activeMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = CameraMode.values()
    val listState = rememberLazyListState()

    // Scroll active mode into center
    LaunchedEffect(activeMode) {
        val index = modes.indexOf(activeMode)
        if (index >= 0) {
            listState.animateScrollToItem((index - 1).coerceAtLeast(0))
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("mode_carousel")
    ) {
        items(modes) { mode ->
            val isSelected = mode == activeMode
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) PixelYellow.copy(alpha = 0.2f) else Color.Transparent,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onModeSelected(mode) }
                    .testTag("mode_${mode.name.lowercase()}")
            ) {
                Text(
                    text = mode.title,
                    color = if (isSelected) PixelYellow else PixelTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

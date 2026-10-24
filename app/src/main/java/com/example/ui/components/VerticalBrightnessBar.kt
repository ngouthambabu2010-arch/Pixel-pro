package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PixelBlack
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelSurfaceContainer
import com.example.ui.theme.PixelSurfaceDark
import com.example.ui.theme.PixelTextSecondary
import com.example.ui.theme.PixelWhite
import com.example.ui.theme.PixelYellow
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * Standard camera brightness control line (vertical exposure slider) as found in
 * Google Pixel Camera and normal Android camera applications.
 *
 * Supports:
 * - Fluid vertical drag gesture to brighten or darken (+2.0 EV to -2.0 EV)
 * - Draggable glowing sun icon thumb
 * - Digital exposure compensation readout badge (+1.2 EV, 0.0 EV, -0.8 EV)
 * - Step + and - nudge buttons for fine tuning
 * - Tap to reset back to zero EV
 * - High contrast visual baseline track with center tick
 */
@Composable
fun VerticalBrightnessBar(
    exposureValue: Float, // -2.0f to +2.0f
    onExposureChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val view = LocalView.current
    var isInteracting by remember { mutableStateOf(false) }

    // Auto-dim notification after 3 seconds of inactivity if at 0 EV
    LaunchedEffect(isInteracting, exposureValue) {
        if (isInteracting) {
            delay(3500L)
            isInteracting = false
        }
    }

    val barHeightDp = 180.dp
    val barHeightPx = with(density) { barHeightDp.toPx() }
    val isNonZero = kotlin.math.abs(exposureValue) > 0.05f

    Column(
        modifier = modifier
            .testTag("brightness_control_line_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Digital EV Readout Badge
        AnimatedVisibility(
            visible = isInteracting || isNonZero,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PixelBlack.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PixelYellow),
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        try { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) } catch (_: Exception) {}
                        onExposureChange(0f)
                    }
                    .testTag("brightness_value_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (exposureValue > 0.05f) String.format("+%.1f", exposureValue)
                               else if (exposureValue < -0.05f) String.format("%.1f", exposureValue)
                               else "0.0",
                        color = PixelYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("brightness_value_text")
                    )
                    if (isNonZero) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset EV",
                            tint = PixelYellow.copy(alpha = 0.75f),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }

        // Background container capsule containing the vertical brightness line
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PixelSurfaceDark.copy(alpha = if (isInteracting || isNonZero) 0.82f else 0.55f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isInteracting || isNonZero) PixelYellow.copy(alpha = 0.7f) else PixelBorder.copy(alpha = 0.4f)
            ),
            shadowElevation = 6.dp,
            modifier = Modifier
                .width(44.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quick + nudge button (Increase brightness)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            isInteracting = true
                            try { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) } catch (_: Exception) {}
                            val next = (exposureValue + 0.3f).coerceIn(-2.0f, 2.0f)
                            onExposureChange(next)
                        }
                        .testTag("brightness_increase_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Brighten",
                        tint = if (exposureValue >= 1.9f) PixelTextSecondary else PixelWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Drag gesture area covering the brightness control line
                Box(
                    modifier = Modifier
                        .height(barHeightDp)
                        .width(44.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    isInteracting = true
                                    try { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) } catch (_: Exception) {}
                                    onExposureChange(0f)
                                },
                                onTap = { offset ->
                                    isInteracting = true
                                    // Map tap position to exposure
                                    val fraction = 1f - (offset.y / barHeightPx).coerceIn(0f, 1f)
                                    val targetVal = (fraction * 4.0f) - 2.0f
                                    onExposureChange(targetVal.coerceIn(-2.0f, 2.0f))
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    isInteracting = true
                                    try { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) } catch (_: Exception) {}
                                },
                                onDragEnd = { isInteracting = true },
                                onDragCancel = { isInteracting = false }
                            ) { change, dragAmount ->
                                change.consume()
                                isInteracting = true
                                // Dragging up increases exposure, dragging down decreases
                                val delta = -dragAmount / (barHeightPx / 4.0f)
                                val newValue = (exposureValue + delta).coerceIn(-2.0f, 2.0f)
                                onExposureChange(newValue)
                            }
                        }
                        .testTag("brightness_control_line"),
                    contentAlignment = Alignment.Center
                ) {
                    // 1. The Brightness Control Line (Track)
                    Canvas(
                        modifier = Modifier
                            .height(barHeightDp)
                            .width(16.dp)
                    ) {
                        val cx = size.width / 2f
                        val h = size.height
                        val midY = h / 2f

                        // Inactive track line
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(cx, 4.dp.toPx()),
                            end = Offset(cx, h - 4.dp.toPx()),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Plus tick mark (+1 EV)
                        val yPlus1 = midY - (h / 4f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(cx - 3.dp.toPx(), yPlus1),
                            end = Offset(cx + 3.dp.toPx(), yPlus1),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Center zero baseline tick mark (0 EV)
                        drawLine(
                            color = if (isNonZero) Color.White.copy(alpha = 0.6f) else PixelYellow,
                            start = Offset(cx - 6.dp.toPx(), midY),
                            end = Offset(cx + 6.dp.toPx(), midY),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Minus tick mark (-1 EV)
                        val yMinus1 = midY + (h / 4f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(cx - 3.dp.toPx(), yMinus1),
                            end = Offset(cx + 3.dp.toPx(), yMinus1),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Active line track from center to current position
                        val fraction = (exposureValue + 2.0f) / 4.0f
                        val thumbY = (1f - fraction) * h

                        if (kotlin.math.abs(thumbY - midY) > 2.dp.toPx()) {
                            drawLine(
                                color = PixelYellow,
                                start = Offset(cx, midY),
                                end = Offset(cx, thumbY),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // 2. The Sliding Sun Icon & Thumb
                    val fraction = (exposureValue + 2.0f) / 4.0f
                    val thumbYOffsetPx = (1f - fraction) * barHeightPx - (barHeightPx / 2f)
                    val thumbYOffsetDp = with(density) { thumbYOffsetPx.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(y = thumbYOffsetDp)
                            .size(34.dp)
                            .testTag("brightness_slider_thumb"),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PixelBlack.copy(alpha = 0.92f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.8.dp,
                                if (isInteracting || isNonZero) PixelYellow else PixelWhite
                            ),
                            shadowElevation = 6.dp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = "Brightness Thumb",
                                    tint = if (isInteracting || isNonZero) PixelYellow else PixelWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Quick - nudge button (Decrease brightness)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            isInteracting = true
                            try { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) } catch (_: Exception) {}
                            val next = (exposureValue - 0.3f).coerceIn(-2.0f, 2.0f)
                            onExposureChange(next)
                        }
                        .testTag("brightness_decrease_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Darken",
                        tint = if (exposureValue <= -1.9f) PixelTextSecondary else PixelWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

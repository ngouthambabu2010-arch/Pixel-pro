package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PixelBlack
import com.example.ui.theme.PixelYellow
import kotlin.math.roundToInt

@Composable
fun FocusExposureRing(
    tapPoint: Offset?,
    exposureValue: Float, // -2f to +2f
    onExposureChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tapPoint == null) return

    val density = LocalDensity.current
    val ringScale = remember(tapPoint) { Animatable(1.3f) }

    LaunchedEffect(tapPoint) {
        ringScale.animateTo(1.0f, animationSpec = tween(200))
    }

    val dpX = with(density) { (tapPoint.x - 35.dp.toPx()).roundToInt() }
    val dpY = with(density) { (tapPoint.y - 35.dp.toPx()).roundToInt() }

    val trackHeightDp = 120.dp
    val trackHeightPx = with(density) { trackHeightDp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_exposure_overlay")
    ) {
        // 1. Focus Ring
        Box(
            modifier = Modifier
                .offset { IntOffset(dpX, dpY) }
                .size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = (size.minDimension / 2f) * ringScale.value
                drawCircle(
                    color = PixelYellow,
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Center pinpoint dot
                drawCircle(
                    color = PixelYellow,
                    radius = 2.5.dp.toPx()
                )
            }
        }

        // 2. Brightness Control Line (Vertical Line beside focus target)
        val lineOffsetX = with(density) { (tapPoint.x + 48.dp.toPx()).roundToInt() }
        val lineOffsetY = with(density) { (tapPoint.y - (trackHeightPx / 2f)).roundToInt() }

        Box(
            modifier = Modifier
                .offset { IntOffset(lineOffsetX, lineOffsetY) }
                .height(trackHeightDp)
                .width(42.dp)
                .pointerInput(tapPoint) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = -dragAmount / (trackHeightPx / 4.0f)
                        onExposureChange((exposureValue + delta).coerceIn(-2.0f, 2.0f))
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Drawn track line
            Canvas(
                modifier = Modifier
                    .height(trackHeightDp)
                    .width(14.dp)
            ) {
                val cx = size.width / 2f
                val h = size.height
                val midY = h / 2f

                // Inactive line
                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = Offset(cx, 0f),
                    end = Offset(cx, h),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Middle tick mark (0 EV)
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(cx - 4.dp.toPx(), midY),
                    end = Offset(cx + 4.dp.toPx(), midY),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Active yellow indicator line
                val fraction = (exposureValue + 2.0f) / 4.0f
                val thumbY = (1f - fraction) * h
                drawLine(
                    color = PixelYellow,
                    start = Offset(cx, midY),
                    end = Offset(cx, thumbY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draggable sun icon on the brightness control line
            val fraction = (exposureValue + 2.0f) / 4.0f
            val thumbYOffsetPx = (1f - fraction) * trackHeightPx - (trackHeightPx / 2f)
            val thumbYOffsetDp = with(density) { thumbYOffsetPx.toDp() }

            Surface(
                shape = CircleShape,
                color = PixelBlack.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PixelYellow),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .offset(y = thumbYOffsetDp)
                    .size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Brightness Slider Thumb",
                        tint = PixelYellow,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // EV Value Badge
            if (kotlin.math.abs(exposureValue) > 0.05f) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PixelBlack.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PixelYellow),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-24).dp)
                ) {
                    Text(
                        text = String.format("%+.1f", exposureValue),
                        color = PixelYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

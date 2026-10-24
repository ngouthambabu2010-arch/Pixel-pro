package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.model.GridType

@Composable
fun CameraGridOverlay(
    gridType: GridType,
    modifier: Modifier = Modifier
) {
    if (gridType == GridType.OFF) return

    val gridColor = Color.White.copy(alpha = 0.28f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (gridType) {
            GridType.GRID_3X3 -> {
                // Vertical lines
                drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1f)
                drawLine(gridColor, Offset(w * 2f / 3f, 0f), Offset(w * 2f / 3f, h), strokeWidth = 1f)

                // Horizontal lines
                drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, h * 2f / 3f), Offset(w, h * 2f / 3f), strokeWidth = 1f)
            }
            GridType.GOLDEN_RATIO -> {
                val phi = 0.618f
                val v1 = w * (1f - phi)
                val v2 = w * phi
                val h1 = h * (1f - phi)
                val h2 = h * phi

                drawLine(gridColor, Offset(v1, 0f), Offset(v1, h), strokeWidth = 1f)
                drawLine(gridColor, Offset(v2, 0f), Offset(v2, h), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, h1), Offset(w, h1), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, h2), Offset(w, h2), strokeWidth = 1f)
            }
            GridType.OFF -> {}
        }
    }
}

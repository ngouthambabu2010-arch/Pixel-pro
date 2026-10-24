package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

enum class CameraMode(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val supportsNightSight: Boolean = false,
    val supportsHdrPlus: Boolean = true,
    val isVideo: Boolean = false
) {
    NIGHT_SIGHT(
        title = "Night Sight",
        description = "Capture vibrant low-light photos and stars with multi-frame exposure stacking",
        icon = Icons.Default.Bedtime,
        supportsNightSight = true,
        supportsHdrPlus = true
    ),
    PORTRAIT(
        title = "Portrait",
        description = "Add aesthetic background blur with soft natural skin tone processing",
        icon = Icons.Default.Face,
        supportsHdrPlus = true
    ),
    CAMERA(
        title = "Camera",
        description = "Standard Pixel photo mode powered by HDR+ and dynamic tone mapping",
        icon = Icons.Default.CameraAlt,
        supportsHdrPlus = true
    ),
    VIDEO(
        title = "Video",
        description = "High dynamic range video recording with cinematic stabilization",
        icon = Icons.Default.Videocam,
        isVideo = true
    ),
    MODES(
        title = "Modes",
        description = "More specialized modes: Panorama, Photo Sphere, Astrophotography & Lens",
        icon = Icons.Default.GridOn
    )
}

enum class HdrMode(val label: String, val badge: String) {
    OFF("HDR+ Off", "OFF"),
    HDR_PLUS("HDR+ Auto", "HDR+"),
    HDR_ENHANCED("HDR+ Enhanced", "HDR+ ENH")
}

enum class FlashMode(val label: String) {
    OFF("Off"),
    AUTO("Auto"),
    ON("On")
}

enum class TimerMode(val seconds: Int, val label: String) {
    OFF(0, "Off"),
    SEC_3(3, "3s"),
    SEC_10(10, "10s")
}

enum class AspectRatioMode(val label: String, val ratio: Float) {
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_1_1("1:1", 1f),
    RATIO_FULL("Full", 20f / 9f)
}

enum class GridType(val label: String) {
    OFF("Off"),
    GRID_3X3("3 x 3"),
    GOLDEN_RATIO("Golden Ratio")
}

package com.example.model

import android.graphics.Bitmap
import java.io.File

data class CapturedPhoto(
    val id: String,
    val file: File,
    val thumbnail: Bitmap?,
    val timestamp: Long,
    val mode: CameraMode,
    val hdrMode: HdrMode,
    val configName: String,
    val exposureTimeSeconds: Float,
    val iso: Int = 100,
    val focalLengthMm: Float = 24.0f,
    val aperture: Float = 1.85f,
    val width: Int = 4080,
    val height: Int = 3072,
    val isNightSight: Boolean = false
)

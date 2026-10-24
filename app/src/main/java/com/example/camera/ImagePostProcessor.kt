package com.example.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.example.model.LmcConfig
import kotlin.math.cos
import kotlin.math.sin

object ImagePostProcessor {

    /**
     * Applies color science, noise reduction, and sharpness tuning
     * from the active LMC XML configuration file to the bitmap.
     */
    fun process(original: Bitmap, config: LmcConfig, exposureCompensation: Float = 0f): Bitmap {
        val width = original.width
        val height = original.height

        // 1. Color Science (Saturation, Contrast, WB, Tint, Tone Curve)
        val colorTunedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(colorTunedBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cm = ColorMatrix()

        // Saturation adjustment
        cm.setSaturation(config.saturation)

        // Contrast and Brightness / Exposure compensation adjustment
        val contrast = config.contrast
        val exposureOffset = exposureCompensation * 35f
        val shadowLift = config.shadowLift * 50f + exposureOffset
        val contrastMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, shadowLift,
            0f, contrast, 0f, 0f, shadowLift,
            0f, 0f, contrast, 0f, shadowLift,
            0f, 0f, 0f, 1f, 0f
        ))
        cm.postConcat(contrastMatrix)

        // White balance temperature bias (warm = more red, cool = more blue)
        val rBias = (config.wbTempBias * 1.5f).coerceIn(-40f, 40f)
        val bBias = (-config.wbTempBias * 1.5f).coerceIn(-40f, 40f)
        val gBias = (config.tint * 1.2f).coerceIn(-30f, 30f)
        val wbMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, rBias,
            0f, 1f, 0f, 0f, gBias,
            0f, 0f, 1f, 0f, bBias,
            0f, 0f, 0f, 1f, 0f
        ))
        cm.postConcat(wbMatrix)

        // Color matrix presets (Leica, Astro, Cinema)
        when (config.colorMatrixPreset) {
            "LEICA_VIBRANT" -> {
                val leicaMatrix = ColorMatrix(floatArrayOf(
                    1.12f, 0f, 0f, 0f, 5f,
                    0f, 1.05f, 0f, 0f, -2f,
                    0f, 0f, 1.18f, 0f, 8f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(leicaMatrix)
            }
            "ASTRO_DEEP" -> {
                val astroMatrix = ColorMatrix(floatArrayOf(
                    0.95f, 0f, 0f, 0f, -5f,
                    0f, 0.98f, 0f, 0f, -2f,
                    0f, 0f, 1.25f, 0f, 18f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(astroMatrix)
            }
            "CINEMA_FILM" -> {
                val cinemaMatrix = ColorMatrix(floatArrayOf(
                    1.10f, 0f, 0.05f, 0f, 8f,
                    0f, 1.02f, 0f, 0f, 2f,
                    0.05f, 0f, 1.12f, 0f, -4f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(cinemaMatrix)
            }
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(original, 0f, 0f, paint)

        // 2. Sharpness & Noise Reduction Kernel (optimized sample-based pass)
        return applySharpnessAndNoiseReduction(colorTunedBitmap, config)
    }

    private fun applySharpnessAndNoiseReduction(bitmap: Bitmap, config: LmcConfig): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Downsample if huge to process rapidly and avoid OOM
        val maxDim = 1600
        val targetBitmap = if (width > maxDim || height > maxDim) {
            val scale = maxDim.toFloat() / maxOf(width, height)
            Bitmap.createScaledBitmap(bitmap, (width * scale).toInt(), (height * scale).toInt(), true)
        } else {
            bitmap
        }

        val w = targetBitmap.width
        val h = targetBitmap.height
        val pixels = IntArray(w * h)
        targetBitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val outputPixels = IntArray(w * h)

        val strength = (config.edgeStrength - 1.0f).coerceIn(0.1f, 1.5f)
        val nrWeight = when (config.noiseReductionLevel) {
            "ULTRA" -> 0.45f
            "HIGH" -> 0.35f
            "MEDIUM" -> 0.20f
            "LOW" -> 0.10f
            else -> 0.0f
        }

        // Apply 3x3 unsharp mask + spatial smoothing
        for (y in 1 until h - 1) {
            val yOffset = y * w
            for (x in 1 until w - 1) {
                val center = pixels[yOffset + x]
                val left = pixels[yOffset + x - 1]
                val right = pixels[yOffset + x + 1]
                val top = pixels[(y - 1) * w + x]
                val bottom = pixels[(y + 1) * w + x]

                val a = Color.alpha(center)
                val cr = Color.red(center)
                val cg = Color.green(center)
                val cb = Color.blue(center)

                val avgR = (Color.red(left) + Color.red(right) + Color.red(top) + Color.red(bottom)) / 4
                val avgG = (Color.green(left) + Color.green(right) + Color.green(top) + Color.green(bottom)) / 4
                val avgB = (Color.blue(left) + Color.blue(right) + Color.blue(top) + Color.blue(bottom)) / 4

                // Unsharp masking (detail = center - avg)
                val diffR = (cr - avgR)
                val diffG = (cg - avgG)
                val diffB = (cb - avgB)

                // Noise reduction blend (suppresses slight diffs)
                val nrThreshold = nrWeight * 30f
                val sharpR = if (kotlin.math.abs(diffR) < nrThreshold) {
                    cr * (1f - nrWeight) + avgR * nrWeight
                } else {
                    cr + diffR * strength
                }

                val sharpG = if (kotlin.math.abs(diffG) < nrThreshold) {
                    cg * (1f - nrWeight) + avgG * nrWeight
                } else {
                    cg + diffG * strength
                }

                val sharpB = if (kotlin.math.abs(diffB) < nrThreshold) {
                    cb * (1f - nrWeight) + avgB * nrWeight
                } else {
                    cb + diffB * strength
                }

                outputPixels[yOffset + x] = Color.argb(
                    a,
                    sharpR.toInt().coerceIn(0, 255),
                    sharpG.toInt().coerceIn(0, 255),
                    sharpB.toInt().coerceIn(0, 255)
                )
            }
        }

        val resultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(outputPixels, 0, w, 0, 0, w, h)
        return resultBitmap
    }

    /**
     * Generates a high quality test frame when running in emulator or environment
     * without physical camera sensor, so the app is 100% interactive and testable.
     */
    fun createSyntheticPhoto(isNightSight: Boolean, zoom: Float, exposureCompensation: Float = 0f): Bitmap {
        val w = 1200
        val h = 900
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        if (isNightSight) {
            // Night cityscape with starry sky
            paint.color = Color.rgb(10, 15, 30)
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

            // Moon & stars
            paint.color = Color.rgb(250, 245, 200)
            canvas.drawCircle(w * 0.75f, h * 0.22f, 45f, paint)

            // Starry points
            paint.color = Color.WHITE
            for (i in 0..120) {
                val sx = (Math.sin(i * 91.3) * 0.5 + 0.5) * w
                val sy = (Math.cos(i * 47.7) * 0.5 + 0.5) * (h * 0.55)
                val r = (Math.sin(i * 12.3) * 1.5 + 2.0).toFloat()
                canvas.drawCircle(sx.toFloat(), sy.toFloat(), r, paint)
            }

            // City skyline silhouettes & illuminated windows
            paint.color = Color.rgb(18, 22, 35)
            canvas.drawRect(100f, h * 0.45f, 280f, h.toFloat(), paint)
            canvas.drawRect(320f, h * 0.35f, 520f, h.toFloat(), paint)
            canvas.drawRect(560f, h * 0.50f, 750f, h.toFloat(), paint)
            canvas.drawRect(800f, h * 0.40f, 1050f, h.toFloat(), paint)

            // Warm glowing windows
            val winPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            winPaint.color = Color.rgb(255, 214, 110)
            for (bx in listOf(140f, 200f, 360f, 440f, 600f, 680f, 850f, 960f)) {
                for (by in (h * 0.48f).toInt()..(h * 0.85f).toInt() step 50) {
                    canvas.drawRect(bx, by.toFloat(), bx + 22f, by.toFloat() + 22f, winPaint)
                }
            }
        } else {
            // Golden hour scenic landscape with mountains and sun
            paint.color = Color.rgb(245, 175, 110) // Sunset sky gradient base
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

            paint.color = Color.rgb(255, 230, 160)
            canvas.drawCircle(w * 0.48f, h * 0.35f, 70f, paint) // Sun

            // Mountain layers
            paint.color = Color.rgb(130, 95, 120)
            val path1 = android.graphics.Path()
            path1.moveTo(0f, h * 0.55f)
            path1.lineTo(w * 0.3f, h * 0.35f)
            path1.lineTo(w * 0.7f, h * 0.58f)
            path1.lineTo(w.toFloat(), h * 0.42f)
            path1.lineTo(w.toFloat(), h.toFloat())
            path1.lineTo(0f, h.toFloat())
            path1.close()
            canvas.drawPath(path1, paint)

            paint.color = Color.rgb(75, 95, 80)
            val path2 = android.graphics.Path()
            path2.moveTo(0f, h * 0.65f)
            path2.lineTo(w * 0.45f, h * 0.48f)
            path2.lineTo(w * 0.85f, h * 0.62f)
            path2.lineTo(w.toFloat(), h * 0.55f)
            path2.lineTo(w.toFloat(), h.toFloat())
            path2.lineTo(0f, h.toFloat())
            path2.close()
            canvas.drawPath(path2, paint)

            // Foreground lake reflection
            paint.color = Color.rgb(180, 140, 130)
            canvas.drawRect(0f, h * 0.72f, w.toFloat(), h.toFloat(), paint)
        }

        if (exposureCompensation != 0f) {
            val expBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val expCanvas = Canvas(expBmp)
            val expPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val expOffset = exposureCompensation * 35f
            val expMatrix = ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, expOffset,
                0f, 1f, 0f, 0f, expOffset,
                0f, 0f, 1f, 0f, expOffset,
                0f, 0f, 0f, 1f, 0f
            ))
            expPaint.colorFilter = ColorMatrixColorFilter(expMatrix)
            expCanvas.drawBitmap(bmp, 0f, 0f, expPaint)
            return expBmp
        }

        return bmp
    }
}

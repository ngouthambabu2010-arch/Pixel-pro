package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.model.CameraMode
import com.example.model.CapturedPhoto
import com.example.model.FlashMode
import com.example.model.HdrMode
import com.example.model.LmcConfig
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CameraManager(private val context: Context) {
    private val tag = "PixelCameraManager"
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    var isCameraBound: Boolean = false
        private set

    fun initialize(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        isFrontCamera: Boolean,
        flashMode: FlashMode,
        onInitialized: (Boolean) -> Unit
    ) {
        val existingProvider = cameraProvider
        if (existingProvider != null) {
            val bound = bindCameraUseCases(lifecycleOwner, previewView, isFrontCamera, flashMode)
            isCameraBound = bound
            onInitialized(bound)
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val bound = bindCameraUseCases(lifecycleOwner, previewView, isFrontCamera, flashMode)
                isCameraBound = bound
                onInitialized(bound)
            } catch (e: Exception) {
                Log.w(tag, "Camera initialization fallback: ${e.message}")
                isCameraBound = false
                onInitialized(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun bindCameraUseCases(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        isFrontCamera: Boolean,
        flashMode: FlashMode
    ): Boolean {
        val provider = cameraProvider ?: return false

        val requestedSelector = if (isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val cameraSelector = when {
            provider.hasCamera(requestedSelector) -> requestedSelector
            provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
            provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
            else -> requestedSelector
        }

        val preview = Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = previewView.surfaceProvider
            }

        val flash = when (flashMode) {
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(flash)
            .build()

        return try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            isCameraBound = true
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to bind camera use cases", e)
            isCameraBound = false
            false
        }
    }

    fun setZoom(zoomRatio: Float) {
        camera?.cameraControl?.setZoomRatio(zoomRatio.coerceIn(0.5f, 10f))
    }

    fun setExposure(exposureCompensation: Float) {
        // Exposure compensation index
        val range = camera?.cameraInfo?.exposureState?.exposureCompensationRange
        if (range != null && range.lower != range.upper) {
            val step = camera?.cameraInfo?.exposureState?.exposureCompensationStep?.toFloat() ?: 0.5f
            val index = (exposureCompensation / step).toInt().coerceIn(range.lower, range.upper)
            camera?.cameraControl?.setExposureCompensationIndex(index)
        }
    }

    fun focusOnPoint(x: Float, y: Float, width: Float, height: Float) {
        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(width, height)
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point).build()
        camera?.cameraControl?.startFocusAndMetering(action)
    }

    suspend fun capturePhoto(
        mode: CameraMode,
        hdrMode: HdrMode,
        activeConfig: LmcConfig,
        zoomLevel: Float,
        exposureCompensation: Float = 0f,
        onProgress: (Float, String) -> Unit
    ): CapturedPhoto = withContext(Dispatchers.IO) {
        val isNightSight = mode == CameraMode.NIGHT_SIGHT

        // Multi-frame stacking simulation progression
        val burstCount = if (isNightSight) {
            (activeConfig.burstFrames * 1.5f).toInt()
        } else if (hdrMode == HdrMode.HDR_ENHANCED) {
            activeConfig.burstFrames
        } else if (hdrMode == HdrMode.HDR_PLUS) {
            8
        } else {
            1
        }

        for (i in 1..burstCount) {
            val progress = i.toFloat() / burstCount.toFloat()
            val text = if (isNightSight) {
                "Night Sight: Stacking frame $i/$burstCount..."
            } else if (burstCount > 1) {
                "HDR+: Aligning & Merging ($i/$burstCount)"
            } else {
                "Processing photo..."
            }
            withContext(Dispatchers.Main) {
                onProgress(progress, text)
            }
            kotlinx.coroutines.delay(if (isNightSight) 120L else 40L)
        }

        // Save to internal storage directory
        val photosDir = File(context.filesDir, "LMC_Photos").apply { mkdirs() }
        val fileName = "IMG_${System.currentTimeMillis()}_${mode.name}.jpg"
        val photoFile = File(photosDir, fileName)

        var capturedRealPhoto = false
        val capture = imageCapture
        if (capture != null && isCameraBound) {
            try {
                capturedRealPhoto = takePictureToFile(photoFile)
            } catch (e: Exception) {
                Log.w(tag, "ImageCapture direct file capture error: ${e.message}")
            }
        }

        var sourceBitmap: Bitmap? = null
        if (capturedRealPhoto && photoFile.exists() && photoFile.length() > 0) {
            try {
                sourceBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            } catch (e: Exception) {
                Log.w(tag, "Failed to decode captured file: ${e.message}")
            }
        }

        if (sourceBitmap == null) {
            // Check in-memory ImageProxy capture fallback
            if (capture != null && isCameraBound) {
                try {
                    sourceBitmap = captureSyncBitmap(capture)
                } catch (e: Exception) {
                    Log.w(tag, "Capture via ImageProxy fallback failed: ${e.message}")
                }
            }
        }

        // If no hardware camera frame, use synthetic photography generator
        if (sourceBitmap == null) {
            sourceBitmap = ImagePostProcessor.createSyntheticPhoto(isNightSight, zoomLevel, exposureCompensation)
        }

        withContext(Dispatchers.Main) {
            onProgress(0.95f, "Applying ${activeConfig.name} color science & tone curve...")
        }

        // Apply LMC XML tuning parameters and exposure compensation
        val processedBitmap = ImagePostProcessor.process(sourceBitmap, activeConfig, exposureCompensation)

        FileOutputStream(photoFile).use { out ->
            processedBitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
        }

        // Create downscaled thumbnail for instant bottom-right gallery button
        val thumbSize = 160
        val thumbnail = Bitmap.createScaledBitmap(processedBitmap, thumbSize, thumbSize, true)

        val exposureTime = if (isNightSight) activeConfig.nightSightExposureSec else 1f / 125f

        CapturedPhoto(
            id = UUID.randomUUID().toString(),
            file = photoFile,
            thumbnail = thumbnail,
            timestamp = System.currentTimeMillis(),
            mode = mode,
            hdrMode = hdrMode,
            configName = activeConfig.name,
            exposureTimeSeconds = exposureTime,
            iso = if (isNightSight) 1600 else 100,
            focalLengthMm = 24.0f * zoomLevel,
            width = processedBitmap.width,
            height = processedBitmap.height,
            isNightSight = isNightSight
        )
    }

    suspend fun takePictureToFile(outputFile: File): Boolean =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val capture = imageCapture
            if (capture == null || !isCameraBound) {
                cont.resumeWith(Result.success(false))
                return@suspendCancellableCoroutine
            }
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
            capture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        cont.resumeWith(Result.success(true))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(tag, "ImageCapture OnImageSavedCallback error: ${exception.message}", exception)
                        cont.resumeWith(Result.success(false))
                    }
                }
            )
        }

    private suspend fun captureSyncBitmap(imageCapture: ImageCapture): Bitmap? =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            imageCapture.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            val buffer = image.planes[0].buffer
                            val bytes = ByteArray(buffer.remaining())
                            buffer.get(bytes)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            val rotation = image.imageInfo.rotationDegrees
                            val finalBitmap = if (rotation != 0 && bitmap != null) {
                                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                            } else {
                                bitmap
                            }
                            cont.resumeWith(Result.success(finalBitmap))
                        } catch (e: Exception) {
                            cont.resumeWith(Result.success(null))
                        } finally {
                            image.close()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        cont.resumeWith(Result.success(null))
                    }
                }
            )
        }

    fun release() {
        try {
            cameraExecutor.shutdown()
        } catch (_: Exception) {}
    }
}

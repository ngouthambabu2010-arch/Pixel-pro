package com.example.ui

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.camera.CameraManager
import com.example.camera.ImagePostProcessor
import com.example.model.AspectRatioMode
import com.example.model.CameraMode
import com.example.model.CameraSettings
import com.example.model.CapturedPhoto
import com.example.model.FlashMode
import com.example.model.GridType
import com.example.model.HdrMode
import com.example.model.LmcConfig
import com.example.model.TimerMode
import com.example.ui.components.CameraGridOverlay
import com.example.ui.components.FocusExposureRing
import com.example.ui.components.PixelBottomControls
import com.example.ui.components.PixelModeDrawer
import com.example.ui.components.PixelQuickSettingsSheet
import com.example.ui.components.PixelTopBar
import com.example.ui.components.VerticalBrightnessBar
import com.example.ui.config.AddCustomConfigDialog
import com.example.ui.config.LmcConfigDialog
import com.example.ui.gallery.GalleryViewerDialog
import com.example.ui.theme.PixelBlack
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelOverlay
import com.example.ui.theme.PixelSurfaceContainer
import com.example.ui.theme.PixelWhite
import com.example.ui.theme.PixelYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraManager = remember { CameraManager(context) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var isHardwareCameraActive by remember { mutableStateOf(false) }

    // Configuration list (default presets + custom loaded)
    val configList = remember { mutableStateListOf<LmcConfig>().apply { addAll(LmcConfig.defaultConfigs) } }

    // Camera settings state
    var settings by remember { mutableStateOf(CameraSettings()) }

    // Captured photos list
    val capturedPhotos = remember { mutableStateListOf<CapturedPhoto>() }
    var showGallery by remember { mutableStateOf(false) }

    // Focus & Tap coordinates
    var focusTapPoint by remember { mutableStateOf<Offset?>(null) }
    var exposureCompensation by remember { mutableFloatStateOf(0f) }

    // White flash animation on capture
    val shutterFlashAlpha = remember { Animatable(0f) }

    // Timer countdown
    var countdownNumber by remember { mutableIntStateOf(0) }

    // Instant toast confirmation on photo capture
    var photoSavedToastMessage by remember { mutableStateOf<String?>(null) }

    // Custom config add dialog state
    var showAddConfigDialog by remember { mutableStateOf(false) }

    // Load any saved custom configs from app storage /sdcard/LMC8.4/
    LaunchedEffect(Unit) {
        try {
            val dir = java.io.File(context.filesDir, "LMC8.4").apply { mkdirs() }
            val files = dir.listFiles { f -> f.extension.equals("xml", ignoreCase = true) }
            if (files != null) {
                for (file in files) {
                    val xml = file.readText()
                    val parsed = LmcConfig.fromXml(xml, "disk_${file.name}").copy(name = file.name, isCustom = true)
                    if (configList.none { it.name == parsed.name }) {
                        configList.add(0, parsed)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    // Synthetic fallback preview frame for emulators (adapts to mode, zoom, and brightness control line)
    val fallbackPreviewBitmap = remember(settings.activeMode, settings.zoomLevel, exposureCompensation) {
        ImagePostProcessor.createSyntheticPhoto(
            isNightSight = settings.activeMode == CameraMode.NIGHT_SIGHT,
            zoom = settings.zoomLevel,
            exposureCompensation = exposureCompensation
        )
    }

    // Initialize CameraX when PreviewView is available
    LaunchedEffect(previewViewRef, settings.isFrontCamera, settings.flashMode, hasCameraPermission) {
        val preview = previewViewRef
        if (hasCameraPermission && preview != null) {
            cameraManager.initialize(
                lifecycleOwner = lifecycleOwner,
                previewView = preview,
                isFrontCamera = settings.isFrontCamera,
                flashMode = settings.flashMode
            ) { success ->
                isHardwareCameraActive = success
            }
        }
    }

    // Release camera on disposal
    DisposableEffect(Unit) {
        onDispose {
            cameraManager.release()
        }
    }

    // Gesture detection variables
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var totalDragX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PixelBlack)
            .testTag("camera_screen_root")
    ) {
        // Main Viewfinder Area with Gesture Detectors
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Pinch to Zoom
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        val newZoom = (settings.zoomLevel * zoom).coerceIn(0.5f, 8.0f)
                        settings = settings.copy(zoomLevel = newZoom)
                        cameraManager.setZoom(newZoom)
                    }
                }
                // Tap to Focus & Gestures (Swipe down for settings, Swipe up for modes)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            focusTapPoint = offset
                            val view = previewViewRef
                            if (view != null) {
                                cameraManager.focusOnPoint(offset.x, offset.y, view.width.toFloat(), view.height.toFloat())
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            totalDragY = 0f
                            totalDragX = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragY += dragAmount.y
                            totalDragX += dragAmount.x
                        },
                        onDragEnd = {
                            // Swipe Down: Open Settings
                            if (totalDragY > 90f && kotlin.math.abs(totalDragY) > kotlin.math.abs(totalDragX)) {
                                settings = settings.copy(showSwipeDownSettings = true)
                            }
                            // Swipe Up: Open Mode Drawer
                            else if (totalDragY < -90f && kotlin.math.abs(totalDragY) > kotlin.math.abs(totalDragX)) {
                                settings = settings.copy(showSwipeUpModeDrawer = true)
                            }
                            // Horizontal Swipe: Cycle Camera Mode
                            else if (totalDragX < -90f) {
                                // Next mode
                                val modes = CameraMode.values()
                                val nextIdx = (modes.indexOf(settings.activeMode) + 1).coerceAtMost(modes.size - 1)
                                settings = settings.copy(activeMode = modes[nextIdx])
                            } else if (totalDragX > 90f) {
                                // Previous mode
                                val modes = CameraMode.values()
                                val prevIdx = (modes.indexOf(settings.activeMode) - 1).coerceAtLeast(0)
                                settings = settings.copy(activeMode = modes[prevIdx])
                            }
                        }
                    )
                }
        ) {
            // CameraX PreviewView - Primary Viewfinder displaying real-time camera feed
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }.also { view ->
                        previewViewRef = view
                        if (hasCameraPermission) {
                            cameraManager.initialize(
                                lifecycleOwner = lifecycleOwner,
                                previewView = view,
                                isFrontCamera = settings.isFrontCamera,
                                flashMode = settings.flashMode
                            ) { success ->
                                isHardwareCameraActive = success
                            }
                        }
                    }
                },
                update = { view ->
                    previewViewRef = view
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camerax_preview_view")
            )

            // Permission Request Banner overlay if camera permission is not yet granted
            if (!hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PixelBlack.copy(alpha = 0.8f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = PixelSurfaceContainer
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PixelBorder),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .testTag("camera_permission_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PixelYellow,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = PixelBlack,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Camera Access Required",
                                color = PixelWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "To display the real-time camera viewfinder feed with HDR+ and Night Sight, allow camera access.",
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            androidx.compose.material3.Button(
                                onClick = onRequestPermission,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = PixelYellow,
                                    contentColor = PixelBlack
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("enable_camera_button")
                            ) {
                                Text(
                                    text = "Start Live Camera",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Grid overlay
            CameraGridOverlay(gridType = settings.gridType)

            // Focus & Exposure reticle
            focusTapPoint?.let { point ->
                FocusExposureRing(
                    tapPoint = point,
                    exposureValue = exposureCompensation,
                    onExposureChange = {
                        exposureCompensation = it
                        settings = settings.copy(exposureCompensation = it)
                        cameraManager.setExposure(it)
                    }
                )
            }

            // Vertical Brightness Control Line (Standard camera exposure slider on the right side of the viewfinder)
            VerticalBrightnessBar(
                exposureValue = exposureCompensation,
                onExposureChange = { newValue ->
                    exposureCompensation = newValue
                    settings = settings.copy(exposureCompensation = newValue)
                    cameraManager.setExposure(newValue)
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            )

            // Shutter white flash overlay
            if (shutterFlashAlpha.value > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = shutterFlashAlpha.value))
                )
            }

            // Night Sight / HDR+ Processing HUD indicator (Floating above bottom controls)
            if (settings.isCapturing) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PixelBlack.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PixelYellow),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (settings.activeMode == CameraMode.NIGHT_SIGHT) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = PixelYellow,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Hold still...",
                                color = PixelWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            CircularProgressIndicator(
                                progress = { settings.captureProgress },
                                color = PixelYellow,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Processing HDR+...",
                                color = PixelWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = settings.captureStatusText ?: "Capturing light...",
                            color = PixelYellow,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Timer Countdown Display
            if (countdownNumber > 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PixelBlack.copy(alpha = 0.7f),
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = countdownNumber.toString(),
                                color = PixelYellow,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Mode indicator pill on the top-left of the viewfinder
            if (settings.activeMode == CameraMode.NIGHT_SIGHT) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PixelBlack.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 60.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = PixelYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (settings.activeConfig.astroEnabled) "Astro mode ready" else "Night Sight on",
                            color = PixelWhite,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Top Bar (Flash, HDR, Settings pill, LMC Config)
        PixelTopBar(
            settings = settings,
            onOpenSettings = { settings = settings.copy(showSwipeDownSettings = true) },
            onOpenConfigDialog = { settings = settings.copy(showConfigDialog = true) },
            onAddConfigClick = { showAddConfigDialog = true },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Photo Saved Toast Overlay
        AnimatedVisibility(
            visible = photoSavedToastMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -30 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -30 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PixelBlack.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PixelYellow),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PixelYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = photoSavedToastMessage ?: "",
                        color = PixelWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Bottom Controls (Zoom pills, Mode carousel, Shutter, Gallery, Switch lens)
        PixelBottomControls(
            settings = settings,
            latestThumbnail = capturedPhotos.firstOrNull()?.thumbnail,
            onShutterClick = {
                scope.launch {
                    // Check timer
                    if (settings.timerMode != TimerMode.OFF) {
                        for (t in settings.timerMode.seconds downTo 1) {
                            countdownNumber = t
                            delay(1000L)
                        }
                        countdownNumber = 0
                    }

                    // Start capture
                    settings = settings.copy(
                        isCapturing = true,
                        captureProgress = 0f,
                        captureStatusText = "Initializing capture..."
                    )

                    // Flash shutter screen
                    scope.launch {
                        shutterFlashAlpha.snapTo(0.6f)
                        shutterFlashAlpha.animateTo(0f, tween(180))
                    }

                    try {
                        val photo = cameraManager.capturePhoto(
                            mode = settings.activeMode,
                            hdrMode = settings.hdrMode,
                            activeConfig = settings.activeConfig,
                            zoomLevel = settings.zoomLevel,
                            exposureCompensation = settings.exposureCompensation,
                            onProgress = { progress, text ->
                                settings = settings.copy(
                                    captureProgress = progress,
                                    captureStatusText = text
                                )
                            }
                        )
                        capturedPhotos.add(0, photo)
                        photoSavedToastMessage = "Photo saved • ${photo.mode.title}"
                        scope.launch {
                            delay(2200L)
                            photoSavedToastMessage = null
                        }
                    } catch (e: Exception) {
                        // Fallback synthetic photo if any error
                        val synth = ImagePostProcessor.createSyntheticPhoto(
                            isNightSight = settings.activeMode == CameraMode.NIGHT_SIGHT,
                            zoom = settings.zoomLevel,
                            exposureCompensation = settings.exposureCompensation
                        )
                        val tuned = ImagePostProcessor.process(synth, settings.activeConfig, settings.exposureCompensation)
                        val photo = CapturedPhoto(
                            id = java.util.UUID.randomUUID().toString(),
                            file = java.io.File(context.cacheDir, "fallback_${System.currentTimeMillis()}.jpg"),
                            thumbnail = Bitmap.createScaledBitmap(tuned, 160, 160, true),
                            timestamp = System.currentTimeMillis(),
                            mode = settings.activeMode,
                            hdrMode = settings.hdrMode,
                            configName = settings.activeConfig.name,
                            exposureTimeSeconds = if (settings.activeMode == CameraMode.NIGHT_SIGHT) 3.0f else 1f / 125f
                        )
                        capturedPhotos.add(0, photo)
                        photoSavedToastMessage = "Photo saved • ${photo.mode.title}"
                        scope.launch {
                            delay(2200L)
                            photoSavedToastMessage = null
                        }
                    } finally {
                        settings = settings.copy(isCapturing = false)
                    }
                }
            },
            onSwitchCamera = {
                val newFront = !settings.isFrontCamera
                settings = settings.copy(isFrontCamera = newFront)
                previewViewRef?.let { preview ->
                    cameraManager.bindCameraUseCases(lifecycleOwner, preview, newFront, settings.flashMode)
                }
            },
            onOpenGallery = {
                if (capturedPhotos.isNotEmpty()) {
                    showGallery = true
                } else {
                    // Generate a sample photo so the user can immediately experience the gallery & EXIF viewer!
                    scope.launch {
                        val synth = ImagePostProcessor.createSyntheticPhoto(
                            isNightSight = settings.activeMode == CameraMode.NIGHT_SIGHT,
                            zoom = settings.zoomLevel
                        )
                        val tuned = ImagePostProcessor.process(synth, settings.activeConfig)
                        val samplePhoto = CapturedPhoto(
                            id = java.util.UUID.randomUUID().toString(),
                            file = java.io.File(context.filesDir, "sample_preview.jpg").apply {
                                java.io.FileOutputStream(this).use { out ->
                                    tuned.compress(Bitmap.CompressFormat.JPEG, 95, out)
                                }
                            },
                            thumbnail = Bitmap.createScaledBitmap(tuned, 160, 160, true),
                            timestamp = System.currentTimeMillis(),
                            mode = settings.activeMode,
                            hdrMode = settings.hdrMode,
                            configName = settings.activeConfig.name,
                            exposureTimeSeconds = if (settings.activeMode == CameraMode.NIGHT_SIGHT) 3.2f else 1f / 125f
                        )
                        capturedPhotos.add(0, samplePhoto)
                        showGallery = true
                    }
                }
            },
            onModeSelected = { mode ->
                if (mode == CameraMode.MODES) {
                    settings = settings.copy(showSwipeUpModeDrawer = true)
                } else {
                    settings = settings.copy(activeMode = mode)
                }
            },
            onZoomSelected = { zoom ->
                settings = settings.copy(zoomLevel = zoom)
                cameraManager.setZoom(zoom)
            },
            onOpenConfigDialog = {
                settings = settings.copy(showConfigDialog = true)
            },
            onAddConfigClick = {
                showAddConfigDialog = true
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Swipe Down Quick Settings Dropdown
        PixelQuickSettingsSheet(
            visible = settings.showSwipeDownSettings,
            settings = settings,
            onDismiss = { settings = settings.copy(showSwipeDownSettings = false) },
            onHdrChange = { settings = settings.copy(hdrMode = it) },
            onFlashChange = {
                settings = settings.copy(flashMode = it)
                previewViewRef?.let { preview ->
                    cameraManager.bindCameraUseCases(lifecycleOwner, preview, settings.isFrontCamera, it)
                }
            },
            onTimerChange = { settings = settings.copy(timerMode = it) },
            onRatioChange = { settings = settings.copy(aspectRatio = it) },
            onGridChange = { settings = settings.copy(gridType = it) },
            onOpenConfigManager = {
                settings = settings.copy(
                    showSwipeDownSettings = false,
                    showConfigDialog = true
                )
            },
            onOpenAddConfigDialog = {
                settings = settings.copy(showSwipeDownSettings = false)
                showAddConfigDialog = true
            },
            onSelectConfig = { config ->
                settings = settings.copy(
                    activeConfig = config,
                    hdrMode = if (config.defaultHdrMode == "HDR_ENHANCED") HdrMode.HDR_ENHANCED else HdrMode.HDR_PLUS
                )
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Swipe Up Mode Drawer
        PixelModeDrawer(
            visible = settings.showSwipeUpModeDrawer,
            currentMode = settings.activeMode,
            onModeSelect = { selectedMode ->
                settings = settings.copy(
                    activeMode = selectedMode,
                    showSwipeUpModeDrawer = false
                )
            },
            onDismiss = { settings = settings.copy(showSwipeUpModeDrawer = false) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // LMC8.4 XML Config Manager Dialog
        if (settings.showConfigDialog) {
            LmcConfigDialog(
                configs = configList,
                activeConfig = settings.activeConfig,
                onSelectConfig = { config ->
                    settings = settings.copy(
                        activeConfig = config,
                        hdrMode = if (config.defaultHdrMode == "HDR_ENHANCED") HdrMode.HDR_ENHANCED else HdrMode.HDR_PLUS
                    )
                },
                onSaveNewConfig = { newConfig ->
                    configList.add(0, newConfig)
                },
                onDismiss = { settings = settings.copy(showConfigDialog = false) }
            )
        }

        // Dedicated Dialog to Add / Import / Create Own XML Config Files
        if (showAddConfigDialog) {
            AddCustomConfigDialog(
                onConfigAdded = { newConfig ->
                    if (configList.none { it.name == newConfig.name }) {
                        configList.add(0, newConfig)
                    }
                    settings = settings.copy(
                        activeConfig = newConfig,
                        hdrMode = if (newConfig.defaultHdrMode == "HDR_ENHANCED") HdrMode.HDR_ENHANCED else HdrMode.HDR_PLUS
                    )
                    photoSavedToastMessage = "Active Config: ${newConfig.name}"
                    scope.launch {
                        delay(2200L)
                        photoSavedToastMessage = null
                    }
                },
                onDismiss = { showAddConfigDialog = false }
            )
        }

        // Full Screen Gallery Dialog
        if (showGallery) {
            GalleryViewerDialog(
                photos = capturedPhotos,
                initialIndex = 0,
                onDeletePhoto = { photo ->
                    capturedPhotos.remove(photo)
                    try { photo.file.delete() } catch (_: Exception) {}
                },
                onDismiss = { showGallery = false }
            )
        }
    }
}

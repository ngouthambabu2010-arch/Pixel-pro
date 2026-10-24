package com.example.model

data class CameraSettings(
    val activeMode: CameraMode = CameraMode.CAMERA,
    val hdrMode: HdrMode = HdrMode.HDR_PLUS,
    val flashMode: FlashMode = FlashMode.OFF,
    val timerMode: TimerMode = TimerMode.OFF,
    val aspectRatio: AspectRatioMode = AspectRatioMode.RATIO_4_3,
    val gridType: GridType = GridType.OFF,
    val rawEnabled: Boolean = false,
    val nightSightAuto: Boolean = true,
    val zoomLevel: Float = 1.0f,
    val exposureCompensation: Float = 0f, // -2.0 to +2.0
    val activeConfig: LmcConfig = LmcConfig.defaultConfigs[0],
    val isFrontCamera: Boolean = false,
    val showSwipeDownSettings: Boolean = false,
    val showSwipeUpModeDrawer: Boolean = false,
    val showConfigDialog: Boolean = false,
    val isCapturing: Boolean = false,
    val captureProgress: Float = 0f, // 0.0 to 1.0 during Night Sight/HDR+ multi-frame capture
    val captureStatusText: String? = null
)

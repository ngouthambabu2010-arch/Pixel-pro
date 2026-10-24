package com.example.model

import java.io.StringReader
import java.io.StringWriter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import android.util.Xml

data class LmcConfig(
    val id: String,
    val name: String,
    val author: String = "LMC Team",
    val description: String = "Fine-tuned Google Camera profile",
    // Color Parameters
    val saturation: Float = 1.0f,          // 0.5f to 2.0f
    val contrast: Float = 1.0f,            // 0.5f to 2.0f
    val wbTempBias: Int = 0,               // -50 (Cool/Blue) to +50 (Warm/Amber)
    val tint: Int = 0,                     // -50 (Green) to +50 (Magenta)
    val colorMatrixPreset: String = "PIXEL_NATURAL", // PIXEL_NATURAL, LEICA_CLASSIC, LEICA_VIBRANT, ASTRO_DEEP, CINEMA_FILM
    val shadowLift: Float = 0.1f,          // 0.0 to 0.5
    val highlightCompression: Float = 0.2f,// 0.0 to 0.5

    // Noise Reduction Parameters
    val noiseReductionLevel: String = "HIGH", // OFF, LOW, MEDIUM, HIGH, ULTRA
    val chromaDenoise: Int = 70,           // 0 to 100
    val temporalSmoothing: Float = 0.5f,   // 0.0 to 1.0

    // Sharpness Parameters
    val edgeStrength: Float = 1.2f,        // 0.5 to 2.5
    val detailRadius: Float = 1.0f,        // 0.5 to 2.0
    val microContrast: Float = 1.1f,       // 0.8 to 1.8

    // Computational Mode & Stacking Parameters
    val defaultHdrMode: String = "HDR_PLUS", // OFF, HDR_PLUS, HDR_ENHANCED
    val burstFrames: Int = 12,             // 3 to 25 frames
    val nightSightExposureSec: Float = 3.0f, // 1.0 to 8.0s
    val astroEnabled: Boolean = false,
    val isCustom: Boolean = false
) {
    fun toXml(): String {
        val writer = StringWriter()
        val serializer: XmlSerializer = Xml.newSerializer()
        serializer.setOutput(writer)
        serializer.startDocument("UTF-8", true)
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)

        serializer.startTag("", "camera_config")
        serializer.attribute("", "version", "8.4")
        serializer.attribute("", "name", name)
        serializer.attribute("", "author", author)
        serializer.attribute("", "description", description)

        serializer.startTag("", "color_science")
        serializer.attribute("", "saturation", saturation.toString())
        serializer.attribute("", "contrast", contrast.toString())
        serializer.attribute("", "wb_temp_bias", wbTempBias.toString())
        serializer.attribute("", "tint", tint.toString())
        serializer.attribute("", "matrix", colorMatrixPreset)
        serializer.attribute("", "shadow_lift", shadowLift.toString())
        serializer.attribute("", "highlight_compression", highlightCompression.toString())
        serializer.endTag("", "color_science")

        serializer.startTag("", "noise_reduction")
        serializer.attribute("", "spatial_level", noiseReductionLevel)
        serializer.attribute("", "chroma_denoise", chromaDenoise.toString())
        serializer.attribute("", "temporal_smoothing", temporalSmoothing.toString())
        serializer.endTag("", "noise_reduction")

        serializer.startTag("", "sharpness")
        serializer.attribute("", "edge_strength", edgeStrength.toString())
        serializer.attribute("", "detail_radius", detailRadius.toString())
        serializer.attribute("", "micro_contrast", microContrast.toString())
        serializer.endTag("", "sharpness")

        serializer.startTag("", "computational")
        serializer.attribute("", "hdr_mode", defaultHdrMode)
        serializer.attribute("", "burst_frames", burstFrames.toString())
        serializer.attribute("", "night_sight_exposure_sec", nightSightExposureSec.toString())
        serializer.attribute("", "astro_enabled", astroEnabled.toString())
        serializer.endTag("", "computational")

        serializer.endTag("", "camera_config")
        serializer.endDocument()
        return writer.toString()
    }

    companion object {
        fun fromXml(xmlContent: String, fallbackId: String = "custom_xml"): LmcConfig {
            try {
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = false
                val parser = factory.newPullParser()
                parser.setInput(StringReader(xmlContent))

                var name = "Imported Config"
                var author = "LMC User"
                var description = "Custom imported XML config"
                var saturation = 1.0f
                var contrast = 1.0f
                var wbTempBias = 0
                var tint = 0
                var matrix = "PIXEL_NATURAL"
                var shadowLift = 0.1f
                var highlightCompression = 0.2f
                var noiseReductionLevel = "HIGH"
                var chromaDenoise = 70
                var temporalSmoothing = 0.5f
                var edgeStrength = 1.2f
                var detailRadius = 1.0f
                var microContrast = 1.1f
                var defaultHdrMode = "HDR_PLUS"
                var burstFrames = 12
                var nightSightExposureSec = 3.0f
                var astroEnabled = false

                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        when (parser.name) {
                            "camera_config" -> {
                                name = parser.getAttributeValue("", "name") ?: name
                                author = parser.getAttributeValue("", "author") ?: author
                                description = parser.getAttributeValue("", "description") ?: description
                            }
                            "color_science" -> {
                                saturation = parser.getAttributeValue("", "saturation")?.toFloatOrNull() ?: saturation
                                contrast = parser.getAttributeValue("", "contrast")?.toFloatOrNull() ?: contrast
                                wbTempBias = parser.getAttributeValue("", "wb_temp_bias")?.toIntOrNull() ?: wbTempBias
                                tint = parser.getAttributeValue("", "tint")?.toIntOrNull() ?: tint
                                matrix = parser.getAttributeValue("", "matrix") ?: matrix
                                shadowLift = parser.getAttributeValue("", "shadow_lift")?.toFloatOrNull() ?: shadowLift
                                highlightCompression = parser.getAttributeValue("", "highlight_compression")?.toFloatOrNull() ?: highlightCompression
                            }
                            "noise_reduction" -> {
                                noiseReductionLevel = parser.getAttributeValue("", "spatial_level") ?: noiseReductionLevel
                                chromaDenoise = parser.getAttributeValue("", "chroma_denoise")?.toIntOrNull() ?: chromaDenoise
                                temporalSmoothing = parser.getAttributeValue("", "temporal_smoothing")?.toFloatOrNull() ?: temporalSmoothing
                            }
                            "sharpness" -> {
                                edgeStrength = parser.getAttributeValue("", "edge_strength")?.toFloatOrNull() ?: edgeStrength
                                detailRadius = parser.getAttributeValue("", "detail_radius")?.toFloatOrNull() ?: detailRadius
                                microContrast = parser.getAttributeValue("", "micro_contrast")?.toFloatOrNull() ?: microContrast
                            }
                            "computational" -> {
                                defaultHdrMode = parser.getAttributeValue("", "hdr_mode") ?: defaultHdrMode
                                burstFrames = parser.getAttributeValue("", "burst_frames")?.toIntOrNull() ?: burstFrames
                                nightSightExposureSec = parser.getAttributeValue("", "night_sight_exposure_sec")?.toFloatOrNull() ?: nightSightExposureSec
                                astroEnabled = parser.getAttributeValue("", "astro_enabled")?.toBooleanStrictOrNull() ?: astroEnabled
                            }
                            // Also support legacy GCam preference key-values:
                            "string", "int", "boolean", "float" -> {
                                val key = parser.getAttributeValue("", "name") ?: ""
                                val value = parser.nextText()
                                when {
                                    key.contains("saturation", ignoreCase = true) -> saturation = value.toFloatOrNull() ?: saturation
                                    key.contains("contrast", ignoreCase = true) -> contrast = value.toFloatOrNull() ?: contrast
                                    key.contains("sharpness", ignoreCase = true) -> edgeStrength = value.toFloatOrNull() ?: edgeStrength
                                    key.contains("denoise", ignoreCase = true) -> chromaDenoise = value.toIntOrNull() ?: chromaDenoise
                                    key.contains("frames", ignoreCase = true) -> burstFrames = value.toIntOrNull() ?: burstFrames
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }

                return LmcConfig(
                    id = fallbackId,
                    name = name,
                    author = author,
                    description = description,
                    saturation = saturation.coerceIn(0.2f, 2.5f),
                    contrast = contrast.coerceIn(0.5f, 2.5f),
                    wbTempBias = wbTempBias.coerceIn(-50, 50),
                    tint = tint.coerceIn(-50, 50),
                    colorMatrixPreset = matrix,
                    shadowLift = shadowLift.coerceIn(0f, 0.5f),
                    highlightCompression = highlightCompression.coerceIn(0f, 0.5f),
                    noiseReductionLevel = noiseReductionLevel,
                    chromaDenoise = chromaDenoise.coerceIn(0, 100),
                    temporalSmoothing = temporalSmoothing.coerceIn(0f, 1f),
                    edgeStrength = edgeStrength.coerceIn(0.3f, 3.0f),
                    detailRadius = detailRadius.coerceIn(0.5f, 2.5f),
                    microContrast = microContrast.coerceIn(0.5f, 2.5f),
                    defaultHdrMode = defaultHdrMode,
                    burstFrames = burstFrames.coerceIn(3, 30),
                    nightSightExposureSec = nightSightExposureSec.coerceIn(1f, 15f),
                    astroEnabled = astroEnabled,
                    isCustom = true
                )
            } catch (e: Exception) {
                return defaultConfigs[0].copy(
                    id = fallbackId,
                    name = "Parsed (Fallback)",
                    description = "Recovered default parameters",
                    isCustom = true
                )
            }
        }

        val defaultConfigs: List<LmcConfig> = listOf(
            LmcConfig(
                id = "lmc84_pixel_authentic",
                name = "LMC8.4_Pixel_Authentic.xml",
                author = "Hasli (LMC)",
                description = "True-to-life Google Pixel 8 Pro color science with balanced HDR+ tone curve & natural sharpness",
                saturation = 1.08f,
                contrast = 1.05f,
                wbTempBias = 0,
                tint = 0,
                colorMatrixPreset = "PIXEL_NATURAL",
                shadowLift = 0.15f,
                highlightCompression = 0.25f,
                noiseReductionLevel = "HIGH",
                chromaDenoise = 75,
                temporalSmoothing = 0.65f,
                edgeStrength = 1.25f,
                detailRadius = 1.1f,
                microContrast = 1.15f,
                defaultHdrMode = "HDR_PLUS",
                burstFrames = 15,
                nightSightExposureSec = 3.2f,
                astroEnabled = false
            ),
            LmcConfig(
                id = "lmc84_leica_vibrant",
                name = "Leica_M_Vibrant_Pro.xml",
                author = "Leica Tuner",
                description = "Iconic Leica color tuning: punchy warm reds, deep oceanic blues, high micro-contrast & rich vignetted shadows",
                saturation = 1.25f,
                contrast = 1.20f,
                wbTempBias = 12,
                tint = 4,
                colorMatrixPreset = "LEICA_VIBRANT",
                shadowLift = 0.05f,
                highlightCompression = 0.35f,
                noiseReductionLevel = "MEDIUM",
                chromaDenoise = 60,
                temporalSmoothing = 0.50f,
                edgeStrength = 1.45f,
                detailRadius = 1.2f,
                microContrast = 1.40f,
                defaultHdrMode = "HDR_ENHANCED",
                burstFrames = 18,
                nightSightExposureSec = 4.0f,
                astroEnabled = false
            ),
            LmcConfig(
                id = "lmc84_nightsight_astro",
                name = "NightSight_Astro_Ultra.xml",
                author = "SkyWatcher",
                description = "Extreme low-light sensitivity, deep astronomical blue-black sky tone, 24-frame multi-burst stacking and aggressive luma denoising",
                saturation = 1.12f,
                contrast = 1.15f,
                wbTempBias = -15,
                tint = -5,
                colorMatrixPreset = "ASTRO_DEEP",
                shadowLift = 0.35f,
                highlightCompression = 0.15f,
                noiseReductionLevel = "ULTRA",
                chromaDenoise = 95,
                temporalSmoothing = 0.90f,
                edgeStrength = 1.10f,
                detailRadius = 0.9f,
                microContrast = 1.30f,
                defaultHdrMode = "HDR_ENHANCED",
                burstFrames = 24,
                nightSightExposureSec = 6.0f,
                astroEnabled = true
            ),
            LmcConfig(
                id = "lmc84_natural_portrait",
                name = "Natural_Portrait_SoftSkin.xml",
                author = "PortraitPro",
                description = "Subtle highlight softening, delicate golden-hour skin tones, reduced high-frequency facial noise and simulated f/1.8 depth blur",
                saturation = 0.98f,
                contrast = 0.96f,
                wbTempBias = 8,
                tint = 2,
                colorMatrixPreset = "PIXEL_NATURAL",
                shadowLift = 0.20f,
                highlightCompression = 0.40f,
                noiseReductionLevel = "HIGH",
                chromaDenoise = 80,
                temporalSmoothing = 0.70f,
                edgeStrength = 0.90f,
                detailRadius = 0.8f,
                microContrast = 0.95f,
                defaultHdrMode = "HDR_PLUS",
                burstFrames = 12,
                nightSightExposureSec = 2.5f,
                astroEnabled = false
            ),
            LmcConfig(
                id = "lmc84_film_cinema",
                name = "Pro_Cinema_TealOrange.xml",
                author = "Cinematographer",
                description = "Modern Hollywood cinema grading: teal shadows, warm amber highlights, lifted matte black floor and organic film texture",
                saturation = 1.18f,
                contrast = 1.10f,
                wbTempBias = 6,
                tint = -8,
                colorMatrixPreset = "CINEMA_FILM",
                shadowLift = 0.25f,
                highlightCompression = 0.30f,
                noiseReductionLevel = "LOW",
                chromaDenoise = 50,
                temporalSmoothing = 0.40f,
                edgeStrength = 1.15f,
                detailRadius = 1.0f,
                microContrast = 1.25f,
                defaultHdrMode = "HDR_ENHANCED",
                burstFrames = 15,
                nightSightExposureSec = 3.0f,
                astroEnabled = false
            )
        )
    }
}

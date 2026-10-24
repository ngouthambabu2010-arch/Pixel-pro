package com.example.ui.config

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.LmcConfig
import com.example.ui.theme.PixelBlack
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelGreen
import com.example.ui.theme.PixelSurfaceContainer
import com.example.ui.theme.PixelSurfaceDark
import com.example.ui.theme.PixelSurfaceVariant
import com.example.ui.theme.PixelTextSecondary
import com.example.ui.theme.PixelWhite
import com.example.ui.theme.PixelYellow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Dedicated dialog allowing users to add their own custom XML config files.
 * Provides 3 methods:
 * 1. Browse and import existing .xml file from device storage or downloads
 * 2. Paste raw XML configuration text directly (with sample GCam/LMC XML template helper)
 * 3. Visually customize parameters (Color, Sharpness, Noise Reduction) and export as XML
 */
@Composable
fun AddCustomConfigDialog(
    onConfigAdded: (LmcConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Browse File, 1: Paste XML, 2: Create Custom

    // Paste Tab states
    var pasteName by remember { mutableStateOf("") }
    var pasteXmlContent by remember { mutableStateOf("") }
    var pasteError by remember { mutableStateOf<String?>(null) }

    // Visual Creator Tab states
    var customName by remember { mutableStateOf("My_Custom_Profile.xml") }
    var customAuthor by remember { mutableStateOf("Me") }
    var customDescription by remember { mutableStateOf("Custom tuned GCam profile") }
    var customSaturation by remember { mutableFloatStateOf(1.15f) }
    var customContrast by remember { mutableFloatStateOf(1.10f) }
    var customSharpness by remember { mutableFloatStateOf(1.30f) }
    var customNoiseReduction by remember { mutableStateOf("HIGH") }
    var customNightSightExp by remember { mutableFloatStateOf(3.5f) }
    var customBurstFrames by remember { mutableIntStateOf(16) }

    // File Picker Launcher for XML files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    var fileName = "Imported_Config.xml"
                    uri.lastPathSegment?.let { segment ->
                        val clean = segment.substringAfterLast("/").substringAfterLast(":")
                        if (clean.isNotBlank()) {
                            fileName = if (clean.endsWith(".xml", ignoreCase = true)) clean else "$clean.xml"
                        }
                    }

                    // Parse config
                    val parsed = LmcConfig.fromXml(content, "imported_${System.currentTimeMillis()}").copy(
                        name = fileName,
                        isCustom = true
                    )

                    // Save to app files dir /LMC8.4/
                    saveConfigToDisk(context, parsed)

                    onConfigAdded(parsed)
                    Toast.makeText(context, "Loaded: ${parsed.name}", Toast.LENGTH_SHORT).show()
                    onDismiss()
                } else {
                    Toast.makeText(context, "Selected file was empty", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read XML: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, PixelBorder, RoundedCornerShape(26.dp))
                .testTag("add_custom_config_dialog"),
            color = PixelSurfaceDark,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PixelYellow,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = PixelBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Add Config File",
                                color = PixelWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Load XML tuning into /sdcard/LMC8.4/",
                                color = PixelTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_config_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PixelTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PixelSurfaceContainer,
                    contentColor = PixelYellow,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PixelYellow,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Browse File", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Paste XML", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Create New", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        // TAB 0: Browse and Pick XML File
                        0 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = CircleShape,
                                    color = PixelYellow.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PixelYellow),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.UploadFile,
                                            contentDescription = null,
                                            tint = PixelYellow,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Select XML File from Storage",
                                    color = PixelWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Choose any .xml config file downloaded from GCam communities, Telegram channels, or phone storage.",
                                    color = PixelTextSecondary,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { filePickerLauncher.launch("*/*") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PixelYellow,
                                        contentColor = PixelBlack
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(48.dp)
                                        .testTag("dialog_browse_file_action_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Browse Device Files",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Fast preset shortcuts
                                Text(
                                    text = "Or load a community tuning profile:",
                                    color = PixelTextSecondary,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    PresetChip("Leica Vibrant") {
                                        val c = LmcConfig.defaultConfigs[1].copy(
                                            id = "custom_leica_${System.currentTimeMillis()}",
                                            name = "Leica_Vibrant_Custom.xml",
                                            isCustom = true
                                        )
                                        saveConfigToDisk(context, c)
                                        onConfigAdded(c)
                                        onDismiss()
                                    }
                                    PresetChip("Ultra Astro") {
                                        val c = LmcConfig.defaultConfigs[2].copy(
                                            id = "custom_astro_${System.currentTimeMillis()}",
                                            name = "Astro_Ultra_Custom.xml",
                                            isCustom = true
                                        )
                                        saveConfigToDisk(context, c)
                                        onConfigAdded(c)
                                        onDismiss()
                                    }
                                }
                            }
                        }

                        // TAB 1: Paste Raw XML
                        1 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Config File Name",
                                        color = PixelWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    // Button to fill sample template
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PixelYellow.copy(alpha = 0.2f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                pasteName = "Community_Custom_${System.currentTimeMillis().toString().takeLast(4)}.xml"
                                                pasteXmlContent = LmcConfig.defaultConfigs[0].toXml()
                                            }
                                            .testTag("fill_sample_xml_template_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = PixelYellow,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Sample Template",
                                                color = PixelYellow,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = pasteName,
                                    onValueChange = { pasteName = it },
                                    placeholder = { Text("e.g. My_Film_Recipe.xml") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PixelYellow,
                                        unfocusedBorderColor = PixelBorder,
                                        focusedTextColor = PixelWhite,
                                        unfocusedTextColor = PixelWhite
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("paste_config_name_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "XML Content",
                                    color = PixelWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = pasteXmlContent,
                                    onValueChange = {
                                        pasteXmlContent = it
                                        pasteError = null
                                    },
                                    placeholder = { Text("Paste <camera_config> or GCam XML string preferences here...") },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = PixelWhite
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = PixelBlack,
                                        unfocusedContainerColor = PixelBlack,
                                        focusedBorderColor = PixelYellow,
                                        unfocusedBorderColor = PixelBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .testTag("paste_xml_content_input")
                                )

                                pasteError?.let { err ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = err,
                                        color = Color(0xFFFF5252),
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (pasteXmlContent.isBlank()) {
                                            pasteError = "Please enter or paste XML configuration content"
                                            return@Button
                                        }
                                        val finalName = if (pasteName.isNotBlank()) {
                                            if (pasteName.endsWith(".xml", ignoreCase = true)) pasteName else "$pasteName.xml"
                                        } else {
                                            "Pasted_Config_${System.currentTimeMillis().toString().takeLast(4)}.xml"
                                        }
                                        val parsed = LmcConfig.fromXml(pasteXmlContent, "pasted_${System.currentTimeMillis()}").copy(
                                            name = finalName,
                                            isCustom = true
                                        )
                                        saveConfigToDisk(context, parsed)
                                        onConfigAdded(parsed)
                                        Toast.makeText(context, "Added config: $finalName", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PixelYellow,
                                        contentColor = PixelBlack
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("submit_pasted_xml_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Import XML & Activate", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        // TAB 2: Visual Creator
                        2 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "Config Name (.xml)",
                                    color = PixelWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PixelYellow,
                                        unfocusedBorderColor = PixelBorder,
                                        focusedTextColor = PixelWhite,
                                        unfocusedTextColor = PixelWhite
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("creator_config_name_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Author", color = PixelWhite, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = customAuthor,
                                            onValueChange = { customAuthor = it },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = PixelYellow,
                                                unfocusedBorderColor = PixelBorder,
                                                focusedTextColor = PixelWhite,
                                                unfocusedTextColor = PixelWhite
                                            ),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = PixelBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Saturation Slider
                                SliderRow(
                                    label = "Color Saturation",
                                    value = customSaturation,
                                    range = 0.5f..2.0f,
                                    format = "%.2fx",
                                    onValueChange = { customSaturation = it }
                                )

                                // Contrast Slider
                                SliderRow(
                                    label = "Tone Contrast",
                                    value = customContrast,
                                    range = 0.5f..2.0f,
                                    format = "%.2fx",
                                    onValueChange = { customContrast = it }
                                )

                                // Sharpness Slider
                                SliderRow(
                                    label = "Sharpness & Micro-Contrast",
                                    value = customSharpness,
                                    range = 0.5f..2.5f,
                                    format = "%.2fx",
                                    onValueChange = { customSharpness = it }
                                )

                                // Night Sight Exposure
                                SliderRow(
                                    label = "Night Sight Max Exposure",
                                    value = customNightSightExp,
                                    range = 1.0f..8.0f,
                                    format = "%.1fs",
                                    onValueChange = { customNightSightExp = it }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val finalName = if (customName.isNotBlank()) {
                                            if (customName.endsWith(".xml", ignoreCase = true)) customName else "$customName.xml"
                                        } else {
                                            "Custom_Profile_${System.currentTimeMillis().toString().takeLast(4)}.xml"
                                        }
                                        val newConfig = LmcConfig(
                                            id = "custom_${System.currentTimeMillis()}",
                                            name = finalName,
                                            author = customAuthor.ifBlank { "User" },
                                            description = customDescription,
                                            saturation = customSaturation,
                                            contrast = customContrast,
                                            edgeStrength = customSharpness,
                                            noiseReductionLevel = customNoiseReduction,
                                            burstFrames = customBurstFrames,
                                            nightSightExposureSec = customNightSightExp,
                                            isCustom = true
                                        )
                                        saveConfigToDisk(context, newConfig)
                                        onConfigAdded(newConfig)
                                        Toast.makeText(context, "Saved & Applied $finalName", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PixelYellow,
                                        contentColor = PixelBlack
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("create_and_apply_custom_config_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generate XML & Activate", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = PixelSurfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, PixelBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = PixelYellow,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, color = PixelWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    format: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = PixelWhite, fontSize = 11.sp)
            Text(
                text = String.format(format, value),
                color = PixelYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = PixelYellow,
                activeTrackColor = PixelYellow,
                inactiveTrackColor = PixelSurfaceContainer
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}

/**
 * Persists an LmcConfig as an XML file into internal storage /sdcard/LMC8.4/ equivalent.
 */
fun saveConfigToDisk(context: android.content.Context, config: LmcConfig) {
    try {
        val dir = File(context.filesDir, "LMC8.4").apply { mkdirs() }
        val file = File(dir, config.name)
        val xml = config.toXml()
        FileOutputStream(file).use { out ->
            out.write(xml.toByteArray())
        }
    } catch (_: Exception) {}
}

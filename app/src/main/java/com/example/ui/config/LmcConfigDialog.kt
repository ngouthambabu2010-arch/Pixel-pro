package com.example.ui.config

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.PixelOverlay
import com.example.ui.theme.PixelSurfaceContainer
import com.example.ui.theme.PixelSurfaceDark
import com.example.ui.theme.PixelSurfaceVariant
import com.example.ui.theme.PixelTextSecondary
import com.example.ui.theme.PixelWhite
import com.example.ui.theme.PixelYellow
import java.util.UUID

@Composable
fun LmcConfigDialog(
    configs: List<LmcConfig>,
    activeConfig: LmcConfig,
    onSelectConfig: (LmcConfig) -> Unit,
    onSaveNewConfig: (LmcConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedForLoading by remember { mutableStateOf(activeConfig) }
    var workingConfig by remember(activeConfig) { mutableStateOf(activeConfig) }
    var customXmlInput by remember { mutableStateOf(activeConfig.toXml()) }
    var saveNameInput by remember { mutableStateOf("") }
    var showSavePrompt by remember { mutableStateOf(false) }

    var showPasteDialog by remember { mutableStateOf(false) }
    var pasteXmlInput by remember { mutableStateOf("") }
    var pasteNameInput by remember { mutableStateOf("") }

    val xmlFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    var chosenName = "Custom_Config.xml"
                    uri.lastPathSegment?.let { seg ->
                        val clean = seg.substringAfterLast("/").substringAfterLast(":")
                        if (clean.isNotBlank()) {
                            chosenName = if (clean.endsWith(".xml")) clean else "$clean.xml"
                        }
                    }
                    val parsed = LmcConfig.fromXml(content, "imported_${System.currentTimeMillis()}").copy(
                        name = chosenName,
                        isCustom = true
                    )
                    onSaveNewConfig(parsed)
                    onSelectConfig(parsed)
                    onDismiss()
                }
            } catch (_: Exception) {}
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, PixelBorder, RoundedCornerShape(24.dp))
                .testTag("lmc_config_dialog"),
            color = PixelSurfaceDark,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header mimicking LMC 8.4 XML selection dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PixelYellow,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = PixelBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "LMC8.4 Config Loader",
                                color = PixelWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = PixelYellow,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "/sdcard/LMC8.4/",
                                    color = PixelTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick file picker button in header
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PixelYellow,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { xmlFilePickerLauncher.launch("*/*") }
                                .testTag("header_add_config_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Config",
                                    tint = PixelBlack,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Add File",
                                    color = PixelBlack,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_config_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = PixelTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
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
                        text = { Text("Config Files", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Parameters", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            customXmlInput = workingConfig.toXml()
                            selectedTab = 2
                        },
                        text = { Text("XML Code", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> ConfigListTab(
                            configs = configs,
                            selected = selectedForLoading,
                            activeConfig = activeConfig,
                            onSelect = {
                                selectedForLoading = it
                                workingConfig = it
                            },
                            onBrowseFileClick = { xmlFilePickerLauncher.launch("*/*") },
                            onPasteXmlClick = { showPasteDialog = true }
                        )
                        1 -> ParameterTunerTab(
                            config = workingConfig,
                            onChange = { workingConfig = it }
                        )
                        2 -> XmlCodeTab(
                            xml = customXmlInput,
                            onXmlChange = { customXmlInput = it },
                            onParseAndApply = {
                                val parsed = LmcConfig.fromXml(customXmlInput, "custom_${UUID.randomUUID().toString().take(6)}")
                                workingConfig = parsed
                                selectedForLoading = parsed
                                onSaveNewConfig(parsed)
                                onSelectConfig(parsed)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showSavePrompt = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = PixelWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save XML", color = PixelWhite, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onSelectConfig(if (selectedTab == 1) workingConfig else selectedForLoading)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PixelYellow,
                            contentColor = PixelBlack
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("load_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Load / Apply",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Dialog to name saved config
                if (showSavePrompt) {
                    Dialog(onDismissRequest = { showSavePrompt = false }) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PixelSurfaceContainer),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Save Custom XML Config",
                                    color = PixelWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = saveNameInput,
                                    onValueChange = { saveNameInput = it },
                                    label = { Text("Config Name (e.g. My_Custom_Tuning.xml)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PixelYellow,
                                        unfocusedBorderColor = PixelBorder,
                                        focusedLabelColor = PixelYellow,
                                        unfocusedLabelColor = PixelTextSecondary,
                                        focusedTextColor = PixelWhite,
                                        unfocusedTextColor = PixelWhite
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(onClick = { showSavePrompt = false }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val finalName = if (saveNameInput.isNotBlank()) {
                                                if (saveNameInput.endsWith(".xml")) saveNameInput else "$saveNameInput.xml"
                                            } else {
                                                "Custom_${System.currentTimeMillis().toString().takeLast(4)}.xml"
                                            }
                                            val saved = workingConfig.copy(
                                                id = "custom_${System.currentTimeMillis()}",
                                                name = finalName,
                                                author = "User",
                                                isCustom = true
                                            )
                                            onSaveNewConfig(saved)
                                            selectedForLoading = saved
                                            showSavePrompt = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PixelYellow,
                                            contentColor = PixelBlack
                                        )
                                    ) {
                                        Text("Save", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Dialog to paste custom XML config code
                if (showPasteDialog) {
                    Dialog(onDismissRequest = { showPasteDialog = false }) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PixelSurfaceContainer),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PixelYellow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Import XML Config Code",
                                    color = PixelWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = pasteNameInput,
                                    onValueChange = { pasteNameInput = it },
                                    label = { Text("Config Name (e.g. My_Profile.xml)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PixelYellow,
                                        unfocusedBorderColor = PixelBorder,
                                        focusedLabelColor = PixelYellow,
                                        unfocusedLabelColor = PixelTextSecondary,
                                        focusedTextColor = PixelWhite,
                                        unfocusedTextColor = PixelWhite
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = pasteXmlInput,
                                    onValueChange = { pasteXmlInput = it },
                                    placeholder = { Text("Paste <camera_config> or XML here...") },
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
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(onClick = { showPasteDialog = false }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (pasteXmlInput.isNotBlank()) {
                                                val name = if (pasteNameInput.isNotBlank()) {
                                                    if (pasteNameInput.endsWith(".xml")) pasteNameInput else "$pasteNameInput.xml"
                                                } else {
                                                    "Pasted_Config_${System.currentTimeMillis().toString().takeLast(4)}.xml"
                                                }
                                                val parsed = LmcConfig.fromXml(pasteXmlInput, "pasted_${System.currentTimeMillis()}").copy(
                                                    name = name,
                                                    isCustom = true
                                                )
                                                onSaveNewConfig(parsed)
                                                onSelectConfig(parsed)
                                                showPasteDialog = false
                                                onDismiss()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PixelYellow,
                                            contentColor = PixelBlack
                                        )
                                    ) {
                                        Text("Import & Apply", fontWeight = FontWeight.Bold)
                                    }
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
private fun ConfigListTab(
    configs: List<LmcConfig>,
    selected: LmcConfig,
    activeConfig: LmcConfig,
    onSelect: (LmcConfig) -> Unit,
    onBrowseFileClick: () -> Unit,
    onPasteXmlClick: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("configs_list")
    ) {
        // Dedicated button card to add your own config files
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PixelSurfaceVariant.copy(alpha = 0.65f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PixelYellow.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .testTag("add_custom_config_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = PixelYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Add Your Own Config File (.xml)",
                            color = PixelWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Import custom XML tunings from storage or paste config code directly.",
                        color = PixelTextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onBrowseFileClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PixelYellow,
                                contentColor = PixelBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("browse_config_file_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Browse File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onPasteXmlClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("paste_xml_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = PixelWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste XML", color = PixelWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        items(configs) { item ->
            val isSelected = item.id == selected.id
            val isCurrentActive = item.id == activeConfig.id

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) PixelYellow.copy(alpha = 0.15f) else PixelSurfaceContainer,
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) PixelYellow else PixelBorder.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelect(item) }
                    .testTag("config_item_${item.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = if (isSelected) PixelYellow else PixelTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = item.name,
                                color = if (isSelected) PixelYellow else PixelWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (isCurrentActive) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PixelGreen.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PixelGreen)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = PixelGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.description,
                        color = PixelTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Parameter tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ParamChip("Sat: ${item.saturation}x")
                        ParamChip("NR: ${item.noiseReductionLevel}")
                        ParamChip("Sharp: ${item.edgeStrength}x")
                        ParamChip("Burst: ${item.burstFrames}f")
                    }
                }
            }
        }
    }
}

@Composable
private fun ParamChip(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = PixelSurfaceDark
    ) {
        Text(
            text = label,
            color = PixelWhite.copy(alpha = 0.8f),
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ParameterTunerTab(
    config: LmcConfig,
    onChange: (LmcConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 4.dp)
    ) {
        // Section: Color Science
        Text(
            text = "COLOR SCIENCE & TONE",
            color = PixelYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Saturation Slider
        SliderSetting(
            label = "Saturation",
            value = config.saturation,
            range = 0.5f..2.0f,
            format = "%.2fx",
            onValueChange = { onChange(config.copy(saturation = it)) }
        )

        // Contrast Slider
        SliderSetting(
            label = "Contrast",
            value = config.contrast,
            range = 0.5f..2.0f,
            format = "%.2fx",
            onValueChange = { onChange(config.copy(contrast = it)) }
        )

        // White Balance Temperature bias
        SliderSetting(
            label = "WB Temperature Bias",
            value = config.wbTempBias.toFloat(),
            range = -40f..40f,
            format = "%+.0f",
            onValueChange = { onChange(config.copy(wbTempBias = it.toInt())) }
        )

        // Tint
        SliderSetting(
            label = "Tint (Green / Magenta)",
            value = config.tint.toFloat(),
            range = -30f..30f,
            format = "%+.0f",
            onValueChange = { onChange(config.copy(tint = it.toInt())) }
        )

        Spacer(modifier = Modifier.height(14.dp))
        Divider(color = PixelBorder.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(10.dp))

        // Section: Noise Reduction
        Text(
            text = "NOISE REDUCTION ENGINE",
            color = PixelYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Spatial NR Level selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("LOW", "MEDIUM", "HIGH", "ULTRA").forEach { lvl ->
                val isSelected = config.noiseReductionLevel == lvl
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) PixelYellow else PixelSurfaceContainer,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onChange(config.copy(noiseReductionLevel = lvl)) }
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = lvl,
                            color = if (isSelected) PixelBlack else PixelWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SliderSetting(
            label = "Chroma Denoise",
            value = config.chromaDenoise.toFloat(),
            range = 0f..100f,
            format = "%.0f%%",
            onValueChange = { onChange(config.copy(chromaDenoise = it.toInt())) }
        )

        Spacer(modifier = Modifier.height(14.dp))
        Divider(color = PixelBorder.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(10.dp))

        // Section: Sharpness
        Text(
            text = "SHARPNESS & DETAIL",
            color = PixelYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        SliderSetting(
            label = "Edge Strength",
            value = config.edgeStrength,
            range = 0.5f..2.5f,
            format = "%.2fx",
            onValueChange = { onChange(config.copy(edgeStrength = it)) }
        )

        SliderSetting(
            label = "Micro-Contrast",
            value = config.microContrast,
            range = 0.8f..1.8f,
            format = "%.2fx",
            onValueChange = { onChange(config.copy(microContrast = it)) }
        )

        Spacer(modifier = Modifier.height(14.dp))
        Divider(color = PixelBorder.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(10.dp))

        // Section: Computational HDR+ & Night Sight
        Text(
            text = "COMPUTATIONAL PHOTOGRAPHY",
            color = PixelYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        SliderSetting(
            label = "HDR+ Multi-Frame Burst Count",
            value = config.burstFrames.toFloat(),
            range = 3f..25f,
            format = "%.0f frames",
            onValueChange = { onChange(config.copy(burstFrames = it.toInt())) }
        )

        SliderSetting(
            label = "Night Sight Exposure Integration",
            value = config.nightSightExposureSec,
            range = 1.0f..8.0f,
            format = "%.1fs",
            onValueChange = { onChange(config.copy(nightSightExposureSec = it)) }
        )
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    format: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = PixelWhite, fontSize = 12.sp)
            Text(
                text = String.format(format, value),
                color = PixelYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
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
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
private fun XmlCodeTab(
    xml: String,
    onXmlChange: (String) -> Unit,
    onParseAndApply: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "XML Configuration Source",
                color = PixelYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onParseAndApply,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelYellow,
                    contentColor = PixelBlack
                ),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Parse & Apply", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = xml,
            onValueChange = onXmlChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
            )
        )
    }
}

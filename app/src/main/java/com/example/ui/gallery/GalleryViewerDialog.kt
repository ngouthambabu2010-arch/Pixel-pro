package com.example.ui.gallery

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.model.CapturedPhoto
import com.example.ui.theme.PixelBlack
import com.example.ui.theme.PixelBorder
import com.example.ui.theme.PixelOverlay
import com.example.ui.theme.PixelSurfaceContainer
import com.example.ui.theme.PixelSurfaceDark
import com.example.ui.theme.PixelTextSecondary
import com.example.ui.theme.PixelWhite
import com.example.ui.theme.PixelYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GalleryViewerDialog(
    photos: List<CapturedPhoto>,
    initialIndex: Int = 0,
    onDeletePhoto: (CapturedPhoto) -> Unit,
    onDismiss: () -> Unit
) {
    if (photos.isEmpty()) {
        onDismiss()
        return
    }

    var currentIndex by remember { mutableStateOf(initialIndex.coerceIn(0, photos.size - 1)) }
    val currentPhoto = photos.getOrNull(currentIndex) ?: return
    val context = LocalContext.current
    var showInfoSheet by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PixelBlack)
                .testTag("gallery_viewer_dialog")
        ) {
            // Photo Image
            val bitmap = remember(currentPhoto.file.absolutePath) {
                try {
                    BitmapFactory.decodeFile(currentPhoto.file.absolutePath)
                } catch (e: Exception) {
                    currentPhoto.thumbnail
                }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("gallery_photo_image")
                )
            }

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(PixelOverlay)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("gallery_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PixelWhite
                    )
                }

                // Date & Index title
                val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                val dateString = dateFormat.format(Date(currentPhoto.timestamp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dateString,
                        color = PixelWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${currentIndex + 1} of ${photos.size}",
                        color = PixelTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row {
                    IconButton(onClick = { showInfoSheet = !showInfoSheet }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Photo Details",
                            tint = if (showInfoSheet) PixelYellow else PixelWhite
                        )
                    }
                    IconButton(
                        onClick = {
                            try {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    currentPhoto.file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Photo"))
                            } catch (e: Exception) {
                                // Fallback normal send
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Photo"))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = PixelWhite
                        )
                    }
                }
            }

            // Bottom Bar: Metadata Pill and Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(PixelOverlay)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Applied XML Config & Computational Mode pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PixelSurfaceContainer.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PixelBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = PixelYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${currentPhoto.mode.title} • ${currentPhoto.configName}",
                            color = PixelWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (photos.size > 1) {
                        IconButton(
                            onClick = {
                                if (currentIndex > 0) currentIndex--
                            },
                            enabled = currentIndex > 0
                        ) {
                            Text(
                                text = "Prev",
                                color = if (currentIndex > 0) PixelWhite else PixelTextSecondary.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            onDeletePhoto(currentPhoto)
                            if (photos.size <= 1) {
                                onDismiss()
                            } else {
                                currentIndex = (currentIndex - 1).coerceAtLeast(0)
                            }
                        },
                        modifier = Modifier.testTag("delete_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Photo",
                            tint = PixelWhite
                        )
                    }

                    if (photos.size > 1) {
                        IconButton(
                            onClick = {
                                if (currentIndex < photos.size - 1) currentIndex++
                            },
                            enabled = currentIndex < photos.size - 1
                        ) {
                            Text(
                                text = "Next",
                                color = if (currentIndex < photos.size - 1) PixelWhite else PixelTextSecondary.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // EXIF / Computational info sheet
            if (showInfoSheet) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PixelSurfaceDark.copy(alpha = 0.95f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PixelBorder),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Computational EXIF Details",
                            color = PixelYellow,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        InfoRow("Mode", currentPhoto.mode.title)
                        InfoRow("Processing", currentPhoto.hdrMode.label)
                        InfoRow("LMC Config", currentPhoto.configName)
                        val expStr = if (currentPhoto.exposureTimeSeconds >= 1f) {
                            String.format("%.1fs", currentPhoto.exposureTimeSeconds)
                        } else {
                            "1/125s"
                        }
                        InfoRow("Exposure", expStr)
                        InfoRow("ISO", "ISO ${currentPhoto.iso}")
                        InfoRow("Lens", String.format("%.0fmm (f/%.2f)", currentPhoto.focalLengthMm, currentPhoto.aperture))
                        InfoRow("Resolution", "${currentPhoto.width} × ${currentPhoto.height}")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = PixelTextSecondary, fontSize = 12.sp)
        Text(text = value, color = PixelWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoQualityOption
import com.example.player.TrackInfo
import com.example.ui.theme.LocalAppThemeColors

@Composable
fun AudioTrackDialog(
    audioTracks: List<TrackInfo>,
    subtitleTracks: List<TrackInfo>,
    isSubtitleEnabled: Boolean,
    onSelectAudio: (String) -> Unit,
    onSelectSubtitle: (String) -> Unit,
    onToggleSubtitle: () -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalAppThemeColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(theme.card)
            .border(1.dp, theme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(22.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "AUDIO & SUBTITLES",
                color = theme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle Toggle Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.background)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("Subtitles On / Off", color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Switch(
                    checked = isSubtitleEnabled,
                    onCheckedChange = { onToggleSubtitle() },
                    colors = SwitchDefaults.colors(checkedThumbColor = theme.primary, checkedTrackColor = theme.primary.copy(alpha = 0.3f))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Audio Tracks", color = theme.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            if (audioTracks.isEmpty()) {
                Text("Default audio stream", color = theme.textPrimary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 6.dp))
            } else {
                LazyColumn(modifier = Modifier.height(120.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(audioTracks) { track ->
                        TrackSelectRow(
                            title = track.name,
                            isSelected = track.isSelected,
                            onClick = { onSelectAudio(track.language) }
                        )
                    }
                }
            }

            if (isSubtitleEnabled && subtitleTracks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Subtitle Languages", color = theme.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(modifier = Modifier.height(100.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subtitleTracks) { track ->
                        TrackSelectRow(
                            title = track.name,
                            isSelected = track.isSelected,
                            onClick = { onSelectSubtitle(track.language) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("CLOSE", color = theme.background, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VideoQualityDialog(
    currentQuality: VideoQualityOption,
    onSelectQuality: (VideoQualityOption) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalAppThemeColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(theme.card)
            .border(1.dp, theme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(22.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "SELECT VIDEO QUALITY",
                color = theme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            val options = VideoQualityOption.entries
            LazyColumn(modifier = Modifier.height(240.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(options) { opt ->
                    val isSelected = currentQuality == opt
                    TrackSelectRow(
                        title = opt.label,
                        isSelected = isSelected,
                        onClick = {
                            onSelectQuality(opt)
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("CLOSE", color = theme.background, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TrackSelectRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = when {
        isFocused -> theme.primary.copy(alpha = 0.3f)
        isSelected -> theme.primary.copy(alpha = 0.15f)
        else -> theme.background
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(
                width = if (isFocused) 1.5.dp else if (isSelected) 1.dp else 0.dp,
                color = if (isFocused) theme.focusBorder else if (isSelected) theme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) theme.primary else theme.textPrimary,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = "Selected", tint = theme.primary)
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppThemeColors

data class QuickMenuItem(
    val title: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun QuickActionMenu(
    isOpen: Boolean,
    onOpenChannelList: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenProviders: () -> Unit,
    onOpenNumericKeypad: () -> Unit,
    onOpenQualityDialog: () -> Unit,
    onOpenAudioDialog: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onTriggerPip: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current

    val items = remember {
        listOf(
            QuickMenuItem("Channel List", Icons.Default.List, onOpenChannelList),
            QuickMenuItem("Providers", Icons.Default.Dns, onOpenProviders),
            QuickMenuItem("Keypad", Icons.Default.Dialpad, onOpenNumericKeypad),
            QuickMenuItem("TV Guide", Icons.Default.LiveTv, onOpenGuide),
            QuickMenuItem("Quality", Icons.Default.HighQuality, onOpenQualityDialog),
            QuickMenuItem("Audio/Sub", Icons.Default.Audiotrack, onOpenAudioDialog),
            QuickMenuItem("Aspect Ratio", Icons.Default.AspectRatio, onCycleAspectRatio),
            QuickMenuItem("PiP Mode", Icons.Default.PictureInPicture, onTriggerPip),
            QuickMenuItem("About", Icons.Default.Info, onOpenAbout)
        )
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.background.copy(alpha = 0.95f))
                .border(width = 1.dp, color = theme.primary.copy(alpha = 0.3f), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "QUICK CONTROL MENU",
                    color = theme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items) { item ->
                        QuickMenuCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickMenuCard(item: QuickMenuItem) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = if (isFocused) theme.primary else theme.card
    val contentColor = if (isFocused) theme.background else theme.textPrimary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(width = 100.dp, height = 75.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) theme.focusBorder else theme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = item.action)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

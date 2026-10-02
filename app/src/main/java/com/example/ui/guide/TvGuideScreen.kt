package com.example.ui.guide

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.ui.CurrentScreen
import com.example.ui.ZaynTvViewModel
import com.example.ui.ZaynUiState
import com.example.ui.theme.LocalAppThemeColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TvGuideScreen(
    viewModel: ZaynTvViewModel,
    uiState: ZaynUiState,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current
    var selectedTab by remember { mutableStateOf("ALL") } // ALL, NOW, NEXT

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(20.dp)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { viewModel.navigateTo(CurrentScreen.LIVE_TV) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ELECTRONIC PROGRAM GUIDE (EPG)",
                color = theme.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: TV Guide, Now Playing, Next Up
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("ALL" to "Full TV Guide", "NOW" to "Now Playing", "NEXT" to "Up Next").forEach { (tabId, label) ->
                val isSelected = selectedTab == tabId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) theme.primary else theme.card)
                        .clickable { selectedTab = tabId }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) theme.background else theme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Channel EPG Rows
        val channels = uiState.filteredChannels.ifEmpty { uiState.channels }
        if (channels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No channels available. Add an IPTV provider.", color = theme.textSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(channels, key = { it.id }) { channel ->
                    GuideChannelRow(
                        channel = channel,
                        viewMode = selectedTab,
                        onSelect = {
                            viewModel.playChannel(channel)
                            viewModel.navigateTo(CurrentScreen.LIVE_TV)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideChannelRow(
    channel: Channel,
    viewMode: String,
    onSelect: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val currentProg = channel.currentProgram
    val nextProg = channel.nextProgram

    val bg = if (isFocused) theme.primary.copy(alpha = 0.25f) else theme.card
    val borderColor = if (isFocused) theme.focusBorder else theme.primary.copy(alpha = 0.15f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(width = if (isFocused) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onSelect)
            .padding(14.dp)
    ) {
        // Channel ID / Name box
        Column(modifier = Modifier.width(140.dp)) {
            Text(
                text = channel.formattedNumber,
                color = theme.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = channel.displayName,
                color = theme.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = channel.displayGroup,
                color = theme.textSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // NOW Program Box
        if (viewMode == "ALL" || viewMode == "NOW") {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.primary.copy(alpha = 0.15f))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "NOW PLAYING",
                        color = theme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${timeFormat.format(Date(currentProg.startTimeEpochMs))} - ${timeFormat.format(Date(currentProg.endTimeEpochMs))}",
                        color = theme.textSecondary,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = currentProg.title,
                    color = theme.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { currentProg.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = theme.primary,
                    trackColor = theme.background
                )
            }
        }

        // NEXT Program Box
        if (viewMode == "ALL" || viewMode == "NEXT") {
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier
                    .weight(if (viewMode == "ALL") 0.85f else 1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.secondary.copy(alpha = 0.15f))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "NEXT",
                        color = theme.secondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = timeFormat.format(Date(nextProg.startTimeEpochMs)),
                        color = theme.textSecondary,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = nextProg.title,
                    color = theme.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

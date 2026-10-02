package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.data.model.Channel
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.ZaynLiveRed

@Composable
fun ChannelListOverlay(
    isOpen: Boolean,
    channels: List<Channel>,
    currentChannel: Channel?,
    categories: List<Category>,
    selectedCategory: String,
    favorites: Set<String>,
    onSelectChannel: (Channel) -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current
    val listState = rememberLazyListState()

    var highlightedChannel by remember(currentChannel, channels) {
        mutableStateOf(currentChannel ?: channels.firstOrNull())
    }

    LaunchedEffect(isOpen, currentChannel) {
        if (isOpen && currentChannel != null) {
            highlightedChannel = currentChannel
            val idx = channels.indexOfFirst { it.id == currentChannel.id }
            if (idx >= 0) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInHorizontally { -it },
        exit = fadeOut() + slideOutHorizontally { -it },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(720.dp)
                .background(theme.surface.copy(alpha = 0.96f))
                .border(width = 1.dp, color = theme.primary.copy(alpha = 0.25f), shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxHeight()) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TV GUIDE & CHANNELS",
                            color = theme.primary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "• ${channels.size} Available",
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // DTH 3-Column Layout: Left (Categories) | Center (Channels) | Right (Now/Next EPG)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // PANEL 1: Categories
                    val allCategories = remember(categories) {
                        val list = mutableListOf("All", "Favorites")
                        categories.forEach { if (it.name != "All" && it.name != "Favorites") list.add(it.name) }
                        list
                    }

                    LazyColumn(
                        modifier = Modifier
                            .width(160.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.card.copy(alpha = 0.6f))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allCategories) { catName ->
                            val isSelected = selectedCategory.equals(catName, ignoreCase = true)
                            val catInteraction = remember { MutableInteractionSource() }
                            val isFocused by catInteraction.collectIsFocusedAsState()

                            val bg = when {
                                isFocused -> theme.primary
                                isSelected -> theme.primary.copy(alpha = 0.25f)
                                else -> Color.Transparent
                            }
                            val txtColor = when {
                                isFocused -> theme.background
                                isSelected -> theme.primary
                                else -> theme.textSecondary
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .border(
                                        width = if (isFocused) 1.5.dp else if (isSelected) 1.dp else 0.dp,
                                        color = if (isFocused) theme.focusBorder else if (isSelected) theme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .focusable(interactionSource = catInteraction)
                                    .clickable(interactionSource = catInteraction, indication = null) {
                                        onSelectCategory(catName)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = catName,
                                    color = txtColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // PANEL 2: Channels List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(channels, key = { it.id }) { ch ->
                            val isPlaying = currentChannel?.id == ch.id
                            val isHighlighted = highlightedChannel?.id == ch.id
                            val isFav = favorites.contains(ch.id)

                            val chInteraction = remember { MutableInteractionSource() }
                            val isFocused by chInteraction.collectIsFocusedAsState()

                            LaunchedEffect(isFocused) {
                                if (isFocused) highlightedChannel = ch
                            }

                            val bg = when {
                                isFocused -> theme.primary.copy(alpha = 0.35f)
                                isHighlighted -> theme.card
                                isPlaying -> theme.primary.copy(alpha = 0.15f)
                                else -> theme.card.copy(alpha = 0.5f)
                            }
                            val borderColor = when {
                                isFocused -> theme.focusBorder
                                isPlaying -> theme.primary
                                else -> Color.Transparent
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .border(if (isFocused || isPlaying) 1.5.dp else 0.dp, borderColor, RoundedCornerShape(8.dp))
                                    .focusable(interactionSource = chInteraction)
                                    .clickable(interactionSource = chInteraction, indication = null) {
                                        highlightedChannel = ch
                                        onSelectChannel(ch)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ch.formattedNumber,
                                        color = if (isPlaying) theme.primary else theme.textSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(34.dp)
                                    )

                                    if (!ch.logoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = ch.logoUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Column {
                                        Text(
                                            text = ch.displayName,
                                            color = if (isPlaying) theme.primary else theme.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = if (isPlaying || isFocused) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isPlaying) {
                                            Text("▶ PLAYING", color = ZaynLiveRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { onToggleFavorite(ch.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav) Color(0xFFFFD700) else theme.textSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // PANEL 3: EPG & Now/Next Info for highlighted channel
                    highlightedChannel?.let { ch ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.card)
                                .border(1.dp, theme.primary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!ch.logoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ch.logoUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Column {
                                    Text(
                                        text = "${ch.formattedNumber}  ${ch.displayName}",
                                        color = theme.textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = ch.displayGroup,
                                        color = theme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Now Playing EPG
                            Text("NOW PLAYING", color = ZaynLiveRed, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ch.currentProgram.title,
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (ch.currentProgram.timeRange.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(ch.currentProgram.timeRange, color = theme.textSecondary, fontSize = 11.sp)
                            }
                            if (ch.currentProgram.progressPercent > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { ch.currentProgram.progressPercent / 100f },
                                    color = theme.primary,
                                    trackColor = theme.background,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                            }
                            if (ch.currentProgram.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ch.currentProgram.description,
                                    color = theme.textSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Next Program EPG
                            Text("UP NEXT", color = theme.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ch.nextProgram.title,
                                color = theme.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (ch.nextProgram.timeRange.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(ch.nextProgram.timeRange, color = theme.textSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

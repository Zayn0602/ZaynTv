package com.example.ui.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Channel
import com.example.data.model.Movie
import com.example.ui.CurrentScreen
import com.example.ui.ZaynTvViewModel
import com.example.ui.ZaynUiState
import com.example.ui.theme.LocalAppThemeColors

@Composable
fun HomeScreen(
    viewModel: ZaynTvViewModel,
    uiState: ZaynUiState,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // Modern DTH Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(theme.card, theme.background)
                        )
                    )
                    .padding(28.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_zayntv_logo),
                        contentDescription = "ZaynTV Official Logo",
                        modifier = Modifier
                            .height(60.dp)
                            .padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Premium Live Television & VOD Entertainment",
                        color = theme.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Button(
                            onClick = {
                                if (uiState.channels.isNotEmpty()) {
                                    val target = uiState.currentChannel ?: uiState.channels.first()
                                    viewModel.playChannel(target)
                                } else {
                                    viewModel.navigateTo(CurrentScreen.ADD_PROVIDER)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = theme.background)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WATCH LIVE TV", color = theme.background, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.navigateTo(CurrentScreen.GUIDE) },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.card)
                        ) {
                            Icon(Icons.Default.LiveTv, contentDescription = null, tint = theme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TV GUIDE (EPG)", color = theme.primary, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.navigateTo(CurrentScreen.SEARCH) },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.card)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = theme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SEARCH", color = theme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Navigation Cards
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { QuickNavTile(title = "Live TV", icon = Icons.Default.Tv, count = "${uiState.channels.size} Ch", onClick = { viewModel.navigateTo(CurrentScreen.LIVE_TV) }) }
                item { QuickNavTile(title = "TV Guide", icon = Icons.Default.LiveTv, count = "EPG Grid", onClick = { viewModel.navigateTo(CurrentScreen.GUIDE) }) }
                item { QuickNavTile(title = "Movies", icon = Icons.Default.Movie, count = "${uiState.movies.size} Titles", onClick = { viewModel.navigateTo(CurrentScreen.MOVIES) }) }
                item { QuickNavTile(title = "Series", icon = Icons.Default.Category, count = "${uiState.series.size} Shows", onClick = { viewModel.navigateTo(CurrentScreen.SERIES) }) }
                item { QuickNavTile(title = "Providers", icon = Icons.Default.Add, count = "${uiState.providers.size} Configured", onClick = { viewModel.navigateTo(CurrentScreen.PROVIDERS) }) }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Favorite Channels Row
        val favChannels = uiState.channels.filter { uiState.favorites.contains(it.id) }
        if (favChannels.isNotEmpty()) {
            item {
                SectionHeader("Favorite Channels (${favChannels.size})")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(favChannels) { channel ->
                        ChannelHomeCard(
                            channel = channel,
                            onPlay = { viewModel.playChannel(channel) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(22.dp))
            }
        }

        // Recently Watched Row
        val recentChannels = uiState.recentChannelIds.mapNotNull { id -> uiState.channels.find { it.id == id } }
        if (recentChannels.isNotEmpty()) {
            item {
                SectionHeader("Recently Watched")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentChannels) { channel ->
                        ChannelHomeCard(
                            channel = channel,
                            onPlay = { viewModel.playChannel(channel) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(22.dp))
            }
        }

        // Live Channels Quick Row
        if (uiState.channels.isNotEmpty()) {
            item {
                SectionHeader("All Channels (${uiState.channels.size})")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.channels.take(20)) { channel ->
                        ChannelHomeCard(
                            channel = channel,
                            onPlay = { viewModel.playChannel(channel) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(22.dp))
            }
        }

        // Movies Row
        if (uiState.movies.isNotEmpty()) {
            item {
                SectionHeader("VOD Movies (${uiState.movies.size})")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.movies.take(12)) { movie ->
                        MovieHomeCard(
                            movie = movie,
                            onClick = {
                                viewModel.playerManager.playVod(movie.streamUrl, movie.title)
                                viewModel.navigateTo(CurrentScreen.LIVE_TV)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(22.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val theme = LocalAppThemeColors.current
    Text(
        text = title,
        color = theme.textPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun QuickNavTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: String,
    onClick: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = if (isFocused) theme.primary.copy(alpha = 0.25f) else theme.card
    val borderColor = if (isFocused) theme.focusBorder else theme.primary.copy(alpha = 0.15f)

    Box(
        modifier = Modifier
            .size(width = 150.dp, height = 75.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(width = if (isFocused) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = theme.primary, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(count, color = theme.textSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ChannelHomeCard(
    channel: Channel,
    onPlay: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = if (isFocused) theme.primary.copy(alpha = 0.25f) else theme.card
    val borderColor = if (isFocused) theme.focusBorder else theme.primary.copy(alpha = 0.15f)

    Box(
        modifier = Modifier
            .size(width = 180.dp, height = 105.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(width = if (isFocused) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onPlay)
            .padding(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = channel.formattedNumber,
                    color = theme.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = channel.displayGroup,
                    color = theme.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Text(
                text = channel.displayName,
                color = theme.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MovieHomeCard(
    movie: Movie,
    onClick: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = if (isFocused) theme.secondary.copy(alpha = 0.25f) else theme.card

    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 170.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) theme.focusBorder else theme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Text(
                text = movie.title,
                color = theme.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            if (movie.year.isNotBlank()) {
                Text(movie.year, color = theme.textSecondary, fontSize = 11.sp)
            }
        }
    }
}

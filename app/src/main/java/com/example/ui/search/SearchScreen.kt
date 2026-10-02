package com.example.ui.search

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.data.model.Movie
import com.example.ui.CurrentScreen
import com.example.ui.ZaynTvViewModel
import com.example.ui.ZaynUiState
import com.example.ui.theme.LocalAppThemeColors

@Composable
fun SearchScreen(
    viewModel: ZaynTvViewModel,
    uiState: ZaynUiState,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Top Bar & Search Input
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

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Search channels, numbers, movies, series...", color = theme.textSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.primary) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = theme.textSecondary)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = theme.primary.copy(alpha = 0.3f),
                    focusedTextColor = theme.textPrimary,
                    unfocusedTextColor = theme.textPrimary,
                    focusedContainerColor = theme.card,
                    unfocusedContainerColor = theme.card
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Android TV Remote-Friendly Letter Pad
        val quickKeys = remember {
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                   "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                   "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                   "U", "V", "W", "X", "Y", "Z", "SPACE", "BACK")
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickKeys) { key ->
                TvSearchKey(
                    label = key,
                    onClick = {
                        when (key) {
                            "SPACE" -> viewModel.onSearchQueryChange(uiState.searchQuery + " ")
                            "BACK" -> if (uiState.searchQuery.isNotEmpty()) viewModel.onSearchQueryChange(uiState.searchQuery.dropLast(1))
                            else -> viewModel.onSearchQueryChange(uiState.searchQuery + key)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results Section
        val results = uiState.searchResults
        if (uiState.searchQuery.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Type channel name, number, or movie title",
                        color = theme.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else if (results.channels.isEmpty() && results.movies.isEmpty() && results.series.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No results found for \"${uiState.searchQuery}\"",
                    color = theme.textSecondary,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Live Channels
                if (results.channels.isNotEmpty()) {
                    item {
                        Text(
                            text = "LIVE CHANNELS (${results.channels.size})",
                            color = theme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    items(results.channels, key = { it.id }) { channel ->
                        SearchChannelRow(
                            channel = channel,
                            onClick = {
                                viewModel.playChannel(channel)
                                viewModel.navigateTo(CurrentScreen.LIVE_TV)
                            }
                        )
                    }
                }

                // Movies
                if (results.movies.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "MOVIES (${results.movies.size})",
                            color = theme.secondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    items(results.movies, key = { it.id }) { movie ->
                        SearchMovieRow(
                            movie = movie,
                            onClick = {
                                viewModel.playerManager.playVod(movie.streamUrl, movie.title)
                                viewModel.navigateTo(CurrentScreen.LIVE_TV)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvSearchKey(
    label: String,
    onClick: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = if (isFocused) theme.primary else theme.card
    val contentColor = if (isFocused) theme.background else theme.textPrimary

    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) theme.focusBorder else theme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (label == "BACK") {
            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", tint = contentColor, modifier = Modifier.size(16.dp))
        } else {
            Text(text = label, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SearchChannelRow(
    channel: Channel,
    onClick: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = if (isFocused) theme.primary.copy(alpha = 0.25f) else theme.card

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) theme.focusBorder else theme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tv, contentDescription = null, tint = theme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = channel.formattedNumber,
                color = theme.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = channel.displayName,
                    color = theme.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = channel.displayGroup,
                    color = theme.textSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SearchMovieRow(
    movie: Movie,
    onClick: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = if (isFocused) theme.secondary.copy(alpha = 0.25f) else theme.card

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) theme.focusBorder else theme.secondary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Movie, contentDescription = null, tint = theme.secondary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = movie.title,
                    color = theme.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                if (movie.year.isNotBlank() || movie.genre.isNotBlank()) {
                    Text(
                        text = listOf(movie.year, movie.genre).filter { it.isNotBlank() }.joinToString(" • "),
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

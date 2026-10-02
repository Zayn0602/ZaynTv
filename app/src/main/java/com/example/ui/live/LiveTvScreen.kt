package com.example.ui.live

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AspectRatioMode
import com.example.player.PlaybackState
import com.example.ui.CurrentScreen
import com.example.ui.ZaynTvViewModel
import com.example.ui.ZaynUiState
import com.example.ui.components.AudioTrackDialog
import com.example.ui.components.ChannelInfoOverlay
import com.example.ui.components.ChannelListOverlay
import com.example.ui.components.QuickActionMenu
import com.example.ui.components.TvPlayerView
import com.example.ui.components.VideoQualityDialog
import com.example.ui.components.VirtualNumericKeypad
import com.example.ui.parental.ParentalPinDialog
import com.example.ui.theme.LocalAppThemeColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveTvScreen(
    viewModel: ZaynTvViewModel,
    uiState: ZaynUiState,
    playbackState: PlaybackState,
    onTriggerPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current
    var totalDragY by remember { mutableFloatStateOf(0f) }

    var isAudioDialogOpen by remember { mutableStateOf(false) }
    var isQualityDialogOpen by remember { mutableStateOf(false) }

    val audioTracks by viewModel.playerManager.audioTracks.collectAsState()
    val subtitleTracks by viewModel.playerManager.subtitleTracks.collectAsState()
    val isSubtitleEnabled by viewModel.playerManager.isSubtitleEnabled.collectAsState()
    val currentQuality by viewModel.playerManager.currentQuality.collectAsState()

    var currentTimeString by remember {
        mutableStateOf(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            delay(30000L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { totalDragY = 0f },
                    onDragEnd = {
                        if (totalDragY < -100f) {
                            viewModel.switchChannelPrev()
                        } else if (totalDragY > 100f) {
                            viewModel.switchChannelNext()
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        totalDragY += dragAmount
                    }
                )
            }
    ) {
        // Core Fullscreen Player (Tapping anywhere reveals auto-hiding controls)
        TvPlayerView(
            playerManager = viewModel.playerManager,
            playbackState = playbackState,
            currentChannel = uiState.currentChannel,
            aspectRatio = uiState.settings.aspectRatio,
            watermarkEnabled = uiState.settings.watermarkEnabled,
            watermarkPosition = uiState.settings.watermarkPosition,
            watermarkOpacity = uiState.settings.watermarkOpacity,
            watermarkSize = uiState.settings.watermarkSize,
            onRetry = { viewModel.playerManager.retryCurrentChannel() },
            onNextChannel = { viewModel.switchChannelNext() },
            onOpenChannelList = { viewModel.toggleChannelList() },
            onTapPlayer = { viewModel.toggleControlsOverlay() },
            modifier = Modifier.fillMaxSize()
        )

        // DTH Top Bar (Requirement 2 & 3: [ZaynTV Logo]  [Search]  [Providers]  [⚙ Settings])
        AnimatedVisibility(
            visible = uiState.isControlsOverlayVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                color = theme.card.copy(alpha = 0.94f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.35f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    // Left Section: ZaynTV Brand Logo + Category
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.resetControlsInactivityTimer()
                                    viewModel.toggleChannelList()
                                }
                                .padding(4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_zayntv_brand_logo),
                                contentDescription = "ZaynTV Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(width = 115.dp, height = 36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Category Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.primary.copy(alpha = 0.15f))
                                .border(1.dp, theme.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.resetControlsInactivityTimer()
                                    viewModel.toggleChannelList()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = uiState.selectedCategory.ifBlank { "All Channels" },
                                color = theme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Center & Right Section: Search, Providers, ⚙ Settings, Time, Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick Zapping Controls
                        IconButton(onClick = {
                            viewModel.resetControlsInactivityTimer()
                            viewModel.switchChannelPrev()
                        }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Prev Channel", tint = theme.textPrimary)
                        }

                        IconButton(onClick = {
                            viewModel.resetControlsInactivityTimer()
                            viewModel.toggleChannelList()
                        }) {
                            Icon(Icons.Default.List, contentDescription = "Channel List", tint = theme.primary)
                        }

                        IconButton(onClick = {
                            viewModel.resetControlsInactivityTimer()
                            viewModel.switchChannelNext()
                        }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next Channel", tint = theme.textPrimary)
                        }

                        // Search Button
                        OutlinedButton(
                            onClick = {
                                viewModel.resetControlsInactivityTimer()
                                viewModel.navigateTo(CurrentScreen.SEARCH)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = theme.card),
                            border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = theme.textPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Search", color = theme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Providers Button
                        OutlinedButton(
                            onClick = {
                                viewModel.resetControlsInactivityTimer()
                                viewModel.navigateTo(CurrentScreen.PROVIDERS)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = theme.card),
                            border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Dns, contentDescription = "Providers", tint = theme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Providers", color = theme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Home Button (Access to main dashboard and central settings)
                        OutlinedButton(
                            onClick = {
                                viewModel.resetControlsInactivityTimer()
                                viewModel.navigateTo(CurrentScreen.HOME)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = theme.card),
                            border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = theme.textPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Home", color = theme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Keypad Button
                        IconButton(onClick = {
                            viewModel.resetControlsInactivityTimer()
                            viewModel.toggleNumericKeypad()
                        }) {
                            Icon(Icons.Default.Dialpad, contentDescription = "Keypad", tint = theme.textPrimary)
                        }

                        // Live Time Clock
                        Text(
                            text = currentTimeString,
                            color = theme.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        // Program and Channel Information Overlay (Auto-hides after timeout)
        ChannelInfoOverlay(
            isVisible = uiState.isControlsOverlayVisible,
            channel = uiState.currentChannel,
            isFavorite = uiState.favorites.contains(uiState.currentChannel?.id ?: ""),
            quality = currentQuality,
            onToggleFavorite = {
                viewModel.resetControlsInactivityTimer()
                viewModel.toggleFavorite(it)
            },
            onOpenSettings = {
                viewModel.resetControlsInactivityTimer()
                viewModel.navigateTo(CurrentScreen.SETTINGS)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // DTH 3-Column Channel List & Guide Drawer
        ChannelListOverlay(
            isOpen = uiState.isChannelListOpen,
            channels = uiState.filteredChannels,
            currentChannel = uiState.currentChannel,
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            favorites = uiState.favorites,
            onSelectChannel = {
                viewModel.resetControlsInactivityTimer()
                viewModel.playChannel(it)
            },
            onSelectCategory = {
                viewModel.resetControlsInactivityTimer()
                viewModel.selectCategory(it)
            },
            onToggleFavorite = {
                viewModel.resetControlsInactivityTimer()
                viewModel.toggleFavorite(it)
            },
            onClose = { viewModel.toggleChannelList() },
            modifier = Modifier.align(Alignment.CenterStart)
        )

        // Virtual Numeric Keypad (Center)
        if (uiState.isNumericKeypadOpen) {
            VirtualNumericKeypad(
                input = uiState.numericInput,
                onDigitPress = { viewModel.onNumericKeyPress(it) },
                onBackspace = { viewModel.onNumericBackspace() },
                onConfirm = { viewModel.commitNumericChannel() },
                onClose = { viewModel.toggleNumericKeypad() },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Quick Action Menu (Bottom Sheet style)
        QuickActionMenu(
            isOpen = uiState.isQuickMenuOpen,
            onOpenChannelList = { viewModel.toggleChannelList() },
            onOpenGuide = { viewModel.navigateTo(CurrentScreen.GUIDE) },
            onOpenProviders = { viewModel.navigateTo(CurrentScreen.PROVIDERS) },
            onOpenNumericKeypad = { viewModel.toggleNumericKeypad() },
            onOpenQualityDialog = { isQualityDialogOpen = true },
            onOpenAudioDialog = { isAudioDialogOpen = true },
            onCycleAspectRatio = {
                viewModel.resetControlsInactivityTimer()
                val next = when (uiState.settings.aspectRatio) {
                    AspectRatioMode.FIT -> AspectRatioMode.FILL
                    AspectRatioMode.FILL -> AspectRatioMode.ZOOM
                    AspectRatioMode.ZOOM -> AspectRatioMode.SIXTEEN_NINE
                    AspectRatioMode.SIXTEEN_NINE -> AspectRatioMode.FOUR_THREE
                    AspectRatioMode.FOUR_THREE -> AspectRatioMode.ORIGINAL
                    AspectRatioMode.ORIGINAL -> AspectRatioMode.FIT
                }
                viewModel.setAspectRatio(next)
                viewModel.showToast("Aspect Ratio: ${next.name}")
            },
            onTriggerPip = onTriggerPip,
            onOpenSettings = { viewModel.navigateTo(CurrentScreen.SETTINGS) },
            onOpenAbout = { viewModel.navigateTo(CurrentScreen.SETTINGS) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Audio & Subtitle Track Selection Dialog
        if (isAudioDialogOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xBB000000))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                AudioTrackDialog(
                    audioTracks = audioTracks,
                    subtitleTracks = subtitleTracks,
                    isSubtitleEnabled = isSubtitleEnabled,
                    onSelectAudio = {
                        viewModel.playerManager.selectAudioLanguage(it)
                        viewModel.showToast("Audio track set to $it")
                    },
                    onSelectSubtitle = {
                        viewModel.playerManager.selectSubtitleLanguage(it)
                        viewModel.showToast("Subtitle set to $it")
                    },
                    onToggleSubtitle = {
                        viewModel.playerManager.toggleSubtitle()
                    },
                    onDismiss = { isAudioDialogOpen = false }
                )
            }
        }

        // Video Quality Selection Dialog
        if (isQualityDialogOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xBB000000))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                VideoQualityDialog(
                    currentQuality = currentQuality,
                    onSelectQuality = {
                        viewModel.setVideoQuality(it)
                        viewModel.showToast("Quality set to ${it.label}")
                        isQualityDialogOpen = false
                    },
                    onDismiss = { isQualityDialogOpen = false }
                )
            }
        }

        // Parental Control PIN Verification Dialog
        if (uiState.isParentalPinDialogOpen) {
            ParentalPinDialog(
                channelName = uiState.pendingLockedChannel?.name,
                onConfirm = { viewModel.unlockParentalWithPin(it) },
                onDismiss = { viewModel.dismissParentalDialog() }
            )
        }
    }
}

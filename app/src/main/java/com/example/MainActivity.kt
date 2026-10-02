package com.example

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remote.RemoteAction
import com.example.remote.RemoteKeyHandler
import com.example.ui.CurrentScreen
import com.example.ui.ZaynTvViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.ProfileScreen
import com.example.ui.auth.SignUpScreen
import com.example.ui.guide.TvGuideScreen
import com.example.ui.home.HomeScreen
import com.example.ui.live.LiveTvScreen
import com.example.ui.management.ChannelManagementScreen
import com.example.ui.provider.AddProviderScreen
import com.example.ui.provider.ProvidersScreen
import com.example.ui.search.SearchScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.ZaynTVTheme
import com.example.ui.vod.MoviesScreen
import com.example.ui.vod.SeriesScreen

class MainActivity : ComponentActivity() {

    private val viewModel: ZaynTvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val playbackState by viewModel.playbackState.collectAsState()

            ZaynTVTheme(
                themeType = uiState.settings.themeType,
                customThemeColors = uiState.settings.customThemeColors
            ) {
                val theme = LocalAppThemeColors.current

                // Intelligent Back Press Handler
                BackHandler {
                    val handled = viewModel.handleBackPress()
                    if (!handled) {
                        finish()
                    }
                }

                Box(modifier = Modifier.fillMaxSize().background(theme.background)) {
                    // Screen Router
                    when (uiState.currentScreen) {
                        CurrentScreen.LIVE_TV -> {
                            LiveTvScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                playbackState = playbackState,
                                onTriggerPip = { enterPipMode() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CurrentScreen.HOME -> {
                            Scaffold(
                                bottomBar = {
                                    ZaynBottomNavigation(
                                        currentScreen = uiState.currentScreen,
                                        onSelectScreen = { viewModel.navigateTo(it) }
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            ) { paddingValues ->
                                HomeScreen(
                                    viewModel = viewModel,
                                    uiState = uiState,
                                    modifier = Modifier.padding(paddingValues)
                                )
                            }
                        }
                        CurrentScreen.GUIDE -> {
                            TvGuideScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CurrentScreen.SEARCH -> {
                            SearchScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CurrentScreen.MOVIES -> {
                            MoviesScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CurrentScreen.SERIES -> {
                            SeriesScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CurrentScreen.CHANNEL_MANAGEMENT -> {
                            ChannelManagementScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CurrentScreen.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CurrentScreen.PROFILE -> {
                            ProfileScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CurrentScreen.LOGIN -> {
                            LoginScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CurrentScreen.SIGN_UP -> {
                            SignUpScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CurrentScreen.PROVIDERS -> {
                            ProvidersScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CurrentScreen.ADD_PROVIDER -> {
                            AddProviderScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Toast Notification Banner
                    AnimatedVisibility(
                        visible = uiState.toastMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 32.dp)
                    ) {
                        uiState.toastMessage?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(theme.card.copy(alpha = 0.95f))
                                    .border(1.dp, theme.primary, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = msg,
                                    color = theme.textPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (_: Exception) {}
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyDown(keyCode, event)

        val uiState = viewModel.uiState.value
        val isFullscreenLive = uiState.currentScreen == CurrentScreen.LIVE_TV
        val isOverlayOpen = uiState.isChannelListOpen || uiState.isNumericKeypadOpen || uiState.isQuickMenuOpen

        if (isFullscreenLive) {
            // Wake up controls overlay on any remote key press
            viewModel.resetControlsInactivityTimer()

            // Number keys 0..9 direct zapping on physical remotes
            if (keyCode in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9) {
                val digit = (keyCode - KeyEvent.KEYCODE_0).toString()
                if (!uiState.isNumericKeypadOpen) {
                    viewModel.toggleNumericKeypad()
                }
                viewModel.onNumericKeyPress(digit)
                return true
            }

            val action = RemoteKeyHandler.handleKeyDown(keyCode, event, isOverlayOpen)
            when (action) {
                RemoteAction.CHANNEL_NEXT -> {
                    viewModel.switchChannelNext()
                    return true
                }
                RemoteAction.CHANNEL_PREV -> {
                    viewModel.switchChannelPrev()
                    return true
                }
                RemoteAction.TOGGLE_CHANNEL_LIST -> {
                    viewModel.toggleChannelList()
                    return true
                }
                RemoteAction.SHOW_INFO -> {
                    viewModel.toggleControlsOverlay()
                    return true
                }
                RemoteAction.SHOW_MENU -> {
                    viewModel.toggleQuickMenu()
                    return true
                }
                RemoteAction.SHOW_NUMERIC_KEYPAD -> {
                    viewModel.toggleNumericKeypad()
                    return true
                }
                RemoteAction.SHOW_EPG -> {
                    viewModel.navigateTo(CurrentScreen.GUIDE)
                    return true
                }
                RemoteAction.PLAY_PAUSE -> {
                    viewModel.playerManager.togglePlayPause()
                    return true
                }
                RemoteAction.OPEN_SETTINGS -> {
                    viewModel.navigateTo(CurrentScreen.SETTINGS)
                    return true
                }
                RemoteAction.NONE -> {}
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        viewModel.playerManager.pause()
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.uiState.value.currentScreen == CurrentScreen.LIVE_TV) {
            viewModel.playerManager.play()
        }
    }
}

@Composable
fun ZaynBottomNavigation(
    currentScreen: CurrentScreen,
    onSelectScreen: (CurrentScreen) -> Unit
) {
    val theme = LocalAppThemeColors.current

    NavigationBar(
        containerColor = theme.card,
        contentColor = theme.textPrimary
    ) {
        val navItems = listOf(
            Triple(CurrentScreen.HOME, Icons.Default.Home, "Home"),
            Triple(CurrentScreen.LIVE_TV, Icons.Default.LiveTv, "Live TV"),
            Triple(CurrentScreen.GUIDE, Icons.Default.Tv, "Guide"),
            Triple(CurrentScreen.SEARCH, Icons.Default.Search, "Search"),
            Triple(CurrentScreen.SETTINGS, Icons.Default.Settings, "Settings")
        )

        navItems.forEach { (screen, icon, label) ->
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()
            val isSelected = currentScreen == screen

            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(if (isFocused) 26.dp else 24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isFocused || isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                interactionSource = interactionSource,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (isFocused) 2.dp else 0.dp,
                        color = if (isFocused) theme.focusBorder else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = theme.primary,
                    selectedTextColor = theme.primary,
                    indicatorColor = theme.primary.copy(alpha = 0.25f),
                    unselectedIconColor = theme.textSecondary,
                    unselectedTextColor = theme.textSecondary
                )
            )
        }
    }
}

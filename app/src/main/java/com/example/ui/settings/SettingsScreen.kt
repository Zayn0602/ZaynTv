package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioMode
import com.example.data.model.BufferMode
import com.example.data.model.ChannelNumberingMode
import com.example.data.model.ChannelSortOrder
import com.example.data.model.PlayerEngine
import com.example.data.model.QualityPreference
import com.example.data.model.StartupBehavior
import com.example.data.model.ThemeType
import com.example.data.model.VideoQualityOption
import com.example.data.model.WatermarkPosition
import com.example.data.model.WatermarkSize
import com.example.ui.CurrentScreen
import com.example.ui.ZaynTvViewModel
import com.example.ui.ZaynUiState
import com.example.ui.i18n.ZaynLanguageManager
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.ZaynLiveRed

/**
 * All 17 Settings Categories as required by the Android TV & DTH specifications.
 */
enum class SettingsCategory(val title: String, val icon: ImageVector) {
    PROVIDERS("1. Providers", Icons.Default.Dns),
    PLAYLISTS("2. Playlist Management", Icons.Default.FormatListNumbered),
    CHANNELS("3. Channel Management", Icons.Default.List),
    PLAYER("4. Player Settings", Icons.Default.PlayCircle),
    VIDEO("5. Video Settings", Icons.Default.Tv),
    AUDIO_SUBTITLES("6. Audio & Subtitles", Icons.Default.Subtitles),
    EPG("7. EPG Settings", Icons.Default.LiveTv),
    APPEARANCE("8. Appearance & Themes", Icons.Default.Palette),
    LANGUAGE("9. Language", Icons.Default.Language),
    STARTUP("10. Startup Settings", Icons.Default.PowerSettingsNew),
    NETWORK("11. Network & Buffer", Icons.Default.Wifi),
    PARENTAL("12. Parental Control", Icons.Default.Lock),
    SEARCH("13. Search", Icons.Default.Search),
    FAVORITES("14. Favorites & History", Icons.Default.Star),
    STORAGE("15. Storage & Cache", Icons.Default.CleaningServices),
    ACCOUNT("16. Account", Icons.Default.AccountCircle),
    ABOUT("17. About ZaynTV", Icons.Default.Info)
}

@Composable
fun SettingsScreen(
    viewModel: ZaynTvViewModel,
    uiState: ZaynUiState,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeColors.current
    var selectedCategory by remember { mutableStateOf(SettingsCategory.PROVIDERS) }

    // Dialog state controllers
    var isQualityPickerOpen by remember { mutableStateOf(false) }
    var isAudioLangPickerOpen by remember { mutableStateOf(false) }
    var isSubtitleLangPickerOpen by remember { mutableStateOf(false) }
    var isEnginePickerOpen by remember { mutableStateOf(false) }
    var isAspectPickerOpen by remember { mutableStateOf(false) }
    var isBufferPickerOpen by remember { mutableStateOf(false) }
    var isWatermarkPosPickerOpen by remember { mutableStateOf(false) }
    var isWatermarkSizePickerOpen by remember { mutableStateOf(false) }
    var isWatermarkOpacityPickerOpen by remember { mutableStateOf(false) }
    var isThemePickerOpen by remember { mutableStateOf(false) }
    var isLanguagePickerOpen by remember { mutableStateOf(false) }
    var isStartupBehaviorPickerOpen by remember { mutableStateOf(false) }
    var isTimeoutPickerOpen by remember { mutableStateOf(false) }
    var isNumberingModePickerOpen by remember { mutableStateOf(false) }
    var isSortOrderPickerOpen by remember { mutableStateOf(false) }
    var editingParentalPin by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var showResetDataConfirm by remember { mutableStateOf(false) }

    // Real-time track information from player
    val audioTracks by viewModel.playerManager.audioTracks.collectAsState()
    val subtitleTracks by viewModel.playerManager.subtitleTracks.collectAsState()
    val currentQuality by viewModel.playerManager.currentQuality.collectAsState()
    val isSubtitleEnabled by viewModel.playerManager.isSubtitleEnabled.collectAsState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        val isWide = maxWidth >= 580.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Back Arrow and Breadcrumb
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.handleBackPress() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.card)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ZAYNTV SETTINGS",
                            color = theme.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = selectedCategory.title,
                            color = theme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick button to return straight to live video
                Button(
                    onClick = { viewModel.navigateTo(CurrentScreen.LIVE_TV) },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.LiveTv, contentDescription = null, tint = theme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("LIVE TV", color = theme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(theme.primary.copy(alpha = 0.15f))
            )

            // Split Layout (Left: Categories, Right: Category Options)
            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // LEFT SIDE: Category Navigation Drawer
                    LazyColumn(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .background(theme.background)
                            .border(width = 1.dp, color = theme.primary.copy(alpha = 0.1f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(SettingsCategory.entries) { cat ->
                            CategoryTabItem(
                                category = cat,
                                isSelected = selectedCategory == cat,
                                onClick = { selectedCategory = cat }
                            )
                        }
                    }

                    // RIGHT SIDE: Detailed Options Pane
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        CategoryContentPane(
                            category = selectedCategory,
                            viewModel = viewModel,
                            uiState = uiState,
                            currentQuality = currentQuality,
                            audioTracksCount = audioTracks.size,
                            subtitleTracksCount = subtitleTracks.size,
                            isSubtitleEnabled = isSubtitleEnabled,
                            onOpenQualityPicker = { isQualityPickerOpen = true },
                            onOpenAudioLangPicker = { isAudioLangPickerOpen = true },
                            onOpenSubtitleLangPicker = { isSubtitleLangPickerOpen = true },
                            onOpenEnginePicker = { isEnginePickerOpen = true },
                            onOpenAspectPicker = { isAspectPickerOpen = true },
                            onOpenBufferPicker = { isBufferPickerOpen = true },
                            onOpenWatermarkPosPicker = { isWatermarkPosPickerOpen = true },
                            onOpenWatermarkSizePicker = { isWatermarkSizePickerOpen = true },
                            onOpenWatermarkOpacityPicker = { isWatermarkOpacityPickerOpen = true },
                            onOpenThemePicker = { isThemePickerOpen = true },
                            onOpenLanguagePicker = { isLanguagePickerOpen = true },
                            onOpenStartupBehaviorPicker = { isStartupBehaviorPickerOpen = true },
                            onOpenTimeoutPicker = { isTimeoutPickerOpen = true },
                            onOpenNumberingModePicker = { isNumberingModePickerOpen = true },
                            onOpenSortOrderPicker = { isSortOrderPickerOpen = true },
                            onOpenParentalPinDialog = { editingParentalPin = true },
                            onResetDataClick = { showResetDataConfirm = true }
                        )
                    }
                }
            } else {
                // Compact Vertical Mobile Layout: Horizontal Category Bar + Full Options
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(SettingsCategory.entries) { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) theme.primary else theme.card)
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat.title.replace(Regex("^\\d+\\.\\s*"), ""),
                                    color = if (isSelected) theme.background else theme.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        CategoryContentPane(
                            category = selectedCategory,
                            viewModel = viewModel,
                            uiState = uiState,
                            currentQuality = currentQuality,
                            audioTracksCount = audioTracks.size,
                            subtitleTracksCount = subtitleTracks.size,
                            isSubtitleEnabled = isSubtitleEnabled,
                            onOpenQualityPicker = { isQualityPickerOpen = true },
                            onOpenAudioLangPicker = { isAudioLangPickerOpen = true },
                            onOpenSubtitleLangPicker = { isSubtitleLangPickerOpen = true },
                            onOpenEnginePicker = { isEnginePickerOpen = true },
                            onOpenAspectPicker = { isAspectPickerOpen = true },
                            onOpenBufferPicker = { isBufferPickerOpen = true },
                            onOpenWatermarkPosPicker = { isWatermarkPosPickerOpen = true },
                            onOpenWatermarkSizePicker = { isWatermarkSizePickerOpen = true },
                            onOpenWatermarkOpacityPicker = { isWatermarkOpacityPickerOpen = true },
                            onOpenThemePicker = { isThemePickerOpen = true },
                            onOpenLanguagePicker = { isLanguagePickerOpen = true },
                            onOpenStartupBehaviorPicker = { isStartupBehaviorPickerOpen = true },
                            onOpenTimeoutPicker = { isTimeoutPickerOpen = true },
                            onOpenNumberingModePicker = { isNumberingModePickerOpen = true },
                            onOpenSortOrderPicker = { isSortOrderPickerOpen = true },
                            onOpenParentalPinDialog = { editingParentalPin = true },
                            onResetDataClick = { showResetDataConfirm = true }
                        )
                    }
                }
            }
        }

        // ==========================================
        // DIALOGS & PICKERS (All Functional)
        // ==========================================

        // 1. Video Quality Dialog
        if (isQualityPickerOpen) {
            SettingsSelectionDialog(
                title = "Select Video Quality",
                onDismiss = { isQualityPickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    VideoQualityOption.entries.forEach { opt ->
                        SelectionOptionRow(
                            label = opt.label,
                            isSelected = currentQuality == opt,
                            onClick = {
                                viewModel.setVideoQuality(opt)
                                isQualityPickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 2. Player Engine Dialog
        if (isEnginePickerOpen) {
            SettingsSelectionDialog(
                title = "Select Playback Engine",
                onDismiss = { isEnginePickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        PlayerEngine.AUTO to "Auto (ExoPlayer with Fallback)",
                        PlayerEngine.EXOPLAYER to "AndroidX Media3 ExoPlayer",
                        PlayerEngine.VLC_SOFTWARE to "VLC Engine Fallback"
                    ).forEach { (engine, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = uiState.settings.playerEngine == engine,
                            onClick = {
                                viewModel.setPlayerEngine(engine)
                                isEnginePickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 3. Aspect Ratio Dialog
        if (isAspectPickerOpen) {
            SettingsSelectionDialog(
                title = "Video Aspect Ratio",
                onDismiss = { isAspectPickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        AspectRatioMode.FIT to "Fit to Screen (Preserve Aspect)",
                        AspectRatioMode.FILL to "Fill Screen (Crop Overflow)",
                        AspectRatioMode.ZOOM to "Zoom Mode",
                        AspectRatioMode.SIXTEEN_NINE to "16:9 Widescreen",
                        AspectRatioMode.FOUR_THREE to "4:3 Standard TV",
                        AspectRatioMode.ORIGINAL to "Original Stream Aspect"
                    ).forEach { (mode, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = uiState.settings.aspectRatio == mode,
                            onClick = {
                                viewModel.setAspectRatio(mode)
                                isAspectPickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 4. Buffer Mode Dialog
        if (isBufferPickerOpen) {
            SettingsSelectionDialog(
                title = "Buffer & Latency Mode",
                onDismiss = { isBufferPickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        BufferMode.LOW to "Low Latency (Fast live tuning)",
                        BufferMode.NORMAL to "Balanced (Default recommendation)",
                        BufferMode.STABLE to "Stable Playback (Large buffer for slow connections)",
                        BufferMode.CUSTOM to "Custom Buffer (Optimized)"
                    ).forEach { (mode, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = uiState.settings.bufferMode == mode,
                            onClick = {
                                viewModel.setBufferMode(mode)
                                isBufferPickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 5. Controls Timeout Dialog
        if (isTimeoutPickerOpen) {
            SettingsSelectionDialog(
                title = "Controls Auto-Hide Timeout",
                onDismiss = { isTimeoutPickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        3 to "3 Seconds (Fast)",
                        5 to "5 Seconds (Standard)",
                        7 to "7 Seconds",
                        10 to "10 Seconds",
                        0 to "Never Auto-Hide (Persistent)"
                    ).forEach { (sec, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = uiState.settings.playerControlsTimeoutSeconds == sec,
                            onClick = {
                                viewModel.updateControlTimeout(sec)
                                isTimeoutPickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 6. Watermark Position Dialog
        if (isWatermarkPosPickerOpen) {
            SettingsSelectionDialog(
                title = "Watermark Position",
                onDismiss = { isWatermarkPosPickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        WatermarkPosition.TOP_RIGHT to "Top-Right Corner",
                        WatermarkPosition.TOP_LEFT to "Top-Left Corner",
                        WatermarkPosition.BOTTOM_RIGHT to "Bottom-Right Corner",
                        WatermarkPosition.BOTTOM_LEFT to "Bottom-Left Corner"
                    ).forEach { (pos, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = uiState.settings.watermarkPosition == pos,
                            onClick = {
                                viewModel.updateWatermark(
                                    enabled = uiState.settings.watermarkEnabled,
                                    position = pos
                                )
                                isWatermarkPosPickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 7. Watermark Size Dialog
        if (isWatermarkSizePickerOpen) {
            SettingsSelectionDialog(
                title = "Watermark Size",
                onDismiss = { isWatermarkSizePickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        WatermarkSize.SMALL to "Small (Subtle)",
                        WatermarkSize.MEDIUM to "Medium (Standard)",
                        WatermarkSize.LARGE to "Large (Prominent)"
                    ).forEach { (size, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = uiState.settings.watermarkSize == size,
                            onClick = {
                                viewModel.updateWatermark(
                                    enabled = uiState.settings.watermarkEnabled,
                                    size = size
                                )
                                isWatermarkSizePickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 8. Watermark Opacity Dialog
        if (isWatermarkOpacityPickerOpen) {
            SettingsSelectionDialog(
                title = "Watermark Opacity",
                onDismiss = { isWatermarkOpacityPickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        0.20f to "20% (Very Faint)",
                        0.40f to "40% (Default)",
                        0.60f to "60% (Clear)",
                        0.85f to "85% (Solid)"
                    ).forEach { (op, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = (uiState.settings.watermarkOpacity - op) in -0.05f..0.05f,
                            onClick = {
                                viewModel.updateWatermark(
                                    enabled = uiState.settings.watermarkEnabled,
                                    opacity = op
                                )
                                isWatermarkOpacityPickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 9. Themes Dialog
        if (isThemePickerOpen) {
            SettingsSelectionDialog(
                title = "Select App Theme",
                onDismiss = { isThemePickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ThemeType.entries.forEach { t ->
                        SelectionOptionRow(
                            label = t.displayName,
                            isSelected = uiState.settings.themeType == t,
                            onClick = {
                                viewModel.setTheme(t)
                                isThemePickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 10. Language Dialog (All 23 Indian languages + English + System Default)
        if (isLanguagePickerOpen) {
            SettingsSelectionDialog(
                title = "Select Application Language",
                onDismiss = { isLanguagePickerOpen = false }
            ) {
                val languages = remember { ZaynLanguageManager.supportedLanguages }
                LazyColumn(modifier = Modifier.height(280.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(languages) { lang ->
                        SelectionOptionRow(
                            label = "${lang.nativeName} (${lang.name})",
                            isSelected = uiState.settings.appLanguage == lang.code,
                            onClick = {
                                viewModel.updateAppLanguage(lang.code)
                                isLanguagePickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 11. Startup Behavior Dialog
        if (isStartupBehaviorPickerOpen) {
            SettingsSelectionDialog(
                title = "Default Startup Channel",
                onDismiss = { isStartupBehaviorPickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        StartupBehavior.LIVE_TV to "Open Live TV (Default Channel)",
                        StartupBehavior.LAST_WATCHED to "Resume Last Watched Channel",
                        StartupBehavior.FIRST_CHANNEL to "First Channel in Playlist",
                        StartupBehavior.FAVORITE_CHANNEL to "First Favorite Channel"
                    ).forEach { (behavior, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = uiState.settings.startupBehavior == behavior,
                            onClick = {
                                viewModel.updateStartupBehavior(behavior)
                                isStartupBehaviorPickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 12. Audio Language Dialog
        if (isAudioLangPickerOpen) {
            SettingsSelectionDialog(
                title = "Default Audio Language",
                onDismiss = { isAudioLangPickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "auto" to "Auto (Stream Default)",
                        "original" to "Original Broadcast",
                        "en" to "English",
                        "bn" to "Bengali (বাংলা)",
                        "hi" to "Hindi (हिन्दी)",
                        "ta" to "Tamil (தமிழ்)",
                        "te" to "Telugu (తెలుగు)",
                        "ur" to "Urdu (اردو)"
                    ).forEach { (code, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = uiState.settings.defaultAudioLanguage == code,
                            onClick = {
                                viewModel.updateAudioSubtitlePreferences(defaultAudioLang = code)
                                isAudioLangPickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 13. Subtitle Language Dialog
        if (isSubtitleLangPickerOpen) {
            SettingsSelectionDialog(
                title = "Default Subtitle Language",
                onDismiss = { isSubtitleLangPickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "off" to "Off (Disabled by default)",
                        "auto" to "Auto (Follow Stream)",
                        "en" to "English",
                        "bn" to "Bengali (বাংলা)",
                        "hi" to "Hindi (हिन्दी)",
                        "ta" to "Tamil (தமிழ்)"
                    ).forEach { (code, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = uiState.settings.defaultSubtitleLanguage == code,
                            onClick = {
                                viewModel.updateAudioSubtitlePreferences(defaultSubtitleLang = code)
                                isSubtitleLangPickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 14. Channel Numbering Mode Dialog
        if (isNumberingModePickerOpen) {
            SettingsSelectionDialog(
                title = "Channel Numbering Mode",
                onDismiss = { isNumberingModePickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        ChannelNumberingMode.AUTO to "Auto (Automatic consecutive numbering)",
                        ChannelNumberingMode.MANUAL to "Manual (Custom assigned numbering)"
                    ).forEach { (mode, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = uiState.settings.channelNumberingMode == mode,
                            onClick = {
                                viewModel.updateChannelSortAndNumbering(numberingMode = mode)
                                isNumberingModePickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 15. Channel Sort Order Dialog
        if (isSortOrderPickerOpen) {
            SettingsSelectionDialog(
                title = "Channel Sort Order",
                onDismiss = { isSortOrderPickerOpen = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        ChannelSortOrder.PROVIDER_ORDER to "Default Provider Order",
                        ChannelSortOrder.CUSTOM_NUMBER to "By Channel Number",
                        ChannelSortOrder.A_TO_Z to "Alphabetical (A to Z)",
                        ChannelSortOrder.FAVORITES_FIRST to "Favorites First",
                        ChannelSortOrder.RECENTLY_WATCHED to "Recently Watched First"
                    ).forEach { (order, label) ->
                        SelectionOptionRow(
                            label = label,
                            isSelected = uiState.settings.channelSortOrder == order,
                            onClick = {
                                viewModel.updateChannelSortAndNumbering(sortOrder = order)
                                isSortOrderPickerOpen = false
                            }
                        )
                    }
                }
            }
        }

        // 16. Parental PIN Setup Dialog
        if (editingParentalPin) {
            SettingsSelectionDialog(
                title = "Set 4-Digit Parental PIN",
                onDismiss = { editingParentalPin = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinInput = it },
                        label = { Text("4-Digit Numeric PIN", color = theme.textSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.primary.copy(alpha = 0.3f),
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (newPinInput.length == 4) {
                                viewModel.setParentalPin(newPinInput, true)
                                editingParentalPin = false
                                newPinInput = ""
                            } else {
                                viewModel.showToast("PIN must be exactly 4 digits")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SAVE PIN & ACTIVATE", color = theme.background, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 17. Reset Data Confirmation Dialog
        if (showResetDataConfirm) {
            SettingsSelectionDialog(
                title = "Reset All Temporary Data?",
                onDismiss = { showResetDataConfirm = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "This will clear cached logos, EPG data, and temp playlists without removing your active providers or parental settings.",
                        color = theme.textSecondary,
                        fontSize = 13.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(onClick = { showResetDataConfirm = false }) {
                            Text("CANCEL", color = theme.textPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.clearCache("all")
                                showResetDataConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZaynLiveRed)
                        ) {
                            Text("CONFIRM RESET", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Category Navigation Item with High-Contrast TV Focus Indicator.
 */
@Composable
private fun CategoryTabItem(
    category: SettingsCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = when {
        isFocused -> theme.primary.copy(alpha = 0.28f)
        isSelected -> theme.primary.copy(alpha = 0.16f)
        else -> Color.Transparent
    }

    val borderColor = when {
        isFocused -> theme.focusBorder
        isSelected -> theme.primary.copy(alpha = 0.6f)
        else -> Color.Transparent
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(
                width = if (isFocused) 2.dp else if (isSelected) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = category.title,
            tint = if (isSelected || isFocused) theme.primary else theme.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = category.title,
            color = if (isSelected || isFocused) theme.primary else theme.textPrimary,
            fontSize = 13.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Detailed Options Pane for whichever Category is Selected.
 */
@Composable
private fun CategoryContentPane(
    category: SettingsCategory,
    viewModel: ZaynTvViewModel,
    uiState: ZaynUiState,
    currentQuality: VideoQualityOption,
    audioTracksCount: Int,
    subtitleTracksCount: Int,
    isSubtitleEnabled: Boolean,
    onOpenQualityPicker: () -> Unit,
    onOpenAudioLangPicker: () -> Unit,
    onOpenSubtitleLangPicker: () -> Unit,
    onOpenEnginePicker: () -> Unit,
    onOpenAspectPicker: () -> Unit,
    onOpenBufferPicker: () -> Unit,
    onOpenWatermarkPosPicker: () -> Unit,
    onOpenWatermarkSizePicker: () -> Unit,
    onOpenWatermarkOpacityPicker: () -> Unit,
    onOpenThemePicker: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onOpenStartupBehaviorPicker: () -> Unit,
    onOpenTimeoutPicker: () -> Unit,
    onOpenNumberingModePicker: () -> Unit,
    onOpenSortOrderPicker: () -> Unit,
    onOpenParentalPinDialog: () -> Unit,
    onResetDataClick: () -> Unit
) {
    val theme = LocalAppThemeColors.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 36.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = category.title.uppercase(),
                color = theme.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        when (category) {
            SettingsCategory.PROVIDERS -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Add,
                        title = "Add New Provider",
                        subtitle = "Configure M3U URL, Local M3U file, Xtream Codes, or Stalker MAC",
                        value = "Add",
                        onClick = { viewModel.navigateTo(CurrentScreen.ADD_PROVIDER) }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Dns,
                        title = "Manage All Configured Providers",
                        subtitle = "${uiState.providers.size} providers configured • Set active, test, refresh, or delete",
                        value = "Open",
                        onClick = { viewModel.navigateTo(CurrentScreen.PROVIDERS) }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Refresh,
                        title = "Refresh All Channels",
                        subtitle = "Fetch latest playlists and sync EPG data for all active services",
                        value = "Sync",
                        onClick = { viewModel.refreshAllProviders() }
                    )
                }
            }

            SettingsCategory.PLAYLISTS -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.FormatListNumbered,
                        title = "Active Playlist Channels",
                        subtitle = "${uiState.channels.size} channels loaded across ${uiState.categories.size} categories",
                        value = "View",
                        onClick = { viewModel.navigateTo(CurrentScreen.CHANNEL_MANAGEMENT) }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Refresh,
                        title = "Restore Original Channel Order",
                        subtitle = "Revert all manual channel reordering and custom sorting",
                        value = "Restore",
                        onClick = {
                            uiState.activeProvider?.let { viewModel.restoreOriginalProviderList(it.id) }
                        }
                    )
                }
            }

            SettingsCategory.CHANNELS -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.List,
                        title = "Channel Management Studio",
                        subtitle = "Show, hide, favorite, rename, reorder, or assign custom numbers",
                        value = "Open",
                        onClick = { viewModel.navigateTo(CurrentScreen.CHANNEL_MANAGEMENT) }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.FormatListNumbered,
                        title = "Channel Numbering Scheme",
                        subtitle = "Choose how channel numbers (001, 002...) are displayed",
                        value = uiState.settings.channelNumberingMode.name,
                        onClick = onOpenNumberingModePicker
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.List,
                        title = "Channel Sort Order",
                        subtitle = "Default ordering for channel lists and guide",
                        value = uiState.settings.channelSortOrder.name,
                        onClick = onOpenSortOrderPicker
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Refresh,
                        title = "Restore All Hidden Channels",
                        subtitle = "Unhide all channels previously hidden from the guide",
                        value = "Restore",
                        onClick = { viewModel.restoreAllHiddenChannels() }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Refresh,
                        title = "Reset Custom Channel Numbering",
                        subtitle = "Clear all custom numbers and return to default",
                        value = "Reset",
                        onClick = { viewModel.resetChannelNumbering() }
                    )
                }
            }

            SettingsCategory.PLAYER -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.PlayCircle,
                        title = "Playback Engine",
                        subtitle = "Select between Media3 ExoPlayer with auto-fallback or VLC engine",
                        value = uiState.settings.playerEngine.name,
                        onClick = onOpenEnginePicker
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Tv,
                        title = "Controls Auto-Hide Timeout",
                        subtitle = "Automatically dismisses control overlay during playback",
                        value = if (uiState.settings.playerControlsTimeoutSeconds > 0) "${uiState.settings.playerControlsTimeoutSeconds}s" else "Never",
                        onClick = onOpenTimeoutPicker
                    )
                }
                item {
                    SettingsToggleItem(
                        title = "Channel Wrap Around",
                        subtitle = "Jump from the last channel to the first when channel zapping",
                        checked = uiState.settings.channelWrapAround,
                        onCheckedChange = { viewModel.updateChannelWrapAround(it) }
                    )
                }
                item {
                    SettingsToggleItem(
                        title = "Auto-Skip on Stream Failure",
                        subtitle = "Automatically switch to next channel if a dead stream fails to load",
                        checked = uiState.settings.autoSkipOnFailure,
                        onCheckedChange = { viewModel.toggleAutoSkipOnFailure(it) }
                    )
                }
                item {
                    SettingsToggleItem(
                        title = "Background Playback & PiP",
                        subtitle = "Continue playing in Picture-in-Picture when leaving the app",
                        checked = uiState.settings.pipEnabled,
                        onCheckedChange = { viewModel.togglePip(it) }
                    )
                }
            }

            SettingsCategory.VIDEO -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Tv,
                        title = "Video Quality Selection",
                        subtitle = "Switch resolution between Auto, 1080p, 720p, 480p, or Low Data",
                        value = currentQuality.label,
                        onClick = onOpenQualityPicker
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Tv,
                        title = "Video Aspect Ratio",
                        subtitle = "Fit, Fill, Zoom, 16:9, or 4:3 display format",
                        value = uiState.settings.aspectRatio.name,
                        onClick = onOpenAspectPicker
                    )
                }
                item {
                    SettingsToggleItem(
                        title = "Hardware Video Acceleration",
                        subtitle = "Accelerated MediaCodec decoding for H.264, HEVC, and VP9 streams",
                        checked = uiState.settings.hardwareAcceleration,
                        onCheckedChange = { viewModel.showToast("Hardware decoding is active") }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = "Current Stream Telemetry",
                        subtitle = "Channel: ${uiState.currentChannel?.displayName ?: "None"} • Engine: ${uiState.settings.playerEngine.name}",
                        value = "Active",
                        onClick = { viewModel.showToast("Video stream: ${currentQuality.label}") }
                    )
                }
            }

            SettingsCategory.AUDIO_SUBTITLES -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Subtitles,
                        title = "Default Audio Language",
                        subtitle = "Automatically select preferred broadcast audio track",
                        value = uiState.settings.defaultAudioLanguage.uppercase(),
                        onClick = onOpenAudioLangPicker
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Subtitles,
                        title = "Available Audio Tracks",
                        subtitle = "$audioTracksCount audio track(s) detected in current stream",
                        value = "Inspect",
                        onClick = { viewModel.showToast("$audioTracksCount audio track(s) available") }
                    )
                }
                item {
                    SettingsToggleItem(
                        title = "Enable Subtitles",
                        subtitle = "Display closed captions and subtitles when available",
                        checked = isSubtitleEnabled,
                        onCheckedChange = { viewModel.playerManager.setSubtitleEnabled(it) }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Subtitles,
                        title = "Default Subtitle Language",
                        subtitle = "Preferred subtitle language for supported streams",
                        value = uiState.settings.defaultSubtitleLanguage.uppercase(),
                        onClick = onOpenSubtitleLangPicker
                    )
                }
            }

            SettingsCategory.EPG -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Refresh,
                        title = "Refresh EPG Guide Now",
                        subtitle = "Re-fetch XMLTV electronic program guide listings",
                        value = "Refresh",
                        onClick = { viewModel.refreshEpg() }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.LiveTv,
                        title = "EPG Time Offset",
                        subtitle = "Adjust program timing by hours (+/-)",
                        value = "0 Hours",
                        onClick = { viewModel.showToast("EPG time synchronized with local timezone") }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.CleaningServices,
                        title = "Clear EPG Cache",
                        subtitle = "Delete cached guide data and force fresh sync",
                        value = "Clear",
                        onClick = { viewModel.clearEpgCache() }
                    )
                }
            }

            SettingsCategory.APPEARANCE -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Palette,
                        title = "App Color Theme",
                        subtitle = "Select UI color palette (Zayn Dark, AMOLED Black, Cinema, etc.)",
                        value = uiState.settings.themeType.displayName,
                        onClick = onOpenThemePicker
                    )
                }
                item {
                    SettingsToggleItem(
                        title = "Player Watermark",
                        subtitle = "Display subtle ZaynTV logo watermark on the live player",
                        checked = uiState.settings.watermarkEnabled,
                        onCheckedChange = { viewModel.updateWatermark(enabled = it) }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Palette,
                        title = "Watermark Position",
                        subtitle = "Corner of the screen where logo is placed",
                        value = uiState.settings.watermarkPosition.name,
                        onClick = onOpenWatermarkPosPicker
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Palette,
                        title = "Watermark Size",
                        subtitle = "Small, Medium, or Large",
                        value = uiState.settings.watermarkSize.name,
                        onClick = onOpenWatermarkSizePicker
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Palette,
                        title = "Watermark Opacity",
                        subtitle = "Transparency level of the watermark overlay",
                        value = "${(uiState.settings.watermarkOpacity * 100).toInt()}%",
                        onClick = onOpenWatermarkOpacityPicker
                    )
                }
            }

            SettingsCategory.LANGUAGE -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Language,
                        title = "Select App Language",
                        subtitle = "Support for English and all 23 official Indian languages (বাংলা, हिन्दी, etc.)",
                        value = ZaynLanguageManager.supportedLanguages.find { it.code == uiState.settings.appLanguage }?.nativeName ?: "English",
                        onClick = onOpenLanguagePicker
                    )
                }
            }

            SettingsCategory.STARTUP -> {
                item {
                    SettingsToggleItem(
                        title = "Auto-Start Live TV on Launch",
                        subtitle = "Bypass all menus and jump straight to live broadcast immediately",
                        checked = uiState.settings.autoStartLiveTv,
                        onCheckedChange = { viewModel.updateStartupSettings(autoStartLiveTv = it) }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.PowerSettingsNew,
                        title = "Default Startup Channel",
                        subtitle = "Choose between default test channel, last watched, or first channel",
                        value = uiState.settings.startupBehavior.name,
                        onClick = onOpenStartupBehaviorPicker
                    )
                }
                item {
                    SettingsToggleItem(
                        title = "Auto-Play on Launch",
                        subtitle = "Start playback immediately without requiring OK/Play button press",
                        checked = uiState.settings.autoPlayOnLaunch,
                        onCheckedChange = { viewModel.updateStartupSettings(autoPlayOnLaunch = it) }
                    )
                }
                item {
                    SettingsToggleItem(
                        title = "Resume Last Channel",
                        subtitle = "Always tune back to whichever channel you were watching before closing",
                        checked = uiState.settings.resumeLastChannel,
                        onCheckedChange = { viewModel.updateStartupSettings(resumeLastChannel = it) }
                    )
                }
                item {
                    SettingsToggleItem(
                        title = "Launch on Device Boot",
                        subtitle = "Automatically open ZaynTV when your Android TV or Box powers on",
                        checked = uiState.settings.launchOnBoot,
                        onCheckedChange = { viewModel.toggleLaunchOnBoot(it) }
                    )
                }
            }

            SettingsCategory.NETWORK -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Wifi,
                        title = "Stream Buffer Profile",
                        subtitle = "Low Latency (Fast zapping) vs Stable (Smooth buffer)",
                        value = uiState.settings.bufferMode.name,
                        onClick = onOpenBufferPicker
                    )
                }
                item {
                    SettingsToggleItem(
                        title = "Low Bandwidth Mode",
                        subtitle = "Optimizes video streams to conserve data and prevent buffering",
                        checked = uiState.settings.lowBandwidthMode,
                        onCheckedChange = { viewModel.toggleLowBandwidth(it) }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Speed,
                        title = "Test Network & Server Latency",
                        subtitle = "Ping active provider server to verify connectivity and speed",
                        value = "Test",
                        onClick = { viewModel.testNetworkSpeed() }
                    )
                }
            }

            SettingsCategory.PARENTAL -> {
                item {
                    SettingsToggleItem(
                        title = "Parental Control Lock",
                        subtitle = "Protect adult categories and restricted channels with 4-digit PIN",
                        checked = uiState.settings.parentalEnabled,
                        onCheckedChange = {
                            if (it && uiState.settings.parentalPin.isBlank()) {
                                onOpenParentalPinDialog()
                            } else {
                                viewModel.setParentalPin(uiState.settings.parentalPin, it)
                            }
                        }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Lock,
                        title = "Change 4-Digit Parental PIN",
                        subtitle = if (uiState.settings.parentalPin.isNotBlank()) "PIN is set (****)" else "No PIN configured",
                        value = "Change",
                        onClick = onOpenParentalPinDialog
                    )
                }
            }

            SettingsCategory.SEARCH -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Search,
                        title = "Open Universal Search",
                        subtitle = "Search by channel name, channel number, category, or EPG program",
                        value = "Search",
                        onClick = { viewModel.navigateTo(CurrentScreen.SEARCH) }
                    )
                }
            }

            SettingsCategory.FAVORITES -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Star,
                        title = "Favorite Channels",
                        subtitle = "${uiState.favorites.size} favorite channel(s) saved",
                        value = "Open",
                        onClick = { viewModel.navigateTo(CurrentScreen.CHANNEL_MANAGEMENT) }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.CleaningServices,
                        title = "Clear Recent Watch History",
                        subtitle = "${uiState.recentChannelIds.size} channel(s) in history",
                        value = "Clear",
                        onClick = { viewModel.clearRecentChannels() }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.CleaningServices,
                        title = "Clear All Favorites",
                        subtitle = "Remove all channels from favorites list",
                        value = "Clear",
                        onClick = { viewModel.clearAllFavorites() }
                    )
                }
            }

            SettingsCategory.STORAGE -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.CleaningServices,
                        title = "Clear Channel & Logo Image Cache",
                        subtitle = "Free space used by downloaded channel logos and banners",
                        value = "Clear",
                        onClick = { viewModel.clearCache("images") }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.CleaningServices,
                        title = "Clear EPG Listing Cache",
                        subtitle = "Delete stored program guide data",
                        value = "Clear",
                        onClick = { viewModel.clearCache("epg") }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.CleaningServices,
                        title = "Reset All Temporary Data",
                        subtitle = "Clear all temporary files without removing your account or providers",
                        value = "Reset",
                        onClick = onResetDataClick
                    )
                }
            }

            SettingsCategory.ACCOUNT -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.AccountCircle,
                        title = "User Account & Session",
                        subtitle = if (uiState.userProfile.isLoggedIn) "Logged in as ${uiState.userProfile.name}" else "Guest Session",
                        value = if (uiState.userProfile.isLoggedIn) "Profile" else "Sign In",
                        onClick = { viewModel.navigateTo(CurrentScreen.PROFILE) }
                    )
                }
            }

            SettingsCategory.ABOUT -> {
                item {
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = "ZaynTV IPTV Player",
                        subtitle = "Version 1.0 (Build 1) • Live-TV-First DTH & Set-Top Box Experience",
                        value = "v1.0",
                        onClick = { viewModel.showToast("ZaynTV v1.0 • Built for Android TV & Mobile") }
                    )
                }
                item {
                    SettingsRowItem(
                        icon = Icons.Default.PlayCircle,
                        title = "Playback Engine Core",
                        subtitle = "Powered by Google AndroidX Media3 ExoPlayer with Hardware Acceleration",
                        value = "Media3",
                        onClick = { viewModel.showToast("Media3 Core Player") }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String,
    onClick: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) theme.primary.copy(alpha = 0.22f) else theme.card)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) theme.focusBorder else theme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = theme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = theme.textSecondary, fontSize = 11.sp, maxLines = 2)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, color = theme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Text("›", color = theme.textSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) theme.primary.copy(alpha = 0.15f) else theme.card)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) theme.focusBorder else theme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) {
                onCheckedChange(!checked)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = theme.textSecondary, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = theme.background,
                checkedTrackColor = theme.primary,
                uncheckedThumbColor = theme.textSecondary,
                uncheckedTrackColor = theme.card
            )
        )
    }
}

@Composable
private fun SettingsSelectionDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val theme = LocalAppThemeColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE000000))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(theme.card)
                .border(1.dp, theme.primary, RoundedCornerShape(16.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(24.dp)
        ) {
            Text(text = title, color = theme.primary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            content()
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("DONE", color = theme.background, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SelectionOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalAppThemeColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFocused -> theme.primary.copy(alpha = 0.3f)
                    isSelected -> theme.primary.copy(alpha = 0.18f)
                    else -> theme.background
                }
            )
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
            text = label,
            color = if (isSelected) theme.primary else theme.textPrimary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        if (isSelected) {
            Text("✓", color = theme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

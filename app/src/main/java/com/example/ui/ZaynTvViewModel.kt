package com.example.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.auth.AuthResult
import com.example.data.model.AspectRatioMode
import com.example.data.model.BufferMode
import com.example.data.model.Category
import com.example.data.model.Channel
import com.example.data.model.ChannelNumberingMode
import com.example.data.model.ChannelSortOrder
import com.example.data.model.CustomThemeColors
import com.example.data.model.FilterRule
import com.example.data.model.ImportSummary
import com.example.data.model.Movie
import com.example.data.model.PlayerEngine
import com.example.data.model.Provider
import com.example.data.model.QualityPreference
import com.example.data.model.Series
import com.example.data.model.StartupBehavior
import com.example.data.model.ThemeType
import com.example.data.model.UserProfile
import com.example.data.model.UserSettings
import com.example.data.model.VideoQualityOption
import com.example.data.model.WatermarkPosition
import com.example.data.model.WatermarkSize
import com.example.data.model.XtreamConnectionResult
import com.example.data.repository.IptvRepository
import com.example.player.PlaybackState
import com.example.player.ZaynPlayerManager
import com.example.storage.PreferencesManager
import com.example.ui.parental.ParentalControlManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.InputStream

enum class CurrentScreen {
    HOME,
    LIVE_TV,
    MOVIES,
    SERIES,
    GUIDE,
    SEARCH,
    PROVIDERS,
    CHANNEL_MANAGEMENT,
    SETTINGS,
    PROFILE,
    LOGIN,
    SIGN_UP,
    ADD_PROVIDER
}

data class UniversalSearchResult(
    val channels: List<Channel> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val series: List<Series> = emptyList(),
    val categories: List<Category> = emptyList()
)

data class ZaynUiState(
    val currentScreen: CurrentScreen = CurrentScreen.LIVE_TV,
    val channels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val currentChannel: Channel? = null,
    val currentChannelIndex: Int = 0,
    val categories: List<Category> = emptyList(),
    val selectedCategory: String = "All",
    val providers: List<Provider> = emptyList(),
    val activeProvider: Provider? = null,
    val movies: List<Movie> = emptyList(),
    val series: List<Series> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val hiddenChannelIds: Set<String> = emptySet(),
    val lockedCategories: Set<String> = emptySet(),
    val lockedChannels: Set<String> = emptySet(),
    val recentChannelIds: List<String> = emptyList(),
    val settings: UserSettings = UserSettings(),
    val userProfile: UserProfile = UserProfile(),
    val isControlsOverlayVisible: Boolean = true,
    val isChannelListOpen: Boolean = false,
    val isNumericKeypadOpen: Boolean = false,
    val numericInput: String = "",
    val isQuickMenuOpen: Boolean = false,
    val searchQuery: String = "",
    val searchResults: UniversalSearchResult = UniversalSearchResult(),
    val errorMessage: String? = null,
    val toastMessage: String? = null,
    val isImporting: Boolean = false,
    val importSummary: ImportSummary? = null,
    val testConnectionResult: XtreamConnectionResult? = null,
    val isTestingConnection: Boolean = false,
    val isParentalPinDialogOpen: Boolean = false,
    val pendingLockedChannel: Channel? = null,
    val isAudioTrackDialogVisible: Boolean = false,
    val isQualityDialogVisible: Boolean = false,
    val filterRules: List<FilterRule> = emptyList(),
    val navigationBackStack: List<CurrentScreen> = emptyList()
)

class ZaynTvViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesManager = PreferencesManager(application)
    val repository = IptvRepository(application, preferencesManager)
    val playerManager = ZaynPlayerManager(application)
    val authManager = AuthManager(preferencesManager)
    val parentalControlManager = ParentalControlManager(preferencesManager)

    private val _uiState = MutableStateFlow(ZaynUiState())
    val uiState: StateFlow<ZaynUiState> = _uiState.asStateFlow()

    val playbackState = playerManager.playbackState

    private var controlsDismissJob: Job? = null
    private var autoTuneJob: Job? = null
    private var autoSkipJob: Job? = null
    private var lastBackPressMs: Long = 0L

    init {
        observeRepository()
        observePlaybackState()
        observeUserSettings()
        observeUserProfile()

        viewModelScope.launch {
            try {
                repository.initialize()
                handleInitialStartup()
            } catch (e: Exception) {
                Log.e("ZaynTvViewModel", "Init error", e)
            }
        }
    }

    private fun observePlaybackState() {
        viewModelScope.launch {
            playerManager.playbackState.collectLatest { state ->
                autoSkipJob?.cancel()
                if (state is PlaybackState.Error && _uiState.value.settings.autoSkipOnFailure) {
                    autoSkipJob = viewModelScope.launch {
                        delay(6000L)
                        if (playerManager.playbackState.value is PlaybackState.Error) {
                            showToast("Auto-skipping to next channel...")
                            switchChannelNext()
                        }
                    }
                }
            }
        }
    }

    private fun observeUserSettings() {
        viewModelScope.launch {
            preferencesManager.userSettingsFlow.collectLatest { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
                playerManager.setAspectRatio(settings.aspectRatio)
                playerManager.setEngine(settings.playerEngine)
                playerManager.setVideoQuality(settings.videoQuality)
                playerManager.setQualityPreference(settings.qualityPreference)
                playerManager.setBufferMode(settings.bufferMode)
            }
        }

        viewModelScope.launch {
            preferencesManager.hiddenChannelIdsFlow.collectLatest { hidden ->
                _uiState.value = _uiState.value.copy(hiddenChannelIds = hidden)
                updateFilteredChannels()
            }
        }

        viewModelScope.launch {
            preferencesManager.lockedCategoriesFlow.collectLatest { locked ->
                _uiState.value = _uiState.value.copy(lockedCategories = locked)
                updateFilteredChannels()
            }
        }

        viewModelScope.launch {
            preferencesManager.lockedChannelsFlow.collectLatest { locked ->
                _uiState.value = _uiState.value.copy(lockedChannels = locked)
                updateFilteredChannels()
            }
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            preferencesManager.userProfileFlow.collectLatest { profile ->
                _uiState.value = _uiState.value.copy(userProfile = profile)
            }
        }
    }

    private fun observeRepository() {
        viewModelScope.launch {
            repository.providers.collectLatest { list ->
                _uiState.value = _uiState.value.copy(providers = list)
            }
        }

        viewModelScope.launch {
            repository.activeProvider.collectLatest { provider ->
                _uiState.value = _uiState.value.copy(activeProvider = provider)
            }
        }

        viewModelScope.launch {
            repository.channels.collectLatest { list ->
                val currCh = _uiState.value.currentChannel
                val updatedCurr = if (currCh != null) list.find { it.id == currCh.id } ?: currCh else null
                _uiState.value = _uiState.value.copy(
                    channels = list,
                    currentChannel = updatedCurr
                )
                updateFilteredChannels()
            }
        }

        viewModelScope.launch {
            repository.categories.collectLatest { list ->
                _uiState.value = _uiState.value.copy(categories = list)
            }
        }

        viewModelScope.launch {
            repository.movies.collectLatest { list ->
                _uiState.value = _uiState.value.copy(movies = list)
            }
        }

        viewModelScope.launch {
            repository.series.collectLatest { list ->
                _uiState.value = _uiState.value.copy(series = list)
            }
        }

        viewModelScope.launch {
            repository.favoritesFlow.collectLatest { favs ->
                _uiState.value = _uiState.value.copy(favorites = favs)
                updateFilteredChannels()
            }
        }

        viewModelScope.launch {
            repository.recentChannelsFlow.collectLatest { recents ->
                _uiState.value = _uiState.value.copy(recentChannelIds = recents)
            }
        }

        viewModelScope.launch {
            repository.filterRules.collectLatest { rules ->
                _uiState.value = _uiState.value.copy(filterRules = rules)
            }
        }
    }

    private fun updateFilteredChannels() {
        val all = _uiState.value.channels
        val category = _uiState.value.selectedCategory
        val query = _uiState.value.searchQuery
        val favs = _uiState.value.favorites
        val hidden = _uiState.value.hiddenChannelIds
        val lockedCats = _uiState.value.lockedCategories

        val filtered = all.filter { ch ->
            // Skip hidden channels
            if (hidden.contains(ch.id) || ch.isCustomHidden) return@filter false

            // Parental category filter if not unlocked
            if (lockedCats.contains(ch.displayGroup)) return@filter false

            val matchesCategory = when (category) {
                "All" -> true
                "Favorites" -> favs.contains(ch.id)
                else -> ch.displayGroup.equals(category, ignoreCase = true)
            }

            val matchesQuery = query.isBlank() ||
                ch.displayName.contains(query, ignoreCase = true) ||
                ch.displayChannelNumber.toString().contains(query)

            matchesCategory && matchesQuery
        }

        _uiState.value = _uiState.value.copy(filteredChannels = filtered)
    }

    private suspend fun handleInitialStartup() {
        val settings = preferencesManager.userSettingsFlow.first()
        val channels = repository.channels.value
        if (channels.isEmpty()) return

        // 1. Requirement 1: ZaynTV opens directly into Live TV
        _uiState.value = _uiState.value.copy(currentScreen = CurrentScreen.LIVE_TV)

        // 2. Requirement 2: Auto-select and play the configured startup channel (default: test Live TV channel demo_ch_1)
        val targetChannel = when {
            settings.resumeLastChannel && !settings.lastChannelId.isNullOrBlank() -> {
                channels.find { it.id == settings.lastChannelId } ?: channels.find { it.id == "demo_ch_1" } ?: channels.firstOrNull()
            }
            settings.startupBehavior == StartupBehavior.CUSTOM_CHANNEL -> {
                channels.find { it.displayChannelNumber == settings.customStartupChannelNumber }
                    ?: channels.find { it.id == "demo_ch_1" } ?: channels.firstOrNull()
            }
            settings.startupBehavior == StartupBehavior.FAVORITE_CHANNEL -> {
                val favs = repository.favoritesFlow.first()
                channels.find { favs.contains(it.id) } ?: channels.find { it.id == "demo_ch_1" } ?: channels.firstOrNull()
            }
            else -> {
                // Default startup channel = existing test Live TV channel (demo_ch_1)
                channels.find { it.id == "demo_ch_1" } ?: channels.firstOrNull()
            }
        }

        if (settings.autoPlayOnLaunch && targetChannel != null) {
            playChannel(targetChannel)
        } else if (targetChannel != null) {
            _uiState.value = _uiState.value.copy(currentChannel = targetChannel)
        }
    }

    fun isOnline(): Boolean {
        return try {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    fun playChannel(channel: Channel) {
        viewModelScope.launch {
            if (parentalControlManager.isChannelLocked(channel.id, channel.displayGroup)) {
                _uiState.value = _uiState.value.copy(
                    isParentalPinDialogOpen = true,
                    pendingLockedChannel = channel
                )
                return@launch
            }
            proceedPlayChannel(channel)
        }
    }

    private fun proceedPlayChannel(channel: Channel) {
        autoSkipJob?.cancel()
        if (!isOnline() && !channel.streamUrl.startsWith("file://") && !channel.streamUrl.startsWith("content://")) {
            showToast("No internet connection")
        }

        val list = _uiState.value.channels
        val index = list.indexOfFirst { it.id == channel.id }.coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(
            currentChannel = channel,
            currentChannelIndex = index,
            isChannelListOpen = false,
            isNumericKeypadOpen = false,
            numericInput = "",
            isQuickMenuOpen = false,
            currentScreen = CurrentScreen.LIVE_TV
        )

        playerManager.playChannel(channel)
        showControlsOverlay()

        viewModelScope.launch {
            preferencesManager.saveLastChannel(channel.providerId, channel.id, channel.displayGroup)
            preferencesManager.addRecentChannel(channel.id)
        }
    }

    fun unlockParentalWithPin(pin: String) {
        viewModelScope.launch {
            val valid = parentalControlManager.verifyPin(pin)
            if (valid) {
                _uiState.value = _uiState.value.copy(isParentalPinDialogOpen = false)
                _uiState.value.pendingLockedChannel?.let { proceedPlayChannel(it) }
                _uiState.value = _uiState.value.copy(pendingLockedChannel = null)
                showToast("Parental control unlocked")
            } else {
                showToast("Incorrect PIN")
            }
        }
    }

    fun dismissParentalDialog() {
        _uiState.value = _uiState.value.copy(
            isParentalPinDialogOpen = false,
            pendingLockedChannel = null
        )
    }

    fun switchChannelNext() {
        val list = _uiState.value.filteredChannels.ifEmpty { _uiState.value.channels }
        if (list.isEmpty()) return
        val currentId = _uiState.value.currentChannel?.id
        val currentIndex = list.indexOfFirst { it.id == currentId }

        var nextIndex = currentIndex + 1
        if (nextIndex >= list.size) {
            if (_uiState.value.settings.channelWrapAround) {
                nextIndex = 0
            } else {
                return
            }
        }
        playChannel(list[nextIndex])
    }

    fun switchChannelPrev() {
        val list = _uiState.value.filteredChannels.ifEmpty { _uiState.value.channels }
        if (list.isEmpty()) return
        val currentId = _uiState.value.currentChannel?.id
        val currentIndex = list.indexOfFirst { it.id == currentId }

        var prevIndex = currentIndex - 1
        if (prevIndex < 0) {
            if (_uiState.value.settings.channelWrapAround) {
                prevIndex = list.size - 1
            } else {
                return
            }
        }
        playChannel(list[prevIndex])
    }

    // --- Player Control Auto-Hide System ---
    fun showControlsOverlay() {
        controlsDismissJob?.cancel()
        _uiState.value = _uiState.value.copy(isControlsOverlayVisible = true)

        val timeoutSec = _uiState.value.settings.playerControlsTimeoutSeconds
        if (timeoutSec > 0) {
            controlsDismissJob = viewModelScope.launch {
                delay(timeoutSec * 1000L)
                // Automatically hide when no user activity occurs
                _uiState.value = _uiState.value.copy(
                    isControlsOverlayVisible = false,
                    isQuickMenuOpen = false,
                    isAudioTrackDialogVisible = false,
                    isQualityDialogVisible = false
                )
            }
        }
    }

    fun hideControlsOverlay() {
        controlsDismissJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isControlsOverlayVisible = false,
            isQuickMenuOpen = false,
            isAudioTrackDialogVisible = false,
            isQualityDialogVisible = false
        )
    }

    fun toggleControlsOverlay() {
        if (_uiState.value.isControlsOverlayVisible) {
            hideControlsOverlay()
        } else {
            showControlsOverlay()
        }
    }

    fun resetControlsInactivityTimer() {
        if (_uiState.value.isControlsOverlayVisible) {
            showControlsOverlay()
        }
    }

    fun toggleChannelList() {
        val next = !_uiState.value.isChannelListOpen
        _uiState.value = _uiState.value.copy(
            isChannelListOpen = next,
            isNumericKeypadOpen = false,
            isQuickMenuOpen = false
        )
        if (next) showControlsOverlay()
    }

    fun toggleNumericKeypad() {
        val next = !_uiState.value.isNumericKeypadOpen
        _uiState.value = _uiState.value.copy(
            isNumericKeypadOpen = next,
            numericInput = if (next) "" else _uiState.value.numericInput,
            isChannelListOpen = false,
            isQuickMenuOpen = false
        )
        if (next) showControlsOverlay()
    }

    fun onNumericKeyPress(digit: String) {
        resetControlsInactivityTimer()
        autoTuneJob?.cancel()
        val current = _uiState.value.numericInput
        if (current.length >= 4) return
        val updated = current + digit
        _uiState.value = _uiState.value.copy(numericInput = updated)

        val delayMs = _uiState.value.settings.autoTuneNumericDelayMs
        if (delayMs > 0) {
            autoTuneJob = viewModelScope.launch {
                delay(delayMs)
                commitNumericChannel()
            }
        }
    }

    fun onNumericBackspace() {
        resetControlsInactivityTimer()
        autoTuneJob?.cancel()
        val current = _uiState.value.numericInput
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(numericInput = current.dropLast(1))
        }
    }

    fun commitNumericChannel() {
        autoTuneJob?.cancel()
        val number = _uiState.value.numericInput.toIntOrNull()
        if (number != null) {
            val match = _uiState.value.channels.find { it.displayChannelNumber == number }
            if (match != null) {
                playChannel(match)
            } else {
                showToast("Channel $number not found")
            }
        }
        _uiState.value = _uiState.value.copy(isNumericKeypadOpen = false, numericInput = "")
    }

    fun toggleQuickMenu() {
        val next = !_uiState.value.isQuickMenuOpen
        _uiState.value = _uiState.value.copy(
            isQuickMenuOpen = next,
            isChannelListOpen = false,
            isNumericKeypadOpen = false
        )
        if (next) showControlsOverlay()
    }

    fun selectCategory(categoryName: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = categoryName)
        updateFilteredChannels()
    }

    fun toggleFavorite(channelId: String) {
        viewModelScope.launch {
            preferencesManager.toggleFavorite(channelId)
            val updated = _uiState.value.channels.map {
                if (it.id == channelId) it.copy(isFavorite = !it.isFavorite) else it
            }
            _uiState.value = _uiState.value.copy(channels = updated)
            updateFilteredChannels()
        }
    }

    fun navigateTo(screen: CurrentScreen) {
        val current = _uiState.value.currentScreen
        val newStack = if (current != screen) {
            _uiState.value.navigationBackStack + current
        } else {
            _uiState.value.navigationBackStack
        }
        _uiState.value = _uiState.value.copy(
            currentScreen = screen,
            navigationBackStack = newStack,
            isChannelListOpen = false,
            isNumericKeypadOpen = false,
            isQuickMenuOpen = false
        )
    }

    // --- Universal Search ---
    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        val q = query.trim()
        if (q.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = UniversalSearchResult())
            updateFilteredChannels()
            return
        }

        val chs = _uiState.value.channels.filter {
            it.displayName.contains(q, ignoreCase = true) ||
            it.displayChannelNumber.toString().contains(q) ||
            it.displayGroup.contains(q, ignoreCase = true)
        }

        val movs = _uiState.value.movies.filter {
            it.title.contains(q, ignoreCase = true) || it.genre.contains(q, ignoreCase = true)
        }

        val sers = _uiState.value.series.filter {
            it.title.contains(q, ignoreCase = true) || it.genre.contains(q, ignoreCase = true)
        }

        val cats = _uiState.value.categories.filter {
            it.name.contains(q, ignoreCase = true)
        }

        _uiState.value = _uiState.value.copy(
            searchResults = UniversalSearchResult(channels = chs, movies = movs, series = sers, categories = cats)
        )
        updateFilteredChannels()
    }

    // --- Channel Management ---
    fun hideChannel(channelId: String) {
        viewModelScope.launch {
            preferencesManager.toggleChannelHidden(channelId)
            repository.setChannelHidden(channelId, true)
            showToast("Channel hidden")
        }
    }

    fun restoreHiddenChannel(channelId: String) {
        viewModelScope.launch {
            preferencesManager.toggleChannelHidden(channelId)
            repository.setChannelHidden(channelId, false)
            showToast("Channel restored")
        }
    }

    fun restoreAllHiddenChannels() {
        viewModelScope.launch {
            preferencesManager.restoreAllHiddenChannels()
            _uiState.value.channels.forEach { repository.setChannelHidden(it.id, false) }
            showToast("All channels restored")
        }
    }

    fun renameChannelLocally(channelId: String, newName: String) {
        viewModelScope.launch {
            repository.renameChannelLocally(channelId, newName)
            showToast("Channel renamed")
        }
    }

    fun setCustomChannelNumber(channelId: String, number: Int) {
        viewModelScope.launch {
            repository.setCustomChannelNumber(channelId, number)
            showToast("Channel number updated to $number")
        }
    }

    fun moveChannelOrder(channelId: String, moveUp: Boolean) {
        viewModelScope.launch {
            repository.moveChannelOrder(channelId, moveUp)
        }
    }

    fun restoreOriginalProviderList(providerId: String) {
        viewModelScope.launch {
            repository.restoreOriginalProviderList(providerId)
            showToast("Original channel list restored")
        }
    }

    // --- Playback Settings ---
    fun setAspectRatio(mode: AspectRatioMode) {
        viewModelScope.launch {
            preferencesManager.updateAspectRatio(mode)
        }
    }

    fun setPlayerEngine(engine: PlayerEngine) {
        viewModelScope.launch {
            preferencesManager.updatePlayerEngine(engine)
            showToast("Player Engine: ${engine.name}")
        }
    }

    fun setVideoQuality(option: VideoQualityOption) {
        viewModelScope.launch {
            preferencesManager.updateVideoQuality(option)
            playerManager.setVideoQuality(option)
            showToast("Quality set to ${option.label}")
        }
    }

    fun setQualityPreference(preference: QualityPreference) {
        viewModelScope.launch {
            preferencesManager.updateQualityPreference(preference)
            playerManager.setQualityPreference(preference)
        }
    }

    fun setBufferMode(mode: BufferMode) {
        viewModelScope.launch {
            preferencesManager.updateBufferMode(mode)
            playerManager.setBufferMode(mode)
            showToast("Buffer mode: ${mode.name}")
        }
    }

    fun setPlayerControlsTimeout(seconds: Int) {
        viewModelScope.launch {
            preferencesManager.updateControlTimeout(seconds)
            showToast(if (seconds == 0) "Controls set to Never Hide" else "Controls timeout: ${seconds}s")
        }
    }

    fun updateControlTimeout(seconds: Int) = setPlayerControlsTimeout(seconds)

    // --- Theme Management ---
    fun setThemeType(themeType: ThemeType) {
        viewModelScope.launch {
            preferencesManager.updateTheme(themeType)
            showToast("Theme: ${themeType.displayName}")
        }
    }

    fun setTheme(themeType: ThemeType) = setThemeType(themeType)

    fun updateChannelWrapAround(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateWrapAround(enabled)
            showToast(if (enabled) "Channel wrap-around enabled" else "Channel wrap-around disabled")
        }
    }

    fun toggleLowBandwidth(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateLowBandwidth(enabled)
            showToast(if (enabled) "Low bandwidth mode active" else "Low bandwidth mode disabled")
        }
    }

    fun setCustomThemeColors(colors: CustomThemeColors) {
        viewModelScope.launch {
            preferencesManager.updateCustomThemeColors(colors)
            showToast("Custom theme applied")
        }
    }

    // --- Startup & Boot ---
    fun updateStartupBehavior(behavior: StartupBehavior, customCh: Int? = null, favChId: String? = null) {
        viewModelScope.launch {
            preferencesManager.updateStartupBehavior(behavior, customCh, favChId)
            showToast("Startup set to ${behavior.name}")
        }
    }

    fun toggleLaunchOnBoot(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateLaunchOnBoot(enabled)
            showToast(if (enabled) "ZaynTV will launch after boot" else "Launch on boot disabled")
        }
    }

    fun togglePip(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updatePipEnabled(enabled)
        }
    }

    // --- Parental Control ---
    fun setParentalPin(pin: String, enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setParentalPin(pin, enabled)
            showToast(if (enabled) "Parental control PIN activated" else "Parental control disabled")
        }
    }

    fun toggleCategoryLock(categoryName: String) {
        viewModelScope.launch {
            preferencesManager.toggleCategoryLock(categoryName)
            showToast("Category lock updated")
        }
    }

    fun toggleChannelLock(channelId: String) {
        viewModelScope.launch {
            preferencesManager.toggleChannelLock(channelId)
            repository.setParentalLocked(channelId, true)
            showToast("Channel lock updated")
        }
    }

    // --- Watermark & Branding (Requirements 25-29) ---
    fun updateWatermark(
        enabled: Boolean,
        position: WatermarkPosition? = null,
        opacity: Float? = null,
        size: WatermarkSize? = null
    ) {
        viewModelScope.launch {
            preferencesManager.updateWatermark(enabled, position, opacity, size)
            showToast("Watermark settings saved")
        }
    }

    // --- Startup Preferences (Requirements 1 & 2) ---
    fun updateStartupSettings(
        autoStartLiveTv: Boolean? = null,
        autoPlayOnLaunch: Boolean? = null,
        resumeLastChannel: Boolean? = null
    ) {
        viewModelScope.launch {
            preferencesManager.updateStartupSettings(autoStartLiveTv, autoPlayOnLaunch, resumeLastChannel)
            showToast("Startup settings updated")
        }
    }

    // --- Audio & Subtitle Track Preferences (Requirements 14 & 15 & 20) ---
    fun updateAudioSubtitlePreferences(
        defaultAudioLang: String? = null,
        defaultSubtitleLang: String? = null,
        subtitlesEnabled: Boolean? = null
    ) {
        viewModelScope.launch {
            preferencesManager.updateAudioSubtitlePreferences(defaultAudioLang, defaultSubtitleLang, subtitlesEnabled)
            showToast("Playback track preferences saved")
        }
    }

    // --- Channel Sorting & Numbering (Requirements 8 & 9) ---
    fun updateChannelSortAndNumbering(
        sortOrder: ChannelSortOrder? = null,
        numberingMode: ChannelNumberingMode? = null
    ) {
        viewModelScope.launch {
            preferencesManager.updateChannelSortAndNumbering(sortOrder, numberingMode)
            updateFilteredChannels()
            showToast("Channel display order updated")
        }
    }

    // --- App Language (Requirement 32) ---
    fun updateAppLanguage(langCode: String) {
        viewModelScope.launch {
            preferencesManager.updateAppLanguage(langCode)
            showToast("Language updated")
        }
    }

    // --- Provider Management ---
    fun addM3uProvider(name: String, url: String, epg: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val summary = repository.addM3uUrlProvider(name, url, epg)
                _uiState.value = _uiState.value.copy(isImporting = false, importSummary = summary)
                showToast("Imported ${summary.playableCount} channels")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                showToast("Failed to import: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun addXtreamProvider(name: String, url: String, user: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val summary = repository.addXtreamProvider(name, url, user, pass)
                _uiState.value = _uiState.value.copy(isImporting = false, importSummary = summary)
                showToast("Connected: ${summary.playableCount} channels")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                showToast("Xtream connection failed: ${e.message ?: "Unknown"}")
            }
        }
    }

    fun addStalkerProvider(name: String, portalUrl: String, macAddress: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val summary = repository.addStalkerProvider(name, portalUrl, macAddress)
                _uiState.value = _uiState.value.copy(isImporting = false, importSummary = summary)
                showToast("Connected: ${summary.playableCount} channels")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                showToast("Portal connection failed: ${e.message ?: "Unknown"}")
            }
        }
    }

    fun testConnection(provider: Provider) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingConnection = true)
            val result = repository.testProviderConnection(provider)
            _uiState.value = _uiState.value.copy(
                isTestingConnection = false,
                testConnectionResult = result
            )
        }
    }

    fun dismissTestConnectionDialog() {
        _uiState.value = _uiState.value.copy(testConnectionResult = null)
    }

    fun refreshProvider(provider: Provider) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            repository.refreshProvider(provider)
            _uiState.value = _uiState.value.copy(isImporting = false)
            showToast("Provider refreshed")
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            repository.refreshAll()
            _uiState.value = _uiState.value.copy(isImporting = false)
            showToast("All playlists and channels refreshed")
        }
    }

    fun refreshAllProviders() = refreshAll()

    fun selectProvider(provider: Provider) {
        viewModelScope.launch {
            repository.setActiveProvider(provider)
            showToast("Switched to ${provider.name}")
        }
    }

    fun testProvider(provider: Provider) = testConnection(provider)

    fun addFilterRule(rule: FilterRule) {
        viewModelScope.launch {
            repository.addFilterRule(rule)
            showToast("Filter rule added")
        }
    }

    fun removeFilterRule(id: String) {
        viewModelScope.launch {
            repository.removeFilterRule(id)
            showToast("Filter rule removed")
        }
    }

    fun resetAllFilters() {
        viewModelScope.launch {
            repository.resetAllFilters()
            showToast("All filter rules cleared")
        }
    }

    fun deleteProvider(providerId: String) {
        viewModelScope.launch {
            repository.deleteProvider(providerId)
            showToast("Provider removed")
        }
    }

    fun importLocalM3u(name: String, inputStream: InputStream) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            try {
                val summary = repository.importM3uStream(name, inputStream)
                _uiState.value = _uiState.value.copy(isImporting = false, importSummary = summary)
                showToast("Imported ${summary.playableCount} channels")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                showToast("Local import failed: ${e.message ?: "Unknown"}")
            }
        }
    }

    fun dismissImportSummaryAndPlay() {
        _uiState.value = _uiState.value.copy(importSummary = null)
        navigateTo(CurrentScreen.LIVE_TV)
        repository.channels.value.firstOrNull()?.let { playChannel(it) }
    }

    fun dismissImportSummary() {
        _uiState.value = _uiState.value.copy(importSummary = null)
    }

    // --- Authentication ---
    fun login(id: String, pass: String, rememberMe: Boolean = true) {
        viewModelScope.launch {
            val res = authManager.login(id, pass, rememberMe)
            when (res) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(userProfile = res.profile)
                    showToast("Welcome back, ${res.profile.name}!")
                    navigateTo(CurrentScreen.LIVE_TV)
                }
                is AuthResult.Error -> showToast(res.message)
            }
        }
    }

    fun signUp(name: String, username: String, email: String, phone: String, pass: String) {
        viewModelScope.launch {
            val res = authManager.signUp(name, username, email, phone, pass)
            when (res) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(userProfile = res.profile)
                    showToast("Account created successfully!")
                    navigateTo(CurrentScreen.LIVE_TV)
                }
                is AuthResult.Error -> showToast(res.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authManager.logout()
            _uiState.value = _uiState.value.copy(userProfile = UserProfile())
            showToast("Logged out")
            navigateTo(CurrentScreen.LIVE_TV)
        }
    }

    fun updateProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            val res = authManager.updateProfile(name, email, phone, _uiState.value.userProfile)
            if (res is AuthResult.Success) {
                _uiState.value = _uiState.value.copy(userProfile = res.profile)
                showToast("Profile updated")
            }
        }
    }

    // --- Storage & Cache ---
    fun clearCache(type: String) {
        viewModelScope.launch {
            repository.clearCache(type)
            showToast("Cache cleared successfully")
        }
    }

    fun clearRecentChannels() {
        viewModelScope.launch {
            preferencesManager.clearRecentChannels()
            showToast("Watch history cleared")
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            preferencesManager.clearAllFavorites()
            showToast("All favorites cleared")
        }
    }

    fun resetChannelNumbering() {
        viewModelScope.launch {
            repository.resetAllChannelCustomizations()
            showToast("Channel numbering reset to default")
        }
    }

    fun refreshEpg() {
        viewModelScope.launch {
            repository.refreshActiveEpg()
            showToast("EPG refreshed")
        }
    }

    fun clearEpgCache() {
        viewModelScope.launch {
            repository.clearCache("epg")
            showToast("EPG cache cleared")
        }
    }

    fun toggleAutoSkipOnFailure(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateAutoSkipOnFailure(enabled)
            showToast(if (enabled) "Auto-skip on failure enabled" else "Auto-skip on failure disabled")
        }
    }

    fun testNetworkSpeed() {
        viewModelScope.launch {
            val prov = _uiState.value.activeProvider
            if (prov == null) {
                showToast("No active provider configured to test")
                return@launch
            }
            showToast("Testing connectivity to ${prov.name}...")
            val result = repository.testProviderConnection(prov)
            if (result.isSuccess) {
                showToast("Connected: Server reachable, ${result.liveCount} live streams available")
            } else {
                showToast("Connection test result: ${result.statusMessage}")
            }
        }
    }

    // --- Navigation & Remote Handling ---
    fun handleBackPress(): Boolean {
        if (_uiState.value.isAudioTrackDialogVisible || _uiState.value.isQualityDialogVisible) {
            _uiState.value = _uiState.value.copy(
                isAudioTrackDialogVisible = false,
                isQualityDialogVisible = false
            )
            return true
        }
        if (_uiState.value.isParentalPinDialogOpen) {
            dismissParentalDialog()
            return true
        }
        if (_uiState.value.isNumericKeypadOpen) {
            _uiState.value = _uiState.value.copy(isNumericKeypadOpen = false, numericInput = "")
            return true
        }
        if (_uiState.value.isChannelListOpen) {
            _uiState.value = _uiState.value.copy(isChannelListOpen = false)
            return true
        }
        if (_uiState.value.isQuickMenuOpen) {
            _uiState.value = _uiState.value.copy(isQuickMenuOpen = false)
            return true
        }
        if (_uiState.value.isControlsOverlayVisible && _uiState.value.currentScreen == CurrentScreen.LIVE_TV) {
            hideControlsOverlay()
            return true
        }
        if (_uiState.value.importSummary != null) {
            _uiState.value = _uiState.value.copy(importSummary = null)
            return true
        }
        if (_uiState.value.navigationBackStack.isNotEmpty()) {
            val stack = _uiState.value.navigationBackStack.toMutableList()
            val prevScreen = stack.removeAt(stack.size - 1)
            _uiState.value = _uiState.value.copy(
                currentScreen = prevScreen,
                navigationBackStack = stack,
                isChannelListOpen = false,
                isNumericKeypadOpen = false,
                isQuickMenuOpen = false
            )
            return true
        }
        if (_uiState.value.currentScreen != CurrentScreen.HOME && _uiState.value.currentScreen != CurrentScreen.LIVE_TV) {
            _uiState.value = _uiState.value.copy(
                currentScreen = CurrentScreen.HOME,
                isChannelListOpen = false,
                isNumericKeypadOpen = false,
                isQuickMenuOpen = false
            )
            return true
        }
        if (_uiState.value.settings.doubleBackToExit) {
            val now = System.currentTimeMillis()
            if (now - lastBackPressMs < 2000L) {
                return false
            } else {
                lastBackPressMs = now
                showToast("Press BACK again to exit ZaynTV")
                return true
            }
        }
        return false
    }

    fun showToast(msg: String) {
        _uiState.value = _uiState.value.copy(toastMessage = msg)
        viewModelScope.launch {
            delay(3000)
            if (_uiState.value.toastMessage == msg) {
                _uiState.value = _uiState.value.copy(toastMessage = null)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoSkipJob?.cancel()
        controlsDismissJob?.cancel()
        autoTuneJob?.cancel()
        playerManager.release()
    }
}

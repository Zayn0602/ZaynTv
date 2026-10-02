package com.example.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AspectRatioMode
import com.example.data.model.BufferMode
import com.example.data.model.ChannelNumberingMode
import com.example.data.model.ChannelSortOrder
import com.example.data.model.CustomThemeColors
import com.example.data.model.PlayerEngine
import com.example.data.model.QualityPreference
import com.example.data.model.StartupBehavior
import com.example.data.model.ThemeType
import com.example.data.model.UserProfile
import com.example.data.model.UserSettings
import com.example.data.model.VideoQualityOption
import com.example.data.model.WatermarkPosition
import com.example.data.model.WatermarkSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "zayntv_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_STARTUP_BEHAVIOR = stringPreferencesKey("startup_behavior")
        private val KEY_LAST_PROVIDER_ID = stringPreferencesKey("last_provider_id")
        private val KEY_LAST_CHANNEL_ID = stringPreferencesKey("last_channel_id")
        private val KEY_LAST_CATEGORY = stringPreferencesKey("last_category")
        private val KEY_CUSTOM_STARTUP_CH = intPreferencesKey("custom_startup_ch")
        private val KEY_FAV_STARTUP_CH = stringPreferencesKey("fav_startup_ch")
        private val KEY_WRAP_AROUND = booleanPreferencesKey("channel_wrap_around")
        private val KEY_AUTO_TUNE_DELAY = longPreferencesKey("auto_tune_delay")
        private val KEY_ASPECT_RATIO = stringPreferencesKey("aspect_ratio")
        private val KEY_DOUBLE_BACK_EXIT = booleanPreferencesKey("double_back_exit")
        private val KEY_HARDWARE_ACCEL = booleanPreferencesKey("hardware_accel")
        private val KEY_FAVORITES = stringSetPreferencesKey("favorite_channel_ids")
        private val KEY_RECENT_CHANNELS = stringPreferencesKey("recent_channel_ids")

        // New IPTV Settings Keys
        private val KEY_CONTROL_TIMEOUT = intPreferencesKey("control_timeout_seconds")
        private val KEY_PLAYER_ENGINE = stringPreferencesKey("player_engine")
        private val KEY_VIDEO_QUALITY = stringPreferencesKey("video_quality")
        private val KEY_QUALITY_PREF = stringPreferencesKey("quality_preference")
        private val KEY_BUFFER_MODE = stringPreferencesKey("buffer_mode")
        private val KEY_THEME_TYPE = stringPreferencesKey("theme_type")
        private val KEY_CUSTOM_THEME_JSON = stringPreferencesKey("custom_theme_json")
        private val KEY_LAUNCH_ON_BOOT = booleanPreferencesKey("launch_on_boot")
        private val KEY_PIP_ENABLED = booleanPreferencesKey("pip_enabled")
        private val KEY_PARENTAL_PIN = stringPreferencesKey("parental_pin")
        private val KEY_PARENTAL_ENABLED = booleanPreferencesKey("parental_enabled")
        private val KEY_LOCKED_CATEGORIES = stringSetPreferencesKey("parental_locked_categories")
        private val KEY_LOCKED_CHANNELS = stringSetPreferencesKey("parental_locked_channels")
        private val KEY_HIDDEN_CHANNELS = stringSetPreferencesKey("hidden_channel_ids")
        private val KEY_RETRY_COUNT = intPreferencesKey("retry_count")
        private val KEY_CONN_TIMEOUT = intPreferencesKey("conn_timeout")
        private val KEY_LOW_BANDWIDTH = booleanPreferencesKey("low_bandwidth_mode")
        private val KEY_AUTO_SKIP_ON_FAILURE = booleanPreferencesKey("auto_skip_on_failure")

        // Live TV First & Startup Preferences
        private val KEY_AUTO_START_LIVE_TV = booleanPreferencesKey("auto_start_live_tv")
        private val KEY_AUTO_PLAY_ON_LAUNCH = booleanPreferencesKey("auto_play_on_launch")
        private val KEY_RESUME_LAST_CHANNEL = booleanPreferencesKey("resume_last_channel")

        // Watermark Preferences
        private val KEY_WATERMARK_ENABLED = booleanPreferencesKey("watermark_enabled")
        private val KEY_WATERMARK_POSITION = stringPreferencesKey("watermark_position")
        private val KEY_WATERMARK_OPACITY = floatPreferencesKey("watermark_opacity")
        private val KEY_WATERMARK_SIZE = stringPreferencesKey("watermark_size")

        // Playback Track Preferences
        private val KEY_DEFAULT_AUDIO_LANG = stringPreferencesKey("default_audio_language")
        private val KEY_DEFAULT_SUBTITLE_LANG = stringPreferencesKey("default_subtitle_language")
        private val KEY_SUBTITLES_ENABLED = booleanPreferencesKey("subtitles_enabled")

        // Channel Customization Preferences
        private val KEY_CHANNEL_SORT_ORDER = stringPreferencesKey("channel_sort_order")
        private val KEY_CHANNEL_NUMBERING_MODE = stringPreferencesKey("channel_numbering_mode")
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")

        // Auth
        private val KEY_USER_PROFILE_JSON = stringPreferencesKey("user_profile_json")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val startupStr = prefs[KEY_STARTUP_BEHAVIOR] ?: StartupBehavior.LIVE_TV.name
        val startup = try { StartupBehavior.valueOf(startupStr) } catch (_: Exception) { StartupBehavior.LIVE_TV }

        val aspectStr = prefs[KEY_ASPECT_RATIO] ?: AspectRatioMode.FIT.name
        val aspect = try { AspectRatioMode.valueOf(aspectStr) } catch (_: Exception) { AspectRatioMode.FIT }

        val engineStr = prefs[KEY_PLAYER_ENGINE] ?: PlayerEngine.AUTO.name
        val engine = try { PlayerEngine.valueOf(engineStr) } catch (_: Exception) { PlayerEngine.AUTO }

        val qualityStr = prefs[KEY_VIDEO_QUALITY] ?: VideoQualityOption.AUTO.name
        val quality = try { VideoQualityOption.valueOf(qualityStr) } catch (_: Exception) { VideoQualityOption.AUTO }

        val qualPrefStr = prefs[KEY_QUALITY_PREF] ?: QualityPreference.AUTO_QUALITY.name
        val qualPref = try { QualityPreference.valueOf(qualPrefStr) } catch (_: Exception) { QualityPreference.AUTO_QUALITY }

        val bufStr = prefs[KEY_BUFFER_MODE] ?: BufferMode.NORMAL.name
        val bufMode = try { BufferMode.valueOf(bufStr) } catch (_: Exception) { BufferMode.NORMAL }

        val themeStr = prefs[KEY_THEME_TYPE] ?: ThemeType.ZAYN_DARK.name
        val themeType = try { ThemeType.valueOf(themeStr) } catch (_: Exception) { ThemeType.ZAYN_DARK }

        val customThemeJson = prefs[KEY_CUSTOM_THEME_JSON]
        val customColors = parseCustomThemeColors(customThemeJson)

        val wmPosStr = prefs[KEY_WATERMARK_POSITION] ?: WatermarkPosition.BOTTOM_RIGHT.name
        val wmPos = try { WatermarkPosition.valueOf(wmPosStr) } catch (_: Exception) { WatermarkPosition.BOTTOM_RIGHT }

        val wmSizeStr = prefs[KEY_WATERMARK_SIZE] ?: WatermarkSize.SMALL.name
        val wmSize = try { WatermarkSize.valueOf(wmSizeStr) } catch (_: Exception) { WatermarkSize.SMALL }

        val sortStr = prefs[KEY_CHANNEL_SORT_ORDER] ?: ChannelSortOrder.PROVIDER_ORDER.name
        val sortOrder = try { ChannelSortOrder.valueOf(sortStr) } catch (_: Exception) { ChannelSortOrder.PROVIDER_ORDER }

        val numModeStr = prefs[KEY_CHANNEL_NUMBERING_MODE] ?: ChannelNumberingMode.AUTO.name
        val numMode = try { ChannelNumberingMode.valueOf(numModeStr) } catch (_: Exception) { ChannelNumberingMode.AUTO }

        UserSettings(
            startupBehavior = startup,
            autoStartLiveTv = prefs[KEY_AUTO_START_LIVE_TV] ?: true,
            autoPlayOnLaunch = prefs[KEY_AUTO_PLAY_ON_LAUNCH] ?: true,
            resumeLastChannel = prefs[KEY_RESUME_LAST_CHANNEL] ?: true,
            lastProviderId = prefs[KEY_LAST_PROVIDER_ID],
            lastChannelId = prefs[KEY_LAST_CHANNEL_ID],
            lastCategory = prefs[KEY_LAST_CATEGORY] ?: "All",
            customStartupChannelNumber = prefs[KEY_CUSTOM_STARTUP_CH] ?: 1,
            favoriteStartupChannelId = prefs[KEY_FAV_STARTUP_CH],
            channelWrapAround = prefs[KEY_WRAP_AROUND] ?: true,
            autoTuneNumericDelayMs = prefs[KEY_AUTO_TUNE_DELAY] ?: 1800L,
            aspectRatio = aspect,
            doubleBackToExit = prefs[KEY_DOUBLE_BACK_EXIT] ?: true,
            hardwareAcceleration = prefs[KEY_HARDWARE_ACCEL] ?: true,
            playerControlsTimeoutSeconds = prefs[KEY_CONTROL_TIMEOUT] ?: 5,
            playerEngine = engine,
            videoQuality = quality,
            qualityPreference = qualPref,
            bufferMode = bufMode,
            themeType = themeType,
            customThemeColors = customColors,
            launchOnBoot = prefs[KEY_LAUNCH_ON_BOOT] ?: false,
            pipEnabled = prefs[KEY_PIP_ENABLED] ?: true,
            parentalPin = prefs[KEY_PARENTAL_PIN] ?: "",
            parentalEnabled = prefs[KEY_PARENTAL_ENABLED] ?: false,
            retryCount = prefs[KEY_RETRY_COUNT] ?: 2,
            connectionTimeoutSeconds = prefs[KEY_CONN_TIMEOUT] ?: 15,
            lowBandwidthMode = prefs[KEY_LOW_BANDWIDTH] ?: false,
            autoSkipOnFailure = prefs[KEY_AUTO_SKIP_ON_FAILURE] ?: true,
            watermarkEnabled = prefs[KEY_WATERMARK_ENABLED] ?: true,
            watermarkPosition = wmPos,
            watermarkOpacity = prefs[KEY_WATERMARK_OPACITY] ?: 0.40f,
            watermarkSize = wmSize,
            defaultAudioLanguage = prefs[KEY_DEFAULT_AUDIO_LANG] ?: "auto",
            defaultSubtitleLanguage = prefs[KEY_DEFAULT_SUBTITLE_LANG] ?: "off",
            subtitlesEnabled = prefs[KEY_SUBTITLES_ENABLED] ?: false,
            channelSortOrder = sortOrder,
            channelNumberingMode = numMode,
            appLanguage = prefs[KEY_APP_LANGUAGE] ?: "system"
        )
    }

    val favoriteIdsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITES] ?: emptySet()
    }

    val hiddenChannelIdsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_HIDDEN_CHANNELS] ?: emptySet()
    }

    val lockedCategoriesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOCKED_CATEGORIES] ?: emptySet()
    }

    val lockedChannelsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOCKED_CHANNELS] ?: emptySet()
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_USER_PROFILE_JSON]
        if (jsonStr.isNullOrBlank()) {
            UserProfile()
        } else {
            try {
                val json = JSONObject(jsonStr)
                UserProfile(
                    id = json.optString("id", "user_1"),
                    name = json.optString("name", "User"),
                    username = json.optString("username", "user"),
                    email = json.optString("email", ""),
                    phone = json.optString("phone", ""),
                    avatarUrl = json.optString("avatarUrl", ""),
                    sessionToken = json.optString("sessionToken", ""),
                    isLoggedIn = json.optBoolean("isLoggedIn", false),
                    rememberMe = json.optBoolean("rememberMe", true)
                )
            } catch (_: Exception) {
                UserProfile()
            }
        }
    }

    suspend fun saveLastChannel(providerId: String?, channelId: String?, category: String?) {
        context.dataStore.edit { prefs ->
            if (providerId != null) prefs[KEY_LAST_PROVIDER_ID] = providerId
            if (channelId != null) prefs[KEY_LAST_CHANNEL_ID] = channelId
            if (category != null) prefs[KEY_LAST_CATEGORY] = category
        }
    }

    suspend fun updateStartupBehavior(behavior: StartupBehavior, customCh: Int? = null, favChId: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[KEY_STARTUP_BEHAVIOR] = behavior.name
            if (customCh != null) prefs[KEY_CUSTOM_STARTUP_CH] = customCh
            if (favChId != null) prefs[KEY_FAV_STARTUP_CH] = favChId
        }
    }

    suspend fun updateAspectRatio(aspect: AspectRatioMode) {
        context.dataStore.edit { prefs -> prefs[KEY_ASPECT_RATIO] = aspect.name }
    }

    suspend fun updatePlayerEngine(engine: PlayerEngine) {
        context.dataStore.edit { prefs -> prefs[KEY_PLAYER_ENGINE] = engine.name }
    }

    suspend fun updateVideoQuality(quality: VideoQualityOption) {
        context.dataStore.edit { prefs -> prefs[KEY_VIDEO_QUALITY] = quality.name }
    }

    suspend fun updateQualityPreference(pref: QualityPreference) {
        context.dataStore.edit { prefs -> prefs[KEY_QUALITY_PREF] = pref.name }
    }

    suspend fun updateBufferMode(mode: BufferMode) {
        context.dataStore.edit { prefs -> prefs[KEY_BUFFER_MODE] = mode.name }
    }

    suspend fun updateControlTimeout(seconds: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_CONTROL_TIMEOUT] = seconds }
    }

    suspend fun updateTheme(themeType: ThemeType) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME_TYPE] = themeType.name }
    }

    suspend fun updateCustomThemeColors(colors: CustomThemeColors) {
        context.dataStore.edit { prefs ->
            val json = JSONObject().apply {
                put("primary", colors.primaryColor)
                put("accent", colors.accentColor)
                put("background", colors.backgroundColor)
                put("card", colors.cardColor)
                put("text", colors.textColor)
                put("focus", colors.focusColor)
            }
            prefs[KEY_CUSTOM_THEME_JSON] = json.toString()
            prefs[KEY_THEME_TYPE] = ThemeType.CUSTOM.name
        }
    }

    suspend fun updateLaunchOnBoot(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_LAUNCH_ON_BOOT] = enabled }
    }

    suspend fun updatePipEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_PIP_ENABLED] = enabled }
    }

    suspend fun setParentalPin(pin: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PARENTAL_PIN] = pin
            prefs[KEY_PARENTAL_ENABLED] = enabled
        }
    }

    suspend fun toggleCategoryLock(categoryName: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_LOCKED_CATEGORIES] ?: emptySet()).toMutableSet()
            if (current.contains(categoryName)) current.remove(categoryName) else current.add(categoryName)
            prefs[KEY_LOCKED_CATEGORIES] = current
        }
    }

    suspend fun toggleChannelLock(channelId: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_LOCKED_CHANNELS] ?: emptySet()).toMutableSet()
            if (current.contains(channelId)) current.remove(channelId) else current.add(channelId)
            prefs[KEY_LOCKED_CHANNELS] = current
        }
    }

    suspend fun toggleChannelHidden(channelId: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_HIDDEN_CHANNELS] ?: emptySet()).toMutableSet()
            if (current.contains(channelId)) current.remove(channelId) else current.add(channelId)
            prefs[KEY_HIDDEN_CHANNELS] = current
        }
    }

    suspend fun restoreAllHiddenChannels() {
        context.dataStore.edit { prefs -> prefs[KEY_HIDDEN_CHANNELS] = emptySet() }
    }

    suspend fun toggleFavorite(channelId: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_FAVORITES] ?: emptySet()).toMutableSet()
            if (current.contains(channelId)) current.remove(channelId) else current.add(channelId)
            prefs[KEY_FAVORITES] = current
        }
    }

    suspend fun clearAllFavorites() {
        context.dataStore.edit { prefs -> prefs[KEY_FAVORITES] = emptySet() }
    }

    suspend fun updateWrapAround(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_WRAP_AROUND] = enabled }
    }

    suspend fun updateDoubleBackExit(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_DOUBLE_BACK_EXIT] = enabled }
    }

    suspend fun updateLowBandwidth(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_LOW_BANDWIDTH] = enabled }
    }

    suspend fun updateAutoSkipOnFailure(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTO_SKIP_ON_FAILURE] = enabled }
    }

    suspend fun updateNetworkSettings(retryCount: Int, connTimeout: Int, lowBandwidth: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RETRY_COUNT] = retryCount
            prefs[KEY_CONN_TIMEOUT] = connTimeout
            prefs[KEY_LOW_BANDWIDTH] = lowBandwidth
        }
    }

    suspend fun addRecentChannel(channelId: String) {
        context.dataStore.edit { prefs ->
            val existing = prefs[KEY_RECENT_CHANNELS]?.split(",")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
            existing.remove(channelId)
            existing.add(0, channelId)
            val trimmed = existing.take(30).joinToString(",")
            prefs[KEY_RECENT_CHANNELS] = trimmed
        }
    }

    suspend fun clearRecentChannels() {
        context.dataStore.edit { prefs -> prefs[KEY_RECENT_CHANNELS] = "" }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            val json = JSONObject().apply {
                put("id", profile.id)
                put("name", profile.name)
                put("username", profile.username)
                put("email", profile.email)
                put("phone", profile.phone)
                put("avatarUrl", profile.avatarUrl)
                put("sessionToken", profile.sessionToken)
                put("isLoggedIn", profile.isLoggedIn)
                put("rememberMe", profile.rememberMe)
            }
            prefs[KEY_USER_PROFILE_JSON] = json.toString()
        }
    }

    suspend fun clearUserSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_PROFILE_JSON)
        }
    }

    suspend fun updateWatermark(
        enabled: Boolean,
        position: WatermarkPosition? = null,
        opacity: Float? = null,
        size: WatermarkSize? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WATERMARK_ENABLED] = enabled
            if (position != null) prefs[KEY_WATERMARK_POSITION] = position.name
            if (opacity != null) prefs[KEY_WATERMARK_OPACITY] = opacity
            if (size != null) prefs[KEY_WATERMARK_SIZE] = size.name
        }
    }

    suspend fun updateStartupSettings(
        autoStartLiveTv: Boolean? = null,
        autoPlayOnLaunch: Boolean? = null,
        resumeLastChannel: Boolean? = null
    ) {
        context.dataStore.edit { prefs ->
            if (autoStartLiveTv != null) prefs[KEY_AUTO_START_LIVE_TV] = autoStartLiveTv
            if (autoPlayOnLaunch != null) prefs[KEY_AUTO_PLAY_ON_LAUNCH] = autoPlayOnLaunch
            if (resumeLastChannel != null) prefs[KEY_RESUME_LAST_CHANNEL] = resumeLastChannel
        }
    }

    suspend fun updateAudioSubtitlePreferences(
        defaultAudioLang: String? = null,
        defaultSubtitleLang: String? = null,
        subtitlesEnabled: Boolean? = null
    ) {
        context.dataStore.edit { prefs ->
            if (defaultAudioLang != null) prefs[KEY_DEFAULT_AUDIO_LANG] = defaultAudioLang
            if (defaultSubtitleLang != null) prefs[KEY_DEFAULT_SUBTITLE_LANG] = defaultSubtitleLang
            if (subtitlesEnabled != null) prefs[KEY_SUBTITLES_ENABLED] = subtitlesEnabled
        }
    }

    suspend fun updateChannelSortAndNumbering(
        sortOrder: ChannelSortOrder? = null,
        numberingMode: ChannelNumberingMode? = null
    ) {
        context.dataStore.edit { prefs ->
            if (sortOrder != null) prefs[KEY_CHANNEL_SORT_ORDER] = sortOrder.name
            if (numberingMode != null) prefs[KEY_CHANNEL_NUMBERING_MODE] = numberingMode.name
        }
    }

    suspend fun updateAppLanguage(langCode: String) {
        context.dataStore.edit { prefs -> prefs[KEY_APP_LANGUAGE] = langCode }
    }

    val recentChannelIdsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_RECENT_CHANNELS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    private fun parseCustomThemeColors(jsonStr: String?): CustomThemeColors {
        if (jsonStr.isNullOrBlank()) return CustomThemeColors()
        return try {
            val json = JSONObject(jsonStr)
            CustomThemeColors(
                primaryColor = json.optLong("primary", 0xFF00E5FF),
                accentColor = json.optLong("accent", 0xFF7C4DFF),
                backgroundColor = json.optLong("background", 0xFF070A10),
                cardColor = json.optLong("card", 0xFF172033),
                textColor = json.optLong("text", 0xFFFFFFFF),
                focusColor = json.optLong("focus", 0xFF00E5FF)
            )
        } catch (_: Exception) {
            CustomThemeColors()
        }
    }
}

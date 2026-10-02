package com.example.data.model

import com.example.data.epg.EpgParser

enum class ProviderType {
    M3U_URL,
    M3U_LOCAL,
    XTREAM,
    STALKER
}

enum class StartupBehavior {
    LAST_WATCHED,
    FIRST_CHANNEL,
    FAVORITE_CHANNEL,
    CUSTOM_CHANNEL,
    HOME_SCREEN,
    LIVE_TV
}

enum class AspectRatioMode {
    FIT,
    FILL,
    ZOOM,
    FOUR_THREE,
    SIXTEEN_NINE,
    ORIGINAL
}

enum class PlayerEngine {
    AUTO,
    EXOPLAYER,
    VLC_SOFTWARE
}

enum class VideoQualityOption(val label: String, val maxHeight: Int?) {
    AUTO("Auto", null),
    ORIGINAL("Original", null),
    Q_4K("4K (2160p)", 2160),
    Q_1080P("1080p Full HD", 1080),
    Q_720P("720p HD", 720),
    Q_480P("480p SD", 480),
    Q_360P("360p Low", 360),
    LOW_DATA("Low Data Mode", 240)
}

enum class QualityPreference {
    AUTO_QUALITY,
    ADAPTIVE_BITRATE,
    PREFER_BEST_QUALITY,
    PREFER_STABLE,
    LOW_BANDWIDTH
}

enum class BufferMode(val minBufferMs: Int, val maxBufferMs: Int, val startBufferMs: Int) {
    LOW(5_000, 15_000, 800),       // Fast channel switching
    NORMAL(15_000, 30_000, 1_500),  // Balanced
    STABLE(30_000, 60_000, 3_000),  // Resistant to network dips
    CUSTOM(20_000, 45_000, 2_000)
}

enum class ThemeType(val displayName: String) {
    ZAYN_DARK("Zayn Dark"),
    MIDNIGHT_BLACK("Midnight Black"),
    AMOLED_BLACK("AMOLED Black"),
    OCEAN_BLUE("Ocean Blue"),
    ROYAL_PURPLE("Royal Purple"),
    EMERALD("Emerald"),
    RED_CINEMA("Red Cinema"),
    LIGHT("Light"),
    SYSTEM("System Default"),
    CUSTOM("Custom Theme")
}

data class CustomThemeColors(
    val primaryColor: Long = 0xFF00E5FF,
    val accentColor: Long = 0xFF7C4DFF,
    val backgroundColor: Long = 0xFF070A10,
    val cardColor: Long = 0xFF172033,
    val textColor: Long = 0xFFFFFFFF,
    val focusColor: Long = 0xFF00E5FF
)

data class FilterRule(
    val id: String,
    val type: FilterType,
    val pattern: String,
    val isExclude: Boolean = true,
    val isEnabled: Boolean = true
)

enum class FilterType {
    CATEGORY,
    CHANNEL_NAME,
    GROUP
}

data class Provider(
    val id: String,
    val name: String,
    val type: ProviderType,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val epgUrl: String = "",
    val macAddress: String = "",
    val isActive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),
    val channelCount: Int = 0
)

data class Channel(
    val id: String,
    val providerId: String,
    val channelNumber: Int,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val group: String = "General",
    val tvgId: String = "",
    val tvgName: String = "",
    val customName: String? = null,
    val customNumber: Int? = null,
    val customGroup: String? = null,
    val isFavorite: Boolean = false,
    val isCustomHidden: Boolean = false,
    val isParentalLocked: Boolean = false,
    val headers: Map<String, String> = emptyMap(),
    val alternativeUrls: List<String> = emptyList(),
    val language: String = "",
    val country: String = "",
    val isPlayable: Boolean = true,
    val sortOrder: Int = 0
) {
    val displayName: String
        get() = customName ?: name

    val displayChannelNumber: Int
        get() = customNumber ?: channelNumber

    val displayGroup: String
        get() = customGroup ?: group

    val formattedNumber: String
        get() = displayChannelNumber.toString().padStart(3, '0')

    val currentProgram: Program
        get() = EpgParser.getFallbackPrograms(displayName, displayGroup).first

    val nextProgram: Program
        get() = EpgParser.getFallbackPrograms(displayName, displayGroup).second
}

data class Category(
    val id: String,
    val name: String,
    val channelCount: Int = 0,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val isCustom: Boolean = false,
    val orderIndex: Int = 0
)

data class Program(
    val id: String,
    val channelId: String,
    val title: String,
    val description: String = "",
    val category: String = "",
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long
) {
    val progress: Float
        get() {
            val now = System.currentTimeMillis()
            if (now < startTimeEpochMs) return 0f
            if (now >= endTimeEpochMs) return 1f
            val duration = (endTimeEpochMs - startTimeEpochMs).toFloat()
            return if (duration > 0f) ((now - startTimeEpochMs) / duration).coerceIn(0f, 1f) else 0f
        }

    val isCurrent: Boolean
        get() {
            val now = System.currentTimeMillis()
            return now in startTimeEpochMs until endTimeEpochMs
        }

    val timeRange: String
        get() {
            if (startTimeEpochMs <= 0L || endTimeEpochMs <= 0L) return ""
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            return "${sdf.format(java.util.Date(startTimeEpochMs))} - ${sdf.format(java.util.Date(endTimeEpochMs))}"
        }

    val progressPercent: Int
        get() = (progress * 100).toInt()
}

data class Movie(
    val id: String,
    val providerId: String,
    val title: String,
    val streamUrl: String,
    val posterUrl: String? = null,
    val genre: String = "",
    val year: String = "",
    val duration: String = "",
    val description: String = "",
    val rating: String = ""
)

data class Series(
    val id: String,
    val providerId: String,
    val title: String,
    val posterUrl: String? = null,
    val genre: String = "",
    val releaseDate: String = "",
    val rating: String = "",
    val description: String = "",
    val seasonsCount: Int = 1
)

data class ImportSummary(
    val totalEntries: Int,
    val playableCount: Int,
    val skippedCount: Int,
    val providerName: String
)

data class XtreamConnectionResult(
    val isSuccess: Boolean,
    val serverReachable: Boolean,
    val authSuccess: Boolean,
    val apiWorking: Boolean,
    val liveCount: Int = 0,
    val vodCount: Int = 0,
    val seriesCount: Int = 0,
    val expirationDate: String = "",
    val statusMessage: String = ""
)

data class UserProfile(
    val id: String = "user_default",
    val name: String = "Guest User",
    val username: String = "guest",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val sessionToken: String = "",
    val isLoggedIn: Boolean = false,
    val rememberMe: Boolean = true
)

enum class WatermarkPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

enum class WatermarkSize {
    SMALL,
    MEDIUM,
    LARGE
}

enum class ChannelSortOrder {
    PROVIDER_ORDER,
    CUSTOM_NUMBER,
    A_TO_Z,
    FAVORITES_FIRST,
    RECENTLY_WATCHED
}

enum class ChannelNumberingMode {
    AUTO,
    MANUAL
}

data class UserSettings(
    val startupBehavior: StartupBehavior = StartupBehavior.LIVE_TV,
    val autoStartLiveTv: Boolean = true,
    val autoPlayOnLaunch: Boolean = true,
    val resumeLastChannel: Boolean = true,
    val lastProviderId: String? = null,
    val lastChannelId: String? = null,
    val lastCategory: String = "All",
    val customStartupChannelNumber: Int = 1,
    val favoriteStartupChannelId: String? = null,
    val channelWrapAround: Boolean = true,
    val autoTuneNumericDelayMs: Long = 1800L,
    val aspectRatio: AspectRatioMode = AspectRatioMode.FIT,
    val doubleBackToExit: Boolean = true,
    val hardwareAcceleration: Boolean = true,
    val autoZapDelayMs: Long = 400L,
    val showChannelLogo: Boolean = true,
    val channelFailTimeoutSeconds: Int = 10,
    val autoSkipOnFailure: Boolean = true,
    val playerControlsTimeoutSeconds: Int = 5, // 3, 5, 7, or 0 (Never)
    val playerEngine: PlayerEngine = PlayerEngine.AUTO,
    val videoQuality: VideoQualityOption = VideoQualityOption.AUTO,
    val qualityPreference: QualityPreference = QualityPreference.AUTO_QUALITY,
    val bufferMode: BufferMode = BufferMode.NORMAL,
    val themeType: ThemeType = ThemeType.ZAYN_DARK,
    val customThemeColors: CustomThemeColors = CustomThemeColors(),
    val launchOnBoot: Boolean = false,
    val pipEnabled: Boolean = true,
    val parentalPin: String = "",
    val parentalEnabled: Boolean = false,
    val autoRetryOnError: Boolean = true,
    val retryCount: Int = 2,
    val connectionTimeoutSeconds: Int = 15,
    val lowBandwidthMode: Boolean = false,
    val watermarkEnabled: Boolean = true,
    val watermarkPosition: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
    val watermarkOpacity: Float = 0.50f,
    val watermarkSize: WatermarkSize = WatermarkSize.SMALL,
    val defaultAudioLanguage: String = "auto",
    val defaultSubtitleLanguage: String = "off",
    val subtitlesEnabled: Boolean = false,
    val channelSortOrder: ChannelSortOrder = ChannelSortOrder.PROVIDER_ORDER,
    val channelNumberingMode: ChannelNumberingMode = ChannelNumberingMode.AUTO,
    val appLanguage: String = "system"
)

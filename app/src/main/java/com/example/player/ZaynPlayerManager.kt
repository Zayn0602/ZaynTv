package com.example.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.BehindLiveWindowException
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.data.model.AspectRatioMode
import com.example.data.model.BufferMode
import com.example.data.model.Channel
import com.example.data.model.PlayerEngine
import com.example.data.model.QualityPreference
import com.example.data.model.VideoQualityOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PlaybackState {
    object Idle : PlaybackState
    object Buffering : PlaybackState
    object Playing : PlaybackState
    object Paused : PlaybackState
    data class FallbackAttempt(val message: String) : PlaybackState
    data class Error(
        val message: String,
        val channel: Channel?,
        val canRetry: Boolean = true
    ) : PlaybackState
}

data class TrackInfo(
    val id: String,
    val name: String,
    val language: String,
    val isSelected: Boolean
)

@OptIn(UnstableApi::class)
class ZaynPlayerManager(private val context: Context) {

    companion object {
        private const val TAG = "ZaynPlayer"
        private const val DEFAULT_USER_AGENT = "ZaynTV/2.0 (Linux; Android TV; ExoPlayer)"
        private const val MAX_AUTO_RETRIES = 2
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var exoPlayer: ExoPlayer? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState = _playbackState.asStateFlow()

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel = _currentChannel.asStateFlow()

    private val _aspectRatio = MutableStateFlow(AspectRatioMode.FIT)
    val aspectRatio = _aspectRatio.asStateFlow()

    private val _currentQuality = MutableStateFlow(VideoQualityOption.AUTO)
    val currentQuality = _currentQuality.asStateFlow()

    private val _audioTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val audioTracks = _audioTracks.asStateFlow()

    private val _subtitleTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val subtitleTracks = _subtitleTracks.asStateFlow()

    private val _isSubtitleEnabled = MutableStateFlow(false)
    val isSubtitleEnabled = _isSubtitleEnabled.asStateFlow()

    private var retryCount = 0
    private var isReleased = false
    private var currentUrlIndex = 0
    private var activeUrlsToTry = listOf<String>()

    private var currentEngine: PlayerEngine = PlayerEngine.AUTO
    private var currentBufferMode: BufferMode = BufferMode.NORMAL

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> {
                    if (_playbackState.value !is PlaybackState.FallbackAttempt) {
                        _playbackState.value = PlaybackState.Buffering
                    }
                }
                Player.STATE_READY -> {
                    retryCount = 0
                    currentUrlIndex = 0
                    val isPlaying = exoPlayer?.isPlaying == true
                    _playbackState.value = if (isPlaying) PlaybackState.Playing else PlaybackState.Paused
                }
                Player.STATE_ENDED -> {
                    _playbackState.value = PlaybackState.Paused
                }
                Player.STATE_IDLE -> {
                    if (_playbackState.value !is PlaybackState.Error && _playbackState.value !is PlaybackState.FallbackAttempt) {
                        _playbackState.value = PlaybackState.Idle
                    }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                _playbackState.value = PlaybackState.Playing
            } else if (exoPlayer?.playbackState == Player.STATE_READY) {
                _playbackState.value = PlaybackState.Paused
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            val audioList = mutableListOf<TrackInfo>()
            val subList = mutableListOf<TrackInfo>()

            for (group in tracks.groups) {
                if (group.type == C.TRACK_TYPE_AUDIO) {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        val lang = format.language ?: "und"
                        val label = format.label ?: "Audio ${audioList.size + 1} ($lang)"
                        audioList.add(
                            TrackInfo(
                                id = format.id ?: "${group.type}_$i",
                                name = label,
                                language = lang,
                                isSelected = group.isTrackSelected(i)
                            )
                        )
                    }
                } else if (group.type == C.TRACK_TYPE_TEXT) {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        val lang = format.language ?: "und"
                        val label = format.label ?: "Subtitle ${subList.size + 1} ($lang)"
                        subList.add(
                            TrackInfo(
                                id = format.id ?: "${group.type}_$i",
                                name = label,
                                language = lang,
                                isSelected = group.isTrackSelected(i)
                            )
                        )
                    }
                }
            }
            _audioTracks.value = audioList
            _subtitleTracks.value = subList
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "PlaybackException: ${error.errorCodeName}", error)
            val cause = error.cause

            // 1. Auto-recover from HLS BehindLiveWindowException
            if (cause is BehindLiveWindowException) {
                exoPlayer?.let { player ->
                    player.seekToDefaultPosition()
                    player.prepare()
                    return
                }
            }

            // 2. Fallback to alternative stream format (e.g. .ts <-> .m3u8 for Xtream)
            if (currentUrlIndex + 1 < activeUrlsToTry.size) {
                currentUrlIndex++
                val nextUrl = activeUrlsToTry[currentUrlIndex]
                _playbackState.value = PlaybackState.FallbackAttempt("Trying another playback method...")
                mainHandler.postDelayed({
                    playUrlInternal(nextUrl, _currentChannel.value?.id ?: "ch", isRetry = true)
                }, 600L)
                return
            }

            // 3. Fallback engine attempt if Auto mode was active
            if (currentEngine == PlayerEngine.AUTO && retryCount == 0) {
                retryCount++
                _playbackState.value = PlaybackState.FallbackAttempt("Trying software decoder engine...")
                mainHandler.postDelayed({
                    rebuildPlayerWithEngine(PlayerEngine.VLC_SOFTWARE)
                    _currentChannel.value?.let { playChannel(it) }
                }, 800L)
                return
            }

            // 4. Clean human-readable error messages
            val friendlyMsg = when {
                cause is HttpDataSource.InvalidResponseCodeException -> {
                    when (cause.responseCode) {
                        403 -> "Access forbidden by IPTV server (HTTP 403)."
                        404 -> "Channel stream not found (HTTP 404)."
                        410 -> "Stream link has expired (HTTP 410)."
                        in 500..599 -> "IPTV server error (${cause.responseCode}). Please try again later."
                        else -> "HTTP error ${cause.responseCode} from stream server."
                    }
                }
                cause is HttpDataSource.HttpDataSourceException -> "Network error connecting to stream server."
                error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
                error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "Network connection failed or timed out."
                error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ||
                error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> "Stream format unsupported or codec error."
                else -> "Unable to play this channel."
            }

            _playbackState.value = PlaybackState.Error(
                message = friendlyMsg,
                channel = _currentChannel.value
            )
        }
    }

    @Synchronized
    fun getPlayer(): ExoPlayer {
        if (exoPlayer == null || isReleased) {
            initializePlayer()
        }
        return exoPlayer ?: run {
            ExoPlayer.Builder(context)
                .setLooper(Looper.getMainLooper())
                .build().also {
                    exoPlayer = it
                    isReleased = false
                }
        }
    }

    @Synchronized
    private fun initializePlayer() {
        try {
            isReleased = false

            // Renderer mode based on engine
            val renderersFactory = DefaultRenderersFactory(context).apply {
                if (currentEngine == PlayerEngine.VLC_SOFTWARE) {
                    setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                    setEnableDecoderFallback(true)
                } else {
                    setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                    setEnableDecoderFallback(true)
                }
            }

            // Buffer LoadControl based on BufferMode
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    currentBufferMode.minBufferMs,
                    currentBufferMode.maxBufferMs,
                    currentBufferMode.startBufferMs,
                    currentBufferMode.startBufferMs + 1000
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()

            val bandwidthMeter = DefaultBandwidthMeter.getSingletonInstance(context)

            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent(DEFAULT_USER_AGENT)
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15_000)
                .setReadTimeoutMs(20_000)

            val defaultDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
            val mediaSourceFactory = DefaultMediaSourceFactory(defaultDataSourceFactory)

            exoPlayer = ExoPlayer.Builder(context)
                .setLooper(Looper.getMainLooper())
                .setRenderersFactory(renderersFactory)
                .setLoadControl(loadControl)
                .setMediaSourceFactory(mediaSourceFactory)
                .setBandwidthMeter(bandwidthMeter)
                .build().apply {
                    playWhenReady = true
                    addListener(playerListener)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing player", e)
            try {
                exoPlayer = ExoPlayer.Builder(context)
                    .setLooper(Looper.getMainLooper())
                    .build().apply {
                        playWhenReady = true
                        addListener(playerListener)
                    }
                isReleased = false
            } catch (_: Exception) {}
        }
    }

    fun setEngine(engine: PlayerEngine) {
        if (currentEngine != engine) {
            currentEngine = engine
            rebuildPlayerWithEngine(engine)
        }
    }

    fun setBufferMode(bufferMode: BufferMode) {
        if (currentBufferMode != bufferMode) {
            currentBufferMode = bufferMode
            rebuildPlayerWithEngine(currentEngine)
        }
    }

    private fun rebuildPlayerWithEngine(engine: PlayerEngine) {
        ensureMainThread {
            val wasPlaying = exoPlayer?.isPlaying == true
            val currentMedia = exoPlayer?.currentMediaItem
            val currentPosition = exoPlayer?.currentPosition ?: 0L

            exoPlayer?.removeListener(playerListener)
            exoPlayer?.release()
            exoPlayer = null

            initializePlayer()

            if (currentMedia != null && exoPlayer != null) {
                exoPlayer?.setMediaItem(currentMedia)
                if (currentPosition > 0) exoPlayer?.seekTo(currentPosition)
                exoPlayer?.prepare()
                if (wasPlaying) exoPlayer?.play()
            }
        }
    }

    fun playChannel(channel: Channel) {
        retryCount = 0
        currentUrlIndex = 0
        _currentChannel.value = channel

        // Build list of all candidate URLs (primary + alternatives)
        val candidateUrls = mutableListOf(channel.streamUrl)
        candidateUrls.addAll(channel.alternativeUrls)
        activeUrlsToTry = candidateUrls.distinct()

        playUrlInternal(activeUrlsToTry[0], channel.id, isRetry = false)
    }

    private fun playUrlInternal(url: String, mediaId: String, isRetry: Boolean) {
        ensureMainThread {
            try {
                _playbackState.value = PlaybackState.Buffering

                val rawUrl = url.trim().replace("\r", "")
                if (rawUrl.isBlank()) {
                    _playbackState.value = PlaybackState.Error("Stream URL is empty.", _currentChannel.value)
                    return@ensureMainThread
                }

                val sanitizedUrl = if (rawUrl.contains(" ")) rawUrl.replace(" ", "%20") else rawUrl
                val uri = try { Uri.parse(sanitizedUrl) } catch (e: Exception) {
                    _playbackState.value = PlaybackState.Error("Invalid URL format.", _currentChannel.value)
                    return@ensureMainThread
                }

                val player = getPlayer()
                val mimeType = detectMimeType(sanitizedUrl)
                val mediaItemBuilder = MediaItem.Builder()
                    .setUri(uri)
                    .setMediaId(mediaId)

                if (mimeType != null) {
                    mediaItemBuilder.setMimeType(mimeType)
                }

                val mediaItem = mediaItemBuilder.build()
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting playback", e)
                _playbackState.value = PlaybackState.Error("Error starting playback: ${e.localizedMessage ?: "Unknown"}", _currentChannel.value)
            }
        }
    }

    fun playVod(url: String, title: String) {
        ensureMainThread {
            try {
                _playbackState.value = PlaybackState.Buffering
                val player = getPlayer()
                val cleanUrl = url.trim().replace(" ", "%20")
                val uri = Uri.parse(cleanUrl)
                val mime = detectMimeType(cleanUrl)
                val item = MediaItem.Builder()
                    .setUri(uri)
                    .setMediaId(title)
                    .apply { if (mime != null) setMimeType(mime) }
                    .build()
                player.setMediaItem(item)
                player.prepare()
                player.play()
            } catch (e: Exception) {
                _playbackState.value = PlaybackState.Error("Unable to play video: ${e.message}", null)
            }
        }
    }

    // Video Quality Selection via TrackSelectionParameters
    fun setVideoQuality(option: VideoQualityOption) {
        ensureMainThread {
            _currentQuality.value = option
            val player = exoPlayer ?: return@ensureMainThread
            val paramsBuilder = player.trackSelectionParameters.buildUpon()

            if (option.maxHeight != null) {
                paramsBuilder.setMaxVideoSize(Int.MAX_VALUE, option.maxHeight)
            } else {
                paramsBuilder.clearVideoSizeConstraints()
            }

            player.trackSelectionParameters = paramsBuilder.build()
        }
    }

    fun setQualityPreference(preference: QualityPreference) {
        ensureMainThread {
            val player = exoPlayer ?: return@ensureMainThread
            val paramsBuilder = player.trackSelectionParameters.buildUpon()
            when (preference) {
                QualityPreference.LOW_BANDWIDTH -> {
                    paramsBuilder.setMaxVideoSize(Int.MAX_VALUE, 480)
                }
                QualityPreference.PREFER_BEST_QUALITY -> {
                    paramsBuilder.clearVideoSizeConstraints()
                }
                QualityPreference.PREFER_STABLE -> {
                    paramsBuilder.setMaxVideoSize(Int.MAX_VALUE, 1080)
                }
                QualityPreference.AUTO_QUALITY, QualityPreference.ADAPTIVE_BITRATE -> {
                    paramsBuilder.clearVideoSizeConstraints()
                }
            }
            player.trackSelectionParameters = paramsBuilder.build()
        }
    }

    // Audio & Subtitle Selection
    fun selectAudioLanguage(lang: String) {
        ensureMainThread {
            val player = exoPlayer ?: return@ensureMainThread
            player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                .setPreferredAudioLanguage(lang)
                .build()
        }
    }

    fun selectSubtitleLanguage(lang: String) {
        ensureMainThread {
            val player = exoPlayer ?: return@ensureMainThread
            player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                .setPreferredTextLanguage(lang)
                .build()
            _isSubtitleEnabled.value = true
        }
    }

    fun toggleSubtitle() {
        ensureMainThread {
            val player = exoPlayer ?: return@ensureMainThread
            val nextState = !_isSubtitleEnabled.value
            _isSubtitleEnabled.value = nextState
            if (!nextState) {
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setPreferredTextLanguage(null)
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    .build()
            } else {
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    .build()
            }
        }
    }

    fun setSubtitleEnabled(enabled: Boolean) {
        ensureMainThread {
            val player = exoPlayer ?: return@ensureMainThread
            _isSubtitleEnabled.value = enabled
            if (!enabled) {
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setPreferredTextLanguage(null)
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    .build()
            } else {
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    .build()
            }
        }
    }

    private fun detectMimeType(url: String): String? {
        val lower = url.lowercase()
        return when {
            lower.contains(".m3u8") || lower.contains("/hls/") -> MimeTypes.APPLICATION_M3U8
            lower.contains(".ts") || lower.endsWith(".ts") -> MimeTypes.VIDEO_MP2T
            lower.contains(".mpd") -> MimeTypes.APPLICATION_MPD
            lower.endsWith(".mp4") -> MimeTypes.VIDEO_MP4
            lower.endsWith(".mkv") -> MimeTypes.VIDEO_MATROSKA
            else -> null
        }
    }

    fun retryCurrentChannel() {
        retryCount = 0
        currentUrlIndex = 0
        _currentChannel.value?.let { playChannel(it) }
    }

    fun pause() {
        ensureMainThread { exoPlayer?.pause() }
    }

    fun play() {
        ensureMainThread { exoPlayer?.play() }
    }

    fun togglePlayPause() {
        ensureMainThread {
            exoPlayer?.let {
                if (it.isPlaying) it.pause() else it.play()
            }
        }
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        _aspectRatio.value = mode
    }

    fun getResizeMode(mode: AspectRatioMode): Int {
        return when (mode) {
            AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            AspectRatioMode.FOUR_THREE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            AspectRatioMode.SIXTEEN_NINE -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

    fun release() {
        ensureMainThread {
            isReleased = true
            try {
                exoPlayer?.removeListener(playerListener)
                exoPlayer?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing ExoPlayer", e)
            }
            exoPlayer = null
            _playbackState.value = PlaybackState.Idle
        }
    }

    private inline fun ensureMainThread(crossinline action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post { action() }
        }
    }
}

package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.config.DeveloperConfig
import com.example.data.db.ChannelEntity
import com.example.data.db.FilterRuleEntity
import com.example.data.db.ProviderEntity
import com.example.data.db.ZaynDatabase
import com.example.data.model.Category
import com.example.data.model.Channel
import com.example.data.model.FilterRule
import com.example.data.model.FilterType
import com.example.data.model.ImportSummary
import com.example.data.model.Movie
import com.example.data.model.Provider
import com.example.data.model.ProviderType
import com.example.data.model.Series
import com.example.data.model.XtreamConnectionResult
import com.example.data.playlist.M3uParseResult
import com.example.data.playlist.M3uParser
import com.example.data.stalker.StalkerClient
import com.example.data.xtream.XtreamClient
import com.example.storage.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

class IptvRepository(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val xtreamClient: XtreamClient = XtreamClient(),
    private val stalkerClient: StalkerClient = StalkerClient()
) {
    private val database = ZaynDatabase.getInstance(context)
    private val channelDao = database.channelDao()
    private val providerDao = database.providerDao()
    private val filterRuleDao = database.filterRuleDao()

    private val providersJsonFile = File(context.filesDir, "zayntv_providers.json")
    private val customNumbersJsonFile = File(context.filesDir, "zayntv_custom_numbers.json")

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val _providers = MutableStateFlow<List<Provider>>(emptyList())
    val providers = _providers.asStateFlow()

    private val _activeProvider = MutableStateFlow<Provider?>(null)
    val activeProvider = _activeProvider.asStateFlow()

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels = _channels.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies = _movies.asStateFlow()

    private val _series = MutableStateFlow<List<Series>>(emptyList())
    val series = _series.asStateFlow()

    private val _filterRules = MutableStateFlow<List<FilterRule>>(emptyList())
    val filterRules = _filterRules.asStateFlow()

    val favoritesFlow: Flow<Set<String>> = preferencesManager.favoriteIdsFlow
    val recentChannelsFlow: Flow<List<String>> = preferencesManager.recentChannelIdsFlow

    suspend fun initialize() = withContext(Dispatchers.IO) {
        migrateLegacyJsonToRoomIfNeeded()

        // 1. Observe and load providers
        val initialProviders = providerDao.getAllProviders().firstOrNull()?.map { it.toProvider() } ?: emptyList()
        _providers.value = initialProviders

        // 2. Observe filter rules
        val rules = filterRuleDao.getAllRules().firstOrNull()?.map { it.toFilterRule() } ?: emptyList()
        _filterRules.value = rules

        val settings = preferencesManager.userSettingsFlow.first()
        val targetProvider = initialProviders.find { it.id == settings.lastProviderId }
            ?: initialProviders.firstOrNull { it.isActive }
            ?: initialProviders.firstOrNull()

        if (targetProvider != null) {
            _activeProvider.value = targetProvider
            // Load channels from Room DB instantly! (Zero network delay on cold start)
            loadCachedChannelsForProvider(targetProvider.id)
        } else {
            loadInitialSampleData()
        }
    }

    private suspend fun migrateLegacyJsonToRoomIfNeeded() {
        try {
            val existingInRoom = providerDao.getAllProviders().firstOrNull()
            if (!existingInRoom.isNullOrEmpty()) return

            if (providersJsonFile.exists()) {
                val array = JSONArray(providersJsonFile.readText())
                val list = mutableListOf<ProviderEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ProviderEntity(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            type = obj.getString("type"),
                            url = obj.optString("url", ""),
                            username = obj.optString("username", ""),
                            password = obj.optString("password", ""),
                            epgUrl = obj.optString("epgUrl", ""),
                            isActive = obj.optBoolean("isActive", true)
                        )
                    )
                }
                list.forEach { providerDao.insertProvider(it) }
            }
        } catch (e: Exception) {
            Log.e("IptvRepository", "Migration error", e)
        }
    }

    private suspend fun loadInitialSampleData() {
        val sampleProvider = Provider(
            id = "demo_provider",
            name = "ZaynTV Test Streams",
            type = ProviderType.M3U_URL,
            url = "",
            isActive = true
        )
        providerDao.insertProvider(ProviderEntity.fromProvider(sampleProvider))
        _providers.value = listOf(sampleProvider)
        _activeProvider.value = sampleProvider

        val sampleChannels = listOf(
            Channel(
                id = "demo_ch_1",
                providerId = "demo_provider",
                channelNumber = 101,
                name = "Test Stream HD (Mux)",
                streamUrl = DeveloperConfig.TEST_STREAM_URL,
                group = "Entertainment",
                tvgId = "demo.mux",
                tvgName = "Test Stream HD"
            ),
            Channel(
                id = "demo_ch_2",
                providerId = "demo_provider",
                channelNumber = 102,
                name = "Sintel Cinema HLS",
                streamUrl = "https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
                group = "Movies",
                tvgId = "sintel.movie",
                tvgName = "Sintel Cinema"
            ),
            Channel(
                id = "demo_ch_3",
                providerId = "demo_provider",
                channelNumber = 103,
                name = "Tears of Steel Sports",
                streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                group = "Sports",
                tvgId = "tos.sports",
                tvgName = "Tears of Steel Sports"
            )
        )
        saveChannelsToRoomAndMemory("demo_provider", sampleChannels)
    }

    private suspend fun loadCachedChannelsForProvider(providerId: String) = withContext(Dispatchers.IO) {
        val cached = channelDao.getChannelsByProviderOnce(providerId).map { it.toChannel() }
        if (cached.isNotEmpty()) {
            setChannelsDirect(cached)
        } else {
            // If empty in DB, fetch from provider
            _activeProvider.value?.let { refreshProvider(it) }
        }
    }

    suspend fun setActiveProvider(provider: Provider) {
        _activeProvider.value = provider
        preferencesManager.saveLastChannel(provider.id, null, null)
        loadCachedChannelsForProvider(provider.id)
    }

    private suspend fun saveChannelsToRoomAndMemory(providerId: String, channelList: List<Channel>) = withContext(Dispatchers.IO) {
        channelDao.deleteChannelsByProvider(providerId)
        val entities = channelList.map { ChannelEntity.fromChannel(it) }
        channelDao.insertChannels(entities)
        setChannelsDirect(channelList)

        // Update provider channel count
        val prov = providerDao.getProviderById(providerId)
        if (prov != null) {
            providerDao.updateProvider(prov.copy(channelCount = channelList.size, lastUpdated = System.currentTimeMillis()))
            val all = providerDao.getAllProviders().firstOrNull()?.map { it.toProvider() } ?: emptyList()
            _providers.value = all
        }
    }

    suspend fun addM3uUrlProvider(name: String, url: String, epgUrl: String = ""): ImportSummary = withContext(Dispatchers.IO) {
        val provider = Provider(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "M3U Playlist" },
            type = ProviderType.M3U_URL,
            url = url.trim(),
            epgUrl = epgUrl.trim(),
            isActive = true
        )

        val request = Request.Builder()
            .url(provider.url)
            .header("User-Agent", "ZaynTV/1.0 (Android TV)")
            .build()

        val parseResult: M3uParseResult = try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                }
                val bodyStream = response.body?.byteStream()
                    ?: throw IllegalStateException("Empty playlist response body")
                M3uParser.parse(bodyStream, provider.id)
            }
        } catch (e: Exception) {
            throw e
        }

        providerDao.insertProvider(ProviderEntity.fromProvider(provider.copy(channelCount = parseResult.channels.size)))
        val all = providerDao.getAllProviders().firstOrNull()?.map { it.toProvider() } ?: emptyList()
        _providers.value = all

        setActiveProvider(provider)
        saveChannelsToRoomAndMemory(provider.id, parseResult.channels)

        ImportSummary(
            totalEntries = parseResult.totalEntries,
            playableCount = parseResult.channels.size,
            skippedCount = parseResult.skippedEntries,
            providerName = provider.name
        )
    }

    suspend fun importM3uStream(name: String, inputStream: InputStream): ImportSummary = withContext(Dispatchers.IO) {
        val provider = Provider(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Local Playlist" },
            type = ProviderType.M3U_LOCAL,
            isActive = true
        )
        val parseResult = M3uParser.parse(inputStream, provider.id)

        providerDao.insertProvider(ProviderEntity.fromProvider(provider.copy(channelCount = parseResult.channels.size)))
        val all = providerDao.getAllProviders().firstOrNull()?.map { it.toProvider() } ?: emptyList()
        _providers.value = all

        setActiveProvider(provider)
        saveChannelsToRoomAndMemory(provider.id, parseResult.channels)

        ImportSummary(
            totalEntries = parseResult.totalEntries,
            playableCount = parseResult.channels.size,
            skippedCount = parseResult.skippedEntries,
            providerName = provider.name
        )
    }

    suspend fun addXtreamProvider(name: String, serverUrl: String, user: String, pass: String): ImportSummary = withContext(Dispatchers.IO) {
        val provider = Provider(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Xtream Server" },
            type = ProviderType.XTREAM,
            url = xtreamClient.normalizeBaseUrl(serverUrl),
            username = user.trim(),
            password = pass.trim(),
            isActive = true
        )

        val liveCats = xtreamClient.getLiveCategories(provider.url, provider.username, provider.password)
        val catMap = liveCats.associate { it.id to it.name }
        val parsed = xtreamClient.getLiveChannels(provider.url, provider.username, provider.password, provider.id, catMap)

        providerDao.insertProvider(ProviderEntity.fromProvider(provider.copy(channelCount = parsed.size)))
        val all = providerDao.getAllProviders().firstOrNull()?.map { it.toProvider() } ?: emptyList()
        _providers.value = all

        setActiveProvider(provider)
        saveChannelsToRoomAndMemory(provider.id, parsed)

        _movies.value = xtreamClient.getVodStreams(provider.url, provider.username, provider.password, provider.id)
        _series.value = xtreamClient.getSeriesList(provider.url, provider.username, provider.password, provider.id)

        ImportSummary(
            totalEntries = parsed.size,
            playableCount = parsed.size,
            skippedCount = 0,
            providerName = provider.name
        )
    }

    suspend fun addStalkerProvider(name: String, portalUrl: String, macAddress: String): ImportSummary = withContext(Dispatchers.IO) {
        val provider = Provider(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "MAG Portal" },
            type = ProviderType.STALKER,
            url = portalUrl.trim(),
            macAddress = macAddress.trim(),
            isActive = true
        )

        val parsed = stalkerClient.getChannels(provider.url, provider.macAddress, provider.id)
        providerDao.insertProvider(ProviderEntity.fromProvider(provider.copy(channelCount = parsed.size)))
        val all = providerDao.getAllProviders().firstOrNull()?.map { it.toProvider() } ?: emptyList()
        _providers.value = all

        setActiveProvider(provider)
        saveChannelsToRoomAndMemory(provider.id, parsed)

        ImportSummary(
            totalEntries = parsed.size,
            playableCount = parsed.size,
            skippedCount = 0,
            providerName = provider.name
        )
    }

    suspend fun refreshProvider(provider: Provider) = withContext(Dispatchers.IO) {
        when (provider.type) {
            ProviderType.M3U_URL -> {
                if (provider.url.isNotBlank()) {
                    try {
                        val request = Request.Builder()
                            .url(provider.url)
                            .header("User-Agent", "ZaynTV/1.0 (Android TV)")
                            .build()
                        okHttpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val bodyStream = response.body?.byteStream()
                                if (bodyStream != null) {
                                    val parsed = M3uParser.parse(bodyStream, provider.id)
                                    saveChannelsToRoomAndMemory(provider.id, parsed.channels)
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
            ProviderType.XTREAM -> {
                try {
                    val liveCats = xtreamClient.getLiveCategories(provider.url, provider.username, provider.password)
                    val catMap = liveCats.associate { it.id to it.name }
                    val parsed = xtreamClient.getLiveChannels(provider.url, provider.username, provider.password, provider.id, catMap)
                    saveChannelsToRoomAndMemory(provider.id, parsed)
                    _movies.value = xtreamClient.getVodStreams(provider.url, provider.username, provider.password, provider.id)
                    _series.value = xtreamClient.getSeriesList(provider.url, provider.username, provider.password, provider.id)
                } catch (_: Exception) {}
            }
            ProviderType.STALKER -> {
                try {
                    val parsed = stalkerClient.getChannels(provider.url, provider.macAddress, provider.id)
                    saveChannelsToRoomAndMemory(provider.id, parsed)
                } catch (_: Exception) {}
            }
            ProviderType.M3U_LOCAL -> {}
        }
    }

    suspend fun refreshAll() = withContext(Dispatchers.IO) {
        _providers.value.forEach { provider ->
            refreshProvider(provider)
        }
    }

    suspend fun testProviderConnection(provider: Provider): XtreamConnectionResult = withContext(Dispatchers.IO) {
        if (provider.type == ProviderType.XTREAM) {
            xtreamClient.testConnection(provider.url, provider.username, provider.password)
        } else {
            try {
                val req = Request.Builder().url(provider.url).head().build()
                okHttpClient.newCall(req).execute().use { res ->
                    XtreamConnectionResult(
                        isSuccess = res.isSuccessful,
                        serverReachable = true,
                        authSuccess = true,
                        apiWorking = res.isSuccessful,
                        statusMessage = if (res.isSuccessful) "Server online and accessible" else "HTTP error: ${res.code}"
                    )
                }
            } catch (e: Exception) {
                XtreamConnectionResult(
                    isSuccess = false,
                    serverReachable = false,
                    authSuccess = false,
                    apiWorking = false,
                    statusMessage = "Cannot reach server: ${e.localizedMessage ?: "Network error"}"
                )
            }
        }
    }

    private fun setChannelsDirect(channels: List<Channel>) {
        val sorted = channels.sortedWith(
            compareBy<Channel> { it.sortOrder }
                .thenBy { it.displayChannelNumber }
        )
        _channels.value = sorted

        val groups = sorted.groupBy { it.displayGroup }
        val categoryList = mutableListOf(Category(id = "all", name = "All", channelCount = sorted.size, orderIndex = 0))
        var index = 1
        groups.forEach { (groupName, list) ->
            categoryList.add(Category(id = groupName.lowercase(), name = groupName, channelCount = list.size, orderIndex = index++))
        }
        _categories.value = categoryList
    }

    // --- Channel Management Operations ---
    suspend fun setCustomChannelNumber(channelId: String, newNumber: Int) = withContext(Dispatchers.IO) {
        channelDao.updateCustomNumber(channelId, newNumber)
        val updated = _channels.value.map {
            if (it.id == channelId) it.copy(customNumber = newNumber) else it
        }
        setChannelsDirect(updated)
    }

    suspend fun renameChannelLocally(channelId: String, newName: String) = withContext(Dispatchers.IO) {
        channelDao.updateCustomName(channelId, newName)
        val updated = _channels.value.map {
            if (it.id == channelId) it.copy(customName = newName) else it
        }
        setChannelsDirect(updated)
    }

    suspend fun setChannelHidden(channelId: String, hidden: Boolean) = withContext(Dispatchers.IO) {
        channelDao.updateHidden(channelId, hidden)
        val updated = _channels.value.map {
            if (it.id == channelId) it.copy(isCustomHidden = hidden) else it
        }
        setChannelsDirect(updated)
    }

    suspend fun setParentalLocked(channelId: String, locked: Boolean) = withContext(Dispatchers.IO) {
        channelDao.updateParentalLocked(channelId, locked)
        val updated = _channels.value.map {
            if (it.id == channelId) it.copy(isParentalLocked = locked) else it
        }
        setChannelsDirect(updated)
    }

    suspend fun moveChannelOrder(channelId: String, moveUp: Boolean) = withContext(Dispatchers.IO) {
        val list = _channels.value.toMutableList()
        val index = list.indexOfFirst { it.id == channelId }
        if (index == -1) return@withContext
        val targetIndex = if (moveUp) index - 1 else index + 1
        if (targetIndex in 0 until list.size) {
            val itemA = list[index]
            val itemB = list[targetIndex]
            val orderA = itemA.sortOrder
            val orderB = itemB.sortOrder
            val newOrderA = if (orderA == orderB) (if (moveUp) orderB - 1 else orderB + 1) else orderB
            channelDao.updateSortOrder(itemA.id, newOrderA)
            channelDao.updateSortOrder(itemB.id, orderA)
            list[index] = itemB.copy(sortOrder = orderA)
            list[targetIndex] = itemA.copy(sortOrder = newOrderA)
            setChannelsDirect(list)
        }
    }

    suspend fun restoreOriginalProviderList(providerId: String) = withContext(Dispatchers.IO) {
        channelDao.restoreOriginalProviderList(providerId)
        loadCachedChannelsForProvider(providerId)
    }

    suspend fun restoreAllHiddenChannels() = withContext(Dispatchers.IO) {
        channelDao.unhideAllChannels()
        _activeProvider.value?.let { loadCachedChannelsForProvider(it.id) }
    }

    suspend fun resetAllChannelCustomizations() = withContext(Dispatchers.IO) {
        channelDao.resetAllCustomizations()
        _activeProvider.value?.let { loadCachedChannelsForProvider(it.id) }
    }

    suspend fun refreshActiveEpg() = withContext(Dispatchers.IO) {
        // Clear EPG cache and reload for active provider
        File(context.filesDir, "epg_cache").deleteRecursively()
        _activeProvider.value?.let { refreshProvider(it) }
    }

    suspend fun deleteProvider(providerId: String) = withContext(Dispatchers.IO) {
        providerDao.deleteProviderById(providerId)
        channelDao.deleteChannelsByProvider(providerId)
        val all = providerDao.getAllProviders().firstOrNull()?.map { it.toProvider() } ?: emptyList()
        _providers.value = all

        if (_activeProvider.value?.id == providerId) {
            val next = all.firstOrNull()
            _activeProvider.value = next
            if (next != null) {
                loadCachedChannelsForProvider(next.id)
            } else {
                _channels.value = emptyList()
                _categories.value = emptyList()
            }
        }
    }

    // --- Filter Rules ---
    suspend fun addFilterRule(rule: FilterRule) = withContext(Dispatchers.IO) {
        filterRuleDao.insertRule(FilterRuleEntity.fromFilterRule(rule))
        val rules = filterRuleDao.getAllRules().firstOrNull()?.map { it.toFilterRule() } ?: emptyList()
        _filterRules.value = rules
    }

    suspend fun removeFilterRule(id: String) = withContext(Dispatchers.IO) {
        filterRuleDao.deleteRuleById(id)
        val rules = filterRuleDao.getAllRules().firstOrNull()?.map { it.toFilterRule() } ?: emptyList()
        _filterRules.value = rules
    }

    suspend fun resetAllFilters() = withContext(Dispatchers.IO) {
        filterRuleDao.clearRules()
        _filterRules.value = emptyList()
    }

    // --- Storage & Cache Clearing ---
    suspend fun clearCache(type: String) = withContext(Dispatchers.IO) {
        when (type) {
            "all" -> {
                context.cacheDir.deleteRecursively()
            }
            "images" -> {
                File(context.cacheDir, "image_cache").deleteRecursively()
            }
            "epg" -> {
                File(context.filesDir, "epg_cache").deleteRecursively()
            }
            "playlists" -> {
                // Clear temporary parsed files
                File(context.cacheDir, "playlists").deleteRecursively()
            }
        }
    }
}

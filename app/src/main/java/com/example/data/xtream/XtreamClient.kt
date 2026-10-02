package com.example.data.xtream

import com.example.data.model.Category
import com.example.data.model.Channel
import com.example.data.model.Movie
import com.example.data.model.Series
import com.example.data.model.XtreamConnectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class XtreamClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {
    fun normalizeBaseUrl(url: String): String {
        var base = url.trim()
        if (!base.startsWith("http://", ignoreCase = true) && !base.startsWith("https://", ignoreCase = true)) {
            base = "http://$base"
        }
        return base.trimEnd('/')
    }

    suspend fun testConnection(baseUrl: String, user: String, pass: String): XtreamConnectionResult = withContext(Dispatchers.IO) {
        val normUrl = normalizeBaseUrl(baseUrl)
        val authUrl = "$normUrl/player_api.php?username=${user.trim()}&password=${pass.trim()}"
        try {
            val request = Request.Builder()
                .url(authUrl)
                .header("User-Agent", "ZaynTV/1.0 (Android TV)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext XtreamConnectionResult(
                        isSuccess = false,
                        serverReachable = true,
                        authSuccess = false,
                        apiWorking = false,
                        statusMessage = "HTTP Error ${response.code}: ${response.message}"
                    )
                }

                val body = response.body?.string() ?: ""
                if (body.isBlank()) {
                    return@withContext XtreamConnectionResult(
                        isSuccess = false,
                        serverReachable = true,
                        authSuccess = false,
                        apiWorking = false,
                        statusMessage = "Server returned empty response."
                    )
                }

                val json = try { JSONObject(body) } catch (_: Exception) { null }
                if (json == null) {
                    return@withContext XtreamConnectionResult(
                        isSuccess = false,
                        serverReachable = true,
                        authSuccess = false,
                        apiWorking = false,
                        statusMessage = "Invalid JSON response from server."
                    )
                }

                val userInfo = json.optJSONObject("user_info")
                if (userInfo == null) {
                    return@withContext XtreamConnectionResult(
                        isSuccess = false,
                        serverReachable = true,
                        authSuccess = false,
                        apiWorking = true,
                        statusMessage = "Authentication failed: invalid username or password."
                    )
                }

                val authStatus = userInfo.optString("status", "")
                val authOk = authStatus.equals("Active", ignoreCase = true) || json.has("user_info")
                val expDateSec = userInfo.optLong("exp_date", 0L)
                val expFormatted = if (expDateSec > 0) {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(expDateSec * 1000L))
                } else {
                    "Unlimited"
                }

                // Quick test of live streams
                val liveStreams = getLiveChannels(baseUrl, user, pass, "test").size

                XtreamConnectionResult(
                    isSuccess = authOk,
                    serverReachable = true,
                    authSuccess = authOk,
                    apiWorking = true,
                    liveCount = liveStreams,
                    expirationDate = expFormatted,
                    statusMessage = if (authOk) "Connected successfully! Account active until $expFormatted." else "Account is inactive or expired ($authStatus)."
                )
            }
        } catch (e: Exception) {
            XtreamConnectionResult(
                isSuccess = false,
                serverReachable = false,
                authSuccess = false,
                apiWorking = false,
                statusMessage = "Cannot connect to server: ${e.localizedMessage ?: "Network error"}"
            )
        }
    }

    suspend fun getLiveCategories(baseUrl: String, user: String, pass: String): List<Category> = withContext(Dispatchers.IO) {
        val categories = mutableListOf<Category>()
        try {
            val normUrl = normalizeBaseUrl(baseUrl)
            val url = "$normUrl/player_api.php?username=${user.trim()}&password=${pass.trim()}&action=get_live_categories"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ZaynTV/1.0 (Android TV)")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    val array = JSONArray(body)
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        categories.add(
                            Category(
                                id = item.optString("category_id"),
                                name = item.optString("category_name"),
                                orderIndex = i
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}
        categories
    }

    suspend fun getLiveChannels(
        baseUrl: String,
        user: String,
        pass: String,
        providerId: String,
        categoryMap: Map<String, String> = emptyMap()
    ): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        try {
            val normUrl = normalizeBaseUrl(baseUrl)
            val url = "$normUrl/player_api.php?username=${user.trim()}&password=${pass.trim()}&action=get_live_streams"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ZaynTV/1.0 (Android TV)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    val array = JSONArray(body)
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val streamId = item.optInt("stream_id", i + 1)
                        val num = item.optInt("num", i + 1)
                        val catId = item.optString("category_id")
                        val groupName = categoryMap[catId] ?: "General"

                        // Build both .ts and .m3u8 stream formats to enable instant auto-fallback
                        val tsUrl = "$normUrl/live/${user.trim()}/${pass.trim()}/$streamId.ts"
                        val m3u8Url = "$normUrl/live/${user.trim()}/${pass.trim()}/$streamId.m3u8"
                        val directUrl = "$normUrl/${user.trim()}/${pass.trim()}/$streamId"

                        channels.add(
                            Channel(
                                id = "${providerId}_xtream_$streamId",
                                providerId = providerId,
                                channelNumber = if (num > 0) num else (i + 1),
                                name = item.optString("name", "Channel $streamId").trim(),
                                streamUrl = tsUrl,
                                alternativeUrls = listOf(m3u8Url, directUrl),
                                logoUrl = item.optString("stream_icon").takeIf { it.isNotBlank() },
                                group = groupName,
                                tvgId = item.optString("epg_channel_id"),
                                tvgName = item.optString("name"),
                                isPlayable = true,
                                sortOrder = i
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}
        channels
    }

    suspend fun getVodStreams(
        baseUrl: String,
        user: String,
        pass: String,
        providerId: String
    ): List<Movie> = withContext(Dispatchers.IO) {
        val movies = mutableListOf<Movie>()
        try {
            val normUrl = normalizeBaseUrl(baseUrl)
            val url = "$normUrl/player_api.php?username=${user.trim()}&password=${pass.trim()}&action=get_vod_streams"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ZaynTV/1.0 (Android TV)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    val array = JSONArray(body)
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val streamId = item.optInt("stream_id")
                        val ext = item.optString("container_extension", "mp4").ifEmpty { "mp4" }
                        val streamUrl = "$normUrl/movie/${user.trim()}/${pass.trim()}/$streamId.$ext"
                        movies.add(
                            Movie(
                                id = "${providerId}_vod_$streamId",
                                providerId = providerId,
                                title = item.optString("name", "Movie $streamId"),
                                streamUrl = streamUrl,
                                posterUrl = item.optString("stream_icon").takeIf { it.isNotBlank() },
                                rating = item.optString("rating", ""),
                                year = item.optString("year", "")
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}
        movies
    }

    suspend fun getSeriesList(
        baseUrl: String,
        user: String,
        pass: String,
        providerId: String
    ): List<Series> = withContext(Dispatchers.IO) {
        val seriesList = mutableListOf<Series>()
        try {
            val normUrl = normalizeBaseUrl(baseUrl)
            val url = "$normUrl/player_api.php?username=${user.trim()}&password=${pass.trim()}&action=get_series"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ZaynTV/1.0 (Android TV)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    val array = JSONArray(body)
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val seriesId = item.optInt("series_id")
                        seriesList.add(
                            Series(
                                id = "${providerId}_series_$seriesId",
                                providerId = providerId,
                                title = item.optString("name", "Series $seriesId"),
                                posterUrl = item.optString("cover").takeIf { it.isNotBlank() },
                                genre = item.optString("genre", ""),
                                rating = item.optString("rating", ""),
                                releaseDate = item.optString("releaseDate", "")
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}
        seriesList
    }

    fun getXmltvUrl(baseUrl: String, user: String, pass: String): String {
        val norm = normalizeBaseUrl(baseUrl)
        return "$norm/xmltv.php?username=${user.trim()}&password=${pass.trim()}"
    }

    fun getM3uUrl(baseUrl: String, user: String, pass: String, format: String = "ts"): String {
        val norm = normalizeBaseUrl(baseUrl)
        return "$norm/get.php?username=${user.trim()}&password=${pass.trim()}&type=m3u_plus&output=$format"
    }
}

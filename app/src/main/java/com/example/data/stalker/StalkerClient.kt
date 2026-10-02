package com.example.data.stalker

import com.example.data.model.Category
import com.example.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class StalkerClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()
) {
    private fun normalizePortalUrl(url: String): String {
        var base = url.trim()
        if (!base.startsWith("http://", ignoreCase = true) && !base.startsWith("https://", ignoreCase = true)) {
            base = "http://$base"
        }
        if (!base.endsWith("/server/load.php") && !base.endsWith("/c/")) {
            base = base.trimEnd('/') + "/server/load.php"
        }
        return base
    }

    suspend fun getChannels(
        portalUrl: String,
        macAddress: String,
        providerId: String
    ): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        try {
            val endpoint = normalizePortalUrl(portalUrl)
            val cleanMac = macAddress.trim()

            // 1. Handshake request
            val handshakeUrl = "$endpoint?type=stb&action=handshake&JsHttpRequest=1-xml"
            val handshakeReq = Request.Builder()
                .url(handshakeUrl)
                .header("User-Agent", "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3")
                .header("Cookie", "mac=$cleanMac")
                .build()

            var token = ""
            client.newCall(handshakeReq).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = try { JSONObject(body) } catch (_: Exception) { null }
                    val jsObj = json?.optJSONObject("js")
                    token = jsObj?.optString("token") ?: ""
                }
            }

            // 2. Fetch all channels
            val channelsUrl = "$endpoint?type=itv&action=get_all_channels&JsHttpRequest=1-xml"
            val reqBuilder = Request.Builder()
                .url(channelsUrl)
                .header("User-Agent", "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3")
                .header("Cookie", "mac=$cleanMac${if (token.isNotEmpty()) "; stb_token=$token" else ""}")

            if (token.isNotEmpty()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }

            client.newCall(reqBuilder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    val json = JSONObject(body)
                    val jsObj = json.optJSONObject("js")
                    val dataArray = jsObj?.optJSONArray("data") ?: JSONArray()

                    for (i in 0 until dataArray.length()) {
                        val chObj = dataArray.getJSONObject(i)
                        val name = chObj.optString("name", "Channel ${i + 1}")
                        val num = chObj.optInt("number", i + 1)
                        val cmd = chObj.optString("cmd", "")
                        val logo = chObj.optString("logo")
                        val tvgName = chObj.optString("tvg_name", name)

                        // Stalker cmd is typically "ffmpeg http://..." or direct url
                        val streamUrl = if (cmd.contains("http")) {
                            cmd.substring(cmd.indexOf("http")).trim()
                        } else {
                            cmd
                        }

                        if (streamUrl.startsWith("http://", ignoreCase = true) || streamUrl.startsWith("https://", ignoreCase = true)) {
                            channels.add(
                                Channel(
                                    id = "${providerId}_stalker_$num",
                                    providerId = providerId,
                                    channelNumber = num,
                                    name = name,
                                    streamUrl = streamUrl,
                                    logoUrl = if (logo.isNotBlank()) logo else null,
                                    group = "Live TV",
                                    tvgName = tvgName,
                                    isPlayable = true,
                                    sortOrder = i
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        channels
    }
}

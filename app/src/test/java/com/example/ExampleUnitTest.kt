package com.example

import com.example.data.model.Channel
import com.example.data.model.PlayerEngine
import com.example.data.model.ThemeType
import com.example.data.model.VideoQualityOption
import com.example.data.playlist.M3uParser
import com.example.data.xtream.XtreamClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testM3uParsingWithPipeHeadersAndMalformedLines() {
        val sampleM3u = """
            #EXTM3U
            # Some comment line
            
            #EXTINF:-1 tvg-id="espn.hd" tvg-name="ESPN HD" tvg-logo="https://example.com/logo.png" tvg-chno="101" group-title="Sports",ESPN HD
            https://stream.server.com/live/espn.m3u8|User-Agent=Mozilla/5.0&Referer=http://stream.server.com
            
            # Channel without quotes and with #EXTGRP
            #EXTINF:-1 tvg-name=Discovery group-title=Documentary,Discovery Channel
            #EXTGRP:Science & Nature
            http://stream.server.com/live/discovery.ts
            
            # Malformed entry (no URL, followed by another INF)
            #EXTINF:-1,Broken Channel 1
            
            #EXTINF:-1,Valid Channel 2
            http://stream.server.com/live/ch2.m3u8
            
            # Garbage lines that must not crash
            Random garbage line without hash
            INVALID_URL_SCHEME://xyz
            
            #EXTINF:-1,Channel With Spaces
            http://stream.server.com/live/stream with spaces.m3u8
        """.trimIndent()

        val inputStream = ByteArrayInputStream(sampleM3u.toByteArray(Charsets.UTF_8))
        val result = M3uParser.parse(inputStream, "test_provider")

        assertTrue(result.channels.isNotEmpty())
        assertEquals(4, result.channels.size)

        val espn = result.channels.first { it.name == "ESPN HD" }
        assertEquals(101, espn.channelNumber)
        assertEquals("https://stream.server.com/live/espn.m3u8", espn.streamUrl)
        assertEquals("Mozilla/5.0", espn.headers["User-Agent"])
        assertEquals("http://stream.server.com", espn.headers["Referer"])
        assertEquals("Sports", espn.group)

        val discovery = result.channels.first { it.name == "Discovery Channel" }
        assertEquals("http://stream.server.com/live/discovery.ts", discovery.streamUrl)
        assertEquals("Science & Nature", discovery.group)

        val chSpaces = result.channels.first { it.name == "Channel With Spaces" }
        assertEquals("http://stream.server.com/live/stream with spaces.m3u8", chSpaces.streamUrl)
    }

    @Test
    fun testUrlSanitizationAndValidation() {
        val (cleanUrl, headers) = M3uParser.sanitizeAndExtractUrl("http://cdn.com/live.m3u8|User-Agent=ZaynPlayer&Origin=http://cdn.com\r")
        assertEquals("http://cdn.com/live.m3u8", cleanUrl)
        assertEquals("ZaynPlayer", headers["User-Agent"])
        assertEquals("http://cdn.com", headers["Origin"])

        assertTrue(M3uParser.isValidStreamUrl("http://cdn.com/live.m3u8"))
        assertTrue(M3uParser.isValidStreamUrl("https://cdn.com/live.m3u8"))
        assertTrue(M3uParser.isValidStreamUrl("http://cdn.com/stream with spaces.ts"))
        assertTrue(!M3uParser.isValidStreamUrl("not_a_url"))
        assertTrue(!M3uParser.isValidStreamUrl(""))
    }

    @Test
    fun testXtreamUrlNormalization() {
        val client = XtreamClient()
        assertEquals("http://example.com:8080", client.normalizeBaseUrl("example.com:8080/"))
        assertEquals("https://secure.tv:8443", client.normalizeBaseUrl("https://secure.tv:8443/"))
        assertEquals("http://server.net", client.normalizeBaseUrl("server.net"))

        val xmltv = client.getXmltvUrl("server.net", "user", "pass")
        assertEquals("http://server.net/xmltv.php?username=user&password=pass", xmltv)
    }

    @Test
    fun testChannelModelCustomizations() {
        val ch = Channel(
            id = "c1",
            providerId = "p1",
            channelNumber = 5,
            name = "BBC One",
            streamUrl = "http://test.com/stream.m3u8",
            customName = "BBC One HD Custom",
            customNumber = 101,
            customGroup = "Favorites HD"
        )
        assertEquals("BBC One HD Custom", ch.displayName)
        assertEquals(101, ch.displayChannelNumber)
        assertEquals("101", ch.formattedNumber)
        assertEquals("Favorites HD", ch.displayGroup)
    }
}

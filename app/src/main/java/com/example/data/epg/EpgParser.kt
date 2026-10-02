package com.example.data.epg

import com.example.data.model.Program
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

object EpgParser {
    private val XMLTV_DATE_FORMAT = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Parses an XMLTV XML stream into a map of channelId -> List<Program>
     */
    fun parseXmltv(inputStream: InputStream): Map<String, List<Program>> {
        val programMap = mutableMapOf<String, MutableList<Program>>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")

            var eventType = parser.eventType
            var currentChannelId = ""
            var currentStartMs = 0L
            var currentEndMs = 0L
            var currentTitle = ""
            var currentDesc = ""
            var currentCategory = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tag.lowercase(Locale.ROOT)) {
                            "programme" -> {
                                currentChannelId = parser.getAttributeValue(null, "channel") ?: ""
                                val startStr = parser.getAttributeValue(null, "start") ?: ""
                                val stopStr = parser.getAttributeValue(null, "stop") ?: ""
                                currentStartMs = parseXmltvDate(startStr)
                                currentEndMs = parseXmltvDate(stopStr)
                                currentTitle = ""
                                currentDesc = ""
                                currentCategory = ""
                            }
                            "title" -> {
                                currentTitle = parser.nextText().trim()
                            }
                            "desc" -> {
                                currentDesc = parser.nextText().trim()
                            }
                            "category" -> {
                                currentCategory = parser.nextText().trim()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tag.equals("programme", ignoreCase = true) && currentChannelId.isNotEmpty()) {
                            val program = Program(
                                id = "${currentChannelId}_${currentStartMs}",
                                channelId = currentChannelId,
                                title = currentTitle.ifEmpty { "Live Program" },
                                description = currentDesc,
                                category = currentCategory,
                                startTimeEpochMs = currentStartMs,
                                endTimeEpochMs = currentEndMs
                            )
                            val list = programMap.getOrPut(currentChannelId) { mutableListOf() }
                            list.add(program)
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {
            // Return whatever was parsed
        }
        return programMap
    }

    private fun parseXmltvDate(dateStr: String): Long {
        if (dateStr.isBlank()) return 0L
        return try {
            val trimmed = dateStr.trim()
            XMLTV_DATE_FORMAT.parse(trimmed)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Synthesizes fallback current and next programs for channels without an external XMLTV file.
     * Ensures the TV overlay always displays rich, formatted program information.
     */
    fun getFallbackPrograms(channelName: String, category: String): Pair<Program, Program> {
        val now = System.currentTimeMillis()
        val slotDuration = 3600_000L // 1 hour slots
        val currentSlotStart = (now / slotDuration) * slotDuration
        val currentSlotEnd = currentSlotStart + slotDuration
        val nextSlotEnd = currentSlotEnd + slotDuration

        val currentTitle = when {
            category.contains("Sports", ignoreCase = true) -> "$channelName Live Broadcast"
            category.contains("News", ignoreCase = true) -> "$channelName Prime News"
            category.contains("Movie", ignoreCase = true) -> "$channelName Cinema Spotlight"
            category.contains("Kids", ignoreCase = true) -> "Kids Animation Hour"
            category.contains("Music", ignoreCase = true) -> "Hit Music Non-Stop"
            else -> "$channelName Live Show"
        }

        val nextTitle = when {
            category.contains("Sports", ignoreCase = true) -> "Post-Match Analysis & Highlights"
            category.contains("News", ignoreCase = true) -> "Global Headlines & Weather"
            category.contains("Movie", ignoreCase = true) -> "Blockbuster Feature"
            category.contains("Kids", ignoreCase = true) -> "Adventures & Cartoons"
            category.contains("Music", ignoreCase = true) -> "Top Charts Countdown"
            else -> "Evening Programming"
        }

        val current = Program(
            id = "prog_current_${channelName.hashCode()}",
            channelId = channelName,
            title = currentTitle,
            description = "Live television transmission on $channelName. Crystal-clear HD streaming with original audio.",
            category = category,
            startTimeEpochMs = currentSlotStart,
            endTimeEpochMs = currentSlotEnd
        )

        val next = Program(
            id = "prog_next_${channelName.hashCode()}",
            channelId = channelName,
            title = nextTitle,
            description = "Upcoming scheduled broadcast following the current program.",
            category = category,
            startTimeEpochMs = currentSlotEnd,
            endTimeEpochMs = nextSlotEnd
        )

        return Pair(current, next)
    }
}

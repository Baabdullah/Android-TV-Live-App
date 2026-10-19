package com.openlive.tv.utilities

import com.openlive.tv.models.*
import java.net.URI

object Urls {
    private val videoId = Regex("[A-Za-z0-9_-]{11}")
    fun youtubeId(value: String): String? = runCatching {
        val u = URI(value.trim())
        if (u.scheme !in listOf("https", "http")) return null
        val host = u.host?.lowercase()?.removePrefix("www.") ?: return null
        val parts = u.path.orEmpty().split('/').filter { it.isNotEmpty() }
        val id = when (host) {
            "youtu.be" -> parts.firstOrNull()
            "youtube.com", "m.youtube.com", "youtube-nocookie.com" -> when (parts.firstOrNull()) {
                "watch" -> u.rawQuery?.split('&')?.firstOrNull { it.startsWith("v=") }?.substringAfter('=')
                "live", "embed", "shorts" -> parts.getOrNull(1)
                else -> null
            }
            else -> null
        }
        id?.takeIf { videoId.matches(it) }
    }.getOrNull()
    fun classify(value: String): StreamType = when {
        youtubeId(value) != null -> StreamType.YOUTUBE
        runCatching { URI(value).path.lowercase().endsWith(".m3u8") }.getOrDefault(false) -> StreamType.HLS
        else -> StreamType.DIRECT
    }
    fun secureUrl(value: String): Boolean = runCatching {
        val u = URI(value)
        u.scheme == "https" && !u.host.isNullOrBlank() && u.rawUserInfo == null &&
            u.rawQuery.orEmpty().split('&').none {
                it.isNotBlank() && it.substringBefore('=').lowercase() !in setOf("v", "t", "start", "end", "feature", "si", "quality", "format", "bitrate", "language")
            }
    }.getOrDefault(false)
    fun validate(c: Channel): List<String> = buildList {
        if (c.id.isBlank()) add("Channel ID is required.")
        if (c.name.isBlank()) add("Enter a channel name.")
        if (c.name.length > 120) add("Channel name must be at most 120 characters.")
        if (!secureUrl(c.streamUrl)) add("Use a valid HTTPS URL without credentials or secret query parameters.")
        if (c.logoUrl.isNotBlank() && !secureUrl(c.logoUrl)) add("Logo must be an HTTPS URL without credentials.")
        if (c.streamType == StreamType.YOUTUBE && youtubeId(c.streamUrl) == null) add("Enter a YouTube watch, live, embed, shorts, or youtu.be video URL.")
        if (c.streamType != StreamType.YOUTUBE && youtubeId(c.streamUrl) != null) add("Select YouTube for this URL.")
        if (c.category.length > 80) add("Category must be at most 80 characters.")
    }
}

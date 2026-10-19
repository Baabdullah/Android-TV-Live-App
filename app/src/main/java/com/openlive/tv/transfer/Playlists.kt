package com.openlive.tv.transfer

import com.openlive.tv.models.*
import com.openlive.tv.utilities.Urls
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class ImportResult(val channels: List<Channel>, val warnings: List<String>)
object Playlists {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    fun encode(channels: List<Channel>): String = json.encodeToString(ChannelConfig(channels))
    fun decode(text: String): ImportResult {
        require(text.length <= 2_000_000) { "Playlist exceeds the 2 MB limit." }
        return if (text.trimStart().startsWith("{")) {
            val config = json.decodeFromString<ChannelConfig>(text)
            validate(config.channels)
        } else {
            require(text.lineSequence().any { it.trim().removePrefix("\uFEFF") == "#EXTM3U" }) { "Choose a JSON configuration or an #EXTM3U playlist." }
            m3u(text)
        }
    }
    private fun validate(channels: List<Channel>): ImportResult {
        require(channels.size <= 2000) { "Maximum 2000 channels per import." }
        val errors = mutableListOf<String>()
        val ids = mutableSetOf<String>()
        val valid = channels.filter { c ->
            val e = Urls.validate(c)
            when {
                e.isNotEmpty() -> { errors.add("${c.name.ifBlank { "Unnamed" }}: ${e.joinToString(" ")}"); false }
                !ids.add(c.id) -> { errors.add("Duplicate ID skipped: ${c.id}"); false }
                else -> true
            }
        }
        return ImportResult(valid, errors)
    }
    fun m3u(text: String): ImportResult {
        val channels = mutableListOf<Channel>()
        val warnings = mutableListOf<String>()
        var info: String? = null
        for ((index, raw) in text.lineSequence().withIndex()) {
            val line = raw.trim().removePrefix("\uFEFF")
            when {
                line.startsWith("#EXTINF:") -> {
                    if (info != null) warnings.add("Entry without a stream before line ${index + 1}.")
                    info = line
                }
                line.isEmpty() || line.startsWith('#') -> Unit
                else -> {
                    val meta = info
                    info = null
                    fun attr(key: String) = meta?.let { Regex("$key=\"([^\"]*)\"").find(it)?.groupValues?.get(1) }.orEmpty()
                    var quoted = false
                    val comma = meta?.indexOfFirst { char -> if (char == '"') quoted = !quoted; char == ',' && !quoted } ?: -1
                    val name = if (comma >= 0) meta!!.substring(comma + 1).trim() else ""
                    if (name.isEmpty()) { warnings.add("Unnamed entry at line ${index + 1} skipped."); continue }
                    channels.add(Channel(name = name, streamUrl = line, streamType = Urls.classify(line), logoUrl = attr("tvg-logo"), category = attr("group-title").ifBlank { "General" }, sortOrder = channels.size))
                    require(channels.size <= 2000) { "Maximum 2000 channels per import." }
                }
            }
        }
        if (info != null) warnings.add("Final entry has no stream URL.")
        val result = validate(channels)
        return result.copy(warnings = warnings + result.warnings)
    }
}

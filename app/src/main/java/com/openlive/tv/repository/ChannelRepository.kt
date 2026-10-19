package com.openlive.tv.repository

import com.openlive.tv.data.LocalStore
import com.openlive.tv.models.Channel
import com.openlive.tv.utilities.Urls

class ChannelRepository(private val store: LocalStore) {
    val channels = store.channels
    suspend fun save(channel: Channel) {
        require(Urls.validate(channel).isEmpty()) { Urls.validate(channel).joinToString(" ") }
        store.updateChannels { list ->
            if (list.any { it.id == channel.id }) list.map { if (it.id == channel.id) channel else it }
            else { require(list.size < 2000) { "Maximum 2000 channels." }; list + channel.copy(sortOrder = list.size) }
        }
    }
    suspend fun delete(id: String) = store.updateChannels { it.filterNot { c -> c.id == id } }
    suspend fun watched(id: String) = store.updateChannels { it.map { c -> if (c.id == id) c.copy(lastWatched = System.currentTimeMillis()) else c } }
    suspend fun merge(channels: List<Channel>) = store.updateChannels { existing ->
        val merged = existing.associateBy { it.id }.toMutableMap()
        channels.forEach { merged[it.id] = it }
        require(merged.size <= 2000) { "Import would exceed 2000 channels." }
        merged.values.mapIndexed { i, c -> c.copy(sortOrder = i) }
    }
    suspend fun move(id: String, delta: Int) = store.updateChannels { list ->
        val sorted = list.sortedBy { it.sortOrder }.toMutableList()
        val i = sorted.indexOfFirst { it.id == id }; val j = i + delta
        if (i >= 0 && j in sorted.indices) java.util.Collections.swap(sorted, i, j)
        sorted.mapIndexed { index, c -> c.copy(sortOrder = index) }
    }
}

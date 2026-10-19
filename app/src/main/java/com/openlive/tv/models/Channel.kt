package com.openlive.tv.models

import kotlinx.serialization.Serializable

@Serializable enum class StreamType { HLS, DIRECT, YOUTUBE }
@Serializable data class Channel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val streamUrl: String,
    val streamType: StreamType = StreamType.HLS,
    val logoUrl: String = "",
    val category: String = "General",
    val isFavorite: Boolean = false,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
    val lastWatched: Long = 0
)
@Serializable data class ChannelConfig(val channels: List<Channel> = emptyList())

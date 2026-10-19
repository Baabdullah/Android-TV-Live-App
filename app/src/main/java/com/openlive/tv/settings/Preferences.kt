package com.openlive.tv.settings

import kotlinx.serialization.Serializable

@Serializable enum class ThemeMode { SYSTEM, LIGHT, DARK }
@Serializable enum class Aspect { FIT, FILL, ZOOM }
@Serializable enum class Startup { ALL, FAVORITES, RECENT }
@Serializable data class Preferences(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val aspect: Aspect = Aspect.FIT,
    val autoReconnect: Boolean = true,
    val startup: Startup = Startup.ALL
)

package com.openlive.tv.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.openlive.tv.models.Channel
import com.openlive.tv.settings.Preferences
import com.openlive.tv.transfer.Playlists
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

private val Context.store by preferencesDataStore("openlive")
class LocalStore(context: Context) {
    private val store = context.applicationContext.store
    private val channelsKey = stringPreferencesKey("channels")
    private val settingsKey = stringPreferencesKey("settings")
    private val json = Json { ignoreUnknownKeys = true }
    val channels = store.data.map { p -> p[channelsKey]?.let { Playlists.decode(it).channels }.orEmpty() }
    val settings = store.data.map { p -> p[settingsKey]?.let { json.decodeFromString<Preferences>(it) } ?: Preferences() }
    suspend fun updateChannels(transform: (List<Channel>) -> List<Channel>) {
        store.edit { p -> p[channelsKey] = Playlists.encode(transform(p[channelsKey]?.let { Playlists.decode(it).channels }.orEmpty())) }
    }
    suspend fun settings(value: Preferences) { store.edit { it[settingsKey] = json.encodeToString(value) } }
    suspend fun reset() { store.edit { it.clear() } }
}

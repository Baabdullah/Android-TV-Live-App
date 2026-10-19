package com.openlive.tv.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openlive.tv.data.LocalStore
import com.openlive.tv.repository.ChannelRepository
import com.openlive.tv.settings.Preferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    val store = LocalStore(app)
    val repository = ChannelRepository(store)
    val loadError = MutableStateFlow<String?>(null)
    private fun <T> Flow<T>.reportLoadFailure(): Flow<T> = catch { e ->
        if (e is kotlinx.coroutines.CancellationException) throw e
        loadError.value = "Unable to read local configuration. Restart the app and check device storage. Existing data has not been overwritten."
    }
    val channels = repository.channels.reportLoadFailure().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val preferences = store.settings.reportLoadFailure().stateIn(viewModelScope, SharingStarted.Eagerly, Preferences())
    val ready = combine(store.channels, store.settings) { _, _ -> true }.reportLoadFailure().stateIn(viewModelScope, SharingStarted.Eagerly, false)
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()
    fun run(action: suspend () -> Unit) { viewModelScope.launch { try { action() } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; _messages.emit(e.message ?: "Operation failed. Please try again.") } } }
    suspend fun message(text: String) { _messages.emit(text) }
}

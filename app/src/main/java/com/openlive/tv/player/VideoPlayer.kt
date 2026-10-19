@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
package com.openlive.tv.player

import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.*
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.*
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.openlive.tv.models.*
import com.openlive.tv.settings.*
import com.openlive.tv.utilities.Urls
import kotlinx.coroutines.delay

@Composable fun VideoPlayer(channel: Channel, preferences: Preferences, previous: () -> Unit, next: () -> Unit, close: () -> Unit) {
    val context = LocalContext.current
    var aspect by remember(channel.id) { mutableStateOf(preferences.aspect) }
    Column(Modifier.fillMaxSize()) {
        Text(channel.name, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = close) { Text("Back") }
            TextButton(onClick = previous) { Text("Previous") }
            TextButton(onClick = next) { Text("Next") }
        }
        if (channel.streamType == StreamType.YOUTUBE) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("YouTube Live", style = MaterialTheme.typography.headlineMedium)
                Text("Play through the official YouTube app or browser. YouTube handles advertisements, availability, age verification, region and account restrictions. Return here to switch channels.")
                var error by remember { mutableStateOf<String?>(null) }
                Button(onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${Urls.youtubeId(channel.streamUrl)}"))) }
                        .onFailure { error = "No YouTube app or browser is installed on this device." }
                }) { Text("Open in YouTube / browser") }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        } else {
            var detectedType by remember(channel.streamUrl, channel.streamType) { mutableStateOf<StreamType?>(if (channel.streamType == StreamType.HLS) StreamType.HLS else null) }
            LaunchedEffect(channel.streamUrl, channel.streamType) {
                if (channel.streamType == StreamType.DIRECT) {
                    detectedType = try {
                        if (StreamProbe.test(channel.streamUrl).startsWith("HLS")) StreamType.HLS else StreamType.DIRECT
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        StreamType.DIRECT // Let Media3 report the precise playback failure.
                    }
                }
            }
            val resolved = detectedType
            if (resolved == null) Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
            else NativePlayer(channel.copy(streamType = resolved), preferences.autoReconnect, aspect, Modifier.weight(1f))
            Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Aspect.entries.forEach { value -> FilterChip(selected = aspect == value, onClick = { aspect = value }, label = { Text(value.name) }) }
                Text("Volume: device keys", Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable private fun NativePlayer(channel: Channel, autoReconnect: Boolean, aspect: Aspect, modifier: Modifier) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var error by remember(channel.streamUrl) { mutableStateOf<String?>(null) }
    var buffering by remember(channel.streamUrl) { mutableStateOf(true) }
    var isLive by remember(channel.streamUrl) { mutableStateOf(false) }
    var retryCount by remember(channel.streamUrl) { mutableIntStateOf(0) }
    val player = remember(channel.streamUrl, channel.streamType) {
        val http = DefaultHttpDataSource.Factory().setConnectTimeoutMs(10_000).setReadTimeoutMs(15_000).setAllowCrossProtocolRedirects(false)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(http).setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(2)))
            .setLoadControl(DefaultLoadControl.Builder().setBufferDurationsMs(15_000, 45_000, 1500, 3000).build())
            .build().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true)
                setHandleAudioBecomingNoisy(true)
                val item = MediaItem.Builder().setUri(channel.streamUrl)
                if (channel.streamType == StreamType.HLS) item.setMimeType(MimeTypes.APPLICATION_M3U8)
                setMediaItem(item.build()); prepare(); playWhenReady = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            }
    }
    fun reconnect() { error = null; buffering = true; player.prepare(); player.play() }
    DisposableEffect(player, lifecycle) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) { buffering = state == Player.STATE_BUFFERING; isLive = player.isCurrentMediaItemLive }
            override fun onPlayerError(e: PlaybackException) { buffering = false; error = "${e.errorCodeName}: Playback failed. Check connection, URL expiry, access restrictions and device codec support." }
        }
        var resume = true
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> { resume = player.playWhenReady; player.pause() }
                Lifecycle.Event.ON_RESUME -> if (resume) player.play()
                Lifecycle.Event.ON_STOP -> player.pause()
                else -> Unit
            }
        }
        player.addListener(listener); lifecycle.addObserver(observer)
        val activity = context as? android.app.Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { lifecycle.removeObserver(observer); player.removeListener(listener); player.release(); activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    LaunchedEffect(error, autoReconnect) {
        if (error != null && autoReconnect && retryCount < 2) {
            delay(2000L * (retryCount + 1)); retryCount++
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) reconnect()
        }
    }
    Box(modifier.fillMaxWidth()) {
        AndroidView(factory = { PlayerView(it).apply { this.player = player; useController = true; setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER); setShowNextButton(false); setShowPreviousButton(false) } }, update = {
            it.player = player
            it.resizeMode = when (aspect) { Aspect.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT; Aspect.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL; Aspect.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM }
        }, modifier = Modifier.fillMaxSize(), onRelease = { it.player = null })
        if (buffering) CircularProgressIndicator(Modifier.align(androidx.compose.ui.Alignment.Center))
        if (isLive) Surface(Modifier.align(androidx.compose.ui.Alignment.TopEnd).padding(12.dp), color = MaterialTheme.colorScheme.errorContainer) { Text("● LIVE", Modifier.padding(8.dp)) }
        error?.let { message ->
            Surface(Modifier.align(androidx.compose.ui.Alignment.Center).padding(24.dp)) {
                Column(Modifier.padding(20.dp)) { Text(message); Text("Automatic reconnects: $retryCount / 2"); Button(onClick = { retryCount = 0; reconnect() }) { Text("Retry / reconnect") } }
            }
        }
    }
}

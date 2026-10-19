package com.openlive.tv.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openlive.tv.models.*
import com.openlive.tv.navigation.Screen
import com.openlive.tv.player.*
import com.openlive.tv.settings.*
import com.openlive.tv.transfer.*
import com.openlive.tv.utilities.Urls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

@Composable fun OpenLiveApp(vm: MainViewModel) {
    val channels by vm.channels.collectAsStateWithLifecycle()
    val prefs by vm.preferences.collectAsStateWithLifecycle()
    val ready by vm.ready.collectAsStateWithLifecycle()
    val loadError by vm.loadError.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snack = remember { SnackbarHostState() }
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var playingId by rememberSaveable { mutableStateOf<String?>(null) }
    var editor by remember { mutableStateOf<Channel?>(null) }
    var deleting by remember { mutableStateOf<Channel?>(null) }
    var resetting by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<ImportResult?>(null) }
    val playing = channels.firstOrNull { it.id == playingId && it.isEnabled }
    LaunchedEffect(vm) { vm.messages.collect { snack.showSnackbar(it) } }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) vm.run {
            val text = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytesBounded(2_000_000); String(bytes, Charsets.UTF_8)
                } ?: error("Unable to open file.")
            }
            pending = withContext(Dispatchers.Default) { Playlists.decode(text) }
        }
    }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.run {
            withContext(Dispatchers.IO) { context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { it.write(Playlists.encode(channels)) } ?: error("Unable to save file.") }
            vm.message("Configuration exported. Keep channel URLs private when sharing.")
        }
    }
    fun play(c: Channel) { playingId = c.id; vm.run { vm.repository.watched(c.id) } }
    fun switch(delta: Int) {
        val enabled = channels.filter { it.isEnabled }.sortedBy { it.sortOrder }
        if (enabled.isNotEmpty()) {
            val index = enabled.indexOfFirst { it.id == playingId } + delta
            play(enabled[((index % enabled.size) + enabled.size) % enabled.size])
        }
    }
    BackHandler(playingId != null || screen != Screen.HOME) { if (playingId != null) playingId = null else screen = Screen.HOME }
    val dark = when (prefs.theme) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.DARK -> true; ThemeMode.LIGHT -> false }
    MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF64D8CB)) else lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF006A60))) {
        Scaffold(modifier = Modifier.safeDrawingPadding(), snackbarHost = { SnackbarHost(snack) }) { padding ->
            Surface(Modifier.fillMaxSize().padding(padding)) {
                if (loadError != null) Text(loadError.orEmpty(), Modifier.padding(24.dp), color = MaterialTheme.colorScheme.error)
                else if (!ready) Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
                else if (playing != null) {
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        var fullScreen by rememberSaveable { mutableStateOf(false) }
                        val wide = maxWidth >= 840.dp
                        Row(Modifier.fillMaxSize()) {
                            if (wide && !fullScreen) {
                                Surface(Modifier.width(240.dp).fillMaxHeight(), color = MaterialTheme.colorScheme.surfaceContainer) {
                                    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        item { Text("CHANNELS", style = MaterialTheme.typography.titleMedium) }
                                        items(channels.filter { it.isEnabled }.sortedBy { it.sortOrder }, key = { it.id }) { c ->
                                            OutlinedButton(onClick = { play(c) }, modifier = Modifier.fillMaxWidth()) { Text((if (c.id == playing.id) "▶ " else "") + c.name) }
                                        }
                                    }
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                if (wide) TextButton(onClick = { fullScreen = !fullScreen }) { Text(if (fullScreen) "Show channels" else "Full screen") }
                                VideoPlayer(playing, prefs, { switch(-1) }, { switch(1) }, { playingId = null })
                            }
                        }
                    }
                } else Column {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("OpenLive TV", style = MaterialTheme.typography.headlineMedium)
                        Text("Your channels. Your screen.", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (screen == Screen.HOME) {
                            Button(onClick = { editor = Channel(name = "", streamUrl = "") }) { Text("Add channel") }
                            OutlinedButton(onClick = { screen = Screen.SETTINGS }) { Text("Settings") }
                        } else TextButton(onClick = { screen = Screen.HOME }) { Text("Back") }
                        }
                    }
                    if (screen == Screen.HOME) Home(channels, prefs.startup, ::play, { editor = it }, { vm.run { vm.repository.save(it.copy(isFavorite = !it.isFavorite)) } }, { deleting = it }, { c, d -> vm.run { vm.repository.move(c.id, d) } }, { vm.run { pending = Playlists.decode(context.assets.open("sample-channels.json").bufferedReader().use { it.readText() }) } })
                    else Settings(prefs, { vm.run { vm.store.settings(it) } }, { runCatching { importer.launch(arrayOf("application/json", "audio/*", "application/vnd.apple.mpegurl", "application/x-mpegURL", "text/*", "application/octet-stream")) }.onFailure { vm.run { vm.message("No compatible document picker is installed. You can still add channels in the editor.") } } }, { runCatching { exporter.launch("openlive-channels.json") }.onFailure { vm.run { vm.message("No document provider is installed for saving files.") } } }, { resetting = true })
                }
            }
        }
        editor?.let { channel -> ChannelEditor(channel, { editor = null }, { value -> vm.run { vm.repository.save(value); editor = null; vm.message("Channel saved.") } }) }
        deleting?.let { c -> Confirm("Delete ${c.name}?", "This removes the channel from this device.", { deleting = null }) { vm.run { vm.repository.delete(c.id); deleting = null } } }
        if (resetting) Confirm("Reset configuration?", "All channels, history, favorites and settings will be removed.", { resetting = false }) { vm.run { vm.store.reset(); resetting = false; playingId = null } }
        pending?.let { result ->
            AlertDialog(onDismissRequest = { pending = null }, title = { Text("Import preview") }, text = {
                Column(Modifier.heightIn(max = 350.dp).verticalScroll(rememberScrollState())) {
                    Text("${result.channels.size} valid channels. Matching IDs will be replaced; other channels are retained.")
                    if (result.channels.isEmpty()) Text("No valid channels found. Nothing will be imported.")
                    result.warnings.take(30).forEach { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (result.warnings.size > 30) Text("${result.warnings.size - 30} more warnings.")
                }
            }, confirmButton = { TextButton(enabled = result.channels.isNotEmpty(), onClick = { vm.run { vm.repository.merge(result.channels); pending = null; vm.message("Imported ${result.channels.size} channels.") } }) { Text("Import") } }, dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancel") } })
        }
    }
}

private fun java.io.InputStream.readBytesBounded(max: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream(); val buffer = ByteArray(8192)
    while (true) { val n = read(buffer); if (n == -1) break; require(out.size() + n <= max) { "File exceeds 2 MB limit." }; out.write(buffer, 0, n) }
    return out.toByteArray()
}

@Composable private fun Home(channels: List<Channel>, startup: Startup, play: (Channel) -> Unit, edit: (Channel) -> Unit, favorite: (Channel) -> Unit, delete: (Channel) -> Unit, move: (Channel, Int) -> Unit, sample: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(startup) }
    var category by rememberSaveable { mutableStateOf("All") }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search channels") }, singleLine = true)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Startup.entries.forEach { v -> FilterChip(filter == v, { filter = v }, label = { Text(when(v) { Startup.ALL -> "All channels"; Startup.FAVORITES -> "Favorites"; Startup.RECENT -> "Recently watched" }) }) }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (listOf("All") + channels.map { it.category }.distinct().sorted()).forEach { c -> FilterChip(category == c, { category = c }, label = { Text(c) }) }
        }
        val filtered = channels.filter { c -> (c.name.contains(query, true) || c.category.contains(query, true)) && (category == "All" || c.category == category) && when(filter) { Startup.ALL -> true; Startup.FAVORITES -> c.isFavorite; Startup.RECENT -> c.lastWatched > 0 } }.let { if (filter == Startup.RECENT) it.sortedByDescending { c -> c.lastWatched } else it.sortedBy { c -> c.sortOrder } }
        if (channels.isEmpty()) {
            Column(Modifier.padding(vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("A world of live TV starts here", style = MaterialTheme.typography.headlineSmall)
                Text("Add your authorized HTTPS stream or YouTube link, or import a playlist in Settings.")
                OutlinedButton(onClick = sample) { Text("Import France 24 example") }
                Text("Example availability is unverified. Streams may have region or device restrictions.", style = MaterialTheme.typography.bodySmall)
            }
        } else if (filtered.isEmpty()) Text("No matching channels.", Modifier.padding(24.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(filtered, key = { it.id }) { c ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ChannelLogo(c.logoUrl, c.name)
                            Column(Modifier.weight(1f)) { Text(c.name, style = MaterialTheme.typography.titleLarge); Text("${c.category} · ${c.streamType.name} · ${if (c.isEnabled) "Ready to open" else "Disabled"}"); Text("Availability checked on playback", style = MaterialTheme.typography.bodySmall) }
                            TextButton(onClick = { favorite(c) }) { Text(if (c.isFavorite) "★ Favorite" else "☆ Favorite") }
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(enabled = c.isEnabled, onClick = { play(c) }) { Text("Watch") }
                            OutlinedButton(onClick = { edit(c) }) { Text("Edit") }
                            TextButton(onClick = { move(c, -1) }) { Text("Move up") }
                            TextButton(onClick = { move(c, 1) }) { Text("Move down") }
                            TextButton(onClick = { delete(c) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ChannelLogo(url: String, name: String) {
    val bitmap by produceState<android.graphics.Bitmap?>(null, url) {
        value = if (url.isBlank()) null else withContext(Dispatchers.IO) {
            runCatching {
                val c = URI(url).toURL().openConnection() as HttpURLConnection
                try {
                    c.connectTimeout = 5000; c.readTimeout = 5000; c.instanceFollowRedirects = false
                    require(c.responseCode == 200)
                    val bytes = c.inputStream.use { it.readBytesBounded(512_000) }
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                    require(options.outWidth in 1..4096 && options.outHeight in 1..4096)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = 4 })
                } finally { c.disconnect() }
            }.getOrNull()
        }
    }
    val image = bitmap
    if (image != null) Image(image.asImageBitmap(), "$name logo", Modifier.size(48.dp))
    else Surface(Modifier.size(48.dp), color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) { Box(contentAlignment = androidx.compose.ui.Alignment.Center) { Text("TV") } }
}

@Composable private fun Confirm(title: String, text: String, dismiss: () -> Unit, confirm: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text(title) }, text = { Text(text) }, confirmButton = { TextButton(onClick = confirm) { Text("Confirm") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}

package com.openlive.tv.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.openlive.tv.models.*
import com.openlive.tv.player.StreamProbe
import com.openlive.tv.utilities.Urls
import kotlinx.coroutines.launch

@Composable fun ChannelEditor(original: Channel, dismiss: () -> Unit, save: (Channel) -> Unit) {
    var name by remember { mutableStateOf(original.name) }
    var url by remember { mutableStateOf(original.streamUrl) }
    var logo by remember { mutableStateOf(original.logoUrl) }
    var category by remember { mutableStateOf(original.category) }
    var type by remember { mutableStateOf(original.streamType) }
    var favorite by remember { mutableStateOf(original.isFavorite) }
    var enabled by remember { mutableStateOf(original.isEnabled) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<String?>(null) }
    var testing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(16.dp).widthIn(max = 640.dp), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (original.name.isBlank()) "Add channel" else "Edit channel", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Channel name") }, singleLine = true)
                OutlinedTextField(url, { url = it; result = null; type = Urls.classify(it.trim()) }, Modifier.fillMaxWidth(), label = { Text("HTTPS stream or YouTube URL") }, singleLine = true)
                Text("Select HLS explicitly for extensionless HLS streams. Webpage URLs cannot be played as direct media.", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { StreamType.entries.forEach { t -> FilterChip(type == t, { type = t }, label = { Text(t.name) }) } }
                OutlinedTextField(logo, { logo = it }, Modifier.fillMaxWidth(), label = { Text("Logo HTTPS URL (optional)") }, singleLine = true)
                OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Category") }, singleLine = true)
                Row { Checkbox(favorite, { favorite = it }); Text("Favorite", Modifier.padding(top = 12.dp)); Spacer(Modifier.width(16.dp)); Checkbox(enabled, { enabled = it }); Text("Enabled", Modifier.padding(top = 12.dp)) }
                Text("Use streams you are authorized to access. Passwords, tokens and custom authentication headers are not stored by this version.", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(enabled = !testing, onClick = {
                    testing = true; result = null
                    scope.launch {
                        try { result = StreamProbe.test(url.trim()) }
                        catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; result = "URL test failed: ${e.message}" }
                        finally { testing = false }
                    }
                }) { Text(if (testing) "Testing…" else "Test URL") }
                if (testing) LinearProgressIndicator(Modifier.fillMaxWidth())
                result?.let { Text(it) }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = dismiss) { Text("Cancel") }
                    Button(onClick = {
                        val c = original.copy(name = name.trim(), streamUrl = url.trim(), logoUrl = logo.trim(), category = category.trim().ifBlank { "General" }, streamType = type, isFavorite = favorite, isEnabled = enabled)
                        val errors = Urls.validate(c)
                        if (errors.isEmpty()) save(c) else error = errors.joinToString("\n")
                    }) { Text("Save channel") }
                }
            }
        }
    }
}

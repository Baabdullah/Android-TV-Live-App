package com.openlive.tv.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openlive.tv.BuildConfig
import com.openlive.tv.settings.*

@Composable fun Settings(prefs: Preferences, update: (Preferences) -> Unit, import: () -> Unit, export: () -> Unit, reset: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Text("Appearance")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ThemeMode.entries.forEach { t -> FilterChip(prefs.theme == t, { update(prefs.copy(theme = t)) }, label = { Text(t.name) }) } }
        Text("Default aspect ratio")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Aspect.entries.forEach { a -> FilterChip(prefs.aspect == a, { update(prefs.copy(aspect = a)) }, label = { Text(a.name) }) } }
        Row { Switch(prefs.autoReconnect, { update(prefs.copy(autoReconnect = it)) }); Text("Auto reconnect (up to 2 attempts)", Modifier.padding(12.dp)) }
        Text("Startup channel view")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Startup.entries.forEach { s -> FilterChip(prefs.startup == s, { update(prefs.copy(startup = s)) }, label = { Text(s.name) }) } }
        HorizontalDivider()
        Button(onClick = import) { Text("Import JSON / M3U playlist") }
        OutlinedButton(onClick = export) { Text("Export channel configuration") }
        TextButton(onClick = reset) { Text("Reset all configuration") }
        HorizontalDivider()
        Text("About OpenLive TV ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleLarge)
        Text("Android 6.0+ (API 23). HLS and direct media supported by Media3 and your device codecs. YouTube opens in the official app or browser. No DRM or authentication bypasses.")
        Text("Phone, tablet and TV launchers supported. Use touch, keyboard, mouse or D-pad. Android TV boxes and Android virtual machines need working video decoders, graphics drivers and network access. Some older devices cannot play newer codecs or high resolutions.")
        Text("HTTPS only. HTTP streams and credential-bearing URLs are rejected. Streams can be offline, geoblocked, expired or require an authorized login; use the provider’s official app for protected content.")
        Text("No feeds are installed automatically. The France 24 sample is an example, not a guarantee of availability. LiveHDTV webpages are not direct media feeds; obtain authorized URLs from providers.")
    }
}

# OpenLive TV

**[Download testing APK](https://github.com/Baabdullah/Android-TV-Live-App/raw/refs/heads/main/downloads/OpenLiveTV-debug.apk)** · **[Full Android Studio build and testing guide](docs/ANDROID_STUDIO_GUIDE.md)**

[Complete project directory tree](docs/PROJECT_TREE.md) · [Actual build results](docs/BUILD_RESULTS.md)

A configurable Kotlin / Jetpack Compose live TV player for authorized HTTPS HLS and direct media, with official YouTube app/browser playback. No feeds are installed automatically. The France 24 example is supplied by the project requirements and is **unverified**.

## Compatibility

- Minimum Android 6.0 (API 23). Compile SDK 37.2; target API 37.
- Phones, tablets, Android TV and Android-based IPTV boxes. Touchscreen hardware is optional, and both standard and TV launchers are declared. Buttons support keyboard and D-pad focus.
- Pure managed application code: no app-bundled CPU-specific native decoder. ARM, ARM64, x86 and x86_64 devices can install it when Android/API requirements are met. Actual playback depends on the OS's MediaCodec implementation, hardware/graphics drivers, supported codecs, resolution and network access.
- Android running in a Windows VM can be used if it supplies a compatible Android runtime and working graphics/video decoding. This is an Android APK, not a Windows executable. Windows alone does not run it.
- No promise of universal device compatibility. API 23, IPTV hardware, remote navigation, rotation and VM playback require the checks in [the manual checklist](docs/MANUAL_TESTING.md). Android versions before API 23 are unsupported.
- YouTube requires an installed official YouTube app or browser. Devices without either display an explanatory error. Protected, age-restricted or region-restricted videos remain subject to YouTube's rules.
- Import/export uses Android's document picker. Some TV/box firmware omits a document provider; install/use a compatible provider or manage channels with the editor instead.

## Build and install

Open this directory in an Android Studio version supporting AGP 9.4.1. Use JDK 21, SDK Platform 37.2 and Android Build Tools 36.0.0. Accept the Android SDK license through SDK Manager. AGP manages Kotlin; the Compose and serialization compiler plugins are pinned to Kotlin 2.2.10. Gradle 9.8.2's wrapper includes its official distribution SHA-256.

With Android command-line tools on PATH:

```sh
sdkmanager 'platforms;android-37.2' 'build-tools;36.0.0' 'platform-tools'
# Accept licenses interactively if not already accepted:
sdkmanager --licenses
# ANDROID_HOME must point to the installed SDK, or set sdk.dir in local.properties.
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

On Windows, use `gradlew.bat` and `adb.exe`. Enable developer options / USB or network debugging on the device, then install over ADB. Alternatively copy the APK to the device and permit installation from your chosen file manager. A debug APK is for testing. Configure a private release signing key in your own secure build process for distribution; do not commit keys, passwords or signing credentials.

See [BUILD_RESULTS.md](docs/BUILD_RESULTS.md) for actual validation. The Gradle wrapper, source and resources are included; no IDE caches, cloud proxy settings, private keys or machine-specific SDK path belong in the source ZIP.

## Use

1. Select **Add channel**. Enter a name and an authorized HTTPS stream or copy a real YouTube video URL from YouTube. Do not enter a provider website as a direct media stream.
2. Select HLS for M3U8 streams, including extensionless URLs. YouTube links are detected from watch, live, embed, shorts and youtu.be formats. Other supported media can use DIRECT.
3. Use **Test URL** to check reachability/content hints. This does not prove codec compatibility or sustained playback. Save, then Watch for a real playback test.
4. Search, filter by category, choose Favorites or Recently watched, edit, disable or reorder channels. Disabled channels remain editable but are skipped in playback switching. Reordering operates on the global channel order.
5. Native playback has system volume key support, Media3 play/pause/seek controls, previous/next channel, buffering/error displays, a live marker when the manifest identifies live content, aspect options and reconnect. The player pauses in the background and is released when leaving the player or destroying the activity. Rotation creates a fresh live connection.
6. Wide screens (840 dp+) show the channel sidebar beside playback. **Full screen** hides the sidebar. Smaller screens dedicate the player view to the current channel. System bars remain accessible.
7. Settings controls the theme, aspect default, bounded auto reconnect and startup channel view. It also imports/exports configuration and resets all local data with confirmation.

### Import and export

Settings imports UTF-8 JSON or standard `#EXTM3U` files with `#EXTINF` names and optional `tvg-logo` / `group-title` attributes. A preview lists valid entries and malformed-entry warnings. Empty/invalid imports do not replace your library. Matching JSON IDs replace existing entries; other channels are retained. M3U imports create fresh IDs. Import is limited to 2 MB and 2,000 channels to bound memory use. Export writes JSON with all channels, favorites, ordering, enabled flags and watch timestamps; app theme/player settings are local to each installation.

The bundled [sample-channels.json](app/src/main/assets/sample-channels.json) can be imported from the empty state. It is not an availability guarantee and has not been verified as a live playback source.

### Security and source restrictions

HTTPS certificate checking stays enabled. HTTP is deliberately unsupported and blocked by the Android manifest. No cookies, authentication headers, passwords, DRM bypasses or credential storage are provided. URL user information is rejected. Query parameters are restricted to `v`, `t`, `start`, `end`, `feature`, `si`, `quality`, `format`, `bitrate`, and `language`; credential/signed URL parameters are not supported. Never put secrets into a channel name, category, logo or URL path. Local JSON exports contain your supplied URLs, so treat exports as private.

DNS, certificate, HTTP, timeout, expired URL, unsupported codec and region/access failures are reported by the URL test or Media3 error UI. Media3 performs at most two underlying load retries before surfacing an error; the app adds at most two automatic reconnects per selected source. Manual Retry starts a new attempt. Use the provider's official app for streams requiring accounts, cookies, DRM or unsupported codecs. No YouTube scraping, media-URL extraction, hidden ads, modified user agents or injected website scripts are used.

The requested `https://www.livehdtv.net/` returned an environment proxy CONNECT 403 during development. No channel URLs or rights could be verified. Its webpage links are not substituted for direct media URLs. Obtain authorized source URLs or a permitted playlist from the broadcaster/provider and import them.

## Architecture

- `models`: serializable channel/configuration types.
- `utilities`: secure URL validation and YouTube classification.
- `data`: transactional Preferences DataStore; no Room database is needed.
- `repository`: channel updates, merge, ordering and history.
- `settings`: persisted appearance/player/startup preferences.
- `transfer`: JSON serialization and M3U parsing with diagnostics.
- `navigation`: home/settings routing; selected channel owns the player route.
- `player`: Media3 lifecycle, network policy, bounded reconnect and URL probe.
- `ui`: adaptive Compose screens and AndroidView integration, ViewModel state and asynchronous operations.

Pure JVM tests cover classification, validation and import/export. Real streaming and device behavior are intentionally separate from parser/build validation.

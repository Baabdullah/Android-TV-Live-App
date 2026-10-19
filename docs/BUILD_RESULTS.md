# Actual validation — 2026-10-19

Command executed in `/workspace/Android-TV-Live-App`, using the prepared JDK/SDK:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
```

- **BUILD SUCCESSFUL**, exit status 0.
- `assembleDebug`: debug APK produced successfully.
- Unit tests: **19 executed, 19 passed, 0 failures, 0 errors, 0 skipped**. The tests cover URL classification/validation, malformed URLs, JSON import/export, duplicate IDs, empty configurations, malformed M3U entries and quoted metadata.
- `lintDebug`: **0 errors, 3 warnings**. Advisory warnings are the 320×180 TV vector banner size, a newer serialization library being available, and a suggestion to replace `Uri.parse` with a Kotlin extension. No checks or assertions were disabled to obtain these results.
- APK signature verification: passed, debug signing with v1 and v2 schemes. This is a test APK, not a privately signed distribution release.
- APK inspection: minimum API 23, target API 37; phone and Leanback launcher activities; ARM64, ARMv7, x86 and x86_64 native dependency libraries.
- Gradle 9.8.2 distribution SHA-256 and generated wrapper JAR SHA-256 match the official Gradle metadata. Android tools/platform archives and JDK archives were checked against their publishers' checksum metadata. TLS certificate verification remained enabled.

## Not executed / limitations

No Android device installation, UI interaction, actual process-restart persistence test, decoder/resource profiling or real streaming playback was performed. The cloud has no connected target or accelerated Android emulator. The manual checklist covers those required release checks; a successful build and parser tests do not establish playback compatibility on a phone, Mini IPTV box or Android-on-Windows virtual machine.

Both the supplied France 24 URL and `www.livehdtv.net` were blocked by the environment proxy with CONNECT HTTP 403. Channel availability/authorization was not verified and no LiveHDTV feeds were extracted or bundled. The environment draft adds `www.livehdtv.net`, `livehdtv.net` and `live.france24.com`; saving the draft did not activate network changes. Review/save settings and publish to reuse the prepared cloud environment. Publication and fresh-task restoration were not performed here.

The cloud draft also saves `install_script` and `start_skill` using the retained, verified toolchain and `/workspace/toolchains/openlive-env.sh`. Machine-specific settings, proxy trust files, SDK/JDK installations and the debug signing key are excluded from the portable project source archive. See README.md for standard Android Studio/SDK setup on your own computer.

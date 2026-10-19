# Build, modify and test OpenLive TV in Android Studio

## 1. Download the existing testing APK

Open the repository's `downloads/OpenLiveTV-debug.apk` file and select **Download raw file**, or use:

https://github.com/Baabdullah/Android-TV-Live-App/raw/refs/heads/main/downloads/OpenLiveTV-debug.apk

This is a debug APK for testing, version 1.0.0, package `com.openlive.tv`. GitHub stores the file; install and run it on an Android device or emulator. GitHub's website does not run Android APKs.

The app requires Android 6.0/API 23 or newer. The APK includes ARMv7, ARM64, x86 and x86_64 dependency libraries. Streaming still depends on the device's codecs, graphics and network. Actual phone, TV-box and VM playback has not been verified in this cloud session.

### Install the downloaded APK

1. Download the APK on the Android phone/tablet/box, or transfer it from your computer.
2. Open it with a file manager. If prompted, grant that browser/file manager **Install unknown apps** permission, then return and install. Settings names vary by Android version and box firmware. You can turn that permission off afterward.
3. Open **OpenLive TV** from the application launcher. On a TV box, use its Android TV launcher or ordinary Apps list.
4. Add an authorized HTTPS HLS URL or a real YouTube video link. You can also import a JSON/M3U file from Settings. The optional France 24 sample is unverified and may be unavailable or restricted.
5. For YouTube playback, the official YouTube application or a browser must be installed.

A Windows computer needs an Android emulator/Android VM or a connected Android device to run this APK. Double-clicking the APK in ordinary Windows does not run it.

## 2. Install development tools

Install Android Studio from https://developer.android.com/studio using the installer for your computer. Use an Android Studio release compatible with **Android Gradle Plugin 9.4.1**; an older Studio release can reject this project.

The project uses:

| Tool | Required configuration |
| --- | --- |
| Gradle | 9.8.2, through the included wrapper |
| Android Gradle Plugin | 9.4.1 |
| JDK used for the verified build | 21 |
| Compile SDK | Android SDK Platform 37.2 |
| Target API | 37 |
| Build Tools | 36.0.0 |
| Minimum device API | 23 (Android 6.0) |

Do not install or select Gradle manually: use the project's wrapper. JDK 21 runs the build; the project deliberately compiles its own Java/Kotlin code to JVM 17 bytecode.

## 3. Get and open the source

Choose either method:

- In Android Studio's welcome screen select **Get from VCS** / **Clone Repository** (the label varies by version), paste `https://github.com/Baabdullah/Android-TV-Live-App.git`, choose a local folder and clone it.
- On GitHub select **Code → Download ZIP**. Extract the archive, then select **Open** in Android Studio and choose the extracted directory containing `settings.gradle.kts`, `build.gradle.kts` and `gradlew`.

Open the project root, not only the `app` folder. Trust the project if Studio prompts you, then allow Gradle sync to finish. The initial sync downloads dependencies and can take several minutes.

## 4. Configure JDK and Android SDK

### Gradle JDK

On Windows/Linux open **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**. On macOS open **Android Studio → Settings** and follow the same Build Tools path.

Set **Gradle JDK** to a JDK 21 installation. If the embedded JDK is version 21, you can select it. Otherwise use Studio's **Download JDK** option, where available, or install a JDK 21 distribution and select its home directory. Apply the change.

For terminal builds, set `JAVA_HOME` to that same JDK directory and ensure its `bin` directory is on PATH. Do not point `JAVA_HOME` at a Java runtime lacking `javac`.

### SDK packages

Open **Tools → SDK Manager**:

1. Under **SDK Platforms**, enable **Show Package Details** if necessary and install **Android SDK Platform 37.2**.
2. Under **SDK Tools**, install **Android SDK Build-Tools 36.0.0**, **Android SDK Platform-Tools**, and current **Android SDK Command-line Tools**. Select **Show Package Details** to choose an exact version.
3. Install **Android Emulator** if you want an emulator.
4. Accept the required licenses and finish installation.
5. Run **File → Sync Project with Gradle Files**.

Studio normally creates `local.properties` containing your computer's `sdk.dir`. That file is ignored by Git; do not copy the cloud's `/workspace/...` SDK path to your computer. If version 37.2 is absent, update Studio and its command-line tools and refresh the SDK list. Do not silently lower SDK or dependency versions; that is a separate toolchain change requiring validation.

## 5. Run on your Android phone or IPTV box

1. On the device open **Settings → About** and tap **Build number** seven times to enable Developer options. Box firmware may use a different route or restrict this feature.
2. Open Developer options and enable **USB debugging**.
3. Connect a data-capable USB cable. Unlock the device and accept the computer's RSA debugging prompt. Windows may also need the manufacturer's USB driver.
4. In Studio select the **app** run configuration and choose the device from the device selector.
5. Click the green **Run ▶** button. Studio builds, installs and launches the app.

If an IPTV box cannot connect over USB, use its documented network/wireless debugging method. On Android versions supporting wireless debugging, Studio offers **Pair Devices Using Wi-Fi**. Pair only on a trusted local network. Older boxes use vendor-specific ADB support; do not assume a universal IP address, port or password.

You can check connectivity from a terminal with `adb devices`. A device marked `unauthorized` needs you to accept its debugging prompt. A device missing from the list needs a cable, driver, connection or debugging-setting fix.

## 6. Run on an Android emulator

1. Open **Tools → Device Manager** and choose **Create device**.
2. Choose a phone/tablet profile, or a TV profile for a separate remote-control check.
3. Download a compatible system image. Choose API 23 or newer; test both your minimum supported OS and a recent OS where images/tools are available. Use an image architecture supported by your computer.
4. Start the virtual device, select it in Studio and click **Run ▶**.
5. Enable the host's supported virtualization/hypervisor features if the emulator reports that hardware acceleration is unavailable.

Running an Android guest inside another Windows VM may require nested virtualization or a supported software renderer and can be slow. Emulator success does not prove that every IPTV box has the same codecs. Also test your real box with its remote control.

## 7. Build an APK you can copy to other devices

Select the **debug** variant under **Build Variants**. Use **Build → Build Bundle(s) / APK(s) → Build APK(s)**; some Studio releases call this **Generate APKs** or **Generate App Bundles or APKs → Generate APKs**. Choose APK rather than an Android App Bundle (`.aab`) for direct installation.

The output is:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Use the build notification's **Locate** action to find it. For a reproducible terminal build, open Studio's Terminal in the project root:

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
```

macOS/Linux:

```sh
./gradlew :app:assembleDebug
```

If macOS/Linux reports permission denied, run `chmod +x gradlew` once. Successful output ends with `BUILD SUCCESSFUL`.

Install through Studio, your device's file manager, or ADB:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 8. Modify the application and rebuild

Use these starting points:

| Change | File/package |
| --- | --- |
| Home, search, channel list, adaptive sidebar | `app/src/main/java/com/openlive/tv/ui/OpenLiveApp.kt` |
| Channel editor and form | `ui/ChannelEditor.kt` |
| Settings screen | `ui/Settings.kt` |
| Player, buffering, reconnect and lifecycle | `player/VideoPlayer.kt` |
| URL reachability test | `player/StreamProbe.kt` |
| Channel fields | `models/Channel.kt` |
| URL validation / YouTube URL classification | `utilities/Urls.kt` |
| JSON and M3U import/export | `transfer/Playlists.kt` |
| Persistence | `data/LocalStore.kt` |
| Sample playlist | `app/src/main/assets/sample-channels.json` |
| Version and dependencies | `app/build.gradle.kts` |

Make a small change, save, and click **Run ▶** again. Studio normally updates the existing installation, preserving local channel configuration when the package and signing key match. A normal incremental build is sufficient; Clean Project is not required for every edit.

Adding or editing channels for normal viewing does not require source changes: use the editor or import feature inside the app. Changing the sample asset does not automatically replace channels already stored on a device.

If you edit Gradle dependencies or build configuration, sync Gradle before rebuilding. If you change the stored data structure, preserve backward-compatible defaults or implement/test a data migration rather than deleting user configuration.

### Important: debug signing keys

The downloaded APK was signed with the cloud build's debug key. Android Studio on your computer normally generates a different debug key. Android will reject installing your locally built APK over the downloaded APK with `INSTALL_FAILED_UPDATE_INCOMPATIBLE` or a signing-certificate mismatch.

Export your channels first, uninstall the downloaded app, install your locally built APK, then import the exported playlist. Uninstalling removes local app data. Later builds on the same computer generally reuse its debug key and can update each other. Do not share or commit private signing keys.

For Play Store or public release distribution, use **Build → Generate Signed App Bundle / APK** and manage a private release key securely. Keep that same key for future updates. Do not use the shared testing APK's debug signature as your production signing identity.

## 9. Run automated checks after changes

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

macOS/Linux:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Reports are written to:

```text
app/build/reports/tests/testDebugUnitTest/index.html
app/build/reports/lint-results-debug.html
```

The original tested source passed 19 unit tests and lint with zero errors and three advisory warnings. That result applies to that source; rerun checks after your changes. Unit tests do not play live streams or prove hardware decoder support.

In Studio you can also right-click `ConfigurationTest` in `app/src/test/java/com/openlive/tv` and select **Run**.

## 10. Check actual playback and troubleshoot

Follow [MANUAL_TESTING.md](MANUAL_TESTING.md). At minimum test an authorized HLS stream, a supported YouTube URL, previous/next channels, remote focus, volume, rotation, background/resume and configuration after force-stop/restart. The app only accepts HTTPS URLs with supported query parameters and does not implement protected-provider authentication.

- **Build fails:** open the first meaningful Gradle error in Build Output. Check JDK 21, SDK 37.2, licenses and internet/proxy access. Avoid randomly changing pinned versions or disabling TLS verification.
- **App crashes:** open **View → Tool Windows → Logcat**, select the device and filter by `com.openlive.tv`. Capture the exception and surrounding lines. Redact any private stream URLs before sharing logs.
- **Network or playback fails:** check the URL in the channel editor. A webpage is not a direct HLS URL. Try explicit HLS for an extensionless playlist. Sources may be offline, expired, region-limited or unsupported by the device's decoder. The app does not bypass those restrictions.
- **YouTube does not open:** install the official YouTube app or a suitable browser. YouTube controls availability and restrictions.
- **Import/export unavailable on the box:** its firmware may not include an Android document provider. Use the editor or install a compatible provider.
- **Installation fails because Android is too old:** this app requires API 23 or later.
- **Installation fails because of signing:** follow the debug-key instructions above, exporting your channels before uninstalling.
- **Want to share your modified APK on GitHub:** build the APK, verify it on your device, then upload it as a GitHub Release asset, or replace the file in `downloads` and update its checksum. Keep signing keys and `local.properties` out of Git.

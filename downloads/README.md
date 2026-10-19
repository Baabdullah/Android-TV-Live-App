# Testing APK

[Download OpenLiveTV-debug.apk](https://github.com/Baabdullah/Android-TV-Live-App/raw/refs/heads/main/downloads/OpenLiveTV-debug.apk)

Android 6.0/API 23 or newer is required. This is a debug build for testing, not a production-signed release. GitHub hosts the download; run it on Android, not inside the GitHub website.

[Full Android Studio build, modification and testing guide](../docs/ANDROID_STUDIO_GUIDE.md)

[Original build results](../docs/BUILD_RESULTS.md) · [Manual device checklist](../docs/MANUAL_TESTING.md)

SHA-256: `0265bbaf8b69b2d2ce075f207d44915f613a001ea279b4fb8a59846076b0d19e`

If you build the project on your own computer, its debug signing key will usually differ. Export your channels and uninstall this testing build before installing your own build if Android reports a signature mismatch. Uninstallation removes local data.

Live playback on physical devices and Android VMs remains unverified. Use authorized sources; the included optional sample is not a guarantee of availability.

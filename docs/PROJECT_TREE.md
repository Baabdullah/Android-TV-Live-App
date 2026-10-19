# Complete repository directory tree

```text
Android-TV-Live-App/
├── .gitignore
├── README.md
├── app
│   ├── build.gradle.kts
│   └── src
│       ├── main
│       │   ├── AndroidManifest.xml
│       │   ├── assets
│       │   │   └── sample-channels.json
│       │   ├── java
│       │   │   └── com
│       │   │       └── openlive
│       │   │           └── tv
│       │   │               ├── MainActivity.kt
│       │   │               ├── data
│       │   │               │   └── LocalStore.kt
│       │   │               ├── models
│       │   │               │   └── Channel.kt
│       │   │               ├── navigation
│       │   │               │   └── Screen.kt
│       │   │               ├── player
│       │   │               │   ├── StreamProbe.kt
│       │   │               │   └── VideoPlayer.kt
│       │   │               ├── repository
│       │   │               │   └── ChannelRepository.kt
│       │   │               ├── settings
│       │   │               │   └── Preferences.kt
│       │   │               ├── transfer
│       │   │               │   └── Playlists.kt
│       │   │               ├── ui
│       │   │               │   ├── ChannelEditor.kt
│       │   │               │   ├── MainViewModel.kt
│       │   │               │   ├── OpenLiveApp.kt
│       │   │               │   └── Settings.kt
│       │   │               └── utilities
│       │   │                   └── Urls.kt
│       │   └── res
│       │       ├── drawable
│       │       │   ├── ic_tv.xml
│       │       │   └── tv_banner.xml
│       │       ├── values
│       │       │   └── styles.xml
│       │       └── xml
│       │           ├── backup_rules.xml
│       │           └── data_extraction_rules.xml
│       └── test
│           └── java
│               └── com
│                   └── openlive
│                       └── tv
│                           └── ConfigurationTest.kt
├── build.gradle.kts
├── docs
│   ├── ANDROID_STUDIO_GUIDE.md
│   ├── BUILD_RESULTS.md
│   ├── MANUAL_TESTING.md
│   └── PROJECT_TREE.md
├── downloads
│   ├── OpenLiveTV-debug.apk
│   ├── README.md
│   └── SHA256SUMS
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

The downloads folder contains the testing APK. Local build outputs, local.properties, IDE caches and signing keys are excluded from Git.

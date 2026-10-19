# Complete project directory tree

```text
OpenLiveTV/
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
│   ├── BUILD_RESULTS.md
│   ├── MANUAL_TESTING.md
│   └── PROJECT_TREE.md
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

Every listed file is included in the source ZIP. Build outputs, local.properties, IDE files, credentials and toolchain caches are excluded.

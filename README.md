# CybNate 🚀

> An upgraded, next-generation Android Terminal & AI Agent Platform. Built with modern Native Android (Kotlin + Jetpack Compose), Material 3, and Termux PTY execution.

---

## 🌟 Why is it Better than AntiGem v2.0.3?

| Feature | AntiGem v2.0.3 (Legacy) | CybNate (This Replica) |
|---|---|---|
| **Android Target SDK** | Target SDK 28 (Android 9 Pie) | **Target SDK 35 (Android 15 Ready)** |
| **UI Framework** | Legacy Views + older UI components | **100% Jetpack Compose Material 3** |
| **Terminal Architecture** | Single-window terminal view | **Multi-Tab Terminal with Virtual Extra-Keys Row** |
| **Agent / Shell Integration**| Basic script execution | **Direct Coroutine-based Streaming & MCP Bridge** |
| **Mini-Apps Hub** | Custom Activity launcher | **Lightweight Sandbox WebView with Two-way RPC** |
| **Background Persistence** | Basic Service | **Modern Foreground Service (`specialUse` Android 15 compliant)** |
| **Chat Persistence** | Simple file/shared preferences | **Robust Room Database with reactive Flow support** |

---

## 🏗️ Project Architecture

```
cybnate/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/ai/cybnate/
│           ├── CybNateApp.kt                     # Application entrypoint & notification channels
│           ├── MainActivity.kt                   # Modern Jetpack Compose Scaffold & Navigation
│           ├── data/
│           │   ├── agent/
│           │   │   ├── AiAgentService.kt         # Streaming agent responses & shell execution
│           │   │   └── McpBridge.kt              # Model Context Protocol JSON-RPC 2.0 handler
│           │   ├── db/
│           │   │   ├── AppDatabase.kt            # Room Database
│           │   │   ├── ChatDao.kt                # Reactive message persistence
│           │   │   └── ChatEntity.kt
│           │   ├── model/
│           │   │   ├── ChatMessage.kt            # Chat models & artifacts
│           │   │   ├── MiniApp.kt                # Mini-App definition
│           │   │   └── TerminalSession.kt        # Multi-tab terminal model
│           │   └── terminal/
│           │       ├── ShellExecutor.kt          # Asynchronous shell execution & streaming
│           │       └── TerminalSessionManager.kt # Multi-tab session coordinator
│           ├── service/
│           │   └── TerminalForegroundService.kt  # Android 15 compliant foreground execution
│           └── ui/
│               ├── chat/                         # Agent chat, code blocks, suggestions
│               ├── terminal/                     # Multi-tab terminal & virtual keys
│               ├── miniapps/                     # Mini-App store & sandbox viewer
│               └── theme/                        # Material 3 colors, typography, dynamic theme
└── gradle/
    └── libs.versions.toml                        # Modern Gradle version catalog
```

---

## 🛠️ How to Build & Run

### In Android Studio / CLI:
1. Open this directory in **Android Studio**.
2. Run `./gradlew assembleDebug` to produce the APK.
3. Install on your device:
   ```sh
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Running on Device / Termux:
You can test the shell bridge and agent logic directly from the Termux environment:
```sh
craft-anything-cli doctor
```

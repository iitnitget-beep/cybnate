# CybNate 🚀

> An upgraded, next-generation Android Terminal & AI Agent Workstation. Built with modern Native Android (Kotlin + Jetpack Compose), Material 3 Cyberpunk aesthetics, Termux PTY execution (`libtermux.so`), and integrated Google Antigravity & Claude Code daemon (`agy_ide_bridge`).

---

## 🌟 Why is CybNate Superior to AntiGem v2.0.3?

| Feature | AntiGem v2.0.3 (Legacy) | CybNate (Next-Gen) |
|---|---|---|
| **Android Target SDK** | Target SDK 28 (Android 9 Pie - Legacy) | **Target SDK 35 (Android 15 Edge-to-Edge Ready)** |
| **UI & Theme** | Generic Material views & mixed UI | **OLED Dark Glassmorphism, Neon Cyan & Matrix Green Accents** |
| **Linux Environment** | Manual / old bootstrap archive | **Automated RootFS (~300MB) Installer with Dual Mirror Fallback** |
| **Agent Bridge** | Hidden Go binary daemon | **Fully Managed `agy_ide_bridge` Daemon Supervisor (Port 1234/1235)** |
| **Supported Coding Agents** | Fragmented | **Google Antigravity (`agy`), Claude Code, Codex & OpenCode** |
| **Terminal Architecture** | Single-window Termux fork | **Multi-Tab Terminal, PTY Subprocesses (`libtermux.so`), Virtual Keys Row** |
| **Mini-Apps Hub** | Heavy Android Activities | **Lightweight Sandbox WebView with Two-way RPC (`CybNateBridge`)** |
| **Background Persistence** | Basic Service | **Modern Foreground Service (`specialUse` Android 15 compliant)** |

---

## 🏗️ Project Architecture

```
cybnate/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/
│       │   ├── bin/
│       │   │   ├── agy_ide_bridge.xz             # Core 14.4MB Go IDE Server & AGY Hub Daemon
│       │   │   └── am.apk, backup_*, git_clone   # POSIX Helper Binaries
│       │   ├── browser-extensions/               # Google Flow agent extension
│       │   └── browser-runtime/                  # Browser API & MCP agent runtime
│       ├── jniLibs/
│       │   ├── arm64-v8a/libtermux.so            # True POSIX Pseudo-Terminal (PTY) JNI
│       │   └── armeabi-v7a/libtermux.so
│       └── java/
│           ├── ai/cybnate/
│           │   ├── CybNateApp.kt                 # Application entrypoint & notification channels
│           │   ├── MainActivity.kt               # Dynamic Scaffold & Lifecycle Coordinator
│           │   ├── data/
│           │   │   ├── agent/
│           │   │   │   ├── AgyBridgeManager.kt   # IDE Bridge Daemon Supervisor (Port 1234/1235)
│           │   │   │   ├── AiAgentService.kt     # Streaming agent responses & shell execution
│           │   │   │   └── McpBridge.kt          # Model Context Protocol JSON-RPC 2.0 handler
│           │   │   ├── bootstrap/
│           │   │   │   └── BootstrapManager.kt   # 300MB RootFS Downloader, Unpacker & Setup
│           │   │   ├── db/
│           │   │   │   ├── AppDatabase.kt        # Room Database
│           │   │   │   ├── ChatDao.kt            # Reactive message persistence
│           │   │   │   └── ChatEntity.kt
│           │   │   ├── model/
│           │   │   │   ├── ChatMessage.kt        # Chat models & artifacts
│           │   │   │   ├── MiniApp.kt            # Mini-App definition
│           │   │   │   └── TerminalSession.kt    # Multi-tab terminal model
│           │   │   └── terminal/
│           │   │       ├── PtyTerminalSession.kt # Native PTY session controller
│           │   │       ├── ShellExecutor.kt      # Asynchronous shell execution & streaming
│           │   │       └── TerminalSessionManager.kt # Multi-tab session coordinator
│           │   ├── service/
│           │   │   └── TerminalForegroundService.kt  # Android 15 compliant foreground execution
│           │   └── ui/
│           │       ├── bootstrap/                # Animated cybernetic setup & download screen
│           │       ├── chat/                     # Agent chat, code blocks, suggestions
│           │       ├── terminal/                 # Multi-tab terminal & virtual keys
│           │       ├── miniapps/                 # Mini-App store & sandbox viewer
│           │       └── theme/                    # OLED pitch black, neon cyan & matrix green theme
│           └── com/termux/terminal/
│               └── JNI.java                      # PTY Subprocess JNI interface for libtermux.so
└── gradle/
    └── libs.versions.toml                        # Modern Gradle version catalog
```

---

## 📥 Automated Cloud CI/CD
Whenever code is pushed to this repository, GitHub Actions automatically compiles and publishes `CybNate-v1.0.0-debug.apk` directly under GitHub Releases and Artifacts.

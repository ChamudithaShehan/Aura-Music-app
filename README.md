# Aura Music 🎵

Aura Music is a high-performance, feature-rich Android music player built with modern Android development standards. It combines a sleek, minimalist UI with a professional-grade audio processing engine to deliver an immersive listening experience.

## ✨ Features

### 🎧 Professional Audio DSP
- **10-Band Equalizer**: Precise control across the frequency spectrum (31Hz to 16kHz).
- **Audio Effects**: Built-in Bass Boost, 3D Virtualizer, and Loudness Enhancer using native Android Audio Effects API.
- **Preset Library**: 12 professional built-in presets (Rock, Pop, Jazz, etc.).
- **Custom Presets**: Create, save, and manage your own custom equalizer settings.

### 📱 Modern User Experience
- **Jetpack Compose**: Entirely built with a declarative UI for smooth animations and performance.
- **Material 3 Design**: Adheres to the latest Material Design guidelines with support for dynamic theming.
- **Glassmorphism Effect**: Stunning visual aesthetics with frosted glass components and immersive backgrounds.
- **Dark Mode**: Optimized for night-time listening and OLED screens.

### 🚀 Performance & Core
- **Media3 (ExoPlayer)**: Powered by the latest Media3 library for robust audio playback and background service support.
- **Room Database**: Efficient local storage for songs, playlists, and custom audio presets.
- **DataStore**: Modern key-value storage for user preferences and audio settings.
- **MVVM Architecture**: Clean, maintainable, and testable codebase.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Audio Engine**: [Media3 (ExoPlayer)](https://developer.android.com/guide/topics/media/media3)
- **Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Preferences**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Manual DI via `AppContainer` pattern.

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 24+ (Min SDK) / 36 (Target SDK)
- Java 11 or higher

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/aura-music.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Run the app on an emulator or a physical device.

## 📂 Project Structure
```text
com.example
├── data          # Data layer (Room, DataStore, Repositories)
├── di            # Dependency Injection (AppContainer)
├── domain        # Domain models
├── player        # Audio playback and DSP management
├── ui            # UI layer (Compose screens, ViewModels, Theme)
└── utils         # Helper classes
```

## 📜 License
This project is licensed under the MIT License - see the LICENSE file for details.

---
Built with by Chamuditha shehan

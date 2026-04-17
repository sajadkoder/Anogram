# AnoGram

AnoGram is an Android messaging app that works without internet. It uses Bluetooth Low Energy to send messages directly between nearby devices. No accounts, no servers, no phone numbers required.

## Tech Stack

- Kotlin 2.0
- Jetpack Compose with Material 3
- MVVM + Clean Architecture
- Hilt for dependency injection
- Room for local database
- Coroutines + Flow for async operations

## Features

- Local chat with message storage
- Send and receive messages
- Reply to messages
- Chat list with search
- BLE peer discovery
- BLE message sending between devices
- Dark mode support
- Settings screen

## Prerequisites

- JDK 17
- Android Studio
- Android SDK 35
- Android device with API 26+

## Installation

```bash
git clone https://github.com/neuralbroker/Anogram.git
cd Anogram
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Usage

1. Install APK on two Android devices
2. Open app and grant Bluetooth + location permissions
3. On each device: Settings -> BLE Mesh -> tap scan icon
4. Devices will discover each other
5. Create a chat and send messages

## Project Structure

```
app/src/main/java/com/anogram/app/
├── data/                    # Data layer
│   ├── bluetooth/           # BLE mesh implementation
│   ├── local/               # Room database (entities, DAOs)
│   └── repository/          # Repository implementations
├── di/                      # Hilt dependency injection
├── domain/                  # Domain layer
│   ├── model/               # Data models (Chat, Message)
│   └── repository/          # Repository interfaces
├── presentation/            # UI layer
│   ├── navigation/          # Navigation setup
│   ├── ui/
│   │   ├── components/      # Reusable UI components
│   │   ├── screens/        # App screens
│   │   └── theme/          # Material 3 theme
│   └── viewmodel/           # ViewModels
├── AnoGramApp.kt           # Application class
└── MainActivity.kt         # Entry point
```

## Environment Variables

None required. All configuration is in code or gradle files.

## Contributing

Pull requests welcome. Open an issue first to discuss changes.

## License

MIT. See LICENSE file for full text.
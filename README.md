<div align="center">

# AnoGram

### Fast. Secure. Private.

A decentralized offline-capable messaging application built with Telegram-inspired UI and bitchat-style Bluetooth mesh networking.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg)](https://kotlinlang.org)
[![MinSDK](https://img.shields.io/badge/MinSDK-26-orange.svg)](https://developer.android.com/about/versions/o)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#license)

[Features](#features) • [Architecture](#architecture) • [Installation](#installation) • [Usage](#usage) • [Contributing](#contributing)

</div>

---

## About

**AnoGram** - Built by **Sajad**

This project is a combination of the best features from Telegram's beautiful UI and bitchat's revolutionary offline mesh networking technology. The result is a powerful, privacy-focused messaging app that works anywhere, even without internet connectivity.

### Key Highlights

- **100% Offline Capable** - Works without internet using Bluetooth mesh (like bitchat)
- **Modern UI/UX** - Telegram-inspired Material 3 design
- **Clean Architecture** - Scalable, testable, maintainable codebase
- **Privacy First** - No accounts, no phone numbers, no servers
- **Peer-to-Peer** - Direct device-to-device communication
- **Multi-hop Relay** - Messages route through nearby devices (up to 7 hops)

---

## Features

### Core Messaging

| Feature | Description |
|---------|-------------|
| Real-time Chat | Instant message delivery with status indicators |
| Message Status | Sending → Sent → Delivered → Read |
| Reply to Messages | Quote and reply to specific messages |
| Message Reactions | React with emojis to messages |
| Message Search | Search through chat history |
| Typing Indicators | See when others are typing |
| Online Status | Real-time online/offline indicators |

### Bluetooth Mesh Networking

| Feature | Description |
|---------|-------------|
| BLE Discovery | Automatic discovery of nearby AnoGram users |
| Peer-to-Peer Messaging | Direct device-to-device communication |
| Multi-hop Relay | Messages route through up to 7 devices |
| Offline Queue | Messages queued until peers available |
| Signal Strength | RSSI indicator for peer proximity |

### User Interface

| Feature | Description |
|---------|-------------|
| Splash Screen | Animated app launch experience |
| Chat List | Searchable list with unread badges |
| Chat Detail | Full messaging interface |
| Settings | Profile, notifications, privacy |
| BLE Settings | Bluetooth discovery and connection |
| Dark Mode | Automatic theme switching |
| Material 3 | Modern design components |

---

## Architecture

AnoGram follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Screens   │  │ ViewModels  │  │      Components     │  │
│  │  (Compose)  │  │   (MVVM)    │  │   (Reusable UI)     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Models    │  │ Repositories│  │      Use Cases      │  │
│  │  (Entities) │  │ (Interfaces)│  │   (Business Logic)  │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │    Room     │  │  BLE Mesh   │  │    Repositories     │  │
│  │  (Local DB) │  │ (P2P Net)   │  │   (Implementations) │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Project Structure

```
app/src/main/java/com/anogram/app/
│
├── data/                          # Data Layer
│   ├── bluetooth/                 # Bluetooth LE Implementation
│   │   ├── BleConstants.kt        # BLE UUIDs and constants
│   │   ├── BleManager.kt          # Device discovery & management
│   │   ├── BleModels.kt           # BLE data models
│   │   └── BleGattService.kt      # GATT server for messaging
│   │
│   ├── local/                     # Local Data Sources
│   │   ├── entity/                # Room entities
│   │   ├── dao/                   # Data Access Objects
│   │   └── AnoGramDatabase.kt     # Room database
│   │
│   └── repository/                # Repository Implementations
│       └── RepositoriesImpl.kt
│
├── di/                            # Dependency Injection
│   └── AppModule.kt               # Hilt modules
│
├── domain/                        # Domain Layer
│   ├── model/                     # Domain models
│   │   └── Models.kt
│   └── repository/                # Repository interfaces
│       └── Repositories.kt
│
├── presentation/                  # Presentation Layer
│   ├── navigation/                # Navigation
│   │   └── NavHost.kt
│   │
│   ├── ui/                        # UI Components
│   │   ├── components/            # Reusable components
│   │   │   ├── ChatListItem.kt
│   │   │   ├── MessageBubble.kt
│   │   │   ├── MessageInput.kt
│   │   │   └── SearchBar.kt
│   │   │
│   │   ├── screens/               # App screens
│   │   │   ├── SplashScreen.kt
│   │   │   ├── ChatListScreen.kt
│   │   │   ├── ChatDetailScreen.kt
│   │   │   ├── SettingsScreen.kt
│   │   │   └── BleSettingsScreen.kt
│   │   │
│   │   └── theme/                 # App theming
│   │       ├── Theme.kt
│   │       └── Typography.kt
│   │
│   └── viewmodel/                 # ViewModels
│       ├── ChatListViewModel.kt
│       ├── ChatDetailViewModel.kt
│       └── BleViewModel.kt
│
├── AnoGramApp.kt                  # Application class
└── MainActivity.kt                # Main entry point
```

---

## Tech Stack

### Core Technologies

| Category | Technology | Version |
|----------|------------|---------|
| Language | Kotlin | 2.0.0 |
| UI Toolkit | Jetpack Compose | BOM 2024.06.00 |
| Architecture | MVVM + Clean Architecture | - |
| DI Framework | Hilt | 2.51.1 |
| Local Database | Room | 2.6.1 |
| Asynchronous | Coroutines + Flow | - |
| Navigation | Navigation Compose | 2.7.7 |

### Libraries

| Library | Purpose |
|---------|---------|
| **Compose BOM** | UI component versions management |
| **Material 3** | Modern design system |
| **Material Icons Extended** | Additional icons |
| **Lifecycle Runtime Compose** | Lifecycle-aware composables |
| **ViewModel Compose** | ViewModel integration |
| **Hilt Navigation Compose** | Hilt + Navigation integration |
| **Coil Compose** | Image loading |
| **KSP** | Kotlin Symbol Processing |

### Bluetooth Stack (from bitchat)

| Component | Description |
|-----------|-------------|
| **BLE Scanner** | Device discovery |
| **GATT Server** | Service hosting |
| **GATT Client** | Service connection |
| **Advertiser** | Device broadcasting |

---

## Installation

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK with API 35
- Android device with API 26+ (for testing)
- Bluetooth LE capability (for mesh features)

### Build from Source

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/anogram.git
   cd anogram
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will prompt to sync Gradle
   - Wait for dependencies to download

4. **Build the APK**
   ```bash
   # Debug build
   ./gradlew assembleDebug
   
   # Release build
   ./gradlew assembleRelease
   ```

5. **Install on Device**
   ```bash
   # Via ADB
   adb install app/build/outputs/apk/debug/app-debug.apk
   
   # Or transfer APK to device and install
   ```

### Download APK

Download the latest release APK from the [Releases](https://github.com/yourusername/anogram/releases) page.

---

## Usage

### Getting Started

1. **Launch the App**
   - Open AnoGram on your device
   - Grant Bluetooth and Location permissions when prompted

2. **Enable Bluetooth Mesh**
   - Go to Settings → BLE Mesh
   - Tap the scan button to discover nearby users
   - Ensure Bluetooth is enabled

3. **Start Chatting**
   - Tap the + button to create a new chat
   - Enter a name for the chat
   - Type your message and send

### BLE Mesh Messaging

AnoGram's offline messaging uses Bluetooth Low Energy for peer-to-peer communication:

```
Device A ←──BLE──→ Device B ←──BLE──→ Device C
                         │
                    (7 hops max)
```

**How it works:**

1. **Discovery**: Devices broadcast their presence via BLE advertising
2. **Connection**: GATT service enables bidirectional communication
3. **Messaging**: Messages are serialized and transmitted over BLE
4. **Relay**: Messages hop through intermediate devices (up to 7 hops)
5. **Delivery**: Target device receives and displays the message

**Message Format:**
```
[MessageID]|[SenderID]|[SenderName]|[Content]|[Timestamp]|[HopCount]
```

### Features Guide

#### Creating a Chat

1. Tap the **+** floating action button
2. Enter a chat name
3. Tap **Create**

#### Sending Messages

1. Open a chat from the list
2. Type your message in the input field
3. Tap the send button

#### Replying to Messages

1. Long-press on a message
2. The reply preview will appear
3. Type your reply and send

#### Using BLE Mesh

1. Go to **Settings → BLE Mesh**
2. Tap the **scan** icon to discover peers
3. Nearby AnoGram users will appear
4. Messages sent will be delivered via BLE

---

## Permissions

AnoGram requires the following permissions:

| Permission | Purpose | Required For |
|------------|---------|--------------|
| `INTERNET` | Network access | Future cloud features |
| `BLUETOOTH` | Bluetooth communication | BLE messaging |
| `BLUETOOTH_ADMIN` | Bluetooth management | BLE operations |
| `BLUETOOTH_SCAN` | Device discovery | Finding peers |
| `BLUETOOTH_CONNECT` | Device connection | P2P messaging |
| `ACCESS_FINE_LOCATION` | Precise location | BLE scan requirement |
| `ACCESS_COARSE_LOCATION` | Approximate location | BLE scan fallback |
| `FOREGROUND_SERVICE` | Background operation | BLE service |
| `POST_NOTIFICATIONS` | Push notifications | Message alerts |

> **Note**: Location permissions are required by Android for BLE scanning. AnoGram does not track or store your location.

---

## Data Models

### Chat

```kotlin
data class Chat(
    val id: Long,
    val name: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int,
    val isOnline: Boolean,
    val isGroup: Boolean,
    val isMuted: Boolean,
    val isPinned: Boolean,
    val typingUsers: List<String>
)
```

### Message

```kotlin
data class Message(
    val id: Long,
    val chatId: Long,
    val content: String,
    val timestamp: Long,
    val isOutgoing: Boolean,
    val status: MessageStatus,
    val replyToId: Long?,
    val replyToContent: String?,
    val reactions: List<Reaction>,
    val isDeleted: Boolean,
    val isEdited: Boolean
)
```

### BLE Message

```kotlin
data class BleMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val hopCount: Int,
    val isRelayed: Boolean
)
```

---

## Database Schema

### Tables

#### `chats`
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PK | Unique identifier |
| name | TEXT | Chat display name |
| avatarUrl | TEXT | Avatar image URL |
| lastMessage | TEXT | Last message preview |
| lastMessageTime | INTEGER | Timestamp (epoch) |
| unreadCount | INTEGER | Unread message count |
| isOnline | INTEGER | Online status |
| isGroup | INTEGER | Group chat flag |
| isMuted | INTEGER | Muted flag |
| isPinned | INTEGER | Pinned flag |

#### `messages`
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PK | Unique identifier |
| chatId | INTEGER FK | Reference to chat |
| content | TEXT | Message content |
| timestamp | INTEGER | Timestamp (epoch) |
| isOutgoing | INTEGER | Sent by user |
| status | TEXT | Message status |
| replyToId | INTEGER | Reply reference |
| isDeleted | INTEGER | Deletion flag |
| isEdited | INTEGER | Edit flag |

#### `users`
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PK | Unique identifier |
| name | TEXT | Display name |
| avatarUrl | TEXT | Avatar URL |
| phone | TEXT | Phone number |
| bio | TEXT | User bio |
| isOnline | INTEGER | Online status |
| lastSeen | INTEGER | Last seen timestamp |

---

## BLE Protocol

### Service UUID
```
0000FFFF-0000-1000-8000-00805F9B34FB
```

### Characteristics

| UUID | Property | Description |
|------|----------|-------------|
| `0000FFFD-...` | READ, WRITE, NOTIFY | Message transfer |
| `0000FFFC-...` | READ, WRITE | Peer information |

### Constants

```kotlin
object BleConstants {
    val SERVICE_UUID: UUID
    val MESSAGE_CHARACTERISTIC_UUID: UUID
    val PEER_INFO_CHARACTERISTIC_UUID: UUID
    const val CHUNK_SIZE = 512
    const val ADVERTISE_NAME = "AnoGram"
    const val SCAN_DURATION = 10000L
    const val MAX_RELAY_HOPS = 7
}
```

### Message Flow

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│  Sender  │────▶│  Relay   │────▶│ Receiver │
│ Device A │     │ Device B │     │ Device C │
└──────────┘     └──────────┘     └──────────┘
     │                │                │
     │   BLE Adv      │   BLE Adv      │
     │◀──────────────▶│◀──────────────▶│
     │                │                │
     │   GATT Conn    │   GATT Conn    │
     │◀──────────────▶│◀──────────────▶│
     │                │                │
     │   Message      │   Relay Msg    │
     │───────────────▶│───────────────▶│
     │   (hop=0)      │   (hop=1)      │
```

---

## Configuration

### Build Variants

| Variant | Debuggable | Signing | Purpose |
|---------|------------|---------|---------|
| `debug` | Yes | Debug key | Development |
| `release` | No | Release key | Production |

### Gradle Properties

```properties
# Memory allocation
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8

# Android settings
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

### Signing Configuration

For release builds, create `app/keystore.properties`:

```properties
storeFile=path/to/keystore.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

---

## Development

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused

### Branch Strategy

```
main          # Production-ready code
  │
  ├── develop # Development branch
  │     │
  │     ├── feature/xxx  # New features
  │     ├── bugfix/xxx   # Bug fixes
  │     └── refactor/xxx # Code refactoring
  │
  └── release/x.x  # Release preparation
```

### Commit Convention

```
feat: Add new feature
fix: Fix a bug
docs: Update documentation
style: Code style changes
refactor: Code refactoring
test: Add or update tests
chore: Build or tooling changes
```

---

## Roadmap

### Version 1.1 (Planned)

- [ ] End-to-end encryption (Noise Protocol)
- [ ] Group chat support
- [ ] Media attachments (images, files)
- [ ] Voice messages
- [ ] Message deletion

### Version 1.2 (Planned)

- [ ] Nostr protocol integration
- [ ] Internet relay servers
- [ ] User profiles with QR codes
- [ ] Chat export/import

### Version 2.0 (Future)

- [ ] iOS support
- [ ] Desktop clients
- [ ] Federation support
- [ ] Plugins system

---

## Contributing

Contributions are welcome! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'feat: Add amazing feature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### Pull Request Guidelines

- Fill out the PR template completely
- Add tests for new features
- Update documentation
- Follow code style guidelines
- Request review from maintainers

---

## Testing

### Unit Tests

```bash
./gradlew test
```

### Instrumentation Tests

```bash
./gradlew connectedAndroidTest
```

### Test Coverage

```bash
./gradlew jacocoTestReport
```

---

## Troubleshooting

### Common Issues

#### Bluetooth Not Working

1. Ensure Bluetooth is enabled
2. Grant location permissions
3. Check if device supports BLE
4. Restart the app

#### Can't Find Peers

1. Ensure both devices have AnoGram running
2. Check Bluetooth is enabled on both
3. Grant all permissions
4. Move devices closer together

#### App Crashes on Launch

1. Clear app data
2. Reinstall the app
3. Check Android version (requires API 26+)

---

## License

```
MIT License

Copyright (c) 2024 AnoGram

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## Acknowledgments

- [Telegram](https://telegram.org) - Beautiful UI/UX design inspiration
- [BitChat](https://github.com/permissionlesstech/bitchat) - BLE mesh networking inspiration
- [Stream Chat](https://getstream.io/chat) - Architecture patterns
- [Android Developers](https://developer.android.com) - Documentation and guides

---

## Support

- **Developer**: Sajad
- **GitHub**: [sajadkoder](https://github.com/sajadkoder)

---

<div align="center">

**Built with ❤️ by Sajad**

[⬆ Back to Top](#anogram)
</div>

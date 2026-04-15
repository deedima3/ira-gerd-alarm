# ⏰ Ira's Cute Alarm

A beautiful, gentle alarm app for managing medicine and meal reminders with cute animations and an adorable puzzle interface to dismiss alarms.

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/yourusername/iras-cute-alarm)](https://github.com/yourusername/iras-cute-alarm/releases)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/yourusername/iras-cute-alarm/build-release.yml)](https://github.com/yourusername/iras-cute-alarm/actions)
[![GitHub](https://img.shields.io/github/license/yourusername/iras-cute-alarm)](LICENSE)

## ✨ Features

- 🔔 **Gentle Reminders** - Soft pastel alarms for medicine and meals
- 🧩 **Cute Puzzle Dismiss** - Drag the icon to a target to stop the alarm
- 🔄 **Auto Updates** - Built-in OTA update system via GitHub Releases
- 🔒 **Lock Screen Support** - Full alarm screen display even when locked
- ⏰ **Reliable Scheduling** - Works even with Doze mode and app killing
- 🎨 **Adorable Design** - Soft pink, mint, and lavender theme
- 📱 **Material 3** - Modern Android UI with Jetpack Compose

## 📲 Download

Get the latest release from [GitHub Releases](https://github.com/deedima3/iras-cute-alarm/releases/latest)

The app will automatically check for and install updates!

## 🚀 Installation

1. Download the APK from the latest release
2. Enable "Install from unknown sources" in Settings
3. Install the APK
4. Grant alarm permissions when prompted

### Initial Setup

⚠️ **For Android 12+ (API 31+)**: 
- Users must grant "Alarms & reminders" permission in Settings
- Go to Settings > Apps > Ira's Cute Alarm > Alarms & reminders > Allow

⚠️ **Battery Optimization**:
- Disable battery optimization for this app to ensure alarms work reliably
- Go to Settings > Battery > Battery Optimization > All Apps > Ira's Cute Alarm > Don't optimize

## 🛠️ Building from Source

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 34

### Build Steps

```bash
# Clone the repository
git clone https://github.com/yourusername/iras-cute-alarm.git
cd iras-cute-alarm

# Build debug version
./gradlew assembleDebug

# Or build release (requires keystore)
./gradlew assembleRelease
```

### Setting up Release Signing

1. Generate keystore:
```bash
keytool -genkey -v -keystore ira-cute-alarm.keystore -alias ira-alarm -keyalg RSA -keysize 2048 -validity 10000
```

2. Add to environment variables or local.properties

## 🔄 OTA Updates

The app includes automatic update checking:
- Checks GitHub Releases API once per day
- Downloads and installs updates automatically
- Users can skip updates if desired

### How it works

1. App checks GitHub Releases API for latest version
2. If newer version exists, shows update dialog
3. User taps download → APK downloads with progress notification
4. After download, prompts to install
5. Future updates work the same way!

### For Maintainers

To release a new version:

1. Update `versionName` and `versionCode` in `app/build.gradle`
2. Commit changes
3. Create and push a tag:
```bash
git tag -a v1.1.0 -m "Release version 1.1.0"
git push origin v1.1.0
```
4. GitHub Actions automatically builds and creates release
5. APK is attached to release automatically

### Setting up GitHub Secrets (for CI/CD)

Add these secrets to your GitHub repository:

1. Go to Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Add these secrets:

- `KEYSTORE_BASE64` - Base64 encoded keystore file:
  ```bash
  base64 -i ira-cute-alarm.keystore | pbcopy  # On Mac
  base64 -i ira-cute-alarm.keystore -w 0 | xclip -selection clipboard  # On Linux
  ```
- `KEYSTORE_PASSWORD` - Your keystore password
- `KEY_ALIAS` - Your key alias (e.g., "ira-alarm")
- `KEY_PASSWORD` - Your key password

## 🏗️ Architecture

- **UI**: Jetpack Compose with Material 3
- **Scheduling**: Android AlarmManager + BroadcastReceivers
- **Foreground Service**: Ensures alarms work when locked
- **Data**: DataStore for persistence
- **Updates**: GitHub Releases API integration

## 📋 Permissions

- `SCHEDULE_EXACT_ALARM` - For precise alarm timing
- `USE_EXACT_ALARM` - For reliable alarm delivery
- `POST_NOTIFICATIONS` - For alarm notifications
- `WAKE_LOCK` - To wake device for alarms
- `RECEIVE_BOOT_COMPLETED` - Reschedule alarms after reboot
- `FOREGROUND_SERVICE` - Keep alarm service running
- `REQUEST_INSTALL_PACKAGES` - For OTA updates
- `INTERNET` - For checking/downloading updates

## 🎨 Color Palette

- **Primary Pink**: `#FFB7D5`
- **Mint**: `#B4E7CE`
- **Lavender**: `#E6E6FA`
- **Cream**: `#FFF8F0`
- **Text**: `#5A4A5E`

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with ❤️ using Jetpack Compose
- Icons and design inspired by cute Korean apps
- Special thanks to Ira for the inspiration!

---

Made with 💕 for Ira

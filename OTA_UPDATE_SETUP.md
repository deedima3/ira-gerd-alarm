# OTA Update System for Ira's Cute Alarm

This app includes an Over-The-Air (OTA) update system that allows users to download and install updates without going through the Play Store.

## How It Works

1. The app checks for updates periodically (once every 24 hours)
2. If an update is available, it shows a cute dialog with release notes
3. Users can download the APK through a foreground service with progress notification
4. After download, the app automatically prompts to install the update

## Setting Up the Update Server

### Option 1: GitHub Releases (Free & Easy)

1. Create a GitHub repository for your app
2. Upload your APK to GitHub Releases
3. Update the `update.json` file in your repo root:

```json
{
  "versionCode": 2,
  "versionName": "1.1.0",
  "downloadUrl": "https://github.com/YOUR_USERNAME/YOUR_REPO/releases/download/v1.1.0/app-release.apk",
  "releaseNotes": "Your release notes here",
  "forceUpdate": false,
  "minSdk": 26
}
```

4. Update `UpdateManager.kt` line 21:
```kotlin
const val DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPO/main/update.json"
```

### Option 2: Firebase Hosting (Free)

1. Install Firebase CLI: `npm install -g firebase-tools`
2. Login: `firebase login`
3. Initialize: `firebase init hosting`
4. Put your `update.json` and APK files in the `public` folder
5. Deploy: `firebase deploy`

### Option 3: Your Own Server

Upload the `update.json` file and APK to any web server with HTTPS enabled.

## Building a Release APK

### Step 1: Generate a Keystore

```bash
keytool -genkey -v -keystore ira-cute-alarm.keystore -alias ira-alarm -keyalg RSA -keysize 2048 -validity 10000
```

### Step 2: Configure Signing

Create `local.properties` in project root:
```properties
RELEASE_STORE_FILE=ira-cute-alarm.keystore
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=ira-alarm
RELEASE_KEY_PASSWORD=your_key_password
```

Or use environment variables:
```bash
export KEYSTORE_PATH=ira-cute-alarm.keystore
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=ira-alarm
export KEY_PASSWORD=your_password
```

### Step 3: Build Release APK

```bash
./gradlew assembleRelease
```

The APK will be at: `app/build/outputs/apk/release/app-release.apk`

### Alternative: Build App Bundle (AAB) for smaller size

```bash
./gradlew bundleRelease
```

## Release Checklist

- [ ] Increment `versionCode` in `app/build.gradle`
- [ ] Update `versionName` in `app/build.gradle`
- [ ] Build signed release APK
- [ ] Update `update.json` with new version info and download URL
- [ ] Upload APK to your server/GitHub releases
- [ ] Test update flow on a device
- [ ] Update release notes

## Testing Updates

1. Install an older version of the app
2. Trigger update check manually (add a button to call `UpdateManager.checkForUpdate()`)
3. Verify the update dialog appears
4. Download and install the update
5. Verify the app updates successfully

## Important Notes

- **HTTPS Required**: The download URL must use HTTPS for Android 9+ (API 28+)
- **Install Permission**: Users need to allow "Install from unknown sources" for this app
- **Auto-start**: The update check runs automatically when the app starts (with 24h interval)
- **Force Updates**: Set `forceUpdate: true` in update.json for critical security updates

## Troubleshooting

### "Download Failed" Error
- Check internet connection
- Verify the download URL is accessible and uses HTTPS
- Check server CORS settings if using a custom server

### "Cannot Install" Error
- User must enable "Install unknown apps" permission in Settings > Apps > Ira's Cute Alarm
- APK must be signed with the same certificate as the installed app

### Update Not Detected
- Verify `versionCode` in update.json is higher than current app version
- Check that the update URL is correct in `UpdateManager.kt`
- Clear app data to reset last check time (for testing)

## Security Considerations

1. Always use HTTPS for update.json and APK downloads
2. Consider adding checksum verification to update.json
3. Sign all releases with the same keystore
4. Keep your keystore file secure and backed up
5. Consider code signing certificate for enterprise distribution

## Customization

You can customize the update dialog appearance in `UpdateDialog.kt`:
- Change colors to match your app theme
- Modify the update check interval in `UpdateManager.kt`
- Add custom release note formatting
- Customize notification appearance in `UpdateDownloadService.kt`

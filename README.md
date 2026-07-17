# Malayalam Learning App

A gamified, interactive Android application designed to help absolute beginners learn to read, write, and speak Malayalam! Inspired by popular language learning platforms, this app focuses on a hands-on, multi-modal approach.

## 🌟 Features

- **Gamified Dashboard:** Track your daily streak, accumulate XP, and level up as you complete exercises.
- **Trace & Learn (Drawing):** An interactive canvas that lets you trace Malayalam letters. It validates your effort locally so you can master the script.
- **Pronunciation & Listening:** Uses Android's native Text-to-Speech (TTS) to pronounce letters aloud, and Speech Recognition (STT) to let you practice speaking.
- **Dynamic Content:** Integrates with Google's Gemini AI (`gemini-1.5-flash`) to generate fresh, dynamic translation exercises in batches, providing infinite learning content without draining resources.

## 📱 Compatibility: Phones & Tablets

This app is built using standard Android UI components (Fragments, ViewModels, Material Design) and is fully compatible with both Android phones and tablets (including Samsung Galaxy Tabs). The layouts will automatically scale and adapt to larger screens!

## 🚀 How to Install on Your Phone or Tablet

You can easily install this app directly onto your physical Android device.

### Method 1: Build an APK (Easiest for sharing)

1. Clone this repository.
2. Open a terminal in the project root and build the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
3. The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.
4. Transfer this `.apk` file to your Android phone or tablet (via Google Drive, email, or USB cable).
5. Open the file on your device to install it (you may need to allow "Install from Unknown Sources" in your device settings).

### Method 2: Direct Install via USB (Developer Mode)

1. **Enable Developer Options:** On your Android device, go to `Settings > About Phone` (or `About Tablet`) and tap `Build Number` 7 times.
2. **Enable USB Debugging:** Go back to `Settings > Developer Options` and turn on `USB Debugging`.
3. Connect your device to your computer via USB.
4. Run the following command in the project root to build and install it directly:
   ```bash
   ./gradlew installDebug
   ```

## 🛠️ Development Setup

- **Java Version:** 17
- **Android SDK:** 36 (Minimum SDK 26)
- **Architecture:** MVVM (Model-View-ViewModel) with Room Database and standard Android architecture components.

### Running Tests
This project strictly follows Test-Driven Development (TDD). You can run the test suite locally:
```bash
./gradlew test
```

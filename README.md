<div align="center">

# 📋 Attendance Manager

### A modern, offline-first student attendance and class schedule management mobile app built with React Native, Expo, and official Material Design 3 (Material You).

[![React Native](https://img.shields.io/badge/React_Native-0.86-61DAFB?logo=react&logoColor=black&style=for-the-badge)](https://reactnative.dev/)
[![Expo SDK](https://img.shields.io/badge/Expo_SDK-57-000020?logo=expo&logoColor=white&style=for-the-badge)](https://expo.dev/)
[![Material Design 3](https://img.shields.io/badge/Material_You-MD3-4285F4?logo=google&logoColor=white&style=for-the-badge)](https://m3.material.io/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript&logoColor=white&style=for-the-badge)](https://www.typescriptlang.org/)
[![Platform](https://img.shields.io/badge/Platform-Android_%7C_iOS-3DDC84?logo=android&logoColor=white&style=for-the-badge)](https://github.com/afnan-nex/Attendance-manager)

</div>

---

## 🌟 Overview

**Attendance Manager** is a full-featured, offline-first attendance tracking and timetable management solution tailored for university students, class representatives (CRs), teachers, and instructors.

Built with **React Native Paper v5** and **`@pchmn/expo-material3-theme`**, the app dynamically adapts to your Android 12+ wallpaper colors (Monet / Material You), delivers silky-smooth 120Hz native animations, provides local biometric security (Fingerprint / Face ID), and allows exporting comprehensive attendance reports directly to **Excel (.xlsx)** and **CSV** spreadsheets.

---

## ✨ Features

### 🎨 Material Design 3 & Material You
- **Dynamic Theming:** Seamlessly extracts system wallpaper palettes on Android 12+ with graceful Material 3 fallbacks on older Android versions and iOS.
- **Dark & Light Mode:** Fully synced with system settings and Paper MD3 typography (15 type scales).
- **M3 Analog Clock Time Picker:** Authentic interactive circular analog clock dial with touch/drag support, rigid hand alignment, and AM/PM segmented controls.
- **M3 Date Picker:** Full-featured calendar modal with high-contrast day circles and range constraints.

### 📅 Smart Attendance Management
- **Horizontal Date Strip:** Infinite 731-day horizontal date slider (`-365` to `+365` days) with free scrolling, "Today" indicator, and non-shifting tap selection.
- **Historical Record Protection (7-Tap Lock):**
  - **Today:** Always open for immediate attendance logging.
  - **Past Dates:** Protected with a security lock requiring **7 taps** to unlock, preventing accidental overwrites.
  - **Future Dates:** Strictly locked against logging attendance in advance.
- **Section Filtering:** Filter students by section (A–F) or view all sections simultaneously.
- **Student Quick-Action Cards:** One-tap WhatsApp chat, phone dialer integration, and clipboard copy for Registration Numbers and CNIC.

### 📚 Timetable & Class Management
- **Weekly Schedule:** Organize classes by day, start/end time, lecture type (*Lecture, Tutorial, Practical Lab, Workshop, Seminar, Other*), and credit hours.
- **Optional Metadata:** Support for optional Instructor/Teacher names and Room/Lab locations.
- **Hide / Archive Classes:** Temporarily hide inactive classes without deleting historical records.
- **Confirmation Protection:** Safe class deletion requiring typed confirmation to avoid accidental data loss.

### 🔒 Privacy & Biometric Security
- **100% Offline & Private:** Powered by a local **SQLite** database (`expo-sqlite`) — no account required and zero data leaves your device.
- **Biometric App Lock:** Protect student records and attendance data using device Biometrics (Fingerprint / Face Unlock / PIN) via `expo-local-authentication`.

### 📊 Excel & CSV Export
- Export full semester or single-subject attendance matrices directly into **Microsoft Excel (.xlsx)** or **CSV** formats using `xlsx`, `expo-file-system`, and `expo-sharing`.

---

## 🏗️ Tech Stack

| Technology | Description |
| :--- | :--- |
| **Framework** | [React Native](https://reactnative.dev/) (0.86) with [Expo SDK](https://expo.dev/) (v57) |
| **Language** | [TypeScript](https://www.typescriptlang.org/) |
| **UI Kit** | [React Native Paper v5](https://callstack.github.io/react-native-paper/) (MD3) |
| **Theming** | [`@pchmn/expo-material3-theme`](https://github.com/pchmn/expo-material3-theme) (Material You Monet) |
| **Database** | [`expo-sqlite`](https://docs.expo.dev/versions/latest/sdk/sqlite/) (Local SQLite Storage) |
| **Security** | [`expo-local-authentication`](https://docs.expo.dev/versions/latest/sdk/local-authentication/) (Biometrics) |
| **Export Engine**| [`xlsx`](https://sheetjs.com/) + [`expo-file-system`](https://docs.expo.dev/versions/latest/sdk/filesystem/) + [`expo-sharing`](https://docs.expo.dev/versions/latest/sdk/sharing/) |
| **Haptics** | [`expo-haptics`](https://docs.expo.dev/versions/latest/sdk/haptics/) |

---

## 📱 Screenshots

<div align="center">
  <img src="./assets/icon.png" width="120" alt="Attendance Manager Logo" />
</div>

---

## 🚀 Getting Started

### Prerequisites
- [Node.js](https://nodejs.org/) (v18 or newer)
- [Android Studio](https://developer.android.com/studio) & Android SDK (for running on Android emulator/device)
- [Expo CLI](https://docs.expo.dev/get-started/installation/)

### Installation

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/afnan-nex/Attendance-manager.git
   cd Attendance-manager
   ```

2. **Install Dependencies:**
   ```bash
   npm install
   ```

3. **Start the Expo Development Server:**
   ```bash
   npx expo start
   ```

4. **Run on Android:**
   ```bash
   npx expo run:android
   ```

---

## 📦 Building the Production APK

To build a standalone signed release APK locally without EAS cloud queues:

```powershell
# Navigate to the android directory
cd android

# Build the release APK
.\gradlew.bat assembleRelease

# The generated APK will be located at:
# android/app/build/outputs/apk/release/app-release.apk
```

To install directly to a connected Android device:
```powershell
adb install -r android/app/build/outputs/apk/release/app-release.apk
```

---

## 📂 Project Structure

```
Attendance-Manager/
├── android/                   # Native Android project configuration
├── assets/                    # App icons, splash screens, and adaptive icon layers
├── scripts/
│   └── generate_icons.js      # Vector-to-mipmap density icon generator
├── src/
│   ├── components/            # Reusable Material Design 3 UI components
│   │   ├── AddEditClassModal.tsx
│   │   ├── AddEditStudentModal.tsx
│   │   ├── BiometricOverlay.tsx
│   │   ├── BottomNavBar.tsx
│   │   ├── CalendarModal.tsx
│   │   ├── ClassCard.tsx
│   │   ├── DateStrip.tsx
│   │   ├── DeleteClassDialog.tsx
│   │   ├── EmptyState.tsx
│   │   ├── Material3TimePickerModal.tsx
│   │   ├── SectionPickerModal.tsx
│   │   ├── StudentDetailModal.tsx
│   │   └── TopAppBar.tsx
│   ├── db/                    # SQLite database schema and service operations
│   │   ├── database.ts
│   │   └── useDatabase.ts
│   ├── screens/               # Main application screens
│   │   ├── AttendanceScreen.tsx
│   │   ├── HomeScreen.tsx
│   │   ├── ManageClassesScreen.tsx
│   │   ├── ManageStudentsScreen.tsx
│   │   └── SettingsScreen.tsx
│   ├── theme/                 # Material 3 color palettes & typography tokens
│   │   ├── colors.ts
│   │   ├── ThemeContext.tsx
│   │   └── typography.ts
│   ├── types/                 # TypeScript interfaces and entity types
│   │   └── index.ts
│   └── utils/                 # Utilities (Date calculations, Exporting, Linking, Toasts)
│       ├── dateUtils.ts
│       ├── exportUtils.ts
│       ├── linking.ts
│       ├── toast.ts
│       └── validation.ts
├── App.tsx                    # Root application component
├── app.json                   # Expo configuration
├── package.json
└── tsconfig.json
```

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

<div align="center">
  Developed with ❤️ by <a href="https://github.com/afnan-nex">Afnan</a>
</div>

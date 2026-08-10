# CR Attendance Management App - Kotlin + Material UI Prompt

## Project Overview
Build a comprehensive Android attendance management app for Class Representatives (CRs) to track student attendance digitally. Replace manual paper-based attendance and manual Google Sheets data entry with a modern, intuitive mobile app.

## Tech Stack Requirements
- **Language**: Kotlin
- **UI Framework**: Material Design 3 (Material UI)
- **Architecture**: MVVM with Repository Pattern
- **Database**: Room (SQLite)
- **Permissions**: Calendar, Contacts, Phone, Biometric, File Storage
- **Min SDK**: 26, Target SDK: 34

## Core Data Models

### Class/Subject
```
- id: UUID
- shortName: String (e.g., "CP")
- fullNameWithCode: String (e.g., "CS-101 Computer Programming")
- lectureType: Enum (Lecture, Tutorial, Practical Lab, Workshop, Seminar, Other)
- dayOfWeek: Int (1-7, Monday-Sunday)
- startTime: LocalTime
- endTime: LocalTime
- teacherName: String
- location: String
- creditHours: Int (1-5)
- isHidden: Boolean (default: false)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### Student
```
- id: UUID
- name: String
- registrationNumber: String
- section: Char (A-F)
- cnic: String (optional)
- whatsappNumber: String
- phoneNumber: String (optional)
- sameAsWhatsapp: Boolean (default: true)
- orderIndex: Int (for drag-reorder, per-section global ordering)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### Attendance Record
```
- id: UUID
- classId: UUID (FK to Class)
- studentId: UUID (FK to Student)
- date: LocalDate
- isPresent: Boolean (toggle, default: false)
- recordedAt: LocalDateTime
```

### App Settings
```
- id: UUID
- selectedSection: Char (default: 'All')
- biometricEnabled: Boolean (default: false)
- lastUpdated: LocalDateTime
```

## Screen Specifications

### Screen 1: Home
**Bottom Nav Button**: Home (icon: house)

**Header**:
- Left: App name/logo (text: "Attendance Manager" or similar)
- Right: Calendar icon (jump to date) + Settings icon (gear)

**Date Selector** (sliding 7-day window):
- Displays Mon-Sun with dates below
- Current/selected date highlighted with accent color
- Tappable to select different days
- Sliding window moves when selecting dates outside current week
- Highlighted date moves with selection

**"Today's Classes" Section**:
- Shows only classes scheduled for selected day
- Empty state if no classes

**Class Card Format** (per class on that day):
```
CP (Bold, Heading 1)
CS-101 Computer Programming (Regular, Heading 2)
[Lecture] (Tag/Chip)
Monday 9:00 AM
Mam Yusra
Ground Floor Room 4 (Bold)
Credit Hours: 3
9:00 AM - 11:30 AM
```

**Behavior**:
- Default shows current day
- Classes are day-wise (one lecture per subject per week)
- Shows 2-3 lectures per day typically
- Non-tappable display only

---

### Screen 2: Attendance
**Bottom Nav Button**: Attendance (icon: checklist)

**Header**: Same as Home (calendar + settings)

**Date Selector**: Same as Home (7-day sliding window)

**Subject Dropdown**:
- Shows only subjects scheduled for selected day
- Dropdown displays: "Subject Code - Full Name"
- Required selection to proceed

**Attendance Table**:
- Columns: Sr. No | Name | Reg No | Attendance (Toggle Switch)
- Rows dynamically populated per selected subject
- Serial numbers: Per-section in attendance view
- Shows only students of currently selected section (setting)
- Order follows manage students order

**Student Row Interaction**:
- Tapping Sr. No, Name, or Reg No opens popup

**Student Popup Format**:
```
Name (Bold, Heading 1)
Reg Number
WhatsApp Number
+923164876322 [Copy icon] [Open icon]
Phone Number
+923164876322 [Copy icon] [Open icon]
[Close Button]
```

**Popup Icon Behaviors**:
- Copy icon: Copies number to clipboard (show toast)
- Open icon (WhatsApp): Intent to WhatsApp app directly (not browser)
- Open icon (Phone): Intent to phone dialer with number pre-filled

**Attendance Toggle**:
- Material 3 Switch component
- On = Present (P in CSV), Off = Absent (empty in CSV)
- Can edit same-day attendance freely

**Edit Lock Logic**:
- Can edit attendance on same day freely
- Next day onwards: locked by default
- Unlock via toggle switch 7 times to edit past attendance
- Show toast message (auto-hide after 2 seconds): "Attendance Edit Unlocked"
- Re-lock after closing screen or selecting new date

---

### Screen 3: Manage Students
**Bottom Nav Button**: Manage Students (icon: person_add)

**Header**:
- Left: App name
- Right: Settings icon (NO calendar icon)

**"+ Add Student" Button**:
- Opens modal bottom sheet or dialog with form

**Add Student Form**:
```
Name [Text Input]
CNIC [Text Input]
Registration Number [Text Input]
Section [Dropdown: A, B, C, D, E, F]
WhatsApp Number [Text Input]
☑ Same as WhatsApp [Checkbox, default: checked]
Phone Number [Text Input, greyed out initially]
[Save Button]
```

**Checkbox Logic**:
- When checked: Phone Number field disabled (greyed out)
- When unchecked: Phone Number field enabled
- Default: checked

**Student List** (below Add Student button):
- Columns: [Drag Handle] | Sr. No | Name | Section
- Drag handle: 6-dot menu icon (⋮⋮)
- Drag-to-reorder changes serial numbers automatically
- Global serial numbering (not per-section)

**Row Interaction** (tapping row):
```
Name (Bold, Heading 1)
Reg Number [Copy icon]
Section: B
CNIC [Copy icon]
WhatsApp Number
+923164876322 [Copy icon] [Open icon]
Phone Number
+923164876322 [Copy icon] [Open icon]
[Edit icon (pencil)] [Close Button]
```

**Edit Icon**:
- Opens same Add Student form pre-filled with student data
- Save updates the record

---

### Screen 4: Manage Classes
**Bottom Nav Button**: Manage Classes (icon: class)

**Header**: App name (left) + Settings icon (right, NO calendar)

**"+ Add Class" Button**:
- Opens modal bottom sheet or dialog

**Add Class Form**:
```
Short Name [Text Input] (e.g., "CP")
Full Name with Code [Text Input] (e.g., "CS-101 Computer Programming")
Lecture Type [Dropdown: Lecture, Tutorial, Practical Lab, Workshop, Seminar, Other]
Day [Dropdown: Monday-Sunday]
Time [Time Picker] (start time)
Teacher Name [Text Input]
Location [Text Input]
Credit Hours [Dropdown: 1, 2, 3, 4, 5]
Start Time [Time Picker]
End Time [Time Picker]
[Save Button]
```

**Note**: Day + Start Time shown together in Home/Attendance as "Monday 9:00 AM"

**Class Cards** (below Add Class button):
- Same format as Home screen cards (non-interactive display)
- Add two action buttons per card:
  - Delete icon (small, trash)
  - Hide toggle/button (eye with slash or similar)

**Delete Behavior**:
- Opens confirmation dialog
- User must type: "DELETE [Class Full Name]" exactly
- Dialog shows guidance text: "Type 'DELETE [Class Full Name]' to confirm"
- On confirm: Deletes class and all associated attendance records
- Advisory message: "Advised to export CSV/Excel from Settings before deleting"

**Hide Behavior**:
- Hides class from Home screen only
- Still appears in Attendance dropdown
- Still appears in Export/Share options
- Toggle state persists

---

### Settings Screen
**Bottom Nav Button**: Settings (icon: settings, or accessible via top-right gear)

**Header**: Settings title + back/close

**Section 1: Your Section**
- Label: "Your Section"
- Current value shown (default: "All")
- Dropdown button triggering modal

**Section Selector Modal**:
- Dropdown menu with options: All, A, B, C, D, E, F
- [Save Button]

**Section Filter Impact**:
- Attendance screen: Shows only students of selected section
- Serial numbers renumbered per-section (1, 2, 3...)
- Manage Students: Shows all students but filters applied on Attendance
- When section changes, Attendance screen updates instantly
- Drag-order from Manage Students preserved per section

**Section 2: Export & Share**
- Label: "Export/Share Attendance"
- Class Selector Dropdown: "Select Class..."
- [Export Button] (greyed out if no class selected)
- [Share Button] (greyed out if no class selected, placed side-by-side with Export)

**Export/Share Logic**:
- Exports attendance for selected class only
- Uses attendance for currently selected section
- Filename: `{SUBJECT_CODE}_{SUBJECT_FULL_NAME}_{HHMM}_{DD-MM-YYYY}.csv`
  - Example: `CS-101_Computer-Programming_0930_15-01-2025.csv`
- Export: Prompts file save dialog (standard Android file picker)
- Share: Opens share sheet (WhatsApp, Email, Drive, etc.)

**CSV Format**:
```
sr_no,name,reg_no,section,2025-01-13,2025-01-14,2025-01-15,...
1,Afnan,25-CS-38,A,P,P,,P,...
2,Ali,25-CS-39,A,,P,P,...
```
- Columns: sr_no, name, reg_no, section, then date columns (YYYY-MM-DD)
- Each date column: "P" if present, empty if absent
- Only dates with attendance records included
- Dates in chronological order
- Only students from selected section included

**Section 3: Biometric Security**
- Label: "Unlock with Biometric"
- Toggle switch (default: off)
- On toggle enable: Prompts biometric setup (Material BiometricPrompt)
- Once enabled: App locks on resume, requires biometric to unlock

---

## Navigation Structure

**Bottom Navigation Bar** (always visible):
1. Home (house icon)
2. Attendance (checklist icon)
3. Manage Students (person_add icon)
4. Manage Classes (class icon)

**Top App Bar** (all screens):
- Left: App title/logo
- Right: Settings gear icon + Calendar icon (Home & Attendance only)

**Calendar Icon Behavior**:
- Opens date picker modal
- User selects any date
- Navigates to that date (updates 7-day window)
- Can jump forward/backward up to 1 year

**Settings Icon Behavior**:
- Opens Settings screen (full navigation)
- From Settings, back returns to previous screen

---

## Data Validation & Constraints

### Class
- Short name: non-empty, max 10 characters
- Full name: non-empty, max 100 characters
- Teacher name: non-empty
- Location: non-empty
- Credit hours: 1-5 only

### Student
- Name: non-empty
- Registration number: non-empty, unique
- CNIC: optional, if provided must be 13 digits
- WhatsApp number: standard Pakistani format (+92 or 03)
- Phone number: required if "Same as WhatsApp" unchecked
- Section: A-F only

### Attendance
- Can only toggle same-day attendance freely
- Past attendance requires unlock toggle (7 taps)
- Cannot add attendance for future dates

---

## UI/UX Details

### Material Design 3 Components Required
- Material Button (filled, outlined, text)
- Material TextField (with icons)
- Material Switch (for attendance toggle)
- Material Dropdown Menu
- Material Card (for classes)
- Material Dialog
- Material BottomSheet (for forms)
- Material TimePicker
- Material DatePicker
- Material Chip (for lecture type tag)
- Material Snackbar (for notifications/toasts)
- Material TopAppBar
- Material NavigationBar (bottom nav)
- Material BiometricPrompt (for biometric)
- Drag & drop via Material library or Jetpack Compose

### Color Scheme
- Follow Material Design 3 color system
- Accent color: Golden yellow (#ffca12) from user's portfolio aesthetic
- Primary: Professional blue
- Use Material theme for light/dark modes

### Typography
- Headings: Use Material Typography scale
- Bold text: For class short names, student names, locations
- Regular text: For details (code, teacher, time)
- Tags/Chips: For lecture type

### Spacing & Layout
- Follow Material 3 spacing scale (4dp base unit)
- Cards: 8dp elevation
- Padding: 16dp horizontal, 12dp vertical
- 7-day date selector: horizontal scroll with snap
- Tables: scrollable horizontally if needed

---

## Database & Local Storage

### Room Database Setup
- Create 4 entities: Class, Student, Attendance, AppSettings
- DAOs: ClassDao, StudentDao, AttendanceDao, SettingsDao
- Repositories: ClassRepository, StudentRepository, AttendanceRepository, SettingsRepository

### Database Migrations
- Version 1: Initial schema with all 4 tables

### Data Persistence
- All data stored locally in SQLite
- No network/cloud sync (local only)
- CSV export handled via CSVWriter

---

## Permissions Required
- READ_CALENDAR / WRITE_CALENDAR (optional, for integration)
- CALL_PHONE (for dialer intent)
- READ_CONTACTS (optional, for contact suggestions)
- USE_BIOMETRIC (for biometric unlock)
- READ_EXTERNAL_STORAGE / WRITE_EXTERNAL_STORAGE (for file export)

---

## File Structure (Recommended)
```
app/src/main/kotlin/com/crattendance/
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── AttendanceScreen.kt
│   │   ├── ManageStudentsScreen.kt
│   │   ├── ManageClassesScreen.kt
│   │   └── SettingsScreen.kt
│   ├── components/
│   │   ├── DateSelector.kt
│   │   ├── ClassCard.kt
│   │   ├── AttendanceTable.kt
│   │   ├── StudentPopup.kt
│   │   └── ...
│   ├── navigation/
│   │   └── NavGraph.kt
│   └── theme/
│       ├── Color.kt
│       ├── Type.kt
│       └── Theme.kt
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   └── dao/
│   ├── model/
│   ├── repository/
│   └── datastore/
├── viewmodel/
│   ├── HomeViewModel.kt
│   ├── AttendanceViewModel.kt
│   ├── ManageStudentsViewModel.kt
│   ├── ManageClassesViewModel.kt
│   └── SettingsViewModel.kt
├── utils/
│   ├── DateUtils.kt
│   ├── CsvExporter.kt
│   ├── BiometricHelper.kt
│   └── IntentHelper.kt
└── MainActivity.kt
```

---

## Key Implementation Notes

### Date Navigation
- Use `LocalDate` and `LocalTime` from Java 8 time API
- 7-day window updates when selecting dates outside current week
- Current date indicator moves with selection
- Historical data accessible (1 year back/forward)

### Attendance Edit Lock
- Store last-edit timestamp per attendance record
- On day change, lock all past records automatically
- Unlock via toggle switch mechanism (7 taps)
- Show toast on unlock (auto-dismiss 2 sec)
- Re-lock on screen navigation away

### Section Filtering
- Settings store selected section preference
- Attendance screen filters students by section
- Serial numbers renumbered per-section (1, 2, 3...)
- Drag-order from Manage Students applies globally, filtered on UI
- Changing section re-renders Attendance list instantly

### CSV Export
- Only include students from selected section
- Only include classes from selected filter
- Date columns in YYYY-MM-DD format
- "P" for present, empty for absent
- Include header row

### Biometric Integration
- Use AndroidX BiometricPrompt API
- Prompt on app resume if enabled
- Allow fallback to PIN/pattern
- Show clear error messages

---

## Testing Checklist
- [ ] Add/edit/delete classes
- [ ] Add/edit/delete students
- [ ] Drag-reorder students and serial numbers update
- [ ] Toggle attendance on same day
- [ ] Attendance lock/unlock past day
- [ ] Section filtering in attendance and export
- [ ] Date navigation and 7-day window update
- [ ] CSV export with correct format
- [ ] WhatsApp intent opens app directly
- [ ] Phone intent opens dialer
- [ ] Biometric unlock works
- [ ] All Material 3 components render correctly
- [ ] Dark mode support

---

## Deliverables
1. **Complete Kotlin source code** with MVVM architecture
2. **Material Design 3 UI** for all screens
3. **Room database** setup with migrations
4. **CSV export** functionality
5. **Biometric unlock** integration
6. **Android manifest** with all permissions
7. **Gradle configuration** with all dependencies
8. **Unit tests** for ViewModels and Repositories
9. **Build-ready project** (no external APIs, local storage only)

---

## Build & Run
```bash
./gradlew build
./gradlew installDebug
```

Target: Android 14 (SDK 34), Min SDK: 26

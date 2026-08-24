export type Weekday = 'Monday' | 'Tuesday' | 'Wednesday' | 'Thursday' | 'Friday' | 'Saturday' | 'Sunday';

export type LectureType = 'Lecture' | 'Tutorial' | 'Practical Lab' | 'Workshop' | 'Seminar' | 'Other';

export interface ClassEntity {
  id: string;
  shortName: string;
  fullNameWithCode: string;
  lectureType: LectureType;
  day: Weekday;
  startTime: string; // "HH:mm" (24h format internally for sorting/parsing) e.g. "09:00"
  endTime: string;   // "HH:mm" e.g. "10:00"
  teacherName: string;
  location: string;
  creditHours: number;
  isHidden: boolean;
}

export type SectionType = 'All' | 'A' | 'B' | 'C' | 'D' | 'E' | 'F';

export type MaterialYouPaletteKey =
  | 'dynamic'
  | 'blue'
  | 'green'
  | 'purple'
  | 'coral'
  | 'amber'
  | 'teal';

export interface StudentEntity {
  id: string;
  orderIndex: number;
  name: string;
  cnic: string;
  regNo: string;
  section: string; // 'A' .. 'F'
  waNumber: string;
  isPhoneSame: boolean;
  phNumber: string;
}

export interface AttendanceRecord {
  id: string;
  classId: string;
  studentId: string;
  date: string; // "YYYY-MM-DD"
  isPresent: boolean;
  recordedAt: number;
}

export interface AppSettings {
  selectedSection: SectionType;
  biometricEnabled: boolean;
  selectedThemePalette: MaterialYouPaletteKey;
}

export type NavRoute = 'home' | 'attendance' | 'students' | 'classes' | 'settings';

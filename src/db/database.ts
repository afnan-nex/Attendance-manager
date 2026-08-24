import AsyncStorage from '@react-native-async-storage/async-storage';
import * as SQLite from 'expo-sqlite';
import { ClassEntity, StudentEntity, AttendanceRecord, AppSettings, SectionType, Weekday, LectureType, MaterialYouPaletteKey } from '../types';

const DB_NAME = 'cr_attendance.db';
const ASYNC_BACKUP_KEY = '@cr_attendance_backup_state';

type Listener = () => void;

class DatabaseService {
  private db: SQLite.SQLiteDatabase | null = null;
  private isInitialized = false;
  private listeners: Set<Listener> = new Set();

  private classes: ClassEntity[] = [];
  private students: StudentEntity[] = [];
  private attendance: AttendanceRecord[] = [];
  private settings: AppSettings = {
    selectedSection: 'All',
    biometricEnabled: false,
    selectedThemePalette: 'blue',
  };

  public subscribe(listener: Listener): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  private notify(): void {
    this.listeners.forEach((l) => {
      try {
        l();
      } catch (err) {
        console.error('Error in db listener:', err);
      }
    });
  }

  public async init(): Promise<void> {
    if (this.isInitialized) return;

    try {
      this.db = await SQLite.openDatabaseAsync(DB_NAME);

      // Create tables
      await this.db.execAsync(`
        PRAGMA foreign_keys = ON;

        CREATE TABLE IF NOT EXISTS classes (
          id TEXT PRIMARY KEY,
          shortName TEXT NOT NULL,
          fullNameWithCode TEXT NOT NULL,
          lectureType TEXT NOT NULL,
          day TEXT NOT NULL,
          startTime TEXT NOT NULL,
          endTime TEXT NOT NULL,
          teacherName TEXT NOT NULL,
          location TEXT NOT NULL,
          creditHours INTEGER NOT NULL,
          isHidden INTEGER NOT NULL DEFAULT 0
        );

        CREATE TABLE IF NOT EXISTS students (
          id TEXT PRIMARY KEY,
          orderIndex INTEGER NOT NULL,
          name TEXT NOT NULL,
          cnic TEXT,
          regNo TEXT UNIQUE NOT NULL,
          section TEXT NOT NULL,
          waNumber TEXT NOT NULL,
          isPhoneSame INTEGER NOT NULL DEFAULT 1,
          phNumber TEXT
        );

        CREATE TABLE IF NOT EXISTS attendance (
          id TEXT PRIMARY KEY,
          classId TEXT NOT NULL,
          studentId TEXT NOT NULL,
          date TEXT NOT NULL,
          isPresent INTEGER NOT NULL,
          recordedAt INTEGER NOT NULL,
          UNIQUE(classId, studentId, date),
          FOREIGN KEY (classId) REFERENCES classes (id) ON DELETE CASCADE,
          FOREIGN KEY (studentId) REFERENCES students (id) ON DELETE CASCADE
        );

        CREATE TABLE IF NOT EXISTS app_settings (
          key TEXT PRIMARY KEY,
          value TEXT NOT NULL
        );
      `);

      // Load data from SQLite
      await this.loadAllFromDb();
      this.isInitialized = true;
      this.notify();
    } catch (sqliteErr) {
      console.warn('SQLite init error, falling back to AsyncStorage:', sqliteErr);
      await this.loadFromAsyncStorage();
      this.isInitialized = true;
      this.notify();
    }
  }

  private async loadAllFromDb(): Promise<void> {
    if (!this.db) return;

    // Load classes
    const classRows = await this.db.getAllAsync<any>(
      'SELECT * FROM classes ORDER BY day, startTime ASC'
    );
    this.classes = classRows.map((r) => ({
      id: r.id,
      shortName: r.shortName,
      fullNameWithCode: r.fullNameWithCode,
      lectureType: r.lectureType as LectureType,
      day: r.day as Weekday,
      startTime: r.startTime,
      endTime: r.endTime,
      teacherName: r.teacherName,
      location: r.location,
      creditHours: Number(r.creditHours),
      isHidden: Boolean(r.isHidden),
    }));

    // Load students
    const studentRows = await this.db.getAllAsync<any>(
      'SELECT * FROM students ORDER BY orderIndex ASC'
    );
    this.students = studentRows.map((r) => ({
      id: r.id,
      orderIndex: Number(r.orderIndex),
      name: r.name,
      cnic: r.cnic || '',
      regNo: r.regNo,
      section: r.section,
      waNumber: r.waNumber,
      isPhoneSame: Boolean(r.isPhoneSame),
      phNumber: r.phNumber || '',
    }));

    // Load attendance
    const attendanceRows = await this.db.getAllAsync<any>(
      'SELECT * FROM attendance'
    );
    this.attendance = attendanceRows.map((r) => ({
      id: r.id,
      classId: r.classId,
      studentId: r.studentId,
      date: r.date,
      isPresent: Boolean(r.isPresent),
      recordedAt: Number(r.recordedAt),
    }));

    // Load settings
    const settingsRows = await this.db.getAllAsync<any>(
      'SELECT * FROM app_settings'
    );
    let section: SectionType = 'All';
    let biometric = false;
    let themePalette: MaterialYouPaletteKey = 'blue';
    settingsRows.forEach((r) => {
      if (r.key === 'selected_section') {
        section = (r.value as SectionType) || 'All';
      } else if (r.key === 'biometric_enabled') {
        biometric = r.value === '1';
      } else if (r.key === 'selected_theme_palette') {
        themePalette = (r.value as MaterialYouPaletteKey) || 'blue';
      }
    });

    this.settings = {
      selectedSection: section,
      biometricEnabled: biometric,
      selectedThemePalette: themePalette,
    };

    // Backup to AsyncStorage as shadow
    this.saveToAsyncStorage();
  }

  private async saveToAsyncStorage(): Promise<void> {
    try {
      const payload = JSON.stringify({
        classes: this.classes,
        students: this.students,
        attendance: this.attendance,
        settings: this.settings,
      });
      await AsyncStorage.setItem(ASYNC_BACKUP_KEY, payload);
    } catch (err) {
      console.warn('AsyncStorage backup error:', err);
    }
  }

  private async loadFromAsyncStorage(): Promise<void> {
    try {
      const data = await AsyncStorage.getItem(ASYNC_BACKUP_KEY);
      if (data) {
        const parsed = JSON.parse(data);
        this.classes = parsed.classes || [];
        this.students = parsed.students || [];
        this.attendance = parsed.attendance || [];
        this.settings = parsed.settings || { selectedSection: 'All', biometricEnabled: false, selectedThemePalette: 'blue' };
      }
    } catch (err) {
      console.warn('AsyncStorage read error:', err);
    }
  }

  // --- Classes CRUD ---

  public getClasses(): ClassEntity[] {
    return [...this.classes];
  }

  public async addClass(cls: ClassEntity): Promise<void> {
    this.classes.push(cls);
    if (this.db) {
      await this.db.runAsync(
        `INSERT OR REPLACE INTO classes (id, shortName, fullNameWithCode, lectureType, day, startTime, endTime, teacherName, location, creditHours, isHidden)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        [
          cls.id,
          cls.shortName,
          cls.fullNameWithCode,
          cls.lectureType,
          cls.day,
          cls.startTime,
          cls.endTime,
          cls.teacherName,
          cls.location,
          cls.creditHours,
          cls.isHidden ? 1 : 0,
        ]
      );
    }
    this.saveToAsyncStorage();
    this.notify();
  }

  public async upsertClasses(newClasses: ClassEntity[]): Promise<void> {
    for (const cls of newClasses) {
      const idx = this.classes.findIndex((c) => c.id === cls.id);
      if (idx >= 0) {
        this.classes[idx] = cls;
      } else {
        this.classes.push(cls);
      }
      if (this.db) {
        await this.db.runAsync(
          `INSERT OR REPLACE INTO classes (id, shortName, fullNameWithCode, lectureType, day, startTime, endTime, teacherName, location, creditHours, isHidden)
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
          [
            cls.id,
            cls.shortName,
            cls.fullNameWithCode,
            cls.lectureType,
            cls.day,
            cls.startTime,
            cls.endTime,
            cls.teacherName,
            cls.location,
            cls.creditHours,
            cls.isHidden ? 1 : 0,
          ]
        );
      }
    }
    this.saveToAsyncStorage();
    this.notify();
  }

  public async updateClass(cls: ClassEntity): Promise<void> {
    const idx = this.classes.findIndex((c) => c.id === cls.id);
    if (idx >= 0) {
      this.classes[idx] = cls;
      if (this.db) {
        await this.db.runAsync(
          `UPDATE classes SET shortName=?, fullNameWithCode=?, lectureType=?, day=?, startTime=?, endTime=?, teacherName=?, location=?, creditHours=?, isHidden=?
           WHERE id=?`,
          [
            cls.shortName,
            cls.fullNameWithCode,
            cls.lectureType,
            cls.day,
            cls.startTime,
            cls.endTime,
            cls.teacherName,
            cls.location,
            cls.creditHours,
            cls.isHidden ? 1 : 0,
            cls.id,
          ]
        );
      }
      this.saveToAsyncStorage();
      this.notify();
    }
  }

  public async toggleClassHidden(id: string): Promise<void> {
    const cls = this.classes.find((c) => c.id === id);
    if (cls) {
      cls.isHidden = !cls.isHidden;
      if (this.db) {
        await this.db.runAsync(
          `UPDATE classes SET isHidden=? WHERE id=?`,
          [cls.isHidden ? 1 : 0, cls.id]
        );
      }
      this.saveToAsyncStorage();
      this.notify();
    }
  }

  public async deleteClass(id: string): Promise<void> {
    this.classes = this.classes.filter((c) => c.id !== id);
    // Cascade delete attendance
    this.attendance = this.attendance.filter((a) => a.classId !== id);

    if (this.db) {
      await this.db.runAsync(`DELETE FROM attendance WHERE classId=?`, [id]);
      await this.db.runAsync(`DELETE FROM classes WHERE id=?`, [id]);
    }
    this.saveToAsyncStorage();
    this.notify();
  }

  // --- Students CRUD ---

  public getStudents(): StudentEntity[] {
    return [...this.students].sort((a, b) => a.orderIndex - b.orderIndex);
  }

  public async addStudent(student: StudentEntity): Promise<void> {
    this.students.push(student);
    if (this.db) {
      await this.db.runAsync(
        `INSERT OR REPLACE INTO students (id, orderIndex, name, cnic, regNo, section, waNumber, isPhoneSame, phNumber)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        [
          student.id,
          student.orderIndex,
          student.name,
          student.cnic || null,
          student.regNo,
          student.section,
          student.waNumber,
          student.isPhoneSame ? 1 : 0,
          student.phNumber || null,
        ]
      );
    }
    this.saveToAsyncStorage();
    this.notify();
  }

  public async upsertStudents(newStudents: StudentEntity[]): Promise<void> {
    for (const student of newStudents) {
      const idx = this.students.findIndex((s) => s.id === student.id);
      if (idx >= 0) {
        this.students[idx] = student;
      } else {
        this.students.push(student);
      }
      if (this.db) {
        await this.db.runAsync(
          `INSERT OR REPLACE INTO students (id, orderIndex, name, cnic, regNo, section, waNumber, isPhoneSame, phNumber)
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
          [
            student.id,
            student.orderIndex,
            student.name,
            student.cnic || null,
            student.regNo,
            student.section,
            student.waNumber,
            student.isPhoneSame ? 1 : 0,
            student.phNumber || null,
          ]
        );
      }
    }
    this.saveToAsyncStorage();
    this.notify();
  }

  public async updateStudent(student: StudentEntity): Promise<void> {
    const idx = this.students.findIndex((s) => s.id === student.id);
    if (idx >= 0) {
      this.students[idx] = student;
      if (this.db) {
        await this.db.runAsync(
          `UPDATE students SET name=?, cnic=?, regNo=?, section=?, waNumber=?, isPhoneSame=?, phNumber=?
           WHERE id=?`,
          [
            student.name,
            student.cnic || null,
            student.regNo,
            student.section,
            student.waNumber,
            student.isPhoneSame ? 1 : 0,
            student.phNumber || null,
            student.id,
          ]
        );
      }
      this.saveToAsyncStorage();
      this.notify();
    }
  }

  public async deleteStudent(id: string): Promise<void> {
    this.students = this.students.filter((s) => s.id !== id);
    // Cascade delete attendance
    this.attendance = this.attendance.filter((a) => a.studentId !== id);

    // Re-index remaining students
    this.students.sort((a, b) => a.orderIndex - b.orderIndex);
    this.students.forEach((s, index) => {
      s.orderIndex = index;
    });

    if (this.db) {
      await this.db.runAsync(`DELETE FROM attendance WHERE studentId=?`, [id]);
      await this.db.runAsync(`DELETE FROM students WHERE id=?`, [id]);
      for (const s of this.students) {
        await this.db.runAsync(`UPDATE students SET orderIndex=? WHERE id=?`, [s.orderIndex, s.id]);
      }
    }
    this.saveToAsyncStorage();
    this.notify();
  }

  public async reorderStudents(reorderedStudents: StudentEntity[]): Promise<void> {
    reorderedStudents.forEach((student, index) => {
      student.orderIndex = index;
      const original = this.students.find((s) => s.id === student.id);
      if (original) {
        original.orderIndex = index;
      }
    });

    if (this.db) {
      for (const s of reorderedStudents) {
        await this.db.runAsync(`UPDATE students SET orderIndex=? WHERE id=?`, [s.orderIndex, s.id]);
      }
    }
    this.saveToAsyncStorage();
    this.notify();
  }

  // --- Attendance CRUD ---

  public getAttendance(): AttendanceRecord[] {
    return [...this.attendance];
  }

  public getAttendanceForClassAndDate(classId: string, date: string): Map<string, boolean> {
    const map = new Map<string, boolean>();
    this.attendance
      .filter((a) => a.classId === classId && a.date === date)
      .forEach((a) => {
        map.set(a.studentId, a.isPresent);
      });
    return map;
  }

  public async toggleAttendance(classId: string, studentId: string, date: string, isPresent: boolean): Promise<void> {
    const existingIndex = this.attendance.findIndex(
      (a) => a.classId === classId && a.studentId === studentId && a.date === date
    );

    const now = Date.now();
    const recordId =
      existingIndex >= 0
        ? this.attendance[existingIndex].id
        : `att_${classId}_${studentId}_${date}`;

    const newRecord: AttendanceRecord = {
      id: recordId,
      classId,
      studentId,
      date,
      isPresent,
      recordedAt: now,
    };

    if (existingIndex >= 0) {
      this.attendance[existingIndex] = newRecord;
    } else {
      this.attendance.push(newRecord);
    }

    if (this.db) {
      await this.db.runAsync(
        `INSERT OR REPLACE INTO attendance (id, classId, studentId, date, isPresent, recordedAt)
         VALUES (?, ?, ?, ?, ?, ?)`,
        [recordId, classId, studentId, date, isPresent ? 1 : 0, now]
      );
    }

    this.saveToAsyncStorage();
    this.notify();
  }

  // --- Settings ---

  public getSettings(): AppSettings {
    return { ...this.settings };
  }

  public async updateSettings(newSettings: Partial<AppSettings>): Promise<void> {
    this.settings = {
      ...this.settings,
      ...newSettings,
    };

    if (this.db) {
      if (newSettings.selectedSection !== undefined) {
        await this.db.runAsync(
          `INSERT OR REPLACE INTO app_settings (key, value) VALUES ('selected_section', ?)`,
          [this.settings.selectedSection]
        );
      }
      if (newSettings.biometricEnabled !== undefined) {
        await this.db.runAsync(
          `INSERT OR REPLACE INTO app_settings (key, value) VALUES ('biometric_enabled', ?)`,
          [this.settings.biometricEnabled ? '1' : '0']
        );
      }
      if (newSettings.selectedThemePalette !== undefined) {
        await this.db.runAsync(
          `INSERT OR REPLACE INTO app_settings (key, value) VALUES ('selected_theme_palette', ?)`,
          [this.settings.selectedThemePalette]
        );
      }
    }

    this.saveToAsyncStorage();
    this.notify();
  }
}

export const dbService = new DatabaseService();

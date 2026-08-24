import * as FileSystem from 'expo-file-system/legacy';
import * as DocumentPicker from 'expo-document-picker';
import * as Sharing from 'expo-sharing';
import { StudentEntity, ClassEntity, LectureType, Weekday } from '../types';
import { formatDayMonthYear, parseTimeTo24h, formatTime12h, WEEKDAYS } from './dateUtils';
import { showShortToast, showLongToast } from './toast';
import { validateStudent, validateClass } from './validation';

let isPickerActive = false;

export const STUDENT_SAMPLE_DATA = [
  {
    Name: 'Muhammad Ali',
    CNIC: '3520112345671',
    'Reg No': '25-CS-001',
    Section: 'A',
    'WA Number': '+923001234567',
    'Ph Number': '+923001234567',
  },
  {
    Name: 'Ayesha Khan',
    CNIC: '3520298765432',
    'Reg No': '25-CS-002',
    Section: 'B',
    'WA Number': '+923217654321',
    'Ph Number': '',
  },
];

export const CLASS_SAMPLE_DATA = [
  {
    'Short Name': 'CP',
    'Full Name': 'CS-101 Computer Programming',
    'Lecture Type': 'Lecture',
    Day: 'Monday',
    'Start Time': '09:00 AM',
    'End Time': '10:00 AM',
    'Teacher Name': 'Dr. Tariq',
    Location: 'Lab 1',
    'Credit Hours': 3,
  },
  {
    'Short Name': 'DSA',
    'Full Name': 'CS-201 Data Structures',
    'Lecture Type': 'Practical Lab',
    Day: 'Wednesday',
    'Start Time': '11:00 AM',
    'End Time': '01:00 PM',
    'Teacher Name': 'Prof. Ahmed',
    Location: 'Lab 3',
    'Credit Hours': 4,
  },
];

/**
 * Save sample students JSON
 */
export async function downloadStudentSample(): Promise<void> {
  try {
    const fileName = 'students_sample.json';
    const jsonStr = JSON.stringify(STUDENT_SAMPLE_DATA, null, 2);
    const dir = FileSystem.cacheDirectory || FileSystem.documentDirectory;
    const filePath = `${dir}${fileName}`;
    await FileSystem.writeAsStringAsync(filePath, jsonStr, {
      encoding: FileSystem.EncodingType.UTF8,
    });

    if (await Sharing.isAvailableAsync()) {
      await Sharing.shareAsync(filePath, {
        mimeType: 'application/json',
        dialogTitle: `Save ${fileName}`,
      });
    }
    showShortToast(`Sample saved: ${fileName}`);
  } catch (err: any) {
    showLongToast(`Failed to save sample: ${err?.message || 'Unknown error'}`);
  }
}

/**
 * Save sample classes JSON
 */
export async function downloadClassSample(): Promise<void> {
  try {
    const fileName = 'classes_sample.json';
    const jsonStr = JSON.stringify(CLASS_SAMPLE_DATA, null, 2);
    const dir = FileSystem.cacheDirectory || FileSystem.documentDirectory;
    const filePath = `${dir}${fileName}`;
    await FileSystem.writeAsStringAsync(filePath, jsonStr, {
      encoding: FileSystem.EncodingType.UTF8,
    });

    if (await Sharing.isAvailableAsync()) {
      await Sharing.shareAsync(filePath, {
        mimeType: 'application/json',
        dialogTitle: `Save ${fileName}`,
      });
    }
    showShortToast(`Sample saved: ${fileName}`);
  } catch (err: any) {
    showLongToast(`Failed to save sample: ${err?.message || 'Unknown error'}`);
  }
}

/**
 * Export students to students_{dd-MM-yyyy}.json
 */
export async function exportStudentsJSON(students: StudentEntity[]): Promise<void> {
  try {
    const dateStr = formatDayMonthYear(new Date());
    const fileName = `students_${dateStr}.json`;

    const exportArray = students.map((s) => ({
      Name: s.name,
      CNIC: s.cnic || '',
      'Reg No': s.regNo,
      Section: s.section,
      'WA Number': s.waNumber,
      'Ph Number': s.isPhoneSame ? '' : s.phNumber,
    }));

    const jsonStr = JSON.stringify(exportArray, null, 2);
    const dir = FileSystem.cacheDirectory || FileSystem.documentDirectory;
    const filePath = `${dir}${fileName}`;
    await FileSystem.writeAsStringAsync(filePath, jsonStr, {
      encoding: FileSystem.EncodingType.UTF8,
    });

    if (await Sharing.isAvailableAsync()) {
      await Sharing.shareAsync(filePath, {
        mimeType: 'application/json',
        dialogTitle: `Save ${fileName}`,
      });
    }
    showShortToast(`Exported ${fileName}`);
  } catch (err: any) {
    showLongToast(`Export failed: ${err?.message || 'Unknown error'}`);
  }
}

/**
 * Export classes to classes_{dd-MM-yyyy}.json
 */
export async function exportClassesJSON(classes: ClassEntity[]): Promise<void> {
  try {
    const dateStr = formatDayMonthYear(new Date());
    const fileName = `classes_${dateStr}.json`;

    const exportArray = classes.map((c) => ({
      'Short Name': c.shortName,
      'Full Name': c.fullNameWithCode,
      'Lecture Type': c.lectureType,
      Day: c.day,
      'Start Time': formatTime12h(c.startTime),
      'End Time': formatTime12h(c.endTime),
      'Teacher Name': c.teacherName,
      Location: c.location,
      'Credit Hours': c.creditHours,
    }));

    const jsonStr = JSON.stringify(exportArray, null, 2);
    const dir = FileSystem.cacheDirectory || FileSystem.documentDirectory;
    const filePath = `${dir}${fileName}`;
    await FileSystem.writeAsStringAsync(filePath, jsonStr, {
      encoding: FileSystem.EncodingType.UTF8,
    });

    if (await Sharing.isAvailableAsync()) {
      await Sharing.shareAsync(filePath, {
        mimeType: 'application/json',
        dialogTitle: `Save ${fileName}`,
      });
    }
    showShortToast(`Exported ${fileName}`);
  } catch (err: any) {
    showLongToast(`Export failed: ${err?.message || 'Unknown error'}`);
  }
}

/**
 * Import students from JSON file
 */
export async function importStudentsJSON(
  currentStudents: StudentEntity[],
  onUpsertStudents: (validStudents: StudentEntity[]) => Promise<void>
): Promise<void> {
  if (isPickerActive) return;
  isPickerActive = true;

  try {
    const res = await DocumentPicker.getDocumentAsync({
      type: ['application/json', 'text/plain', '*/*'],
      copyToCacheDirectory: true,
    });

    if (res.canceled || !res.assets || res.assets.length === 0) {
      return;
    }

    const fileUri = res.assets[0].uri;
    const content = await FileSystem.readAsStringAsync(fileUri, {
      encoding: FileSystem.EncodingType.UTF8,
    });

    let rawData: any;
    try {
      rawData = JSON.parse(content);
    } catch {
      showLongToast('Invalid JSON file format');
      return;
    }

    if (!Array.isArray(rawData)) {
      showLongToast('JSON must be an array of student objects');
      return;
    }

    const validStudents: StudentEntity[] = [];
    const errors: string[] = [];
    let maxOrder = currentStudents.reduce((max, s) => Math.max(max, s.orderIndex), -1);

    // Copy existing students list to track uniqueness within import
    const workingList = [...currentStudents];

    for (let i = 0; i < rawData.length; i++) {
      const row = rawData[i];
      const rowNum = i + 1;

      const name = String(row.Name || row.name || '').trim();
      const cnic = String(row.CNIC || row.cnic || '').replace(/\D/g, '');
      const regNo = String(row['Reg No'] || row.regNo || row.RegNo || '').trim();
      const section = String(row.Section || row.section || '').trim().toUpperCase();
      const waNumber = String(row['WA Number'] || row.waNumber || row.WANumber || '').trim();
      const phNumber = String(row['Ph Number'] || row.phNumber || row.PhNumber || '').trim();

      const isPhoneSame = !phNumber;
      const effectivePhone = isPhoneSame ? waNumber : phNumber;

      // Validate
      const existing = workingList.find((s) => s.regNo === regNo);
      const validation = validateStudent(
        {
          name,
          cnic,
          regNo,
          section,
          waNumber,
          isPhoneSame,
          phNumber: effectivePhone,
        },
        workingList,
        existing?.id
      );

      if (!validation.isValid) {
        errors.push(`Row ${rowNum}: ${validation.errorMessage}`);
        continue;
      }

      if (existing) {
        // Upsert existing
        const updated: StudentEntity = {
          ...existing,
          name,
          cnic,
          regNo,
          section,
          waNumber,
          isPhoneSame,
          phNumber: effectivePhone,
        };
        validStudents.push(updated);
        const idx = workingList.findIndex((s) => s.id === existing.id);
        workingList[idx] = updated;
      } else {
        // Add new
        maxOrder++;
        const newStudent: StudentEntity = {
          id: `student_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`,
          orderIndex: maxOrder,
          name,
          cnic,
          regNo,
          section,
          waNumber,
          isPhoneSame,
          phNumber: effectivePhone,
        };
        validStudents.push(newStudent);
        workingList.push(newStudent);
      }
    }

    if (validStudents.length > 0) {
      await onUpsertStudents(validStudents);
    }

    let toastMsg = `Imported ${validStudents.length} students`;
    if (errors.length > 0) {
      toastMsg += `, ${errors.length} error(s):\n` + errors.slice(0, 3).join('\n');
      showLongToast(toastMsg);
    } else {
      showShortToast(toastMsg);
    }
  } catch (err: any) {
    showLongToast(`Import failed: ${err?.message || 'Unknown error'}`);
  } finally {
    isPickerActive = false;
  }
}

/**
 * Import classes from JSON file
 */
export async function importClassesJSON(
  currentClasses: ClassEntity[],
  onUpsertClasses: (validClasses: ClassEntity[]) => Promise<void>
): Promise<void> {
  if (isPickerActive) return;
  isPickerActive = true;

  try {
    const res = await DocumentPicker.getDocumentAsync({
      type: ['application/json', 'text/plain', '*/*'],
      copyToCacheDirectory: true,
    });

    if (res.canceled || !res.assets || res.assets.length === 0) {
      return;
    }

    const fileUri = res.assets[0].uri;
    const content = await FileSystem.readAsStringAsync(fileUri, {
      encoding: FileSystem.EncodingType.UTF8,
    });

    let rawData: any;
    try {
      rawData = JSON.parse(content);
    } catch {
      showLongToast('Invalid JSON file format');
      return;
    }

    if (!Array.isArray(rawData)) {
      showLongToast('JSON must be an array of class objects');
      return;
    }

    const validClasses: ClassEntity[] = [];
    const errors: string[] = [];
    const workingClasses = [...currentClasses];

    for (let i = 0; i < rawData.length; i++) {
      const row = rawData[i];
      const rowNum = i + 1;

      const shortName = String(row['Short Name'] || row.shortName || '').trim();
      const fullNameWithCode = String(row['Full Name'] || row.fullNameWithCode || '').trim();
      
      let lectureTypeRaw = String(row['Lecture Type'] || row.lectureType || 'Lecture').trim();
      const standardTypes: LectureType[] = ['Lecture', 'Tutorial', 'Practical Lab', 'Workshop', 'Seminar', 'Other'];
      const matchedType = standardTypes.find(
        (t) => t.toLowerCase() === lectureTypeRaw.toLowerCase()
      ) || 'Lecture';

      let dayRaw = String(row.Day || row.day || '').trim();
      const matchedDay = WEEKDAYS.find(
        (d) => d.toLowerCase() === dayRaw.toLowerCase()
      );

      const startTime24 = parseTimeTo24h(String(row['Start Time'] || row.startTime || '09:00'));
      const endTime24 = parseTimeTo24h(String(row['End Time'] || row.endTime || '10:00'));

      const teacherName = String(row['Teacher Name'] || row.teacherName || '').trim();
      const location = String(row.Location || row.location || '').trim();
      
      const creditHoursParsed = parseInt(String(row['Credit Hours'] || row.creditHours || 3), 10);
      const creditHours = isNaN(creditHoursParsed) ? 3 : creditHoursParsed;

      const validation = validateClass({
        shortName,
        fullNameWithCode,
        lectureType: matchedType,
        day: matchedDay,
        startTime: startTime24,
        endTime: endTime24,
        teacherName,
        location,
        creditHours,
      });

      if (!validation.isValid) {
        errors.push(`Row ${rowNum}: ${validation.errorMessage}`);
        continue;
      }

      // Check if existing class by id or exact match
      const existing = workingClasses.find(
        (c) => c.shortName === shortName && c.day === matchedDay && c.startTime === startTime24
      );

      if (existing) {
        const updated: ClassEntity = {
          ...existing,
          shortName,
          fullNameWithCode,
          lectureType: matchedType,
          day: matchedDay as Weekday,
          startTime: startTime24,
          endTime: endTime24,
          teacherName,
          location,
          creditHours,
        };
        validClasses.push(updated);
        const idx = workingClasses.findIndex((c) => c.id === existing.id);
        workingClasses[idx] = updated;
      } else {
        const newClass: ClassEntity = {
          id: `class_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`,
          shortName,
          fullNameWithCode,
          lectureType: matchedType,
          day: matchedDay as Weekday,
          startTime: startTime24,
          endTime: endTime24,
          teacherName,
          location,
          creditHours,
          isHidden: false,
        };
        validClasses.push(newClass);
        workingClasses.push(newClass);
      }
    }

    if (validClasses.length > 0) {
      await onUpsertClasses(validClasses);
    }

    let toastMsg = `Imported ${validClasses.length} classes`;
    if (errors.length > 0) {
      toastMsg += `, ${errors.length} error(s):\n` + errors.slice(0, 3).join('\n');
      showLongToast(toastMsg);
    } else {
      showShortToast(toastMsg);
    }
  } catch (err: any) {
    showLongToast(`Import failed: ${err?.message || 'Unknown error'}`);
  } finally {
    isPickerActive = false;
  }
}

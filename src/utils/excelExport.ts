import * as XLSX from 'xlsx';
import * as FileSystem from 'expo-file-system/legacy';
import * as Sharing from 'expo-sharing';
import { ClassEntity, StudentEntity, AttendanceRecord, SectionType } from '../types';
import { formatDayMonthShort, formatDayMonthYear, formatTimeHHmm, parseDateISO } from './dateUtils';
import { showShortToast, showLongToast } from './toast';

export interface ExportResult {
  success: boolean;
  filePath?: string;
  fileName?: string;
  error?: string;
}

/**
 * Builds deterministic filename according to 3.4:
 * {code-or-shortName}_{plain-name-sanitized}_{HHmm}_{dd-MM-yyyy}.xlsx
 */
export function generateExcelFilename(cls: ClassEntity, now: Date = new Date()): string {
  // Extract code if present in fullNameWithCode (e.g. "CS-101 Computer Programming" -> "CS-101")
  // Or fallback to shortName
  let code = cls.shortName;
  const matchCode = cls.fullNameWithCode.match(/^([A-Za-z0-9-]+)\s+(.+)$/);
  let namePart = cls.fullNameWithCode;
  if (matchCode) {
    code = matchCode[1];
    namePart = matchCode[2];
  }

  // Plain name strips non-alphanumerics except space-dot-underscore-hyphen and spaces become hyphens
  const sanitizedName = namePart
    .replace(/[^A-Za-z0-9 ._-]/g, '')
    .trim()
    .replace(/\s+/g, '-');

  const timeStr = formatTimeHHmm(now);
  const dateStr = formatDayMonthYear(now);

  return `${code}_${sanitizedName}_${timeStr}_${dateStr}.xlsx`;
}

/**
 * Generates the .xlsx file bytes in base64
 */
export function generateExcelWorkbookBase64(
  cls: ClassEntity,
  students: StudentEntity[],
  allAttendance: AttendanceRecord[],
  selectedSection: SectionType
): { base64: string; fileName: string } {
  // 1. Filter students by section if not 'All', preserving global drag order
  const filteredStudents =
    selectedSection === 'All'
      ? students
      : students.filter((s) => s.section.toUpperCase() === selectedSection.toUpperCase());

  // 2. Find distinct recorded dates for this class sorted ascending
  const classAttendance = allAttendance.filter((a) => a.classId === cls.id);
  const dateSet = new Set<string>();
  classAttendance.forEach((a) => dateSet.add(a.date));
  const distinctDates = Array.from(dateSet).sort(); // YYYY-MM-DD sorted

  // 3. Build Header Row
  // Sr#, Name, Reg No, Section, then one column per DISTINCT recorded date sorted ascending headed d-MMM, then Total P, Total A, %
  const headerRow: string[] = ['Sr#', 'Name', 'Reg No', 'Section'];
  distinctDates.forEach((dateStr) => {
    headerRow.push(formatDayMonthShort(parseDateISO(dateStr)));
  });
  headerRow.push('Total P', 'Total A', '%');

  // Column widths: 5 / 28 / 14 / 9 / 9 per date / 9 / 9 / 7
  const colWidths = [
    { wch: 5 },
    { wch: 28 },
    { wch: 14 },
    { wch: 9 },
    ...distinctDates.map(() => ({ wch: 9 })),
    { wch: 9 },
    { wch: 9 },
    { wch: 7 },
  ];

  // 4. Build Data Rows
  const rows: any[][] = [headerRow];

  filteredStudents.forEach((student, index) => {
    const srNo = index + 1;
    const row: any[] = [srNo, student.name, student.regNo, student.section];

    let totalP = 0;
    let totalA = 0;

    distinctDates.forEach((dateStr) => {
      const record = classAttendance.find(
        (a) => a.studentId === student.id && a.date === dateStr
      );
      if (record && record.isPresent) {
        row.push('P');
        totalP++;
      } else {
        row.push(''); // blank
        totalA++;
      }
    });

    const totalDates = distinctDates.length;
    const percentage = totalDates > 0 ? ((totalP / totalDates) * 100).toFixed(1) + '%' : '0.0%';

    row.push(totalP);
    row.push(totalA);
    row.push(percentage);

    rows.push(row);
  });

  // Create worksheet
  const ws = XLSX.utils.aoa_to_sheet(rows);
  ws['!cols'] = colWidths;

  // Single worksheet named with class short name (truncated to 31 chars)
  const sheetName = (cls.shortName || 'Attendance').substring(0, 31);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, sheetName);

  const base64 = XLSX.write(wb, { type: 'base64', bookType: 'xlsx' });
  const fileName = generateExcelFilename(cls);

  return { base64, fileName };
}

/**
 * Handles the Export flow:
 * Writes .xlsx to cache/documents directory and invokes Storage / FileSystem
 */
export async function exportAttendanceExcel(
  cls: ClassEntity,
  students: StudentEntity[],
  allAttendance: AttendanceRecord[],
  selectedSection: SectionType
): Promise<ExportResult> {
  try {
    if (!cls) {
      showLongToast('Selected class no longer exists');
      return { success: false, error: 'Selected class no longer exists' };
    }

    const { base64, fileName } = generateExcelWorkbookBase64(
      cls,
      students,
      allAttendance,
      selectedSection
    );

    const docDir = FileSystem.documentDirectory || FileSystem.cacheDirectory;
    const filePath = `${docDir}${fileName}`;

    await FileSystem.writeAsStringAsync(filePath, base64, {
      encoding: FileSystem.EncodingType.Base64,
    });

    // Check if sharing is available to let user save/open or download
    if (await Sharing.isAvailableAsync()) {
      await Sharing.shareAsync(filePath, {
        mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        dialogTitle: `Save ${fileName}`,
        UTI: 'com.microsoft.excel.xlsx',
      });
    }

    showShortToast(`Exported ${fileName}`);
    return { success: true, filePath, fileName };
  } catch (error: any) {
    const errorMsg = error?.message || 'Could not open the file for writing';
    showLongToast(`Export failed: ${errorMsg}`);
    return { success: false, error: errorMsg };
  }
}

/**
 * Handles the Share flow:
 * Builds same .xlsx -> system share sheet titled "Share Attendance" with file attached from cache.
 */
export async function shareAttendanceExcel(
  cls: ClassEntity,
  students: StudentEntity[],
  allAttendance: AttendanceRecord[],
  selectedSection: SectionType
): Promise<ExportResult> {
  try {
    if (!cls) {
      showLongToast('Selected class no longer exists');
      return { success: false, error: 'Selected class no longer exists' };
    }

    const { base64, fileName } = generateExcelWorkbookBase64(
      cls,
      students,
      allAttendance,
      selectedSection
    );

    const cacheDir = FileSystem.cacheDirectory || FileSystem.documentDirectory;
    const filePath = `${cacheDir}${fileName}`;

    await FileSystem.writeAsStringAsync(filePath, base64, {
      encoding: FileSystem.EncodingType.Base64,
    });

    if (await Sharing.isAvailableAsync()) {
      await Sharing.shareAsync(filePath, {
        mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        dialogTitle: 'Share Attendance',
        UTI: 'com.microsoft.excel.xlsx',
      });
      return { success: true, filePath, fileName };
    } else {
      showLongToast('Sharing is not available on this device');
      return { success: false, error: 'Sharing unavailable' };
    }
  } catch (error: any) {
    const errorMsg = error?.message || 'Unknown error';
    showLongToast(`Export failed: ${errorMsg}`);
    return { success: false, error: errorMsg };
  }
}

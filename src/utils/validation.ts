import { StudentEntity, ClassEntity, LectureType, Weekday } from '../types';

export interface ValidationResult {
  isValid: boolean;
  errorMessage?: string;
}

/**
 * Validates student form data
 * - Name: required
 * - Registration Number: required, unique
 * - Section: required (A-F)
 * - WhatsApp Number: OPTIONAL (if provided, must be Pakistani format)
 * - Phone Number: OPTIONAL (if provided and different, must be valid)
 * - CNIC: OPTIONAL (if provided, 13 digits)
 */
export function validateStudent(
  data: {
    name: string;
    cnic?: string;
    regNo: string;
    section: string;
    waNumber?: string;
    isPhoneSame: boolean;
    phNumber?: string;
  },
  existingStudents: StudentEntity[],
  currentStudentId?: string
): ValidationResult {
  const name = (data.name || '').trim();
  if (!name) {
    return { isValid: false, errorMessage: 'Name is required' };
  }

  const regNo = (data.regNo || '').trim();
  if (!regNo) {
    return { isValid: false, errorMessage: 'Registration number is required' };
  }

  // Globally unique (case-sensitive compare against other students)
  const isDuplicateReg = existingStudents.some(
    (s) => s.id !== currentStudentId && s.regNo.toLowerCase() === regNo.toLowerCase()
  );
  if (isDuplicateReg) {
    return { isValid: false, errorMessage: 'Registration number already exists' };
  }

  const section = (data.section || '').trim().toUpperCase();
  if (!['A', 'B', 'C', 'D', 'E', 'F'].includes(section)) {
    return { isValid: false, errorMessage: 'Section must be between A and F' };
  }

  // WhatsApp Number is now OPTIONAL
  const rawWa = (data.waNumber || '').trim();
  const cleanedWa = rawWa.replace(/[^\d+]/g, '');
  const digitsOnlyWa = cleanedWa.replace(/\+/g, '');

  if (cleanedWa.length > 0 && cleanedWa !== '+92' && cleanedWa !== '+') {
    const hasValidDigits = digitsOnlyWa.length >= 10 && digitsOnlyWa.length <= 13;
    const isPakistaniFormat =
      cleanedWa.startsWith('+92') ||
      cleanedWa.startsWith('92') ||
      cleanedWa.startsWith('03') ||
      cleanedWa.startsWith('0092') ||
      cleanedWa.startsWith('+03');

    if (!hasValidDigits || !isPakistaniFormat) {
      return {
        isValid: false,
        errorMessage: 'WhatsApp number must be in Pakistani format (+92 or 03) with 10-13 digits',
      };
    }
  }

  // Phone number is now OPTIONAL
  if (!data.isPhoneSame) {
    const phNumber = (data.phNumber || '').trim();
    if (phNumber.length > 0 && phNumber !== '+92' && phNumber !== '+') {
      const cleanedPh = phNumber.replace(/[^\d+]/g, '');
      const digitsOnlyPh = cleanedPh.replace(/\+/g, '');
      const hasValidPhDigits = digitsOnlyPh.length >= 10 && digitsOnlyPh.length <= 13;
      if (!hasValidPhDigits) {
        return {
          isValid: false,
          errorMessage: 'Phone number must have 10-13 digits',
        };
      }
    }
  }

  // CNIC is OPTIONAL
  if (data.cnic) {
    const cnicDigits = data.cnic.replace(/\D/g, '');
    if (cnicDigits.length > 0 && cnicDigits.length !== 13) {
      return { isValid: false, errorMessage: 'CNIC must be 13 digits' };
    }
  }

  return { isValid: true };
}

/**
 * Validates class form data
 * - Short Name: required (<= 10 chars)
 * - Full Name with Code: required (<= 100 chars)
 * - Lecture Type: required
 * - Day: required
 * - Start Time / End Time: required (start < end)
 * - Credit Hours: required (1-5)
 * - Teacher Name: OPTIONAL
 * - Location: OPTIONAL
 */
export function validateClass(
  data: {
    shortName: string;
    fullNameWithCode: string;
    lectureType?: LectureType | string;
    day?: Weekday | string;
    startTime: string; // "HH:mm"
    endTime: string;   // "HH:mm"
    teacherName?: string;
    location?: string;
    creditHours?: number;
  }
): ValidationResult {
  const shortName = (data.shortName || '').trim();
  if (!shortName) {
    return { isValid: false, errorMessage: 'Short name is required' };
  }
  if (shortName.length > 10) {
    return { isValid: false, errorMessage: 'Short name must be 10 characters or fewer' };
  }

  const fullNameWithCode = (data.fullNameWithCode || '').trim();
  if (!fullNameWithCode) {
    return { isValid: false, errorMessage: 'Full name with code is required' };
  }
  if (fullNameWithCode.length > 100) {
    return { isValid: false, errorMessage: 'Full name must be 100 characters or fewer' };
  }

  if (!data.lectureType) {
    return { isValid: false, errorMessage: 'Select a lecture type' };
  }

  if (!data.day) {
    return { isValid: false, errorMessage: 'Select a day' };
  }

  // start strictly before end
  if (data.startTime >= data.endTime) {
    return { isValid: false, errorMessage: 'Start time must be before end time' };
  }

  const creditHours = data.creditHours;
  if (!creditHours || creditHours < 1 || creditHours > 5) {
    return { isValid: false, errorMessage: 'Credit hours must be between 1 and 5' };
  }

  // teacherName and location are OPTIONAL now!

  return { isValid: true };
}

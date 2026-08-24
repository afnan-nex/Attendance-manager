import { Weekday } from '../types';

export const WEEKDAYS: Weekday[] = [
  'Monday',
  'Tuesday',
  'Wednesday',
  'Thursday',
  'Friday',
  'Saturday',
  'Sunday',
];

export const WEEKDAY_SHORT = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export const MONTH_NAMES = [
  'January',
  'February',
  'March',
  'April',
  'May',
  'June',
  'July',
  'August',
  'September',
  'October',
  'November',
  'December',
];

export const MONTH_SHORT = [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
];

export interface DayChipData {
  index: number;
  date: Date;
  dateString: string; // YYYY-MM-DD
  dayOfWeekShort: string; // e.g. "Mon"
  dayOfMonth: number; // e.g. 24
  weekdayName: Weekday; // e.g. "Monday"
  isToday: boolean;
}

/** Formats a Date object to YYYY-MM-DD */
export function formatDateISO(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

/** Parse YYYY-MM-DD to Date at midnight */
export function parseDateISO(iso: string): Date {
  const parts = iso.split('-');
  return new Date(parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10));
}

/** Formats a date into {EEEE, d MMMM yyyy} (e.g. "Monday, 24 August 2026") */
export function formatLongDate(date: Date): string {
  const dayName = WEEKDAYS[(date.getDay() + 6) % 7];
  const dayNum = date.getDate();
  const monthName = MONTH_NAMES[date.getMonth()];
  const year = date.getFullYear();
  return `${dayName}, ${dayNum} ${monthName} ${year}`;
}

/** Formats a date into d-MMM (e.g. "15-Jan", "24-Aug") */
export function formatDayMonthShort(date: Date): string {
  const dayNum = date.getDate();
  const monthShort = MONTH_SHORT[date.getMonth()];
  return `${dayNum}-${monthShort}`;
}

/** Formats a date into dd-MM-yyyy */
export function formatDayMonthYear(date: Date): string {
  const d = String(date.getDate()).padStart(2, '0');
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const y = date.getFullYear();
  return `${d}-${m}-${y}`;
}

/** Formats current time into HHmm */
export function formatTimeHHmm(date: Date = new Date()): string {
  const h = String(date.getHours()).padStart(2, '0');
  const m = String(date.getMinutes()).padStart(2, '0');
  return `${h}${m}`;
}

/** Get Weekday name ('Monday'..'Sunday') from a Date */
export function getWeekdayName(date: Date): Weekday {
  const day = date.getDay(); // 0 is Sunday, 1 is Monday ...
  return WEEKDAYS[(day + 6) % 7];
}

/** Compare date with today: -1 past, 0 today, 1 future */
export function compareWithToday(dateString: string): number {
  const todayStr = formatDateISO(new Date());
  if (dateString < todayStr) return -1;
  if (dateString > todayStr) return 1;
  return 0;
}

/** Converts "HH:mm" (24h) to "h:mm AM/PM" (12h) */
export function formatTime12h(time24: string): string {
  if (!time24) return '';
  const [hStr, mStr] = time24.split(':');
  let h = parseInt(hStr, 10);
  const m = parseInt(mStr || '0', 10);
  if (isNaN(h)) h = 9;
  const ampm = h >= 12 ? 'PM' : 'AM';
  let h12 = h % 12;
  if (h12 === 0) h12 = 12;
  const minStr = String(isNaN(m) ? 0 : m).padStart(2, '0');
  return `${h12}:${minStr} ${ampm}`;
}

/** Parse any time string ("9:00 AM", "09:00", etc.) to "HH:mm" */
export function parseTimeTo24h(timeStr: string): string {
  if (!timeStr) return '09:00';
  const trimmed = timeStr.trim().toUpperCase();
  const ampmMatch = trimmed.match(/^(\d{1,2}):(\d{2})\s*(AM|PM)?$/);
  if (ampmMatch) {
    let h = parseInt(ampmMatch[1], 10);
    const m = parseInt(ampmMatch[2], 10);
    const ampm = ampmMatch[3];
    if (ampm === 'PM' && h < 12) h += 12;
    if (ampm === 'AM' && h === 12) h = 0;
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
  }
  // If only HH:mm
  const simpleMatch = trimmed.match(/^(\d{1,2}):(\d{2})$/);
  if (simpleMatch) {
    const h = parseInt(simpleMatch[1], 10);
    const m = parseInt(simpleMatch[2], 10);
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
  }
  return '09:00';
}

/** Generate 731 day chips covering today - 365 to today + 365 */
export function generateDateStripData(): { chips: DayChipData[]; todayIndex: number } {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const todayIso = formatDateISO(today);

  const chips: DayChipData[] = [];
  const start = new Date(today);
  start.setDate(today.getDate() - 365);

  let todayIndex = 365;

  for (let i = 0; i <= 730; i++) {
    const d = new Date(start);
    d.setDate(start.getDate() + i);
    const iso = formatDateISO(d);
    const isToday = iso === todayIso;
    if (isToday) todayIndex = i;

    chips.push({
      index: i,
      date: d,
      dateString: iso,
      dayOfWeekShort: WEEKDAY_SHORT[d.getDay()],
      dayOfMonth: d.getDate(),
      weekdayName: getWeekdayName(d),
      isToday,
    });
  }

  return { chips, todayIndex };
}

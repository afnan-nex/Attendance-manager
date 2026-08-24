import React, { useState, useMemo, useEffect } from 'react';
import {
  View,
  ScrollView,
  StyleSheet,
} from 'react-native';
import {
  Switch,
  Text,
  Card,
  Surface,
  TouchableRipple,
} from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAppTheme } from '../theme/ThemeContext';
import { useDatabase } from '../db/useDatabase';
import { TopAppBar } from '../components/TopAppBar';
import { DateStrip } from '../components/DateStrip';
import { EmptyState } from '../components/EmptyState';
import { CalendarModal } from '../components/CalendarModal';
import { StudentDetailModal } from '../components/StudentDetailModal';
import {
  formatLongDate,
  getWeekdayName,
  formatDateISO,
  compareWithToday,
} from '../utils/dateUtils';
import { showShortToast } from '../utils/toast';
import { NavRoute, StudentEntity } from '../types';

interface AttendanceScreenProps {
  onNavigate: (route: NavRoute) => void;
  selectedDate: Date;
  setSelectedDate: (d: Date) => void;
  isScreenActive: boolean;
}

export const AttendanceScreen: React.FC<AttendanceScreenProps> = ({
  onNavigate,
  selectedDate,
  setSelectedDate,
  isScreenActive,
}) => {
  const { colors, typography } = useAppTheme();
  const {
    classes,
    students,
    attendance,
    settings,
    toggleAttendance,
  } = useDatabase();

  const [isCalendarOpen, setIsCalendarOpen] = useState(false);
  const [selectedClassId, setSelectedClassId] = useState<string | null>(null);
  const [isSubjectDropdownOpen, setIsSubjectDropdownOpen] = useState(false);

  // 7-tap unlock state for past dates
  const [isPastUnlocked, setIsPastUnlocked] = useState(false);
  const [pastTapCount, setPastTapCount] = useState(0);

  // Detail popup state
  const [selectedStudentForDetail, setSelectedStudentForDetail] = useState<StudentEntity | null>(null);

  const selectedIso = formatDateISO(selectedDate);
  const selectedWeekday = useMemo(() => getWeekdayName(selectedDate), [selectedDate]);
  const dateComparison = useMemo(() => compareWithToday(selectedIso), [selectedIso]); // -1 past, 0 today, 1 future

  // Non-hidden classes for selected weekday ordered by start time
  const dayClasses = useMemo(() => {
    return classes
      .filter((c) => !c.isHidden && c.day === selectedWeekday)
      .sort((a, b) => a.startTime.localeCompare(b.startTime));
  }, [classes, selectedWeekday]);

  // Auto-select first class when date changes or fallback if selected class is invalid
  useEffect(() => {
    if (dayClasses.length > 0) {
      const exists = dayClasses.some((c) => c.id === selectedClassId);
      if (!exists) {
        setSelectedClassId(dayClasses[0].id);
      }
    } else {
      setSelectedClassId(null);
    }
  }, [dayClasses, selectedClassId]);

  // Unlock silently resets to locked whenever user changes date/week OR leaves this screen
  useEffect(() => {
    setIsPastUnlocked(false);
    setPastTapCount(0);
  }, [selectedIso, isScreenActive]);

  // Currently selected class entity
  const selectedClass = useMemo(() => {
    return dayClasses.find((c) => c.id === selectedClassId) || dayClasses[0] || null;
  }, [dayClasses, selectedClassId]);

  // Section-filtered students preserving global drag order
  const filteredStudents = useMemo(() => {
    if (settings.selectedSection === 'All') {
      return students;
    }
    return students.filter(
      (s) => s.section.toUpperCase() === settings.selectedSection.toUpperCase()
    );
  }, [students, settings.selectedSection]);

  // Map of student attendance for the selected class and date
  const attendanceMap = useMemo(() => {
    const map = new Map<string, boolean>();
    if (!selectedClass) return map;
    attendance
      .filter((a) => a.classId === selectedClass.id && a.date === selectedIso)
      .forEach((a) => map.set(a.studentId, a.isPresent));
    return map;
  }, [attendance, selectedClass, selectedIso]);

  // Editing allowed condition: today always; past after 7-tap unlock; future never
  const isEditingAllowed = useMemo(() => {
    if (dateComparison === 0) return true; // Today
    if (dateComparison === -1) return isPastUnlocked; // Past
    return false; // Future
  }, [dateComparison, isPastUnlocked]);

  // Handle past lock row tap
  const handlePastLockTap = () => {
    if (isPastUnlocked) return;
    const nextCount = pastTapCount + 1;
    if (nextCount >= 7) {
      setIsPastUnlocked(true);
      setPastTapCount(0);
      showShortToast('Attendance Edit Unlocked');
    } else {
      setPastTapCount(nextCount);
    }
  };

  const handleToggleStudent = async (studentId: string, currentValue: boolean) => {
    if (!isEditingAllowed || !selectedClass) return;
    await toggleAttendance(selectedClass.id, studentId, selectedIso, !currentValue);
  };

  return (
    <Surface style={[styles.container, { backgroundColor: colors.background }]} elevation={0}>
      {/* 1. Top Bar */}
      <TopAppBar
        title="Attendance"
        showCalendar
        onCalendarPress={() => setIsCalendarOpen(true)}
        showSettings
        onSettingsPress={() => onNavigate('settings')}
      />

      {/* 2. Date Strip */}
      <DateStrip
        selectedDate={selectedDate}
        onSelectDate={setSelectedDate}
      />

      {/* Scrollable Body */}
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* 3. If zero classes scheduled on that weekday */}
        {dayClasses.length === 0 ? (
          <EmptyState
            message={`No classes scheduled on ${formatLongDate(selectedDate)}`}
          />
        ) : (
          <View>
            {/* 4a. Subject dropdown */}
            <View style={styles.dropdownContainer}>
              <Text
                variant="labelSmall"
                style={[
                  styles.dropdownFloatingLabel,
                  { color: colors.primary, backgroundColor: colors.background },
                ]}
              >
                Subject
              </Text>
              <Surface
                elevation={1}
                style={[
                  styles.dropdownInputSurface,
                  {
                    borderColor: colors.outline,
                    backgroundColor: colors.surfaceContainer,
                  },
                ]}
              >
                <TouchableRipple
                  borderless
                  onPress={() => setIsSubjectDropdownOpen(!isSubjectDropdownOpen)}
                  style={styles.dropdownTouchable}
                >
                  <View style={styles.dropdownInner}>
                    <Text
                      variant="bodyMedium"
                      style={[
                        styles.dropdownValueText,
                        { color: selectedClass ? colors.onSurface : colors.outline },
                      ]}
                      numberOfLines={1}
                    >
                      {selectedClass
                        ? `${selectedClass.shortName} - ${selectedClass.fullNameWithCode}`
                        : 'Select subject'}
                    </Text>
                    <MaterialCommunityIcons
                      name={isSubjectDropdownOpen ? 'chevron-up' : 'chevron-down'}
                      size={24}
                      color={colors.onSurfaceVariant}
                    />
                  </View>
                </TouchableRipple>
              </Surface>

              {/* Menu items */}
              {isSubjectDropdownOpen && (
                <Surface
                  elevation={3}
                  style={[
                    styles.menuCard,
                    {
                      backgroundColor: colors.surfaceContainerHighest,
                      borderColor: colors.outlineVariant,
                    },
                  ]}
                >
                  {dayClasses.map((cls) => (
                    <TouchableRipple
                      key={cls.id}
                      onPress={() => {
                        setSelectedClassId(cls.id);
                        setIsSubjectDropdownOpen(false);
                      }}
                      style={[
                        styles.menuItem,
                        selectedClass?.id === cls.id && {
                          backgroundColor: colors.primaryContainer,
                        },
                      ]}
                    >
                      <Text
                        variant="bodyMedium"
                        style={{
                          color:
                            selectedClass?.id === cls.id
                              ? colors.onPrimaryContainer
                              : colors.onSurface,
                          fontWeight: selectedClass?.id === cls.id ? '700' : '400',
                        }}
                      >
                        {`${cls.shortName} - ${cls.fullNameWithCode}`}
                      </Text>
                    </TouchableRipple>
                  ))}
                </Surface>
              )}
            </View>

            {/* 4b. Lock-status row variants */}
            <View style={styles.lockRowWrapper}>
              {dateComparison === 0 ? (
                /* Selected date = today */
                <Surface
                  elevation={1}
                  style={[
                    styles.todayBanner,
                    { backgroundColor: colors.primaryContainer },
                  ]}
                >
                  <MaterialCommunityIcons
                    name="check-circle"
                    size={20}
                    color={colors.onPrimaryContainer}
                  />
                  <Text
                    variant="bodyMedium"
                    style={[
                      styles.todayBannerText,
                      { color: colors.onPrimaryContainer, fontWeight: '600' },
                    ]}
                  >
                    Today's attendance – free to edit
                  </Text>
                </Surface>
              ) : dateComparison === 1 ? (
                /* Selected date in future */
                <Surface
                  elevation={1}
                  style={[
                    styles.futureBanner,
                    { backgroundColor: colors.errorContainer },
                  ]}
                >
                  <MaterialCommunityIcons
                    name="clock-alert-outline"
                    size={20}
                    color={colors.onErrorContainer}
                  />
                  <Text
                    variant="bodyMedium"
                    style={[
                      styles.futureBannerText,
                      { color: colors.onErrorContainer, fontWeight: '600' },
                    ]}
                  >
                    Attendance cannot be added for future dates
                  </Text>
                </Surface>
              ) : (
                /* Selected date in past */
                <Surface
                  elevation={1}
                  style={[
                    styles.pastLockSurface,
                    {
                      backgroundColor: colors.surfaceContainer,
                      borderColor: colors.outlineVariant,
                    },
                  ]}
                >
                  <TouchableRipple
                    borderless
                    onPress={handlePastLockTap}
                    style={styles.pastLockRipple}
                  >
                    <View style={styles.pastLockInner}>
                      <View style={styles.pastLockLeft}>
                        <MaterialCommunityIcons
                          name={isPastUnlocked ? 'lock-open-outline' : 'lock-outline'}
                          size={22}
                          color={isPastUnlocked ? colors.primary : colors.onSurfaceVariant}
                          style={styles.padlockIcon}
                        />
                        <View>
                          <Text
                            variant="bodyMedium"
                            style={{
                              color: isPastUnlocked ? colors.primary : colors.onSurface,
                              fontWeight: '600',
                            }}
                          >
                            {isPastUnlocked
                              ? 'Past attendance unlocked'
                              : 'Past attendance is locked'}
                          </Text>
                          {!isPastUnlocked && pastTapCount > 0 && (
                            <Text
                              variant="bodySmall"
                              style={{ color: colors.onSurfaceVariant }}
                            >
                              {`${pastTapCount} / 7 taps`}
                            </Text>
                          )}
                        </View>
                      </View>

                      {isPastUnlocked && (
                        <Text
                          variant="labelMedium"
                          style={{ color: colors.primary, fontWeight: '700' }}
                        >
                          Unlocked
                        </Text>
                      )}
                    </View>
                  </TouchableRipple>
                </Surface>
              )}
            </View>

            {/* 4c. Student table or empty state */}
            {filteredStudents.length === 0 ? (
              <EmptyState
                message={
                  settings.selectedSection === 'All'
                    ? 'No students added yet'
                    : `No students in section ${settings.selectedSection}`
                }
              />
            ) : (
              <Card
                mode="elevated"
                elevation={1}
                style={[
                  styles.tableCard,
                  {
                    backgroundColor: colors.surfaceContainer,
                  },
                ]}
              >
                {/* Header row: Sr. No (44 dp) | Name (flex) | Reg No (96 dp) | Present (56 dp) */}
                <View
                  style={[
                    styles.tableHeaderRow,
                    { borderBottomColor: colors.outlineVariant },
                  ]}
                >
                  <Text
                    variant="labelMedium"
                    style={[
                      styles.colSr,
                      { color: colors.onSurfaceVariant, fontWeight: '700' },
                    ]}
                  >
                    Sr. No
                  </Text>
                  <Text
                    variant="labelMedium"
                    style={[
                      styles.colName,
                      { color: colors.onSurfaceVariant, fontWeight: '700' },
                    ]}
                  >
                    Name
                  </Text>
                  <Text
                    variant="labelMedium"
                    style={[
                      styles.colReg,
                      { color: colors.onSurfaceVariant, fontWeight: '700' },
                    ]}
                  >
                    Reg No
                  </Text>
                  <Text
                    variant="labelMedium"
                    style={[
                      styles.colPresent,
                      { color: colors.onSurfaceVariant, textAlign: 'center', fontWeight: '700' },
                    ]}
                  >
                    Present
                  </Text>
                </View>

                {/* Student rows */}
                {filteredStudents.map((student, index) => {
                  const isPresent = Boolean(attendanceMap.get(student.id));

                  // Row background: primaryContainer when present AND editing allowed
                  const rowBg =
                    isPresent && isEditingAllowed
                      ? colors.primaryContainer
                      : 'transparent';

                  return (
                    <TouchableRipple
                      key={student.id}
                      onPress={() => setSelectedStudentForDetail(student)}
                      style={[
                        styles.studentRowRipple,
                        {
                          backgroundColor: rowBg,
                          borderBottomColor: colors.outlineVariant,
                        },
                      ]}
                    >
                      <View style={styles.studentRowInner}>
                        {/* Sr No */}
                        <Text
                          variant="bodySmall"
                          style={[
                            styles.colSr,
                            { color: isPresent && isEditingAllowed ? colors.onPrimaryContainer : colors.onSurfaceVariant },
                          ]}
                        >
                          {index + 1}
                        </Text>

                        {/* Name bold single-line ellipsized */}
                        <Text
                          variant="bodyMedium"
                          style={[
                            styles.colName,
                            {
                              color: isPresent && isEditingAllowed ? colors.onPrimaryContainer : colors.onSurface,
                              fontWeight: '700',
                            },
                          ]}
                          numberOfLines={1}
                        >
                          {student.name}
                        </Text>

                        {/* Reg No */}
                        <Text
                          variant="bodySmall"
                          style={[
                            styles.colReg,
                            { color: isPresent && isEditingAllowed ? colors.onPrimaryContainer : colors.onSurfaceVariant },
                          ]}
                          numberOfLines={1}
                        >
                          {student.regNo}
                        </Text>

                        {/* Material 3 Present Switch */}
                        <View style={styles.colPresent}>
                          <Switch
                            value={isPresent}
                            disabled={!isEditingAllowed}
                            onValueChange={() =>
                              handleToggleStudent(student.id, isPresent)
                            }
                            color={colors.primary}
                          />
                        </View>
                      </View>
                    </TouchableRipple>
                  );
                })}
              </Card>
            )}

            {/* 5. 24 dp bottom spacer */}
            <View style={{ height: 24 }} />
          </View>
        )}
      </ScrollView>

      {/* Calendar Modal */}
      <CalendarModal
        visible={isCalendarOpen}
        selectedDate={selectedDate}
        onApply={(d) => {
          setSelectedDate(d);
          setIsCalendarOpen(false);
        }}
        onCancel={() => setIsCalendarOpen(false)}
      />

      {/* Student Detail Modal (Read-only on Attendance Screen: no Edit/Delete) */}
      <StudentDetailModal
        visible={selectedStudentForDetail !== null}
        student={selectedStudentForDetail}
        showEditDelete={false}
        onClose={() => setSelectedStudentForDetail(null)}
      />
    </Surface>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    paddingBottom: 90,
  },
  dropdownContainer: {
    paddingHorizontal: 16,
    paddingTop: 12,
    marginBottom: 10,
    position: 'relative',
    zIndex: 10,
  },
  dropdownFloatingLabel: {
    position: 'absolute',
    left: 28,
    top: 4,
    zIndex: 2,
    paddingHorizontal: 4,
    fontWeight: '700',
  },
  dropdownInputSurface: {
    borderWidth: 1,
    borderRadius: 12,
    overflow: 'hidden',
  },
  dropdownTouchable: {
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  dropdownInner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  dropdownValueText: {
    flex: 1,
    marginRight: 8,
  },
  menuCard: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: 12,
    marginTop: 4,
    overflow: 'hidden',
  },
  menuItem: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  lockRowWrapper: {
    paddingHorizontal: 16,
    marginBottom: 12,
  },
  todayBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderRadius: 14,
  },
  todayBannerText: {
    marginLeft: 8,
  },
  futureBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderRadius: 14,
  },
  futureBannerText: {
    marginLeft: 8,
  },
  pastLockSurface: {
    borderRadius: 14,
    borderWidth: 1,
    overflow: 'hidden',
  },
  pastLockRipple: {
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  pastLockInner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  pastLockLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  padlockIcon: {
    marginRight: 10,
  },
  tableCard: {
    marginHorizontal: 16,
    borderRadius: 16,
    overflow: 'hidden',
  },
  tableHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  studentRowRipple: {
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  studentRowInner: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  colSr: {
    width: 44,
  },
  colName: {
    flex: 1,
    paddingRight: 8,
  },
  colReg: {
    width: 96,
    paddingRight: 4,
  },
  colPresent: {
    width: 56,
    alignItems: 'center',
    justifyContent: 'center',
  },
});

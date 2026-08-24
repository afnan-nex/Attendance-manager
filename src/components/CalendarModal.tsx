import React, { useState, useMemo } from 'react';
import {
  View,
  StyleSheet,
  TouchableWithoutFeedback,
} from 'react-native';
import {
  Button,
  IconButton,
  Text,
  Portal,
  Modal,
  Surface,
  TouchableRipple,
} from 'react-native-paper';
import { useAppTheme } from '../theme/ThemeContext';
import {
  MONTH_NAMES,
  WEEKDAY_SHORT,
  formatDateISO,
  formatLongDate,
} from '../utils/dateUtils';

interface CalendarModalProps {
  visible: boolean;
  selectedDate: Date;
  onApply: (date: Date) => void;
  onCancel: () => void;
}

export const CalendarModal: React.FC<CalendarModalProps> = ({
  visible,
  selectedDate,
  onApply,
  onCancel,
}) => {
  const { colors, typography } = useAppTheme();

  // Internal selection state while modal is open
  const [internalDate, setInternalDate] = useState<Date>(selectedDate);
  const [viewYear, setViewYear] = useState<number>(selectedDate.getFullYear());
  const [viewMonth, setViewMonth] = useState<number>(selectedDate.getMonth());

  // Reset when opened
  React.useEffect(() => {
    if (visible) {
      setInternalDate(selectedDate);
      setViewYear(selectedDate.getFullYear());
      setViewMonth(selectedDate.getMonth());
    }
  }, [visible, selectedDate]);

  // Allowed window: today - 365 to today + 365
  const today = useMemo(() => {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    return d;
  }, []);

  const minDate = useMemo(() => {
    const d = new Date(today);
    d.setDate(today.getDate() - 365);
    return d;
  }, [today]);

  const maxDate = useMemo(() => {
    const d = new Date(today);
    d.setDate(today.getDate() + 365);
    return d;
  }, [today]);

  const daysInMonth = useMemo(() => {
    return new Date(viewYear, viewMonth + 1, 0).getDate();
  }, [viewYear, viewMonth]);

  const firstDayWeekday = useMemo(() => {
    return new Date(viewYear, viewMonth, 1).getDay(); // 0 is Sun
  }, [viewYear, viewMonth]);

  const prevMonth = () => {
    if (viewMonth === 0) {
      setViewYear(viewYear - 1);
      setViewMonth(11);
    } else {
      setViewMonth(viewMonth - 1);
    }
  };

  const nextMonth = () => {
    if (viewMonth === 11) {
      setViewYear(viewYear + 1);
      setViewMonth(0);
    } else {
      setViewMonth(viewMonth + 1);
    }
  };

  const selectedIso = formatDateISO(internalDate);
  const todayIso = formatDateISO(today);

  // Calendar grid generation
  const calendarCells = useMemo(() => {
    const cells: (Date | null)[] = [];
    for (let i = 0; i < firstDayWeekday; i++) {
      cells.push(null);
    }
    for (let d = 1; d <= daysInMonth; d++) {
      cells.push(new Date(viewYear, viewMonth, d));
    }
    return cells;
  }, [firstDayWeekday, daysInMonth, viewYear, viewMonth]);

  return (
    <Portal>
      <Modal
        visible={visible}
        onDismiss={onCancel}
        dismissable={true}
        contentContainerStyle={styles.modalContainer}
      >
        <TouchableWithoutFeedback onPress={onCancel}>
          <View style={styles.backdropArea}>
            <TouchableWithoutFeedback onPress={(e) => e.stopPropagation()}>
              <Surface
                elevation={3}
                style={[
                  styles.dialog,
                  { backgroundColor: colors.surfaceContainerHigh },
                ]}
              >
                {/* M3 Header Banner */}
                <View
                  style={[
                    styles.header,
                    { backgroundColor: colors.primary },
                  ]}
                >
                  <Text
                    variant="labelMedium"
                    style={{
                      color: colors.onPrimary,
                      opacity: 0.85,
                      textTransform: 'uppercase',
                      letterSpacing: 1,
                    }}
                  >
                    Select Date
                  </Text>
                  <Text
                    variant="headlineSmall"
                    style={[
                      typography.headlineSmall,
                      { color: colors.onPrimary, marginTop: 4, fontWeight: '700' },
                    ]}
                  >
                    {formatLongDate(internalDate)}
                  </Text>
                </View>

                {/* Month/Year selector row */}
                <View style={styles.monthNavRow}>
                  <IconButton
                    icon="chevron-left"
                    size={24}
                    iconColor={colors.onSurface}
                    onPress={prevMonth}
                  />

                  <Text
                    variant="titleMedium"
                    style={[
                      typography.titleMedium,
                      { color: colors.onSurface, fontWeight: '700' },
                    ]}
                  >
                    {`${MONTH_NAMES[viewMonth]} ${viewYear}`}
                  </Text>

                  <IconButton
                    icon="chevron-right"
                    size={24}
                    iconColor={colors.onSurface}
                    onPress={nextMonth}
                  />
                </View>

                {/* Weekday headers */}
                <View style={styles.weekdayRow}>
                  {WEEKDAY_SHORT.map((wd) => (
                    <Text
                      key={wd}
                      variant="labelSmall"
                      style={[
                        styles.weekdayHeaderCell,
                        { color: colors.onSurfaceVariant, fontWeight: '700' },
                      ]}
                    >
                      {wd.substring(0, 2)}
                    </Text>
                  ))}
                </View>

                {/* Days Grid */}
                <View style={styles.gridContainer}>
                  {calendarCells.map((dateObj, idx) => {
                    if (!dateObj) {
                      return <View key={`empty_${idx}`} style={styles.dayCellWrapper} />;
                    }

                    const cellIso = formatDateISO(dateObj);
                    const isSelected = cellIso === selectedIso;
                    const isToday = cellIso === todayIso;
                    const isOutOfRange = dateObj < minDate || dateObj > maxDate;

                    // Compute text color with strict contrast guarantees
                    const textColor = isOutOfRange
                      ? colors.outlineVariant
                      : isSelected
                      ? colors.onPrimary
                      : isToday
                      ? colors.primary
                      : colors.onSurface;

                    return (
                      <View key={cellIso} style={styles.dayCellWrapper}>
                        <TouchableRipple
                          borderless
                          disabled={isOutOfRange}
                          onPress={() => setInternalDate(dateObj)}
                          style={styles.dayCellRipple}
                        >
                          <View
                            style={[
                              styles.dayCircle,
                              isSelected && {
                                backgroundColor: colors.primary,
                              },
                              !isSelected && isToday && {
                                borderWidth: 1.5,
                                borderColor: colors.primary,
                                backgroundColor: 'transparent',
                              },
                            ]}
                          >
                            <Text
                              variant="bodyMedium"
                              style={[
                                styles.dayText,
                                {
                                  color: textColor,
                                  fontWeight: isSelected || isToday ? '700' : '400',
                                },
                              ]}
                            >
                              {dateObj.getDate()}
                            </Text>
                          </View>
                        </TouchableRipple>
                      </View>
                    );
                  })}
                </View>

                {/* Footer Buttons */}
                <View style={[styles.footerRow, { borderTopColor: colors.outlineVariant }]}>
                  <Button
                    mode="text"
                    onPress={onCancel}
                    textColor={colors.primary}
                  >
                    Cancel
                  </Button>

                  <Button
                    mode="contained"
                    onPress={() => onApply(internalDate)}
                    buttonColor={colors.primary}
                    textColor={colors.onPrimary}
                    style={{ marginLeft: 8 }}
                  >
                    OK
                  </Button>
                </View>
              </Surface>
            </TouchableWithoutFeedback>
          </View>
        </TouchableWithoutFeedback>
      </Modal>
    </Portal>
  );
};

const styles = StyleSheet.create({
  modalContainer: {
    padding: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  backdropArea: {
    width: '100%',
    alignItems: 'center',
    justifyContent: 'center',
  },
  dialog: {
    width: '100%',
    maxWidth: 360,
    borderRadius: 28,
    overflow: 'hidden',
  },
  header: {
    padding: 20,
  },
  monthNavRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 8,
    paddingVertical: 6,
  },
  weekdayRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingHorizontal: 12,
    marginBottom: 6,
  },
  weekdayHeaderCell: {
    width: 38,
    textAlign: 'center',
  },
  gridContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    paddingHorizontal: 12,
    paddingBottom: 8,
  },
  dayCellWrapper: {
    width: '14.28%',
    height: 40,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 1,
  },
  dayCellRipple: {
    width: 36,
    height: 36,
    borderRadius: 18,
    overflow: 'hidden',
    alignItems: 'center',
    justifyContent: 'center',
  },
  dayCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
  dayText: {
    textAlign: 'center',
    includeFontPadding: false,
  },
  footerRow: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    alignItems: 'center',
    padding: 16,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
});

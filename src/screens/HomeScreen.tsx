import React, { useState, useMemo } from 'react';
import {
  View,
  FlatList,
  StyleSheet,
} from 'react-native';
import { Text, Surface } from 'react-native-paper';
import { useAppTheme } from '../theme/ThemeContext';
import { useDatabase } from '../db/useDatabase';
import { TopAppBar } from '../components/TopAppBar';
import { DateStrip } from '../components/DateStrip';
import { ClassCard } from '../components/ClassCard';
import { EmptyState } from '../components/EmptyState';
import { CalendarModal } from '../components/CalendarModal';
import { formatLongDate, getWeekdayName } from '../utils/dateUtils';
import { NavRoute } from '../types';

interface HomeScreenProps {
  onNavigate: (route: NavRoute) => void;
  selectedDate: Date;
  setSelectedDate: (d: Date) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  onNavigate,
  selectedDate,
  setSelectedDate,
}) => {
  const { colors, typography } = useAppTheme();
  const { classes } = useDatabase();

  const [isCalendarOpen, setIsCalendarOpen] = useState(false);

  // Selected date weekday name
  const selectedWeekday = useMemo(() => getWeekdayName(selectedDate), [selectedDate]);

  // Non-hidden classes matching selected weekday, sorted by start time ascending
  const displayedClasses = useMemo(() => {
    return classes
      .filter((c) => !c.isHidden && c.day === selectedWeekday)
      .sort((a, b) => a.startTime.localeCompare(b.startTime));
  }, [classes, selectedWeekday]);

  return (
    <Surface style={[styles.container, { backgroundColor: colors.background }]} elevation={0}>
      {/* 1. Top bar: Attendance Manager */}
      <TopAppBar
        title="Attendance Manager"
        showCalendar
        onCalendarPress={() => setIsCalendarOpen(true)}
        showSettings
        onSettingsPress={() => onNavigate('settings')}
      />

      {/* 2. Date strip */}
      <DateStrip
        selectedDate={selectedDate}
        onSelectDate={setSelectedDate}
      />

      {/* 3. Section label: "Today's Classes" */}
      <View style={styles.sectionLabelContainer}>
        <Text
          variant="titleMedium"
          style={[
            typography.titleMedium,
            { color: colors.onSurface, fontWeight: '700' },
          ]}
        >
          Today's Classes
        </Text>
      </View>

      {/* 4. Body: Empty state or vertically scrolling card list */}
      {displayedClasses.length === 0 ? (
        <EmptyState
          message={`No classes scheduled for ${formatLongDate(selectedDate)}`}
        />
      ) : (
        <FlatList
          data={displayedClasses}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => <ClassCard item={item} />}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
        />
      )}

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
    </Surface>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  sectionLabelContainer: {
    paddingStart: 16,
    paddingVertical: 8,
  },
  listContent: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    paddingBottom: 90,
  },
});

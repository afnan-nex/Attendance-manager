import React, { useRef, useEffect, useMemo } from 'react';
import {
  View,
  FlatList,
  StyleSheet,
} from 'react-native';
import { Text, Surface, TouchableRipple } from 'react-native-paper';
import { useAppTheme } from '../theme/ThemeContext';
import { generateDateStripData, DayChipData, formatDateISO } from '../utils/dateUtils';

interface DateStripProps {
  selectedDate: Date;
  onSelectDate: (date: Date) => void;
}

const CHIP_WIDTH = 54;
const CHIP_GAP = 6;
const ITEM_TOTAL_WIDTH = CHIP_WIDTH + CHIP_GAP;

export const DateStrip: React.FC<DateStripProps> = ({
  selectedDate,
  onSelectDate,
}) => {
  const { colors, typography } = useAppTheme();
  const flatListRef = useRef<FlatList>(null);
  const isInitialMount = useRef(true);
  const isInternalSelectionRef = useRef(false);

  const { chips, todayIndex } = useMemo(() => generateDateStripData(), []);
  const selectedIso = formatDateISO(selectedDate);

  const selectedIndex = useMemo(() => {
    const idx = chips.findIndex((c) => c.dateString === selectedIso);
    return idx >= 0 ? idx : todayIndex;
  }, [chips, selectedIso, todayIndex]);

  // Restrict Index 3 Positioning to:
  // Case A: Screen / App initialization (initial mount)
  // Case B: Explicit external change (Calendar Modal / external jump)
  // Pure tap on any chip in the strip updates selection ONLY without shifting/scrolling the list.
  useEffect(() => {
    if (isInitialMount.current) {
      isInitialMount.current = false;
      const targetIndex = Math.max(0, selectedIndex - 2);
      flatListRef.current?.scrollToOffset({
        offset: targetIndex * ITEM_TOTAL_WIDTH,
        animated: false,
      });
      return;
    }

    if (isInternalSelectionRef.current) {
      isInternalSelectionRef.current = false;
      return;
    }

    // External change (e.g. Calendar modal or Today jump)
    if (selectedIndex >= 0) {
      const targetIndex = Math.max(0, selectedIndex - 2);
      flatListRef.current?.scrollToOffset({
        offset: targetIndex * ITEM_TOTAL_WIDTH,
        animated: false,
      });
    }
  }, [selectedIndex]);

  const handleChipPress = (date: Date) => {
    isInternalSelectionRef.current = true;
    onSelectDate(date);
  };

  const getItemLayout = (_: any, index: number) => ({
    length: ITEM_TOTAL_WIDTH,
    offset: ITEM_TOTAL_WIDTH * index,
    index,
  });

  const renderChip = ({ item }: { item: DayChipData }) => {
    const isSelected = item.dateString === selectedIso;

    // Selected chip: M3 primaryContainer with onPrimaryContainer text
    // Unselected: M3 surfaceContainerHigh with onSurfaceVariant/onSurface text
    const backgroundColor = isSelected
      ? colors.primaryContainer
      : colors.surfaceContainer;

    const weekdayColor = isSelected ? colors.onPrimaryContainer : colors.onSurfaceVariant;
    const numberColor = isSelected ? colors.onPrimaryContainer : colors.onSurface;

    return (
      <Surface
        elevation={isSelected ? 2 : 0}
        style={[
          styles.chipSurface,
          {
            backgroundColor,
            borderColor: isSelected ? colors.primary : colors.outlineVariant,
            borderWidth: isSelected ? 1.5 : StyleSheet.hairlineWidth,
          },
        ]}
      >
        <TouchableRipple
          borderless
          onPress={() => handleChipPress(item.date)}
          style={styles.chipRipple}
        >
          <View style={styles.chipInner}>
            <Text
              variant="labelSmall"
              style={[
                typography.labelSmall,
                styles.weekdayText,
                { color: weekdayColor, fontWeight: isSelected ? '700' : '500' },
              ]}
              numberOfLines={1}
            >
              {item.dayOfWeekShort}
            </Text>

            <Text
              variant="titleMedium"
              style={[
                typography.titleMedium,
                styles.numberText,
                {
                  color: numberColor,
                  fontWeight: isSelected ? '700' : '500',
                },
              ]}
            >
              {item.dayOfMonth}
            </Text>

            {/* Reserved slot under number containing round dot on today's date */}
            <View style={styles.dotSlot}>
              {item.isToday && (
                <View
                  style={[
                    styles.todayDot,
                    { backgroundColor: isSelected ? colors.primary : colors.primary },
                  ]}
                />
              )}
            </View>
          </View>
        </TouchableRipple>
      </Surface>
    );
  };

  return (
    <Surface style={[styles.container, { backgroundColor: colors.surface }]} elevation={0}>
      <FlatList
        ref={flatListRef}
        horizontal
        data={chips}
        keyExtractor={(item) => item.dateString}
        renderItem={renderChip}
        getItemLayout={getItemLayout}
        initialScrollIndex={Math.max(0, todayIndex - 2)}
        onScrollToIndexFailed={(info) => {
          setTimeout(() => {
            flatListRef.current?.scrollToIndex({ index: info.index, animated: false });
          }, 50);
        }}
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.contentContainer}
        ItemSeparatorComponent={() => <View style={{ width: CHIP_GAP }} />}
        initialNumToRender={15}
        maxToRenderPerBatch={20}
        windowSize={5}
      />
    </Surface>
  );
};

const styles = StyleSheet.create({
  container: {
    height: 94,
  },
  contentContainer: {
    paddingHorizontal: 12,
    paddingVertical: 10,
    alignItems: 'center',
  },
  chipSurface: {
    width: CHIP_WIDTH,
    height: 74,
    borderRadius: 16,
    overflow: 'hidden',
  },
  chipRipple: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  chipInner: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 4,
  },
  weekdayText: {
    marginBottom: 2,
  },
  numberText: {
    lineHeight: 22,
  },
  dotSlot: {
    height: 6,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 2,
  },
  todayDot: {
    width: 5,
    height: 5,
    borderRadius: 2.5,
  },
});

import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Card, Text, Chip, IconButton } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAppTheme } from '../theme/ThemeContext';
import { ClassEntity } from '../types';
import { formatTime12h } from '../utils/dateUtils';

interface ClassCardProps {
  item: ClassEntity;
  showActions?: boolean;
  onToggleHidden?: () => void;
  onDeletePress?: () => void;
  onEditPress?: () => void;
}

export const ClassCard: React.FC<ClassCardProps> = ({
  item,
  showActions = false,
  onToggleHidden,
  onDeletePress,
  onEditPress,
}) => {
  const { colors, typography } = useAppTheme();

  const formattedStartTime = formatTime12h(item.startTime);
  const formattedEndTime = formatTime12h(item.endTime);

  return (
    <Card
      mode="elevated"
      elevation={1}
      style={[
        styles.card,
        {
          backgroundColor: colors.surfaceContainer,
          opacity: item.isHidden ? 0.72 : 1.0,
        },
      ]}
    >
      <Card.Content style={styles.cardContent}>
        {/* Header row: Short Name + Trailing action buttons */}
        <View style={styles.headerRow}>
          <Text
            variant="headlineSmall"
            style={[
              typography.headlineSmall,
              styles.shortName,
              { color: colors.onSurface, fontWeight: '700' },
            ]}
            numberOfLines={1}
          >
            {item.shortName}
          </Text>

          {showActions && (
            <View style={styles.actionsRow}>
              {onEditPress && (
                <IconButton
                  icon="pencil-outline"
                  size={20}
                  iconColor={colors.primary}
                  onPress={onEditPress}
                  accessibilityLabel="Edit class"
                  style={styles.actionIcon}
                />
              )}

              <IconButton
                icon={item.isHidden ? 'eye-off-outline' : 'eye-outline'}
                size={20}
                iconColor={colors.onSurfaceVariant}
                onPress={onToggleHidden}
                accessibilityLabel={item.isHidden ? 'Show class' : 'Hide class'}
                style={styles.actionIcon}
              />

              <IconButton
                icon="trash-can-outline"
                size={20}
                iconColor={colors.error}
                onPress={onDeletePress}
                accessibilityLabel="Delete class"
                style={[styles.actionIcon, { marginRight: -8 }]}
              />
            </View>
          )}
        </View>

        {/* Full name with code */}
        <Text
          variant="titleMedium"
          style={[
            typography.titleMedium,
            styles.fullName,
            { color: colors.onSurface, fontWeight: '600' },
          ]}
        >
          {item.fullNameWithCode}
        </Text>

        {/* Material 3 Lecture-Type Chips */}
        <View style={styles.chipWrapper}>
          <Chip
            mode="flat"
            compact
            textStyle={{
              fontSize: 12,
              color: colors.onSecondaryContainer,
              fontWeight: '600',
            }}
            style={{
              backgroundColor: colors.secondaryContainer,
              height: 28,
              borderRadius: 8,
            }}
          >
            {item.lectureType}
          </Chip>

          {item.isHidden && (
            <Chip
              mode="flat"
              compact
              textStyle={{
                fontSize: 12,
                color: colors.onErrorContainer,
                fontWeight: '600',
              }}
              style={{
                backgroundColor: colors.errorContainer,
                marginLeft: 8,
                height: 28,
                borderRadius: 8,
              }}
            >
              Hidden
            </Chip>
          )}
        </View>

        {/* Three meta rows: (16 dp icons, onSurfaceVariant tint, 8 dp gap) */}
        <View style={styles.metaContainer}>
          {/* Weekday and start time */}
          <View style={styles.metaRow}>
            <MaterialCommunityIcons
              name="calendar-clock"
              size={16}
              color={colors.onSurfaceVariant}
              style={styles.metaIcon}
            />
            <Text
              variant="bodyMedium"
              style={[
                typography.bodyMedium,
                { color: colors.onSurfaceVariant },
              ]}
            >
              {`${item.day} ${formattedStartTime}`}
            </Text>
          </View>

          {/* Teacher name (only if present) */}
          {Boolean(item.teacherName && item.teacherName.trim()) && (
            <View style={styles.metaRow}>
              <MaterialCommunityIcons
                name="account-outline"
                size={16}
                color={colors.onSurfaceVariant}
                style={styles.metaIcon}
              />
              <Text
                variant="bodyMedium"
                style={[
                  typography.bodyMedium,
                  { color: colors.onSurfaceVariant },
                ]}
              >
                {item.teacherName}
              </Text>
            </View>
          )}

          {/* Location (only if present) */}
          {Boolean(item.location && item.location.trim()) && (
            <View style={styles.metaRow}>
              <MaterialCommunityIcons
                name="map-marker-outline"
                size={16}
                color={colors.onSurfaceVariant}
                style={styles.metaIcon}
              />
              <Text
                variant="bodyMedium"
                style={[
                  typography.bodyMedium,
                  { color: colors.onSurface, fontWeight: '700' },
                ]}
              >
                {item.location}
              </Text>
            </View>
          )}
        </View>

        {/* Credit Hours: {n} */}
        <Text
          variant="bodyMedium"
          style={[
            typography.bodyMedium,
            styles.creditHoursText,
            { color: colors.onSurface },
          ]}
        >
          {`Credit Hours: ${item.creditHours}`}
        </Text>

        {/* Time range "{h:mm AM} - {h:mm PM}" */}
        <Text
          variant="bodyMedium"
          style={[
            typography.bodyMedium,
            styles.timeRangeText,
            { color: colors.onSurfaceVariant },
          ]}
        >
          {`${formattedStartTime} - ${formattedEndTime}`}
        </Text>
      </Card.Content>
    </Card>
  );
};

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    marginBottom: 12,
  },
  cardContent: {
    paddingVertical: 14,
    paddingHorizontal: 16,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  shortName: {
    flex: 1,
  },
  actionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  actionIcon: {
    margin: 0,
  },
  fullName: {
    marginTop: 2,
  },
  chipWrapper: {
    flexDirection: 'row',
    marginTop: 8,
    marginBottom: 12,
  },
  metaContainer: {
    marginBottom: 8,
  },
  metaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 6,
  },
  metaIcon: {
    marginRight: 8,
  },
  creditHoursText: {
    marginTop: 4,
  },
  timeRangeText: {
    marginTop: 2,
  },
});

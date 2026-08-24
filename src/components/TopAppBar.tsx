import React from 'react';
import { StyleSheet } from 'react-native';
import { Appbar } from 'react-native-paper';
import { useAppTheme } from '../theme/ThemeContext';

interface TopAppBarProps {
  title: string;
  showBack?: boolean;
  onBackPress?: () => void;
  showCalendar?: boolean;
  onCalendarPress?: () => void;
  showSettings?: boolean;
  onSettingsPress?: () => void;
}

export const TopAppBar: React.FC<TopAppBarProps> = ({
  title,
  showBack = false,
  onBackPress,
  showCalendar = false,
  onCalendarPress,
  showSettings = false,
  onSettingsPress,
}) => {
  const { colors, typography } = useAppTheme();

  return (
    <Appbar.Header
      statusBarHeight={0}
      style={[
        styles.header,
        {
          backgroundColor: colors.surface,
        },
      ]}
      mode="small"
      elevated
    >
      {showBack && (
        <Appbar.BackAction
          onPress={onBackPress}
          color={colors.onSurface}
          accessibilityLabel="Back"
        />
      )}

      <Appbar.Content
        title={title}
        titleStyle={[
          typography.titleLarge,
          {
            color: colors.onSurface,
            fontWeight: '700',
          },
        ]}
      />

      {showCalendar && (
        <Appbar.Action
          icon="calendar-month-outline"
          onPress={onCalendarPress}
          color={colors.onSurface}
          accessibilityLabel="Pick a date"
        />
      )}

      {showSettings && (
        <Appbar.Action
          icon="cog-outline"
          onPress={onSettingsPress}
          color={colors.onSurface}
          accessibilityLabel="Settings"
        />
      )}
    </Appbar.Header>
  );
};

const styles = StyleSheet.create({
  header: {
    elevation: 2,
    height: 56,
  },
});

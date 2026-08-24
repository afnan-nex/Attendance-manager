import React, { useEffect, useRef } from 'react';
import {
  View,
  StyleSheet,
  Animated,
} from 'react-native';
import { Text, Surface, TouchableRipple } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useAppTheme } from '../theme/ThemeContext';
import { NavRoute } from '../types';

interface BottomNavBarProps {
  currentRoute: NavRoute;
  onNavigate: (route: NavRoute) => void;
}

interface TabConfig {
  route: NavRoute;
  label: string;
  activeIcon: keyof typeof MaterialCommunityIcons.glyphMap;
  inactiveIcon: keyof typeof MaterialCommunityIcons.glyphMap;
}

const TABS: TabConfig[] = [
  {
    route: 'home',
    label: 'Home',
    activeIcon: 'home',
    inactiveIcon: 'home-outline',
  },
  {
    route: 'attendance',
    label: 'Attendance',
    activeIcon: 'clipboard-text',
    inactiveIcon: 'clipboard-text-outline',
  },
  {
    route: 'students',
    label: 'Manage Students',
    activeIcon: 'account-plus',
    inactiveIcon: 'account-plus-outline',
  },
  {
    route: 'classes',
    label: 'Manage Classes',
    activeIcon: 'book-open-page-variant',
    inactiveIcon: 'book-open-page-variant-outline',
  },
];

const BAR_HEIGHT = 74;

export const BottomNavBar: React.FC<BottomNavBarProps> = ({
  currentRoute,
  onNavigate,
}) => {
  const { colors, typography } = useAppTheme();
  const insets = useSafeAreaInsets();
  const translateY = useRef(new Animated.Value(0)).current;

  const isSettings = currentRoute === 'settings';

  useEffect(() => {
    // 200 ms slide animation
    Animated.timing(translateY, {
      toValue: isSettings ? BAR_HEIGHT + insets.bottom + 10 : 0,
      duration: 200,
      useNativeDriver: true,
    }).start();
  }, [isSettings, insets.bottom]);

  return (
    <Animated.View
      style={[
        styles.animatedWrapper,
        {
          transform: [{ translateY }],
        },
      ]}
    >
      <Surface
        elevation={2}
        style={[
          styles.container,
          {
            backgroundColor: colors.surfaceContainer,
            borderTopColor: colors.outlineVariant,
            paddingBottom: Math.max(insets.bottom, 4),
          },
        ]}
      >
        <View style={styles.tabRow}>
          {TABS.map((tab) => {
            const isActive = currentRoute === tab.route;
            const iconName = isActive ? tab.activeIcon : tab.inactiveIcon;
            const iconColor = isActive ? colors.onPrimaryContainer : colors.onSurfaceVariant;
            const labelColor = isActive ? colors.onSurface : colors.onSurfaceVariant;

            return (
              <View key={tab.route} style={styles.tabItem}>
                <TouchableRipple
                  borderless
                  onPress={() => {
                    if (!isActive) {
                      onNavigate(tab.route);
                    }
                  }}
                  style={styles.rippleContainer}
                >
                  <View style={styles.tabInner}>
                    {/* Material 3 Capsule Active Pill (56 x 32 dp, 16 dp radius) */}
                    <View
                      style={[
                        styles.pill,
                        {
                          backgroundColor: isActive
                            ? colors.primaryContainer
                            : 'transparent',
                        },
                      ]}
                    >
                      <MaterialCommunityIcons
                        name={iconName}
                        size={22}
                        color={iconColor}
                      />
                    </View>

                    <Text
                      variant="labelSmall"
                      style={[
                        typography.labelSmall,
                        styles.tabLabel,
                        {
                          color: labelColor,
                          fontWeight: isActive ? '700' : '500',
                        },
                      ]}
                      numberOfLines={1}
                    >
                      {tab.label}
                    </Text>
                  </View>
                </TouchableRipple>
              </View>
            );
          })}
        </View>
      </Surface>
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  animatedWrapper: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
  },
  container: {
    height: BAR_HEIGHT,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  tabRow: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    paddingHorizontal: 4,
    paddingTop: 4,
  },
  tabItem: {
    flex: 1,
    height: '100%',
  },
  rippleContainer: {
    flex: 1,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  tabInner: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  pill: {
    width: 56,
    height: 32,
    borderRadius: 16,
    overflow: 'hidden',
    alignItems: 'center',
    justifyContent: 'center',
  },
  tabLabel: {
    marginTop: 4,
    textAlign: 'center',
  },
});

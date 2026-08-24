import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  View,
  StyleSheet,
  StatusBar as RNStatusBar,
  BackHandler,
  AppState,
  AppStateStatus,
  Animated,
} from 'react-native';
import { Surface } from 'react-native-paper';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import { ThemeProvider, useAppTheme } from './src/theme/ThemeContext';
import { BottomNavBar } from './src/components/BottomNavBar';
import { BiometricOverlay } from './src/components/BiometricOverlay';
import { HomeScreen } from './src/screens/HomeScreen';
import { AttendanceScreen } from './src/screens/AttendanceScreen';
import { ManageStudentsScreen } from './src/screens/ManageStudentsScreen';
import { ManageClassesScreen } from './src/screens/ManageClassesScreen';
import { SettingsScreen } from './src/screens/SettingsScreen';
import { dbService } from './src/db/database';
import { NavRoute } from './src/types';

function MainApp() {
  const { colors, isDark } = useAppTheme();

  // App initialization state (blank background frame to avoid flashing content before lock decision)
  const [isReady, setIsReady] = useState(false);

  // Navigation state (flat routes)
  const [currentRoute, setCurrentRoute] = useState<NavRoute>('home');
  const [previousRoute, setPreviousRoute] = useState<NavRoute>('home');

  // Cross-fade animation value: 60 ms fade-in / 60 ms fade-out
  const fadeAnim = useRef(new Animated.Value(1)).current;

  // Session-scoped states (survive tab switches within session)
  const [homeSelectedDate, setHomeSelectedDate] = useState<Date>(() => new Date());
  const [attendanceSelectedDate, setAttendanceSelectedDate] = useState<Date>(() => new Date());
  const [selectedExportClassId, setSelectedExportClassId] = useState<string | null>(null);

  // Biometric lock overlay state
  const [isLocked, setIsLocked] = useState(false);
  const appStateRef = useRef<AppStateStatus>(AppState.currentState);

  // Initialize DB and Biometrics
  useEffect(() => {
    async function initialize() {
      await dbService.init();
      const settings = dbService.getSettings();
      if (settings.biometricEnabled) {
        setIsLocked(true);
      }
      setIsReady(true);
    }
    initialize();
  }, []);

  // Listen to AppState for biometric lock when resuming from background
  useEffect(() => {
    const subscription = AppState.addEventListener('change', (nextAppState) => {
      if (
        appStateRef.current.match(/inactive|background/) &&
        nextAppState === 'active'
      ) {
        const settings = dbService.getSettings();
        if (settings.biometricEnabled) {
          setIsLocked(true);
        }
      }
      appStateRef.current = nextAppState;
    });

    return () => {
      subscription.remove();
    };
  }, []);

  // Cross-fade transition between tabs (60 ms out, 60 ms in)
  const navigateTo = useCallback((newRoute: NavRoute) => {
    if (newRoute === currentRoute) return;

    Animated.timing(fadeAnim, {
      toValue: 0,
      duration: 60,
      useNativeDriver: true,
    }).start(() => {
      setPreviousRoute(currentRoute);
      setCurrentRoute(newRoute);
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 60,
        useNativeDriver: true,
      }).start();
    });
  }, [currentRoute, fadeAnim]);

  // Hardware back button handler
  useEffect(() => {
    const onBackPress = () => {
      if (isLocked) return true; // Handled by biometric overlay

      if (currentRoute === 'settings') {
        navigateTo(previousRoute !== 'settings' ? previousRoute : 'home');
        return true;
      }

      if (currentRoute !== 'home') {
        // Popping back to start destination
        navigateTo('home');
        return true;
      }

      // On home: exit app
      return false;
    };

    const backHandler = BackHandler.addEventListener('hardwareBackPress', onBackPress);
    return () => backHandler.remove();
  }, [currentRoute, previousRoute, navigateTo, isLocked]);

  if (!isReady) {
    // Single blank background-colored surface shown while settings load
    return (
      <Surface style={[styles.blankFrame, { backgroundColor: colors.background }]} elevation={0}>
        <View />
      </Surface>
    );
  }

  return (
    <SafeAreaView
      style={[styles.safeArea, { backgroundColor: colors.surface }]}
      edges={['top', 'left', 'right', 'bottom']}
    >
      <RNStatusBar
        barStyle={isDark ? 'light-content' : 'dark-content'}
        backgroundColor={colors.surface}
        translucent={false}
      />

      {/* Screen host container with 60ms crossfade transition */}
      <Animated.View style={[styles.screenContainer, { opacity: fadeAnim }]}>
        {currentRoute === 'home' && (
          <HomeScreen
            onNavigate={navigateTo}
            selectedDate={homeSelectedDate}
            setSelectedDate={setHomeSelectedDate}
          />
        )}

        {currentRoute === 'attendance' && (
          <AttendanceScreen
            onNavigate={navigateTo}
            selectedDate={attendanceSelectedDate}
            setSelectedDate={setAttendanceSelectedDate}
            isScreenActive={currentRoute === 'attendance'}
          />
        )}

        {currentRoute === 'students' && (
          <ManageStudentsScreen onNavigate={navigateTo} />
        )}

        {currentRoute === 'classes' && (
          <ManageClassesScreen onNavigate={navigateTo} />
        )}

        {currentRoute === 'settings' && (
          <SettingsScreen
            onBack={() => navigateTo(previousRoute !== 'settings' ? previousRoute : 'home')}
            selectedExportClassId={selectedExportClassId}
            setSelectedExportClassId={setSelectedExportClassId}
          />
        )}
      </Animated.View>

      {/* Bottom Navigation Bar */}
      <BottomNavBar
        currentRoute={currentRoute}
        onNavigate={navigateTo}
      />

      {/* Global Biometric Lock Overlay */}
      <BiometricOverlay
        visible={isLocked}
        onUnlockSuccess={() => setIsLocked(false)}
      />
    </SafeAreaView>
  );
}

export default function App() {
  return (
    <SafeAreaProvider>
      <ThemeProvider>
        <MainApp />
      </ThemeProvider>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  blankFrame: {
    flex: 1,
  },
  safeArea: {
    flex: 1,
  },
  screenContainer: {
    flex: 1,
  },
});

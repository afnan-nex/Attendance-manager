import React, { useEffect, useRef } from 'react';
import {
  View,
  StyleSheet,
  BackHandler,
} from 'react-native';
import { Button, Text, Surface } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import * as LocalAuthentication from 'expo-local-authentication';
import { useAppTheme } from '../theme/ThemeContext';

interface BiometricOverlayProps {
  visible: boolean;
  onUnlockSuccess: () => void;
}

export const BiometricOverlay: React.FC<BiometricOverlayProps> = ({
  visible,
  onUnlockSuccess,
}) => {
  const { colors, typography } = useAppTheme();
  const isPromptActiveRef = useRef(false);

  const triggerAuth = async () => {
    if (isPromptActiveRef.current) return;
    isPromptActiveRef.current = true;

    try {
      const hasHardware = await LocalAuthentication.hasHardwareAsync();
      const isEnrolled = await LocalAuthentication.isEnrolledAsync();

      if (!hasHardware || !isEnrolled) {
        onUnlockSuccess();
        return;
      }

      const result = await LocalAuthentication.authenticateAsync({
        promptMessage: 'Unlock Attendance Manager',
        cancelLabel: 'Cancel',
        disableDeviceFallback: false,
      });

      if (result.success) {
        onUnlockSuccess();
      }
    } catch (err) {
      console.warn('Biometric prompt error:', err);
    } finally {
      isPromptActiveRef.current = false;
    }
  };

  useEffect(() => {
    if (visible) {
      // System prompt auto-fires ~250 ms after overlay appears
      const timer = setTimeout(() => {
        triggerAuth();
      }, 250);

      // Disable back button while locked
      const backHandler = BackHandler.addEventListener('hardwareBackPress', () => true);

      return () => {
        clearTimeout(timer);
        backHandler.remove();
      };
    }
  }, [visible]);

  if (!visible) return null;

  return (
    <Surface
      elevation={0}
      style={[
        styles.container,
        { backgroundColor: colors.background },
      ]}
    >
      <View style={styles.content}>
        {/* Closed-lock icon 72 dp (primary) */}
        <MaterialCommunityIcons
          name="lock"
          size={72}
          color={colors.primary}
          style={styles.lockIcon}
        />

        {/* Title: Attendance Manager */}
        <Text
          variant="headlineSmall"
          style={[
            typography.headlineSmall,
            styles.title,
            { color: colors.onSurface, fontWeight: '700' },
          ]}
        >
          Attendance Manager
        </Text>

        {/* Muted subtitle: Unlock to continue */}
        <Text
          variant="bodyMedium"
          style={[
            typography.bodyMedium,
            styles.subtitle,
            { color: colors.onSurfaceVariant },
          ]}
        >
          Unlock to continue
        </Text>

        {/* M3 Filled button: Unlock */}
        <Button
          mode="contained"
          onPress={triggerAuth}
          buttonColor={colors.primary}
          textColor={colors.onPrimary}
          style={styles.unlockButton}
          contentStyle={{ height: 48 }}
          labelStyle={{ fontSize: 16, fontWeight: '700' }}
        >
          Unlock
        </Button>
      </View>
    </Surface>
  );
};

const styles = StyleSheet.create({
  container: {
    ...StyleSheet.absoluteFill,
    zIndex: 99999,
    justifyContent: 'center',
    alignItems: 'center',
  },
  content: {
    alignItems: 'center',
    paddingHorizontal: 32,
    width: '100%',
    maxWidth: 360,
  },
  lockIcon: {
    marginBottom: 24,
  },
  title: {
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    textAlign: 'center',
    marginBottom: 32,
  },
  unlockButton: {
    width: '100%',
    borderRadius: 24,
  },
});

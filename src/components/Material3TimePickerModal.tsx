import React, { useState, useEffect, useMemo } from 'react';
import {
  View,
  StyleSheet,
  PanResponder,
  GestureResponderEvent,
  TouchableWithoutFeedback,
  Modal,
  Pressable,
} from 'react-native';
import {
  Button,
  Text,
  Surface,
  TouchableRipple,
} from 'react-native-paper';
import * as Haptics from 'expo-haptics';
import { useAppTheme } from '../theme/ThemeContext';

interface Material3TimePickerModalProps {
  visible: boolean;
  initialTime24?: string; // e.g. "09:00" or "14:30"
  title?: string;
  onApply: (time24: string) => void;
  onCancel: () => void;
}

const DIAL_SIZE = 256;
const RADIUS = DIAL_SIZE / 2; // 128
const NUMBERS_RADIUS = 92;
const BUBBLE_SIZE = 40;

const HOURS = [12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];
const MINUTES_5 = ['00', '05', '10', '15', '20', '25', '30', '35', '40', '45', '50', '55'];

export const Material3TimePickerModal: React.FC<Material3TimePickerModalProps> = ({
  visible,
  initialTime24 = '09:00',
  title = 'Select Time',
  onApply,
  onCancel,
}) => {
  const { colors } = useAppTheme();

  // Mode: 'hour' or 'minute'
  const [mode, setMode] = useState<'hour' | 'minute'>('hour');

  // Selected values
  const [selectedHour, setSelectedHour] = useState<number>(9); // 1-12
  const [selectedMinute, setSelectedMinute] = useState<number>(0); // 0-59
  const [amPm, setAmPm] = useState<'AM' | 'PM'>('AM');

  // Initialize when modal becomes visible
  useEffect(() => {
    if (visible) {
      const [hStr, mStr] = (initialTime24 || '09:00').split(':');
      let h24 = parseInt(hStr, 10) || 9;
      const m = parseInt(mStr, 10) || 0;

      const isPm = h24 >= 12;
      let h12 = h24 % 12;
      if (h12 === 0) h12 = 12;

      setSelectedHour(h12);
      setSelectedMinute(m);
      setAmPm(isPm ? 'PM' : 'AM');
      setMode('hour');
    }
  }, [visible, initialTime24]);

  // Safe Haptic feedback trigger
  const triggerHaptic = () => {
    try {
      Haptics.selectionAsync();
    } catch {
      // ignore in environments without haptic support
    }
  };

  // Convert touch coordinate to angle & value
  const handleTouch = (evt: GestureResponderEvent, isRelease = false) => {
    const { locationX, locationY } = evt.nativeEvent;
    const dx = locationX - RADIUS;
    const dy = locationY - RADIUS;
    const dist = Math.sqrt(dx * dx + dy * dy);

    if (dist < 10) return; // Dead zone around center pin

    // Angle: 0 deg at top (12 o'clock), clockwise 0..360
    let angle = Math.atan2(dy, dx) * (180 / Math.PI) + 90;
    if (angle < 0) angle += 360;

    if (mode === 'hour') {
      let h = Math.round(angle / 30);
      if (h === 0) h = 12;
      if (h !== selectedHour) {
        setSelectedHour(h);
        triggerHaptic();
      }
      if (isRelease) {
        // Smoothly advance to minute mode
        setTimeout(() => {
          setMode('minute');
        }, 120);
      }
    } else {
      let m = Math.round(angle / 6);
      if (m === 60) m = 0;
      if (m !== selectedMinute) {
        setSelectedMinute(m);
        triggerHaptic();
      }
    }
  };

  // Pan responder for smooth circular touch and dragging
  const panResponder = useMemo(
    () =>
      PanResponder.create({
        onStartShouldSetPanResponder: () => true,
        onMoveShouldSetPanResponder: () => true,
        onPanResponderGrant: (evt) => {
          handleTouch(evt, false);
        },
        onPanResponderMove: (evt) => {
          handleTouch(evt, false);
        },
        onPanResponderRelease: (evt) => {
          handleTouch(evt, true);
        },
      }),
    [mode, selectedHour, selectedMinute]
  );

  // Apply button handler
  const handleApply = () => {
    let h24 = selectedHour % 12;
    if (amPm === 'PM') h24 += 12;
    const time24 = `${String(h24).padStart(2, '0')}:${String(selectedMinute).padStart(2, '0')}`;
    onApply(time24);
  };

  // Current angle in degrees (0 = top / 12 o'clock, 90 = 3 o'clock, etc.)
  const currentAngleDeg = useMemo(() => {
    if (mode === 'hour') {
      return (selectedHour % 12) * 30;
    }
    return selectedMinute * 6;
  }, [mode, selectedHour, selectedMinute]);

  const displayedBubbleValue = useMemo(() => {
    if (mode === 'hour') {
      return String(selectedHour);
    }
    return String(selectedMinute).padStart(2, '0');
  }, [mode, selectedHour, selectedMinute]);

  if (!visible) return null;

  return (
    <Modal
      visible={visible}
      transparent={true}
      animationType="fade"
      statusBarTranslucent={true}
      onRequestClose={onCancel}
    >
      <TouchableWithoutFeedback onPress={onCancel}>
        <View style={styles.modalBackdrop}>
          <TouchableWithoutFeedback onPress={(e) => e.stopPropagation()}>
            <Surface
              elevation={4}
              style={[
                styles.dialog,
                { backgroundColor: colors.surfaceContainerHigh },
              ]}
            >
              {/* Title Header */}
              <Text
                variant="labelMedium"
                style={[
                  styles.dialogTitle,
                  { color: colors.onSurfaceVariant, fontWeight: '600' },
                ]}
              >
                {title.toUpperCase()}
              </Text>

              {/* Material 3 Time Display & AM/PM Row */}
              <View style={styles.timeDisplayRow}>
                {/* Hour & Minute Digits */}
                <View style={styles.digitContainer}>
                  {/* Hour Box */}
                  <Pressable
                    onPress={() => {
                      setMode('hour');
                      triggerHaptic();
                    }}
                    style={[
                      styles.digitBox,
                      {
                        backgroundColor:
                          mode === 'hour'
                            ? colors.primaryContainer
                            : colors.surfaceContainerHighest,
                        borderColor:
                          mode === 'hour' ? colors.primary : 'transparent',
                      },
                    ]}
                  >
                    <Text
                      variant="displaySmall"
                      style={[
                        styles.digitText,
                        {
                          color:
                            mode === 'hour'
                              ? colors.onPrimaryContainer
                              : colors.onSurface,
                          fontWeight: '700',
                        },
                      ]}
                    >
                      {String(selectedHour).padStart(2, '0')}
                    </Text>
                  </Pressable>

                  {/* Separator Colon */}
                  <Text
                    variant="displaySmall"
                    style={[
                      styles.colonText,
                      { color: colors.onSurface, fontWeight: '700' },
                    ]}
                  >
                    :
                  </Text>

                  {/* Minute Box */}
                  <Pressable
                    onPress={() => {
                      setMode('minute');
                      triggerHaptic();
                    }}
                    style={[
                      styles.digitBox,
                      {
                        backgroundColor:
                          mode === 'minute'
                            ? colors.primaryContainer
                            : colors.surfaceContainerHighest,
                        borderColor:
                          mode === 'minute' ? colors.primary : 'transparent',
                      },
                    ]}
                  >
                    <Text
                      variant="displaySmall"
                      style={[
                        styles.digitText,
                        {
                          color:
                            mode === 'minute'
                              ? colors.onPrimaryContainer
                              : colors.onSurface,
                          fontWeight: '700',
                        },
                      ]}
                    >
                      {String(selectedMinute).padStart(2, '0')}
                    </Text>
                  </Pressable>
                </View>

                {/* AM / PM Segmented Selector */}
                <View
                  style={[
                    styles.amPmColumn,
                    {
                      borderColor: colors.outlineVariant,
                      backgroundColor: colors.surfaceContainerHighest,
                    },
                  ]}
                >
                  <Pressable
                    onPress={() => {
                      setAmPm('AM');
                      triggerHaptic();
                    }}
                    style={[
                      styles.amPmOption,
                      amPm === 'AM' && {
                        backgroundColor: colors.primary,
                      },
                    ]}
                  >
                    <Text
                      variant="labelLarge"
                      style={{
                        color:
                          amPm === 'AM'
                            ? colors.onPrimary
                            : colors.onSurfaceVariant,
                        fontWeight: amPm === 'AM' ? '700' : '600',
                      }}
                    >
                      AM
                    </Text>
                  </Pressable>

                  <View
                    style={[
                      styles.amPmDivider,
                      { backgroundColor: colors.outlineVariant },
                    ]}
                  />

                  <Pressable
                    onPress={() => {
                      setAmPm('PM');
                      triggerHaptic();
                    }}
                    style={[
                      styles.amPmOption,
                      amPm === 'PM' && {
                        backgroundColor: colors.primary,
                      },
                    ]}
                  >
                    <Text
                      variant="labelLarge"
                      style={{
                        color:
                          amPm === 'PM'
                            ? colors.onPrimary
                            : colors.onSurfaceVariant,
                        fontWeight: amPm === 'PM' ? '700' : '600',
                      }}
                    >
                      PM
                    </Text>
                  </Pressable>
                </View>
              </View>

              {/* Material 3 Analog Dial */}
              <View style={styles.dialWrapper}>
                <View
                  {...panResponder.panHandlers}
                  style={[
                    styles.dialSurface,
                    { backgroundColor: colors.surfaceContainerHighest },
                  ]}
                >
                  {/* Static Clock Numbers */}
                  {mode === 'hour'
                    ? HOURS.map((h, i) => {
                        const angleRad = ((i * 30 - 90) * Math.PI) / 180;
                        const nx = RADIUS + NUMBERS_RADIUS * Math.cos(angleRad);
                        const ny = RADIUS + NUMBERS_RADIUS * Math.sin(angleRad);
                        const isSelected = selectedHour === h;

                        return (
                          <View
                            key={`h_${h}`}
                            pointerEvents="none"
                            style={[
                              styles.numberCell,
                              {
                                left: nx - 18,
                                top: ny - 18,
                              },
                            ]}
                          >
                            <Text
                              variant="bodyLarge"
                              style={[
                                styles.numberText,
                                {
                                  color: isSelected
                                    ? colors.onPrimary
                                    : colors.onSurface,
                                  fontWeight: isSelected ? '700' : '500',
                                  opacity: isSelected ? 0 : 1, // hidden under bubble
                                },
                              ]}
                            >
                              {h}
                            </Text>
                          </View>
                        );
                      })
                    : MINUTES_5.map((mStr, i) => {
                        const mVal = i * 5;
                        const angleRad = ((i * 30 - 90) * Math.PI) / 180;
                        const nx = RADIUS + NUMBERS_RADIUS * Math.cos(angleRad);
                        const ny = RADIUS + NUMBERS_RADIUS * Math.sin(angleRad);
                        const isSelected = selectedMinute === mVal;

                        return (
                          <View
                            key={`m_${mStr}`}
                            pointerEvents="none"
                            style={[
                              styles.numberCell,
                              {
                                left: nx - 18,
                                top: ny - 18,
                              },
                            ]}
                          >
                            <Text
                              variant="bodyLarge"
                              style={[
                                styles.numberText,
                                {
                                  color: isSelected
                                    ? colors.onPrimary
                                    : colors.onSurface,
                                  fontWeight: isSelected ? '700' : '500',
                                  opacity: isSelected ? 0 : 1, // hidden under bubble
                                },
                              ]}
                            >
                              {mStr}
                            </Text>
                          </View>
                        );
                      })}

                  {/* Rigid Rotated Hand + Bubble Layer: Hand and circle NEVER disconnect */}
                  <View
                    pointerEvents="none"
                    style={[
                      StyleSheet.absoluteFill,
                      {
                        transform: [{ rotate: `${currentAngleDeg}deg` }],
                        alignItems: 'center',
                      },
                    ]}
                  >
                    {/* Selector Circle Bubble at top position */}
                    <View
                      style={[
                        styles.selectorBubble,
                        {
                          backgroundColor: colors.primary,
                          marginTop: RADIUS - NUMBERS_RADIUS - BUBBLE_SIZE / 2,
                        },
                      ]}
                    >
                      <Text
                        style={[
                          styles.bubbleText,
                          {
                            color: colors.onPrimary,
                            transform: [{ rotate: `-${currentAngleDeg}deg` }], // keep text upright
                          },
                        ]}
                      >
                        {displayedBubbleValue}
                      </Text>
                    </View>

                    {/* Clock Hand Line pointing directly from center to bubble */}
                    <View
                      style={[
                        styles.rigidHandLine,
                        {
                          top: RADIUS - NUMBERS_RADIUS + BUBBLE_SIZE / 2,
                          height: NUMBERS_RADIUS - BUBBLE_SIZE / 2,
                          backgroundColor: colors.primary,
                        },
                      ]}
                    />
                  </View>

                  {/* Center Pin */}
                  <View
                    pointerEvents="none"
                    style={[
                      styles.centerPin,
                      { backgroundColor: colors.primary },
                    ]}
                  />
                </View>
              </View>

              {/* Dialog Actions */}
              <View
                style={[
                  styles.actionsRow,
                  { borderTopColor: colors.outlineVariant },
                ]}
              >
                <Button
                  mode="text"
                  onPress={onCancel}
                  textColor={colors.primary}
                  style={styles.actionBtn}
                >
                  Cancel
                </Button>

                <Button
                  mode="contained"
                  onPress={handleApply}
                  buttonColor={colors.primary}
                  textColor={colors.onPrimary}
                  style={[styles.actionBtn, { marginLeft: 8 }]}
                >
                  OK
                </Button>
              </View>
            </Surface>
          </TouchableWithoutFeedback>
        </View>
      </TouchableWithoutFeedback>
    </Modal>
  );
};

const styles = StyleSheet.create({
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.54)',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 16,
  },
  dialog: {
    width: '100%',
    maxWidth: 340,
    borderRadius: 28,
    paddingTop: 20,
    overflow: 'hidden',
  },
  dialogTitle: {
    paddingHorizontal: 24,
    marginBottom: 16,
    letterSpacing: 0.8,
  },
  timeDisplayRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 24,
    marginBottom: 20,
  },
  digitContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  digitBox: {
    width: 80,
    height: 68,
    borderRadius: 12,
    borderWidth: 2,
    alignItems: 'center',
    justifyContent: 'center',
  },
  digitText: {
    fontSize: 38,
    lineHeight: 46,
  },
  colonText: {
    fontSize: 36,
    marginHorizontal: 4,
    lineHeight: 44,
  },
  amPmColumn: {
    width: 48,
    height: 68,
    borderRadius: 12,
    borderWidth: 1,
    overflow: 'hidden',
  },
  amPmOption: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  amPmDivider: {
    height: StyleSheet.hairlineWidth,
    width: '100%',
  },
  dialWrapper: {
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },
  dialSurface: {
    width: DIAL_SIZE,
    height: DIAL_SIZE,
    borderRadius: RADIUS,
    position: 'relative',
  },
  numberCell: {
    position: 'absolute',
    width: 36,
    height: 36,
    alignItems: 'center',
    justifyContent: 'center',
  },
  numberText: {
    fontSize: 16,
  },
  selectorBubble: {
    width: BUBBLE_SIZE,
    height: BUBBLE_SIZE,
    borderRadius: BUBBLE_SIZE / 2,
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 10,
  },
  bubbleText: {
    fontSize: 16,
    fontWeight: '700',
  },
  rigidHandLine: {
    position: 'absolute',
    width: 2,
    zIndex: 5,
  },
  centerPin: {
    position: 'absolute',
    left: RADIUS - 4,
    top: RADIUS - 4,
    width: 8,
    height: 8,
    borderRadius: 4,
    zIndex: 20,
  },
  actionsRow: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  actionBtn: {
    borderRadius: 20,
    minWidth: 72,
  },
});

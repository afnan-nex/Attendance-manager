import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  StyleSheet,
  ScrollView,
  Platform,
  Keyboard,
  TouchableWithoutFeedback,
  Modal,
  Pressable,
} from 'react-native';
import {
  TextInput as PaperInput,
  Button,
  Text,
  Surface,
  TouchableRipple,
} from 'react-native-paper';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useAppTheme } from '../theme/ThemeContext';
import { ClassEntity, LectureType, Weekday } from '../types';
import { WEEKDAYS, formatTime12h } from '../utils/dateUtils';
import { validateClass } from '../utils/validation';
import { showShortToast, showLongToast } from '../utils/toast';
import { Material3TimePickerModal } from './Material3TimePickerModal';

interface AddEditClassModalProps {
  visible: boolean;
  classToEdit?: ClassEntity | null;
  onSave: (classItem: ClassEntity) => Promise<void>;
  onCancel: () => void;
}

const LECTURE_TYPES: LectureType[] = [
  'Lecture',
  'Tutorial',
  'Practical Lab',
  'Workshop',
  'Seminar',
  'Other',
];

const CREDIT_HOURS = [1, 2, 3, 4, 5];

export const AddEditClassModal: React.FC<AddEditClassModalProps> = ({
  visible,
  classToEdit,
  onSave,
  onCancel,
}) => {
  const { colors, typography } = useAppTheme();
  const insets = useSafeAreaInsets();
  const isEdit = Boolean(classToEdit);

  // Keyboard height tracker to keep popup strictly above the keyboard
  const [keyboardHeight, setKeyboardHeight] = useState(0);

  useEffect(() => {
    const showEvent = Platform.OS === 'ios' ? 'keyboardWillShow' : 'keyboardDidShow';
    const hideEvent = Platform.OS === 'ios' ? 'keyboardWillHide' : 'keyboardDidHide';

    const showSub = Keyboard.addListener(showEvent, (e) => {
      setKeyboardHeight(e.endCoordinates.height);
    });
    const hideSub = Keyboard.addListener(hideEvent, () => {
      setKeyboardHeight(0);
    });

    return () => {
      showSub.remove();
      hideSub.remove();
    };
  }, []);

  // Form states
  const [shortName, setShortName] = useState('');
  const [fullNameWithCode, setFullNameWithCode] = useState('');
  const [lectureType, setLectureType] = useState<LectureType | ''>('');
  const [day, setDay] = useState<Weekday | ''>('');
  const [startTime, setStartTime] = useState('09:00'); // 24h
  const [endTime, setEndTime] = useState('10:00');     // 24h
  const [teacherName, setTeacherName] = useState('');
  const [location, setLocation] = useState('');
  const [creditHours, setCreditHours] = useState<number | null>(null);

  // Dropdown states
  const [typeDropdownOpen, setTypeDropdownOpen] = useState(false);
  const [dayDropdownOpen, setDayDropdownOpen] = useState(false);
  const [creditsDropdownOpen, setCreditsDropdownOpen] = useState(false);

  // Time picker dialog state
  const [timePickerTarget, setTimePickerTarget] = useState<'start' | 'end' | null>(null);

  // Input refs for keyboard navigation
  const shortNameRef = useRef<any>(null);
  const fullNameRef = useRef<any>(null);
  const teacherNameRef = useRef<any>(null);
  const locationRef = useRef<any>(null);

  useEffect(() => {
    if (visible) {
      if (classToEdit) {
        setShortName(classToEdit.shortName);
        setFullNameWithCode(classToEdit.fullNameWithCode);
        setLectureType(classToEdit.lectureType);
        setDay(classToEdit.day);
        setStartTime(classToEdit.startTime || '09:00');
        setEndTime(classToEdit.endTime || '10:00');
        setTeacherName(classToEdit.teacherName || '');
        setLocation(classToEdit.location || '');
        setCreditHours(classToEdit.creditHours);
      } else {
        setShortName('');
        setFullNameWithCode('');
        setLectureType('');
        setDay('');
        setStartTime('09:00');
        setEndTime('10:00');
        setTeacherName('');
        setLocation('');
        setCreditHours(null);
      }
      setTypeDropdownOpen(false);
      setDayDropdownOpen(false);
      setCreditsDropdownOpen(false);
      setTimePickerTarget(null);
    }
  }, [visible, classToEdit]);

  // Short Name: max 10 chars, first char capitalized
  const handleShortNameChange = (text: string) => {
    let limited = text.slice(0, 10);
    if (limited.length > 0) {
      limited = limited.charAt(0).toUpperCase() + limited.slice(1);
    }
    setShortName(limited);
  };

  // Full Name: max 100 chars, first char capitalized
  const handleFullNameChange = (text: string) => {
    let limited = text.slice(0, 100);
    if (limited.length > 0) {
      limited = limited.charAt(0).toUpperCase() + limited.slice(1);
    }
    setFullNameWithCode(limited);
  };

  // Teacher Name: word capitalization
  const handleTeacherNameChange = (text: string) => {
    const capitalized = text.replace(/(^|\s)\S/g, (l) => l.toUpperCase());
    setTeacherName(capitalized);
  };

  // Location: first char capitalized
  const handleLocationChange = (text: string) => {
    if (!text) {
      setLocation('');
      return;
    }
    const capitalized = text.charAt(0).toUpperCase() + text.slice(1);
    setLocation(capitalized);
  };

  // Save enablement: Required fields are Short Name, Full Name, Type, Day, Credit Hours
  const isSaveEnabled =
    shortName.trim().length > 0 &&
    fullNameWithCode.trim().length > 0 &&
    lectureType !== '' &&
    day !== '' &&
    creditHours !== null;

  const handleSavePress = async () => {
    Keyboard.dismiss();
    const validation = validateClass({
      shortName,
      fullNameWithCode,
      lectureType: lectureType || undefined,
      day: day || undefined,
      startTime,
      endTime,
      teacherName,
      location,
      creditHours: creditHours || undefined,
    });

    if (!validation.isValid) {
      showLongToast(validation.errorMessage || 'Validation error');
      return;
    }

    if (isEdit && classToEdit) {
      const updated: ClassEntity = {
        ...classToEdit,
        shortName: shortName.trim(),
        fullNameWithCode: fullNameWithCode.trim(),
        lectureType: lectureType as LectureType,
        day: day as Weekday,
        startTime,
        endTime,
        teacherName: teacherName.trim(),
        location: location.trim(),
        creditHours: creditHours as number,
      };
      await onSave(updated);
    } else {
      const newClass: ClassEntity = {
        id: `class_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`,
        shortName: shortName.trim(),
        fullNameWithCode: fullNameWithCode.trim(),
        lectureType: lectureType as LectureType,
        day: day as Weekday,
        startTime,
        endTime,
        teacherName: teacherName.trim(),
        location: location.trim(),
        creditHours: creditHours as number,
        isHidden: false,
      };
      await onSave(newClass);
    }

    showShortToast('Class saved');
    onCancel();
  };

  const handleDismiss = () => {
    Keyboard.dismiss();
    onCancel();
  };

  if (!visible) return null;

  return (
    <>
      <Modal
        visible={visible}
        transparent={true}
        animationType="fade"
        statusBarTranslucent={true}
        onRequestClose={handleDismiss}
      >
        <TouchableWithoutFeedback onPress={handleDismiss}>
          <View
            style={[
              styles.modalBackdrop,
              {
                paddingTop: Math.max(insets.top + 8, 16),
                paddingBottom: keyboardHeight > 0 ? keyboardHeight + 8 : Math.max(insets.bottom + 8, 16),
              },
            ]}
          >
            <TouchableWithoutFeedback onPress={(e) => e.stopPropagation()}>
              <Surface
                elevation={3}
                style={[
                  styles.dialog,
                  { backgroundColor: colors.surfaceContainerHigh },
                ]}
              >
                {/* Header: Anchored at the top beneath the notch */}
                <View style={[styles.dialogHeader, { borderBottomColor: colors.outlineVariant }]}>
                  <Text
                    variant="titleLarge"
                    style={[
                      typography.titleLarge,
                      styles.title,
                      { color: colors.onSurface, fontWeight: '700' },
                    ]}
                  >
                    {isEdit ? 'Edit Class' : 'Add Class'}
                  </Text>

                  <Button
                    mode="contained"
                    compact
                    disabled={!isSaveEnabled}
                    onPress={handleSavePress}
                    buttonColor={colors.primary}
                    textColor={colors.onPrimary}
                    style={styles.topSaveBtn}
                  >
                    Save
                  </Button>
                </View>

                {/* Scrollable Form Body */}
                <ScrollView
                  style={styles.scrollArea}
                  contentContainerStyle={styles.scrollContent}
                  keyboardShouldPersistTaps="always"
                  showsVerticalScrollIndicator={true}
                >
                  {/* 1. Short Name */}
                  <PaperInput
                    ref={shortNameRef}
                    mode="outlined"
                    label="Short Name *"
                    value={shortName}
                    onChangeText={handleShortNameChange}
                    placeholder="e.g. CP"
                    maxLength={10}
                    returnKeyType="next"
                    onSubmitEditing={() => fullNameRef.current?.focus()}
                    blurOnSubmit={false}
                    style={styles.m3Input}
                    outlineColor={colors.outline}
                    activeOutlineColor={colors.primary}
                    textColor={colors.onSurface}
                  />

                  {/* 2. Full Name with Code */}
                  <PaperInput
                    ref={fullNameRef}
                    mode="outlined"
                    label="Full Name with Code *"
                    value={fullNameWithCode}
                    onChangeText={handleFullNameChange}
                    placeholder="e.g. CS-101 Computer Programming"
                    maxLength={100}
                    returnKeyType="next"
                    onSubmitEditing={() => teacherNameRef.current?.focus()}
                    blurOnSubmit={false}
                    style={styles.m3Input}
                    outlineColor={colors.outline}
                    activeOutlineColor={colors.primary}
                    textColor={colors.onSurface}
                  />

                  {/* 3. Lecture Type dropdown */}
                  <View style={styles.dropdownContainer}>
                    <Pressable
                      onPress={() => {
                        setTypeDropdownOpen(!typeDropdownOpen);
                        setDayDropdownOpen(false);
                        setCreditsDropdownOpen(false);
                      }}
                      style={styles.dropdownTrigger}
                    >
                      <View pointerEvents="none">
                        <PaperInput
                          mode="outlined"
                          label="Lecture Type *"
                          value={lectureType}
                          placeholder="Select Lecture Type"
                          editable={false}
                          right={
                            <PaperInput.Icon
                              icon={typeDropdownOpen ? 'chevron-up' : 'chevron-down'}
                              color={colors.onSurfaceVariant}
                            />
                          }
                          style={styles.m3Input}
                          outlineColor={colors.outline}
                          activeOutlineColor={colors.primary}
                          textColor={colors.onSurface}
                        />
                      </View>
                    </Pressable>

                    {typeDropdownOpen && (
                      <Surface
                        elevation={2}
                        style={[
                          styles.dropdownMenu,
                          {
                            backgroundColor: colors.surfaceContainerHighest,
                            borderColor: colors.outlineVariant,
                          },
                        ]}
                      >
                        {LECTURE_TYPES.map((lt) => (
                          <TouchableRipple
                            key={lt}
                            onPress={() => {
                              setLectureType(lt);
                              setTypeDropdownOpen(false);
                            }}
                            style={[
                              styles.dropdownOption,
                              lectureType === lt && {
                                backgroundColor: colors.primaryContainer,
                              },
                            ]}
                          >
                            <Text
                              variant="bodyMedium"
                              style={{
                                color:
                                  lectureType === lt
                                    ? colors.onPrimaryContainer
                                    : colors.onSurface,
                                fontWeight: lectureType === lt ? '700' : '500',
                              }}
                            >
                              {lt}
                            </Text>
                          </TouchableRipple>
                        ))}
                      </Surface>
                    )}
                  </View>

                  {/* 4. Day dropdown */}
                  <View style={styles.dropdownContainer}>
                    <Pressable
                      onPress={() => {
                        setDayDropdownOpen(!dayDropdownOpen);
                        setTypeDropdownOpen(false);
                        setCreditsDropdownOpen(false);
                      }}
                      style={styles.dropdownTrigger}
                    >
                      <View pointerEvents="none">
                        <PaperInput
                          mode="outlined"
                          label="Day *"
                          value={day}
                          placeholder="Select Day"
                          editable={false}
                          right={
                            <PaperInput.Icon
                              icon={dayDropdownOpen ? 'chevron-up' : 'chevron-down'}
                              color={colors.onSurfaceVariant}
                            />
                          }
                          style={styles.m3Input}
                          outlineColor={colors.outline}
                          activeOutlineColor={colors.primary}
                          textColor={colors.onSurface}
                        />
                      </View>
                    </Pressable>

                    {dayDropdownOpen && (
                      <Surface
                        elevation={2}
                        style={[
                          styles.dropdownMenu,
                          {
                            backgroundColor: colors.surfaceContainerHighest,
                            borderColor: colors.outlineVariant,
                          },
                        ]}
                      >
                        {WEEKDAYS.map((w) => (
                          <TouchableRipple
                            key={w}
                            onPress={() => {
                              setDay(w);
                              setDayDropdownOpen(false);
                            }}
                            style={[
                              styles.dropdownOption,
                              day === w && {
                                backgroundColor: colors.primaryContainer,
                              },
                            ]}
                          >
                            <Text
                              variant="bodyMedium"
                              style={{
                                color:
                                  day === w
                                    ? colors.onPrimaryContainer
                                    : colors.onSurface,
                                fontWeight: day === w ? '700' : '500',
                              }}
                            >
                              {w}
                            </Text>
                          </TouchableRipple>
                        ))}
                      </Surface>
                    )}
                  </View>

                  {/* 5. Start Time / End Time pickers (Tapping entire box opens picker) */}
                  <View style={styles.timeRowContainer}>
                    <Pressable
                      style={styles.timeCol}
                      onPress={() => setTimePickerTarget('start')}
                    >
                      <View pointerEvents="none">
                        <PaperInput
                          mode="outlined"
                          label="Start Time"
                          value={formatTime12h(startTime)}
                          editable={false}
                          right={
                            <PaperInput.Icon
                              icon="clock-outline"
                              color={colors.primary}
                            />
                          }
                          style={styles.m3Input}
                          outlineColor={colors.outline}
                          activeOutlineColor={colors.primary}
                          textColor={colors.onSurface}
                        />
                      </View>
                    </Pressable>

                    <Pressable
                      style={styles.timeCol}
                      onPress={() => setTimePickerTarget('end')}
                    >
                      <View pointerEvents="none">
                        <PaperInput
                          mode="outlined"
                          label="End Time"
                          value={formatTime12h(endTime)}
                          editable={false}
                          right={
                            <PaperInput.Icon
                              icon="clock-outline"
                              color={colors.primary}
                            />
                          }
                          style={styles.m3Input}
                          outlineColor={colors.outline}
                          activeOutlineColor={colors.primary}
                          textColor={colors.onSurface}
                        />
                      </View>
                    </Pressable>
                  </View>

                  {/* 6. Teacher Name (Optional) */}
                  <PaperInput
                    ref={teacherNameRef}
                    mode="outlined"
                    label="Teacher Name (Optional)"
                    value={teacherName}
                    onChangeText={handleTeacherNameChange}
                    placeholder="e.g. Dr. Tariq"
                    returnKeyType="next"
                    onSubmitEditing={() => locationRef.current?.focus()}
                    blurOnSubmit={false}
                    style={styles.m3Input}
                    outlineColor={colors.outline}
                    activeOutlineColor={colors.primary}
                    textColor={colors.onSurface}
                  />

                  {/* 7. Location (Optional) */}
                  <PaperInput
                    ref={locationRef}
                    mode="outlined"
                    label="Location (Optional)"
                    value={location}
                    onChangeText={handleLocationChange}
                    placeholder="e.g. Lab 1"
                    returnKeyType="done"
                    onSubmitEditing={() => {
                      if (isSaveEnabled) handleSavePress();
                    }}
                    style={styles.m3Input}
                    outlineColor={colors.outline}
                    activeOutlineColor={colors.primary}
                    textColor={colors.onSurface}
                  />

                  {/* 8. Credit Hours dropdown 1-5 */}
                  <View style={styles.dropdownContainer}>
                    <Pressable
                      onPress={() => {
                        setCreditsDropdownOpen(!creditsDropdownOpen);
                        setTypeDropdownOpen(false);
                        setDayDropdownOpen(false);
                      }}
                      style={styles.dropdownTrigger}
                    >
                      <View pointerEvents="none">
                        <PaperInput
                          mode="outlined"
                          label="Credit Hours *"
                          value={creditHours ? `${creditHours} Credit Hour${creditHours > 1 ? 's' : ''}` : ''}
                          placeholder="Select Credit Hours"
                          editable={false}
                          right={
                            <PaperInput.Icon
                              icon={creditsDropdownOpen ? 'chevron-up' : 'chevron-down'}
                              color={colors.onSurfaceVariant}
                            />
                          }
                          style={styles.m3Input}
                          outlineColor={colors.outline}
                          activeOutlineColor={colors.primary}
                          textColor={colors.onSurface}
                        />
                      </View>
                    </Pressable>

                    {creditsDropdownOpen && (
                      <Surface
                        elevation={2}
                        style={[
                          styles.dropdownMenu,
                          {
                            backgroundColor: colors.surfaceContainerHighest,
                            borderColor: colors.outlineVariant,
                          },
                        ]}
                      >
                        {CREDIT_HOURS.map((ch) => (
                          <TouchableRipple
                            key={ch}
                            onPress={() => {
                              setCreditHours(ch);
                              setCreditsDropdownOpen(false);
                            }}
                            style={[
                              styles.dropdownOption,
                              creditHours === ch && {
                                backgroundColor: colors.primaryContainer,
                              },
                            ]}
                          >
                            <Text
                              variant="bodyMedium"
                              style={{
                                color:
                                  creditHours === ch
                                    ? colors.onPrimaryContainer
                                    : colors.onSurface,
                                fontWeight: creditHours === ch ? '700' : '500',
                              }}
                            >
                              {`${ch} Credit Hour${ch > 1 ? 's' : ''}`}
                            </Text>
                          </TouchableRipple>
                        ))}
                      </Surface>
                    )}
                  </View>

                  {/* Bottom Action Buttons inside ScrollView */}
                  <View style={styles.bottomButtonsRow}>
                    <Button
                      mode="outlined"
                      onPress={handleDismiss}
                      textColor={colors.primary}
                      style={styles.bottomActionBtn}
                    >
                      Cancel
                    </Button>

                    <Button
                      mode="contained"
                      disabled={!isSaveEnabled}
                      onPress={handleSavePress}
                      buttonColor={colors.primary}
                      textColor={colors.onPrimary}
                      style={[styles.bottomActionBtn, { marginLeft: 10 }]}
                    >
                      Save Class
                    </Button>
                  </View>
                </ScrollView>
              </Surface>
            </TouchableWithoutFeedback>
          </View>
        </TouchableWithoutFeedback>
      </Modal>

      {/* Material 3 Analog Time Picker Modal */}
      <Material3TimePickerModal
        visible={timePickerTarget !== null}
        title={timePickerTarget === 'start' ? 'Select Start Time' : 'Select End Time'}
        initialTime24={timePickerTarget === 'start' ? startTime : endTime}
        onApply={(time24) => {
          if (timePickerTarget === 'start') {
            setStartTime(time24);
          } else if (timePickerTarget === 'end') {
            setEndTime(time24);
          }
          setTimePickerTarget(null);
        }}
        onCancel={() => setTimePickerTarget(null)}
      />
    </>
  );
};

const styles = StyleSheet.create({
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.54)',
    justifyContent: 'flex-start',
    alignItems: 'center',
    paddingHorizontal: 12,
  },
  dialog: {
    width: '100%',
    maxWidth: 440,
    maxHeight: '100%',
    borderRadius: 24,
    overflow: 'hidden',
    flexDirection: 'column',
  },
  dialogHeader: {
    flexShrink: 0,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingVertical: 14,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  title: {
    flex: 1,
    margin: 0,
  },
  topSaveBtn: {
    borderRadius: 18,
    minWidth: 72,
  },
  scrollArea: {
    flexShrink: 1,
  },
  scrollContent: {
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  m3Input: {
    marginBottom: 10,
    backgroundColor: 'transparent',
  },
  dropdownContainer: {
    marginBottom: 4,
  },
  dropdownTrigger: {
    borderRadius: 8,
  },
  dropdownMenu: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: 12,
    marginTop: -4,
    marginBottom: 10,
    overflow: 'hidden',
  },
  dropdownOption: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  timeRowContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 8,
  },
  timeCol: {
    flex: 1,
  },
  bottomButtonsRow: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    alignItems: 'center',
    marginTop: 12,
    marginBottom: 24,
  },
  bottomActionBtn: {
    borderRadius: 20,
    flex: 1,
  },
});

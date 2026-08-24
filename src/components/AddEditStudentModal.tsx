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
  Checkbox,
  Surface,
  TouchableRipple,
} from 'react-native-paper';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useAppTheme } from '../theme/ThemeContext';
import { StudentEntity } from '../types';
import { validateStudent } from '../utils/validation';
import { showShortToast, showLongToast } from '../utils/toast';

interface AddEditStudentModalProps {
  visible: boolean;
  studentToEdit?: StudentEntity | null;
  existingStudents: StudentEntity[];
  onSave: (student: StudentEntity) => Promise<void>;
  onCancel: () => void;
}

const SECTIONS = ['A', 'B', 'C', 'D', 'E', 'F'];

export const AddEditStudentModal: React.FC<AddEditStudentModalProps> = ({
  visible,
  studentToEdit,
  existingStudents,
  onSave,
  onCancel,
}) => {
  const { colors, typography } = useAppTheme();
  const insets = useSafeAreaInsets();
  const isEdit = Boolean(studentToEdit);

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
  const [name, setName] = useState('');
  const [cnic, setCnic] = useState('');
  const [regNo, setRegNo] = useState('');
  const [section, setSection] = useState('');
  const [waNumber, setWaNumber] = useState('');
  const [isPhoneSame, setIsPhoneSame] = useState(true);
  const [phNumber, setPhNumber] = useState('');
  const [isSectionMenuOpen, setIsSectionMenuOpen] = useState(false);

  // Input refs for smooth "Next" soft keyboard focus progression
  const nameInputRef = useRef<any>(null);
  const cnicInputRef = useRef<any>(null);
  const regNoInputRef = useRef<any>(null);
  const waInputRef = useRef<any>(null);
  const phInputRef = useRef<any>(null);

  useEffect(() => {
    if (visible) {
      if (studentToEdit) {
        setName(studentToEdit.name);
        setCnic(studentToEdit.cnic || '');
        setRegNo(studentToEdit.regNo);
        setSection(studentToEdit.section);
        setWaNumber(studentToEdit.waNumber || '');
        setIsPhoneSame(studentToEdit.isPhoneSame);
        setPhNumber(studentToEdit.isPhoneSame ? '' : studentToEdit.phNumber);
      } else {
        setName('');
        setCnic('');
        setRegNo('');
        setSection('');
        setWaNumber('');
        setIsPhoneSame(true);
        setPhNumber('');
      }
      setIsSectionMenuOpen(false);
    }
  }, [visible, studentToEdit]);

  // Capitalize first letter of every word
  const handleNameChange = (text: string) => {
    const capitalized = text.replace(/(^|\s)\S/g, (l) => l.toUpperCase());
    setName(capitalized);
  };

  // CNIC: numeric, hard limited to 13 digits
  const handleCnicChange = (text: string) => {
    const digits = text.replace(/\D/g, '').slice(0, 13);
    setCnic(digits);
  };

  // Reg No: first char capitalized
  const handleRegNoChange = (text: string) => {
    if (!text) {
      setRegNo('');
      return;
    }
    const capitalized = text.charAt(0).toUpperCase() + text.slice(1);
    setRegNo(capitalized);
  };

  // WhatsApp: max 13 chars
  const handleWaChange = (text: string) => {
    const cleaned = text.replace(/[^\d+]/g, '').slice(0, 13);
    setWaNumber(cleaned);
  };

  // Save enablement: Name non-blank AND Section chosen
  const isSaveEnabled = name.trim().length > 0 && section.trim().length > 0;

  const handleSavePress = async () => {
    Keyboard.dismiss();
    const effectivePhone = isPhoneSame ? waNumber : phNumber;

    const validation = validateStudent(
      {
        name,
        cnic,
        regNo,
        section,
        waNumber,
        isPhoneSame,
        phNumber: effectivePhone,
      },
      existingStudents,
      studentToEdit?.id
    );

    if (!validation.isValid) {
      showLongToast(validation.errorMessage || 'Validation error');
      return;
    }

    if (isEdit && studentToEdit) {
      const updated: StudentEntity = {
        ...studentToEdit,
        name: name.trim(),
        cnic: cnic.trim(),
        regNo: regNo.trim(),
        section: section.trim().toUpperCase(),
        waNumber: waNumber.trim(),
        isPhoneSame,
        phNumber: isPhoneSame ? waNumber.trim() : phNumber.trim(),
      };
      await onSave(updated);
    } else {
      const maxIndex = existingStudents.reduce(
        (max, s) => Math.max(max, s.orderIndex),
        -1
      );
      const newStudent: StudentEntity = {
        id: `student_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`,
        orderIndex: maxIndex + 1,
        name: name.trim(),
        cnic: cnic.trim(),
        regNo: regNo.trim(),
        section: section.trim().toUpperCase(),
        waNumber: waNumber.trim(),
        isPhoneSame,
        phNumber: isPhoneSame ? waNumber.trim() : phNumber.trim(),
      };
      await onSave(newStudent);
    }

    showShortToast('Student saved');
    onCancel();
  };

  const handleDismiss = () => {
    Keyboard.dismiss();
    onCancel();
  };

  if (!visible) return null;

  return (
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
                {
                  backgroundColor: colors.surfaceContainerHigh,
                },
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
                  {isEdit ? 'Edit Student' : 'Add Student'}
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
                {/* 1. Name */}
                <PaperInput
                  ref={nameInputRef}
                  mode="outlined"
                  label="Full Name *"
                  value={name}
                  onChangeText={handleNameChange}
                  placeholder="e.g. Muhammad Ali"
                  returnKeyType="next"
                  onSubmitEditing={() => cnicInputRef.current?.focus()}
                  blurOnSubmit={false}
                  style={styles.m3Input}
                  outlineColor={colors.outline}
                  activeOutlineColor={colors.primary}
                  textColor={colors.onSurface}
                />

                {/* 2. CNIC */}
                <PaperInput
                  ref={cnicInputRef}
                  mode="outlined"
                  label="CNIC (Optional)"
                  value={cnic}
                  onChangeText={handleCnicChange}
                  placeholder="13 digits without dashes"
                  keyboardType="numeric"
                  maxLength={13}
                  returnKeyType="next"
                  onSubmitEditing={() => regNoInputRef.current?.focus()}
                  blurOnSubmit={false}
                  style={styles.m3Input}
                  outlineColor={colors.outline}
                  activeOutlineColor={colors.primary}
                  textColor={colors.onSurface}
                />

                {/* 3. Registration Number */}
                <PaperInput
                  ref={regNoInputRef}
                  mode="outlined"
                  label="Registration Number *"
                  value={regNo}
                  onChangeText={handleRegNoChange}
                  placeholder="e.g. 25-CS-001"
                  returnKeyType="next"
                  onSubmitEditing={() => waInputRef.current?.focus()}
                  blurOnSubmit={false}
                  style={styles.m3Input}
                  outlineColor={colors.outline}
                  activeOutlineColor={colors.primary}
                  textColor={colors.onSurface}
                />

                {/* 4. Section dropdown (Tapping entire box opens dropdown) */}
                <View style={styles.sectionContainer}>
                  <Pressable
                    onPress={() => setIsSectionMenuOpen(!isSectionMenuOpen)}
                    style={styles.dropdownTrigger}
                  >
                    <View pointerEvents="none">
                      <PaperInput
                        mode="outlined"
                        label="Section *"
                        value={section ? `Section ${section}` : ''}
                        placeholder="Select Section (A-F)"
                        editable={false}
                        right={
                          <PaperInput.Icon
                            icon={isSectionMenuOpen ? 'chevron-up' : 'chevron-down'}
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

                  {isSectionMenuOpen && (
                    <Surface
                      elevation={2}
                      style={[
                        styles.sectionMenu,
                        {
                          backgroundColor: colors.surfaceContainerHighest,
                          borderColor: colors.outlineVariant,
                        },
                      ]}
                    >
                      {SECTIONS.map((sec) => (
                        <TouchableRipple
                          key={sec}
                          onPress={() => {
                            setSection(sec);
                            setIsSectionMenuOpen(false);
                          }}
                          style={[
                            styles.sectionOption,
                            section === sec && {
                              backgroundColor: colors.primaryContainer,
                            },
                          ]}
                        >
                          <Text
                            variant="bodyMedium"
                            style={{
                              color:
                                section === sec
                                  ? colors.onPrimaryContainer
                                  : colors.onSurface,
                              fontWeight: section === sec ? '700' : '500',
                            }}
                          >
                            {`Section ${sec}`}
                          </Text>
                        </TouchableRipple>
                      ))}
                    </Surface>
                  )}
                </View>

                {/* 5. WhatsApp Number (Optional) */}
                <PaperInput
                  ref={waInputRef}
                  mode="outlined"
                  label="WhatsApp Number (Optional)"
                  value={waNumber}
                  onChangeText={handleWaChange}
                  placeholder="+923001234567"
                  keyboardType="phone-pad"
                  maxLength={13}
                  returnKeyType={isPhoneSame ? 'done' : 'next'}
                  onSubmitEditing={() => {
                    if (!isPhoneSame) {
                      phInputRef.current?.focus();
                    } else if (isSaveEnabled) {
                      handleSavePress();
                    }
                  }}
                  blurOnSubmit={isPhoneSame}
                  style={styles.m3Input}
                  outlineColor={colors.outline}
                  activeOutlineColor={colors.primary}
                  textColor={colors.onSurface}
                />

                {/* 6. Material 3 Checkbox "Same as WhatsApp" */}
                <TouchableRipple
                  borderless
                  onPress={() => setIsPhoneSame(!isPhoneSame)}
                  style={styles.checkboxRow}
                >
                  <View style={styles.checkboxContent}>
                    <Checkbox.Android
                      status={isPhoneSame ? 'checked' : 'unchecked'}
                      onPress={() => setIsPhoneSame(!isPhoneSame)}
                      color={colors.primary}
                      uncheckedColor={colors.outline}
                    />
                    <Text
                      variant="bodyMedium"
                      style={[
                        styles.checkboxLabel,
                        { color: colors.onSurface, fontWeight: '500' },
                      ]}
                    >
                      Same as WhatsApp
                    </Text>
                  </View>
                </TouchableRipple>

                {/* 7. Phone Number */}
                <PaperInput
                  ref={phInputRef}
                  mode="outlined"
                  label="Phone Number (Optional)"
                  value={isPhoneSame ? waNumber : phNumber}
                  onChangeText={setPhNumber}
                  placeholder={isPhoneSame ? waNumber : '+923001234567'}
                  editable={!isPhoneSame}
                  keyboardType="phone-pad"
                  returnKeyType="done"
                  onSubmitEditing={() => {
                    if (isSaveEnabled) handleSavePress();
                  }}
                  style={[
                    styles.m3Input,
                    isPhoneSame && { opacity: 0.6 },
                  ]}
                  outlineColor={colors.outlineVariant}
                  activeOutlineColor={colors.primary}
                  textColor={colors.onSurface}
                />

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
                      Save Student
                    </Button>
                </View>
              </ScrollView>
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
  sectionContainer: {
    marginBottom: 4,
  },
  dropdownTrigger: {
    borderRadius: 8,
  },
  sectionMenu: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: 12,
    marginTop: -4,
    marginBottom: 10,
    overflow: 'hidden',
  },
  sectionOption: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  checkboxRow: {
    borderRadius: 8,
    marginVertical: 4,
  },
  checkboxContent: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 4,
  },
  checkboxLabel: {
    marginLeft: 4,
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

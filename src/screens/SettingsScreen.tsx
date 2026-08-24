import React, { useState, useMemo } from 'react';
import {
  View,
  ScrollView,
  StyleSheet,
} from 'react-native';
import {
  Card,
  Button,
  Switch,
  IconButton,
  Text,
  Surface,
  TouchableRipple,
} from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import * as LocalAuthentication from 'expo-local-authentication';
import { useAppTheme } from '../theme/ThemeContext';
import { materialYouPalettes, PRESET_SEED_COLORS } from '../theme/colors';
import { useDatabase } from '../db/useDatabase';
import { TopAppBar } from '../components/TopAppBar';
import { SectionPickerModal } from '../components/SectionPickerModal';
import {
  exportAttendanceExcel,
  shareAttendanceExcel,
} from '../utils/excelExport';
import {
  downloadStudentSample,
  downloadClassSample,
  exportStudentsJSON,
  exportClassesJSON,
  importStudentsJSON,
  importClassesJSON,
} from '../utils/jsonExportImport';
import { openGitHub } from '../utils/linking';
import { showShortToast, showLongToast } from '../utils/toast';
import { SectionType, MaterialYouPaletteKey } from '../types';

interface SettingsScreenProps {
  onBack: () => void;
  selectedExportClassId: string | null;
  setSelectedExportClassId: (id: string | null) => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  onBack,
  selectedExportClassId,
  setSelectedExportClassId,
}) => {
  const { colors, typography, paletteKey, setPaletteKey, isDynamicSupported } = useAppTheme();
  const {
    classes,
    students,
    attendance,
    settings,
    updateSettings,
    upsertStudents,
    upsertClasses,
  } = useDatabase();

  const [isSectionModalOpen, setIsSectionModalOpen] = useState(false);
  const [isClassDropdownOpen, setIsClassDropdownOpen] = useState(false);
  const [isExporting, setIsExporting] = useState(false);

  // Selected export class (from all classes including hidden)
  const selectedClass = useMemo(() => {
    return classes.find((c) => c.id === selectedExportClassId) || null;
  }, [classes, selectedExportClassId]);

  // Handle Section Change
  const handleSaveSection = async (sec: SectionType) => {
    await updateSettings({ selectedSection: sec });
    setIsSectionModalOpen(false);
  };

  // Handle Biometric Switch
  const handleBiometricToggle = async (newValue: boolean) => {
    if (!newValue) {
      await updateSettings({ biometricEnabled: false });
      return;
    }

    try {
      const hasHardware = await LocalAuthentication.hasHardwareAsync();
      if (!hasHardware) {
        showLongToast('No biometric hardware on this device');
        return;
      }

      const isEnrolled = await LocalAuthentication.isEnrolledAsync();
      if (!isEnrolled) {
        showLongToast('No fingerprint enrolled – add one in system Settings first');
        return;
      }

      const supportedTypes = await LocalAuthentication.supportedAuthenticationTypesAsync();
      if (supportedTypes.length === 0) {
        showLongToast('Biometric unlock is not supported');
        return;
      }

      const authResult = await LocalAuthentication.authenticateAsync({
        promptMessage: 'Enable biometric lock',
        cancelLabel: 'Cancel',
        disableDeviceFallback: false,
      });

      if (authResult.success) {
        await updateSettings({ biometricEnabled: true });
        showShortToast('Biometric unlock enabled');
      }
    } catch (err: any) {
      const msg = err?.message || '';
      if (msg.includes('temporarily')) {
        showLongToast('Biometrics temporarily unavailable');
      } else {
        showLongToast('Biometric unlock is not supported');
      }
    }
  };

  // Handle Excel Export
  const handleExcelExport = async () => {
    if (!selectedClass) return;
    setIsExporting(true);
    await exportAttendanceExcel(
      selectedClass,
      students,
      attendance,
      settings.selectedSection
    );
    setIsExporting(false);
  };

  // Handle Excel Share
  const handleExcelShare = async () => {
    if (!selectedClass) return;
    setIsExporting(true);
    await shareAttendanceExcel(
      selectedClass,
      students,
      attendance,
      settings.selectedSection
    );
    setIsExporting(false);
  };

  const paletteKeys: MaterialYouPaletteKey[] = [
    'dynamic',
    'blue',
    'green',
    'purple',
    'coral',
    'amber',
    'teal',
  ];

  return (
    <Surface style={[styles.container, { backgroundColor: colors.background }]} elevation={0}>
      {/* 1. Top bar: Settings */}
      <TopAppBar
        title="Settings"
        showBack
        onBackPress={onBack}
      />

      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* Section 1: Your Section */}
        <Text variant="titleMedium" style={[styles.sectionHeader, { color: colors.primary }]}>
          Your Section
        </Text>
        <Card
          mode="elevated"
          elevation={1}
          style={[
            styles.m3Card,
            {
              backgroundColor: colors.surfaceContainer,
            },
          ]}
        >
          <Card.Content style={styles.cardRowContent}>
            <View style={styles.cardHeaderLeft}>
              <Text variant="titleMedium" style={{ color: colors.onSurface, fontWeight: '700' }}>
                Your Section
              </Text>
              <Text variant="bodySmall" style={{ color: colors.onSurfaceVariant, marginTop: 2 }}>
                Attendance and CSV export show only this section
              </Text>
            </View>

            <View style={styles.sectionRightRow}>
              <Text variant="titleMedium" style={{ color: colors.primary, fontWeight: '700', marginRight: 8 }}>
                {settings.selectedSection}
              </Text>
              <Button
                mode="contained-tonal"
                compact
                onPress={() => setIsSectionModalOpen(true)}
              >
                Change
              </Button>
            </View>
          </Card.Content>
        </Card>

        {/* Section: Material You Dynamic Colors */}
        <Text variant="titleMedium" style={[styles.sectionHeader, { color: colors.primary }]}>
          Material You & Dynamic Theme
        </Text>
        <Card
          mode="elevated"
          elevation={1}
          style={[
            styles.m3Card,
            {
              backgroundColor: colors.surfaceContainer,
            },
          ]}
        >
          <Card.Content>
            <Text variant="bodySmall" style={{ color: colors.onSurfaceVariant, marginBottom: 12 }}>
              Dynamic Material 3 tonal palette applied across the entire app
            </Text>

            <View style={styles.paletteGrid}>
              {paletteKeys.map((key) => {
                const isSelected = paletteKey === key;
                const isDynamicKey = key === 'dynamic';
                const displayName = isDynamicKey
                  ? isDynamicSupported
                    ? 'Dynamic (Monet)'
                    : 'System Default'
                  : PRESET_SEED_COLORS[key].name;

                const seedColor = isDynamicKey
                  ? colors.primary
                  : PRESET_SEED_COLORS[key].seedColor;

                return (
                  <Surface
                    key={key}
                    elevation={isSelected ? 2 : 0}
                    style={[
                      styles.paletteChipSurface,
                      {
                        backgroundColor: isSelected
                          ? colors.primaryContainer
                          : colors.surfaceContainerHigh,
                        borderColor: isSelected
                          ? colors.primary
                          : colors.outlineVariant,
                        borderWidth: isSelected ? 1.5 : StyleSheet.hairlineWidth,
                      },
                    ]}
                  >
                    <TouchableRipple
                      borderless
                      onPress={() => setPaletteKey(key)}
                      style={styles.paletteChipRipple}
                    >
                      <View style={styles.paletteChipInner}>
                        <View
                          style={[
                            styles.colorCircle,
                            { backgroundColor: seedColor },
                          ]}
                        >
                          {isDynamicKey ? (
                            <MaterialCommunityIcons
                              name={isSelected ? 'check' : 'auto-fix'}
                              size={16}
                              color={colors.onPrimary}
                            />
                          ) : isSelected ? (
                            <MaterialCommunityIcons
                              name="check"
                              size={16}
                              color="#FFFFFF"
                            />
                          ) : null}
                        </View>
                        <Text
                          variant="labelSmall"
                          style={[
                            styles.paletteChipText,
                            {
                              color: isSelected
                                ? colors.onPrimaryContainer
                                : colors.onSurface,
                              fontWeight: isSelected ? '700' : '500',
                            },
                          ]}
                          numberOfLines={1}
                        >
                          {displayName}
                        </Text>
                      </View>
                    </TouchableRipple>
                  </Surface>
                );
              })}
            </View>
          </Card.Content>
        </Card>

        {/* Section 2: Export & Share */}
        <Text variant="titleMedium" style={[styles.sectionHeader, { color: colors.primary }]}>
          Export & Share
        </Text>
        <Card
          mode="elevated"
          elevation={1}
          style={[
            styles.m3Card,
            {
              backgroundColor: colors.surfaceContainer,
            },
          ]}
        >
          <Card.Content>
            {/* Class Dropdown */}
            <Text variant="labelSmall" style={{ color: colors.onSurfaceVariant, marginBottom: 4, fontWeight: '600' }}>
              Class
            </Text>
            <Surface
              elevation={1}
              style={[
                styles.dropdownSurface,
                {
                  borderColor: colors.outline,
                  backgroundColor: colors.surfaceContainerHigh,
                },
              ]}
            >
              <TouchableRipple
                borderless
                onPress={() => setIsClassDropdownOpen(!isClassDropdownOpen)}
                style={styles.dropdownTouchable}
              >
                <View style={styles.dropdownRow}>
                  <Text
                    variant="bodyMedium"
                    style={{ color: selectedClass ? colors.onSurface : colors.outline }}
                    numberOfLines={1}
                  >
                    {selectedClass
                      ? `${selectedClass.shortName} - ${selectedClass.fullNameWithCode}`
                      : 'Select Class'}
                  </Text>
                  <MaterialCommunityIcons
                    name={isClassDropdownOpen ? 'chevron-up' : 'chevron-down'}
                    size={22}
                    color={colors.onSurfaceVariant}
                  />
                </View>
              </TouchableRipple>
            </Surface>

            {/* Menu items */}
            {isClassDropdownOpen && (
              <Surface
                elevation={3}
                style={[
                  styles.dropdownMenu,
                  {
                    backgroundColor: colors.surfaceContainerHighest,
                    borderColor: colors.outlineVariant,
                  },
                ]}
              >
                {classes.length === 0 ? (
                  <View style={{ padding: 12 }}>
                    <Text variant="bodyMedium" style={{ color: colors.onSurfaceVariant }}>
                      No classes added yet
                    </Text>
                  </View>
                ) : (
                  classes.map((cls) => (
                    <TouchableRipple
                      key={cls.id}
                      onPress={() => {
                        setSelectedExportClassId(cls.id);
                        setIsClassDropdownOpen(false);
                      }}
                      style={[
                        styles.dropdownOption,
                        selectedClass?.id === cls.id && {
                          backgroundColor: colors.primaryContainer,
                        },
                      ]}
                    >
                      <Text
                        variant="bodyMedium"
                        style={{
                          color:
                            selectedClass?.id === cls.id
                              ? colors.onPrimaryContainer
                              : colors.onSurface,
                          fontWeight:
                            selectedClass?.id === cls.id ? '700' : '400',
                        }}
                      >
                        {`${cls.shortName} - ${cls.fullNameWithCode}`}
                      </Text>
                    </TouchableRipple>
                  ))
                )}
              </Surface>
            )}

            {/* Button Pair: Export | Share */}
            <View style={styles.buttonPairRow}>
              <Button
                mode="contained"
                disabled={!selectedClass || isExporting}
                onPress={handleExcelExport}
                loading={isExporting}
                buttonColor={colors.primary}
                textColor={colors.onPrimary}
                style={styles.pairBtn}
              >
                Export
              </Button>

              <Button
                mode="outlined"
                icon="share-variant-outline"
                disabled={!selectedClass || isExporting}
                onPress={handleExcelShare}
                textColor={colors.primary}
                style={[styles.pairBtn, { marginLeft: 8 }]}
              >
                Share
              </Button>
            </View>

            {/* Caption */}
            <Text
              variant="bodySmall"
              style={[
                styles.captionText,
                { color: colors.onSurfaceVariant },
              ]}
            >
              Filename example: CS-101_Computer-Programming_0930_15-01-2025.xlsx – Only the selected section is exported.
            </Text>
          </Card.Content>
        </Card>

        {/* Section 3: Import / Export Students */}
        <Text variant="titleMedium" style={[styles.sectionHeader, { color: colors.primary }]}>
          Import / Export Students
        </Text>
        <Card
          mode="elevated"
          elevation={1}
          style={[
            styles.m3Card,
            {
              backgroundColor: colors.surfaceContainer,
            },
          ]}
        >
          <Card.Content style={styles.ioRowContent}>
            <IconButton
              icon="file-document-outline"
              size={24}
              iconColor={colors.primary}
              onPress={downloadStudentSample}
              accessibilityLabel="Download student sample"
              style={styles.sampleIconBtn}
            />

            <Button
              mode="outlined"
              onPress={() => importStudentsJSON(students, upsertStudents)}
              textColor={colors.primary}
              style={styles.actionRowBtn}
            >
              Import
            </Button>

            <Button
              mode="contained"
              onPress={() => exportStudentsJSON(students)}
              buttonColor={colors.primary}
              textColor={colors.onPrimary}
              style={[styles.actionRowBtn, { marginLeft: 8 }]}
            >
              Export
            </Button>
          </Card.Content>
        </Card>

        {/* Section 4: Import / Export Classes */}
        <Text variant="titleMedium" style={[styles.sectionHeader, { color: colors.primary }]}>
          Import / Export Classes
        </Text>
        <Card
          mode="elevated"
          elevation={1}
          style={[
            styles.m3Card,
            {
              backgroundColor: colors.surfaceContainer,
            },
          ]}
        >
          <Card.Content style={styles.ioRowContent}>
            <IconButton
              icon="file-document-outline"
              size={24}
              iconColor={colors.primary}
              onPress={downloadClassSample}
              accessibilityLabel="Download class sample"
              style={styles.sampleIconBtn}
            />

            <Button
              mode="outlined"
              onPress={() => importClassesJSON(classes, upsertClasses)}
              textColor={colors.primary}
              style={styles.actionRowBtn}
            >
              Import
            </Button>

            <Button
              mode="contained"
              onPress={() => exportClassesJSON(classes)}
              buttonColor={colors.primary}
              textColor={colors.onPrimary}
              style={[styles.actionRowBtn, { marginLeft: 8 }]}
            >
              Export
            </Button>
          </Card.Content>
        </Card>

        {/* Section 5: Biometric Security */}
        <Text variant="titleMedium" style={[styles.sectionHeader, { color: colors.primary }]}>
          Biometric Security
        </Text>
        <Card
          mode="elevated"
          elevation={1}
          style={[
            styles.m3Card,
            {
              backgroundColor: colors.surfaceContainer,
            },
          ]}
        >
          <Card.Content style={styles.cardRowContent}>
            <View style={styles.cardHeaderLeft}>
              <Text variant="titleMedium" style={{ color: colors.onSurface, fontWeight: '700' }}>
                Unlock with Biometric
              </Text>
              <Text variant="bodySmall" style={{ color: colors.onSurfaceVariant, marginTop: 2 }}>
                Locks the app when resumed; unlock with fingerprint or PIN
              </Text>
            </View>

            <Switch
              value={settings.biometricEnabled}
              onValueChange={handleBiometricToggle}
              color={colors.primary}
            />
          </Card.Content>
        </Card>

        {/* Section 6: Footer row */}
        <View style={[styles.footerRow, { borderTopColor: colors.outlineVariant }]}>
          <Text variant="bodySmall" style={{ color: colors.onSurfaceVariant }}>
            Developed by AFNAN with ❤️
          </Text>
          <IconButton
            icon="github"
            size={24}
            iconColor={colors.onSurface}
            onPress={openGitHub}
            accessibilityLabel="GitHub Profile"
            style={styles.githubBtn}
          />
        </View>
      </ScrollView>

      {/* Section Picker Modal */}
      <SectionPickerModal
        visible={isSectionModalOpen}
        currentSection={settings.selectedSection}
        onSave={handleSaveSection}
        onCancel={() => setIsSectionModalOpen(false)}
      />
    </Surface>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: 16,
    paddingTop: 8,
    paddingBottom: 40,
  },
  sectionHeader: {
    marginTop: 18,
    marginBottom: 8,
    fontWeight: '700',
  },
  m3Card: {
    borderRadius: 16,
    marginBottom: 4,
  },
  cardRowContent: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 14,
    paddingHorizontal: 16,
  },
  cardHeaderLeft: {
    flex: 1,
    paddingRight: 12,
  },
  sectionRightRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  paletteGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    gap: 8,
  },
  paletteChipSurface: {
    width: '31%',
    borderRadius: 16,
    overflow: 'hidden',
    marginBottom: 8,
  },
  paletteChipRipple: {
    paddingVertical: 10,
    paddingHorizontal: 6,
    alignItems: 'center',
    justifyContent: 'center',
  },
  paletteChipInner: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  colorCircle: {
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  paletteChipText: {
    marginTop: 4,
    textAlign: 'center',
  },
  dropdownSurface: {
    borderRadius: 12,
    borderWidth: 1,
    overflow: 'hidden',
    marginTop: 2,
    marginBottom: 12,
  },
  dropdownTouchable: {
    paddingHorizontal: 12,
    paddingVertical: 12,
  },
  dropdownRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  dropdownMenu: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: 12,
    marginTop: -8,
    marginBottom: 12,
    overflow: 'hidden',
  },
  dropdownOption: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  buttonPairRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 4,
  },
  pairBtn: {
    flex: 1,
    borderRadius: 20,
  },
  captionText: {
    marginTop: 10,
    lineHeight: 16,
  },
  ioRowContent: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    paddingHorizontal: 12,
  },
  sampleIconBtn: {
    margin: 0,
    marginRight: 4,
  },
  actionRowBtn: {
    flex: 1,
    borderRadius: 20,
  },
  footerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 36,
    paddingVertical: 16,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  githubBtn: {
    margin: 0,
  },
});

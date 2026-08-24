import React from 'react';
import {
  View,
  StyleSheet,
  ScrollView,
  TouchableWithoutFeedback,
} from 'react-native';
import {
  Button,
  IconButton,
  Text,
  Portal,
  Modal,
  Surface,
  Chip,
} from 'react-native-paper';
import { useAppTheme } from '../theme/ThemeContext';
import { StudentEntity } from '../types';
import { copyToClipboard, openWhatsApp, openDialer } from '../utils/linking';

interface StudentDetailModalProps {
  visible: boolean;
  student: StudentEntity | null;
  showEditDelete?: boolean;
  onEdit?: (student: StudentEntity) => void;
  onDelete?: (studentId: string) => void;
  onClose: () => void;
}

export const StudentDetailModal: React.FC<StudentDetailModalProps> = ({
  visible,
  student,
  showEditDelete = false,
  onEdit,
  onDelete,
  onClose,
}) => {
  const { colors, typography } = useAppTheme();

  if (!student) return null;

  const effectivePhone = student.isPhoneSame ? student.waNumber : student.phNumber;
  const hasDistinctPhone =
    !student.isPhoneSame &&
    Boolean(student.phNumber && student.phNumber.trim() && student.phNumber !== student.waNumber);

  return (
    <Portal>
      <Modal
        visible={visible}
        onDismiss={onClose}
        dismissable={true}
        contentContainerStyle={styles.modalContainer}
      >
        <TouchableWithoutFeedback onPress={onClose}>
          <View style={styles.backdropArea}>
            <TouchableWithoutFeedback onPress={(e) => e.stopPropagation()}>
              <Surface
                elevation={3}
                style={[
                  styles.dialog,
                  { backgroundColor: colors.surfaceContainerHigh },
                ]}
              >
                <ScrollView
                  contentContainerStyle={styles.scrollContent}
                  showsVerticalScrollIndicator={false}
                  bounces={false}
                >
                  {/* Header: Student Name & Section Chip */}
                  <View style={styles.headerRow}>
                    <View style={styles.nameContainer}>
                      <Text
                        variant="titleLarge"
                        style={[
                          typography.titleLarge,
                          styles.studentName,
                          { color: colors.onSurface, fontWeight: '700' },
                        ]}
                        numberOfLines={2}
                      >
                        {student.name}
                      </Text>
                    </View>
                    {Boolean(student.section) && (
                      <Chip
                        mode="flat"
                        compact
                        textStyle={{
                          fontSize: 12,
                          color: colors.onPrimaryContainer,
                          fontWeight: '700',
                        }}
                        style={{
                          backgroundColor: colors.primaryContainer,
                          height: 28,
                          borderRadius: 8,
                          marginLeft: 8,
                        }}
                      >
                        {`Sec ${student.section}`}
                      </Chip>
                    )}
                  </View>

                  {/* 1. Reg Number Row */}
                  <View style={[styles.compactRow, { borderBottomColor: colors.outlineVariant }]}>
                    <View style={styles.infoCol}>
                      <Text
                        variant="labelSmall"
                        style={[
                          styles.labelSmallCaps,
                          { color: colors.onSurfaceVariant },
                        ]}
                      >
                        REG NUMBER
                      </Text>
                      <Text
                        variant="bodyMedium"
                        style={[
                          styles.valueText,
                          { color: colors.onSurface },
                        ]}
                      >
                        {student.regNo || '—'}
                      </Text>
                    </View>
                    {Boolean(student.regNo) && (
                      <IconButton
                        icon="content-copy"
                        size={18}
                        iconColor={colors.primary}
                        onPress={() => copyToClipboard(student.regNo)}
                        accessibilityLabel="Copy reg number"
                        style={styles.iconBtn}
                      />
                    )}
                  </View>

                  {/* 2. CNIC Row (only when present) */}
                  {Boolean(student.cnic && student.cnic.trim()) && (
                    <View style={[styles.compactRow, { borderBottomColor: colors.outlineVariant }]}>
                      <View style={styles.infoCol}>
                        <Text
                          variant="labelSmall"
                          style={[
                            styles.labelSmallCaps,
                            { color: colors.onSurfaceVariant },
                          ]}
                        >
                          CNIC
                        </Text>
                        <Text
                          variant="bodyMedium"
                          style={[
                            styles.valueText,
                            { color: colors.onSurface },
                          ]}
                        >
                          {student.cnic}
                        </Text>
                      </View>
                      <IconButton
                        icon="content-copy"
                        size={18}
                        iconColor={colors.primary}
                        onPress={() => copyToClipboard(student.cnic)}
                        accessibilityLabel="Copy CNIC"
                        style={styles.iconBtn}
                      />
                    </View>
                  )}

                  {/* 3. WhatsApp Number Row */}
                  <View style={[styles.compactRow, { borderBottomColor: colors.outlineVariant }]}>
                    <View style={styles.infoCol}>
                      <Text
                        variant="labelSmall"
                        style={[
                          styles.labelSmallCaps,
                          { color: colors.onSurfaceVariant },
                        ]}
                      >
                        WHATSAPP NUMBER
                      </Text>
                      <Text
                        variant="bodyMedium"
                        style={[
                          styles.valueText,
                          { color: colors.onSurface },
                        ]}
                      >
                        {student.waNumber || '—'}
                      </Text>
                    </View>
                    {Boolean(student.waNumber && student.waNumber.trim()) ? (
                      <View style={styles.actionButtonsRow}>
                        <IconButton
                          icon="content-copy"
                          size={18}
                          iconColor={colors.primary}
                          onPress={() => copyToClipboard(student.waNumber)}
                          accessibilityLabel="Copy WhatsApp number"
                          style={styles.iconBtn}
                        />
                        <IconButton
                          icon="whatsapp"
                          size={20}
                          iconColor={colors.primary}
                          onPress={() => openWhatsApp(student.waNumber)}
                          accessibilityLabel="Open WhatsApp"
                          style={styles.iconBtn}
                        />
                      </View>
                    ) : null}
                  </View>

                  {/* 4. Phone Number Row (if different from WhatsApp and exists) */}
                  {hasDistinctPhone ? (
                    <View style={[styles.compactRow, { borderBottomColor: colors.outlineVariant }]}>
                      <View style={styles.infoCol}>
                        <Text
                          variant="labelSmall"
                          style={[
                            styles.labelSmallCaps,
                            { color: colors.onSurfaceVariant },
                          ]}
                        >
                          PHONE NUMBER
                        </Text>
                        <Text
                          variant="bodyMedium"
                          style={[
                            styles.valueText,
                            { color: colors.onSurface },
                          ]}
                        >
                          {student.phNumber}
                        </Text>
                      </View>
                      <View style={styles.actionButtonsRow}>
                        <IconButton
                          icon="content-copy"
                          size={18}
                          iconColor={colors.primary}
                          onPress={() => copyToClipboard(student.phNumber)}
                          accessibilityLabel="Copy phone number"
                          style={styles.iconBtn}
                        />
                        <IconButton
                          icon="phone-outline"
                          size={18}
                          iconColor={colors.primary}
                          onPress={() => openDialer(student.phNumber)}
                          accessibilityLabel="Call student"
                          style={styles.iconBtn}
                        />
                      </View>
                    </View>
                  ) : null}

                  {/* Quick Call Button if WhatsApp number is used as primary contact */}
                  {Boolean(effectivePhone && effectivePhone.trim() && !hasDistinctPhone) && (
                    <View style={[styles.compactRow, { borderBottomColor: colors.outlineVariant }]}>
                      <View style={styles.infoCol}>
                        <Text
                          variant="labelSmall"
                          style={[
                            styles.labelSmallCaps,
                            { color: colors.onSurfaceVariant },
                          ]}
                        >
                          DIRECT CALL
                        </Text>
                        <Text
                          variant="bodyMedium"
                          style={[
                            styles.valueText,
                            { color: colors.onSurfaceVariant, fontSize: 13 },
                          ]}
                        >
                          Same as WhatsApp
                        </Text>
                      </View>
                      <IconButton
                        icon="phone-outline"
                        size={18}
                        iconColor={colors.primary}
                        onPress={() => openDialer(effectivePhone)}
                        accessibilityLabel="Call student"
                        style={styles.iconBtn}
                      />
                    </View>
                  )}
                </ScrollView>

                {/* Footer Row */}
                <View style={[styles.footerRow, { borderTopColor: colors.outlineVariant }]}>
                  {showEditDelete && onDelete ? (
                    <IconButton
                      icon="trash-can-outline"
                      size={22}
                      iconColor={colors.error}
                      onPress={() => {
                        onDelete(student.id);
                        onClose();
                      }}
                      accessibilityLabel="Delete student"
                      style={styles.trashBtn}
                    />
                  ) : (
                    <View />
                  )}

                  <View style={styles.footerRightBtns}>
                    {showEditDelete && onEdit && (
                      <Button
                        mode="outlined"
                        compact
                        onPress={() => {
                          onClose();
                          onEdit(student);
                        }}
                        textColor={colors.primary}
                        style={styles.footerBtn}
                      >
                        Edit
                      </Button>
                    )}

                    <Button
                      mode="contained"
                      compact
                      onPress={onClose}
                      buttonColor={colors.primary}
                      textColor={colors.onPrimary}
                      style={[styles.footerBtn, { marginLeft: 8 }]}
                    >
                      Close
                    </Button>
                  </View>
                </View>
              </Surface>
            </TouchableWithoutFeedback>
          </View>
        </TouchableWithoutFeedback>
      </Modal>
    </Portal>
  );
};

const styles = StyleSheet.create({
  modalContainer: {
    padding: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  backdropArea: {
    width: '100%',
    alignItems: 'center',
    justifyContent: 'center',
  },
  dialog: {
    width: '100%',
    maxWidth: 380,
    borderRadius: 24,
    overflow: 'hidden',
  },
  scrollContent: {
    paddingHorizontal: 20,
    paddingTop: 18,
    paddingBottom: 8,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 10,
  },
  nameContainer: {
    flex: 1,
  },
  studentName: {
    lineHeight: 26,
  },
  compactRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 6,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  infoCol: {
    flex: 1,
  },
  labelSmallCaps: {
    letterSpacing: 0.6,
    marginBottom: 1,
    fontSize: 10,
  },
  valueText: {
    fontWeight: '600',
  },
  actionButtonsRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  iconBtn: {
    margin: 0,
    width: 32,
    height: 32,
  },
  footerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  trashBtn: {
    margin: 0,
    width: 36,
    height: 36,
  },
  footerRightBtns: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  footerBtn: {
    borderRadius: 18,
    minWidth: 72,
  },
});

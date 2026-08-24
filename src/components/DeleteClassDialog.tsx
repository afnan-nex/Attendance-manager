import React, { useState, useEffect } from 'react';
import {
  View,
  StyleSheet,
  TouchableWithoutFeedback,
} from 'react-native';
import {
  TextInput as PaperInput,
  Button,
  Text,
  Portal,
  Modal,
  Surface,
} from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAppTheme } from '../theme/ThemeContext';
import { ClassEntity } from '../types';

interface DeleteClassDialogProps {
  visible: boolean;
  classItem: ClassEntity | null;
  onConfirmDelete: (classId: string) => void;
  onCancel: () => void;
}

export const DeleteClassDialog: React.FC<DeleteClassDialogProps> = ({
  visible,
  classItem,
  onConfirmDelete,
  onCancel,
}) => {
  const { colors, typography } = useAppTheme();
  const [typedText, setTypedText] = useState('');

  useEffect(() => {
    if (visible) {
      setTypedText('');
    }
  }, [visible]);

  if (!classItem) return null;

  const targetPhrase = `DELETE ${classItem.fullNameWithCode}`;
  const isMatch = typedText === targetPhrase;

  return (
    <Portal>
      <Modal
        visible={visible}
        onDismiss={onCancel}
        dismissable={true}
        contentContainerStyle={styles.modalContainer}
      >
        <TouchableWithoutFeedback onPress={onCancel}>
          <View style={styles.backdropArea}>
            <TouchableWithoutFeedback onPress={(e) => e.stopPropagation()}>
              <Surface
                elevation={3}
                style={[
                  styles.dialog,
                  { backgroundColor: colors.surfaceContainerHigh },
                ]}
              >
                <Text
                  variant="headlineSmall"
                  style={[
                    typography.headlineSmall,
                    styles.title,
                    { color: colors.onSurface, fontWeight: '700' },
                  ]}
                >
                  Delete Class?
                </Text>

                <Text
                  variant="bodyMedium"
                  style={[
                    typography.bodyMedium,
                    styles.bodyText,
                    { color: colors.onSurface },
                  ]}
                >
                  {`This permanently deletes "${classItem.fullNameWithCode}" and all its attendance records.`}
                </Text>

                {/* Warning notice */}
                <View
                  style={[
                    styles.warningBox,
                    { backgroundColor: colors.errorContainer },
                  ]}
                >
                  <MaterialCommunityIcons
                    name="alert-outline"
                    size={20}
                    color={colors.onErrorContainer}
                  />
                  <Text
                    variant="bodySmall"
                    style={[
                      typography.bodySmall,
                      styles.warningCaption,
                      { color: colors.onErrorContainer, fontWeight: '500' },
                    ]}
                  >
                    Advised to export CSV/Excel from Settings before deleting.
                  </Text>
                </View>

                {/* Instruction line */}
                <Text
                  variant="labelMedium"
                  style={[
                    typography.labelMedium,
                    styles.instructionText,
                    { color: colors.onSurfaceVariant },
                  ]}
                >
                  {`Type '${targetPhrase}' to confirm:`}
                </Text>

                {/* Input field */}
                <PaperInput
                  mode="outlined"
                  value={typedText}
                  onChangeText={setTypedText}
                  placeholder={targetPhrase}
                  outlineColor={isMatch ? colors.error : colors.outline}
                  activeOutlineColor={isMatch ? colors.error : colors.primary}
                  textColor={colors.onSurface}
                  autoCapitalize="none"
                  autoCorrect={false}
                  style={styles.m3Input}
                />

                {/* Buttons */}
                <View style={styles.footerRow}>
                  <Button
                    mode="text"
                    onPress={onCancel}
                    textColor={colors.primary}
                  >
                    Cancel
                  </Button>

                  <Button
                    mode="contained"
                    disabled={!isMatch}
                    onPress={() => {
                      if (isMatch) {
                        onConfirmDelete(classItem.id);
                      }
                    }}
                    buttonColor={colors.error}
                    textColor={colors.onError}
                    style={{ marginLeft: 8 }}
                  >
                    Delete
                  </Button>
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
    padding: 20,
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
    borderRadius: 28,
    padding: 24,
  },
  title: {
    marginBottom: 12,
  },
  bodyText: {
    lineHeight: 20,
    marginBottom: 14,
  },
  warningBox: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    borderRadius: 12,
    marginBottom: 14,
  },
  warningCaption: {
    marginLeft: 8,
    flex: 1,
  },
  instructionText: {
    marginBottom: 6,
  },
  m3Input: {
    marginBottom: 16,
    backgroundColor: 'transparent',
  },
  footerRow: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
});

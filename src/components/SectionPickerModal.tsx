import React, { useState, useEffect } from 'react';
import {
  View,
  StyleSheet,
  TouchableWithoutFeedback,
} from 'react-native';
import {
  RadioButton,
  Button,
  Text,
  Portal,
  Modal,
  Surface,
  TouchableRipple,
} from 'react-native-paper';
import { useAppTheme } from '../theme/ThemeContext';
import { SectionType } from '../types';

interface SectionPickerModalProps {
  visible: boolean;
  currentSection: SectionType;
  onSave: (section: SectionType) => void;
  onCancel: () => void;
}

const SECTIONS: SectionType[] = ['All', 'A', 'B', 'C', 'D', 'E', 'F'];

export const SectionPickerModal: React.FC<SectionPickerModalProps> = ({
  visible,
  currentSection,
  onSave,
  onCancel,
}) => {
  const { colors, typography } = useAppTheme();
  const [selected, setSelected] = useState<SectionType>(currentSection);

  useEffect(() => {
    if (visible) {
      setSelected(currentSection);
    }
  }, [visible, currentSection]);

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
                  Your Section
                </Text>

                <RadioButton.Group
                  onValueChange={(value) => setSelected(value as SectionType)}
                  value={selected}
                >
                  <View style={styles.list}>
                    {SECTIONS.map((sec) => {
                      const isChosen = selected === sec;
                      return (
                        <TouchableRipple
                          key={sec}
                          borderless
                          onPress={() => setSelected(sec)}
                          style={[
                            styles.radioRow,
                            isChosen && { backgroundColor: colors.primaryContainer },
                          ]}
                        >
                          <View style={styles.radioRowInner}>
                            <RadioButton.Android
                              value={sec}
                              color={colors.primary}
                              uncheckedColor={colors.onSurfaceVariant}
                            />
                            <Text
                              variant="bodyLarge"
                              style={[
                                styles.radioLabel,
                                {
                                  color: isChosen ? colors.onPrimaryContainer : colors.onSurface,
                                  fontWeight: isChosen ? '700' : '400',
                                },
                              ]}
                            >
                              {sec === 'All' ? 'All Sections' : `Section ${sec}`}
                            </Text>
                          </View>
                        </TouchableRipple>
                      );
                    })}
                  </View>
                </RadioButton.Group>

                <View style={[styles.buttonRow, { borderTopColor: colors.outlineVariant }]}>
                  <Button
                    mode="text"
                    onPress={onCancel}
                    textColor={colors.primary}
                  >
                    Cancel
                  </Button>

                  <Button
                    mode="contained"
                    onPress={() => onSave(selected)}
                    buttonColor={colors.primary}
                    textColor={colors.onPrimary}
                    style={{ marginLeft: 8 }}
                  >
                    Save
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
    maxWidth: 360,
    borderRadius: 28,
    paddingTop: 24,
    paddingHorizontal: 24,
    paddingBottom: 16,
  },
  title: {
    marginBottom: 16,
  },
  list: {
    marginBottom: 8,
  },
  radioRow: {
    borderRadius: 12,
    marginVertical: 2,
    overflow: 'hidden',
  },
  radioRowInner: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 6,
    paddingHorizontal: 8,
  },
  radioLabel: {
    marginLeft: 8,
  },
  buttonRow: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 16,
    paddingTop: 12,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
});

import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Text } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAppTheme } from '../theme/ThemeContext';

interface EmptyStateProps {
  message: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({ message }) => {
  const { colors, typography } = useAppTheme();

  return (
    <View style={styles.container}>
      <MaterialCommunityIcons
        name="information-outline"
        size={44}
        color={colors.outline}
      />
      <Text
        variant="bodyLarge"
        style={[
          typography.bodyLarge,
          styles.messageText,
          { color: colors.onSurfaceVariant },
        ]}
      >
        {message}
      </Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    paddingVertical: 48,
    paddingHorizontal: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  messageText: {
    marginTop: 12,
    textAlign: 'center',
  },
});

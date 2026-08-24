import React, { useState, useMemo } from 'react';
import {
  View,
  FlatList,
  StyleSheet,
} from 'react-native';
import { Button, Surface } from 'react-native-paper';
import { useAppTheme } from '../theme/ThemeContext';
import { useDatabase } from '../db/useDatabase';
import { TopAppBar } from '../components/TopAppBar';
import { ClassCard } from '../components/ClassCard';
import { EmptyState } from '../components/EmptyState';
import { AddEditClassModal } from '../components/AddEditClassModal';
import { DeleteClassDialog } from '../components/DeleteClassDialog';
import { WEEKDAYS } from '../utils/dateUtils';
import { NavRoute, ClassEntity } from '../types';

interface ManageClassesScreenProps {
  onNavigate: (route: NavRoute) => void;
}

export const ManageClassesScreen: React.FC<ManageClassesScreenProps> = ({
  onNavigate,
}) => {
  const { colors, typography } = useAppTheme();
  const {
    classes,
    addClass,
    updateClass,
    deleteClass,
    toggleClassHidden,
  } = useDatabase();

  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [classToEdit, setClassToEdit] = useState<ClassEntity | null>(null);
  const [classToDelete, setClassToDelete] = useState<ClassEntity | null>(null);

  // Sorted by weekday then start time ascending (including hidden classes)
  const sortedClasses = useMemo(() => {
    return [...classes].sort((a, b) => {
      const dayA = WEEKDAYS.indexOf(a.day);
      const dayB = WEEKDAYS.indexOf(b.day);
      if (dayA !== dayB) return dayA - dayB;
      return a.startTime.localeCompare(b.startTime);
    });
  }, [classes]);

  return (
    <Surface style={[styles.container, { backgroundColor: colors.background }]} elevation={0}>
      {/* 1. Top bar: Manage Classes */}
      <TopAppBar
        title="Manage Classes"
        showSettings
        onSettingsPress={() => onNavigate('settings')}
      />

      {/* 2. M3 Contained Button "+ Add Class" */}
      <View style={styles.addButtonWrapper}>
        <Button
          mode="contained"
          icon="plus"
          onPress={() => {
            setClassToEdit(null);
            setIsAddModalOpen(true);
          }}
          buttonColor={colors.primary}
          textColor={colors.onPrimary}
          style={styles.addButton}
          contentStyle={{ height: 48 }}
          labelStyle={{ fontSize: 16, fontWeight: '700' }}
        >
          Add Class
        </Button>
      </View>

      {/* 3. Body: Empty state or List of ClassCards */}
      {sortedClasses.length === 0 ? (
        <EmptyState message='No classes yet – tap "+ Add Class" to schedule a subject' />
      ) : (
        <FlatList
          data={sortedClasses}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => (
            <ClassCard
              item={item}
              showActions={true}
              onToggleHidden={() => toggleClassHidden(item.id)}
              onDeletePress={() => setClassToDelete(item)}
              onEditPress={() => {
                setClassToEdit(item);
                setIsAddModalOpen(true);
              }}
            />
          )}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
        />
      )}

      {/* Add / Edit Class Modal */}
      <AddEditClassModal
        visible={isAddModalOpen}
        classToEdit={classToEdit}
        onSave={async (saved) => {
          if (classToEdit) {
            await updateClass(saved);
          } else {
            await addClass(saved);
          }
        }}
        onCancel={() => {
          setIsAddModalOpen(false);
          setClassToEdit(null);
        }}
      />

      {/* Delete Class Confirmation Dialog */}
      <DeleteClassDialog
        visible={classToDelete !== null}
        classItem={classToDelete}
        onConfirmDelete={async (id) => {
          await deleteClass(id);
          setClassToDelete(null);
        }}
        onCancel={() => setClassToDelete(null)}
      />
    </Surface>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  addButtonWrapper: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  addButton: {
    borderRadius: 24,
  },
  listContent: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    paddingBottom: 90,
  },
});

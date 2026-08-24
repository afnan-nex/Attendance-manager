import React, { useState, useRef } from 'react';
import {
  View,
  StyleSheet,
  ScrollView,
  PanResponder,
  Animated,
} from 'react-native';
import { Button, Card, Text, Surface, TouchableRipple } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAppTheme } from '../theme/ThemeContext';
import { useDatabase } from '../db/useDatabase';
import { TopAppBar } from '../components/TopAppBar';
import { EmptyState } from '../components/EmptyState';
import { StudentDetailModal } from '../components/StudentDetailModal';
import { AddEditStudentModal } from '../components/AddEditStudentModal';
import { NavRoute, StudentEntity } from '../types';

interface ManageStudentsScreenProps {
  onNavigate: (route: NavRoute) => void;
}

const ROW_HEIGHT = 60;

export const ManageStudentsScreen: React.FC<ManageStudentsScreenProps> = ({
  onNavigate,
}) => {
  const { colors, typography } = useAppTheme();
  const {
    students,
    addStudent,
    updateStudent,
    deleteStudent,
    reorderStudents,
  } = useDatabase();

  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [studentToEdit, setStudentToEdit] = useState<StudentEntity | null>(null);
  const [selectedStudentForDetail, setSelectedStudentForDetail] = useState<StudentEntity | null>(null);

  // Drag-and-drop ordering states
  const [draggingIndex, setDraggingIndex] = useState<number | null>(null);
  const [localStudents, setLocalStudents] = useState<StudentEntity[]>(students);
  const dragY = useRef(new Animated.Value(0)).current;
  const currentDragIndexRef = useRef<number | null>(null);
  const localListRef = useRef<StudentEntity[]>(students);

  React.useEffect(() => {
    setLocalStudents(students);
    localListRef.current = students;
  }, [students]);

  // Create pan responder for drag handle
  const createPanResponder = (index: number) => {
    return PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onPanResponderGrant: () => {
        setDraggingIndex(index);
        currentDragIndexRef.current = index;
        dragY.setValue(0);
      },
      onPanResponderMove: (_, gestureState) => {
        dragY.setValue(gestureState.dy);
        const fromIdx = currentDragIndexRef.current;
        if (fromIdx === null) return;

        const rawTargetIdx = Math.round((fromIdx * ROW_HEIGHT + gestureState.dy) / ROW_HEIGHT);
        const targetIdx = Math.max(0, Math.min(localListRef.current.length - 1, rawTargetIdx));

        if (targetIdx !== fromIdx) {
          const updated = [...localListRef.current];
          const [moved] = updated.splice(fromIdx, 1);
          updated.splice(targetIdx, 0, moved);
          localListRef.current = updated;
          setLocalStudents(updated);
          currentDragIndexRef.current = targetIdx;
          dragY.setValue(gestureState.dy - (targetIdx - fromIdx) * ROW_HEIGHT);
        }
      },
      onPanResponderRelease: async () => {
        setDraggingIndex(null);
        currentDragIndexRef.current = null;
        dragY.setValue(0);
        // Persist new order globally
        await reorderStudents(localListRef.current);
      },
      onPanResponderTerminate: () => {
        setDraggingIndex(null);
        currentDragIndexRef.current = null;
        dragY.setValue(0);
      },
    });
  };

  const handleEditFromDetail = (student: StudentEntity) => {
    setSelectedStudentForDetail(null);
    setStudentToEdit(student);
    setIsAddModalOpen(true);
  };

  return (
    <Surface style={[styles.container, { backgroundColor: colors.background }]} elevation={0}>
      {/* 1. Top bar: Manage Students */}
      <TopAppBar
        title="Manage Students"
        showSettings
        onSettingsPress={() => onNavigate('settings')}
      />

      {/* 2. M3 Contained button "+ Add Student" */}
      <View style={styles.addButtonWrapper}>
        <Button
          mode="contained"
          icon="plus"
          onPress={() => {
            setStudentToEdit(null);
            setIsAddModalOpen(true);
          }}
          buttonColor={colors.primary}
          textColor={colors.onPrimary}
          style={styles.addButton}
          contentStyle={{ height: 48 }}
          labelStyle={{ fontSize: 16, fontWeight: '700' }}
        >
          Add Student
        </Button>
      </View>

      {/* 3. Body */}
      {localStudents.length === 0 ? (
        <EmptyState message='No students yet – tap "+ Add Student" to get started' />
      ) : (
        <ScrollView
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
        >
          <Card
            mode="elevated"
            elevation={1}
            style={[
              styles.listCard,
              {
                backgroundColor: colors.surfaceContainer,
              },
            ]}
          >
            {localStudents.map((student, index) => {
              const isDragging = draggingIndex === index;
              const panResponder = createPanResponder(index);

              return (
                <Animated.View
                  key={student.id}
                  style={[
                    styles.rowContainer,
                    {
                      borderBottomColor: colors.outlineVariant,
                      backgroundColor: isDragging
                        ? colors.primaryContainer
                        : 'transparent',
                      transform: isDragging ? [{ translateY: dragY }] : [],
                      zIndex: isDragging ? 999 : 1,
                      elevation: isDragging ? 6 : 0,
                    },
                  ]}
                >
                  {/* Drag Handle: 40 dp touch target */}
                  <View
                    {...panResponder.panHandlers}
                    style={styles.dragHandle}
                  >
                    <MaterialCommunityIcons
                      name="drag"
                      size={24}
                      color={isDragging ? colors.onPrimaryContainer : colors.onSurfaceVariant}
                    />
                  </View>

                  {/* Rest of the row is clickable with ripple to open detail modal */}
                  <TouchableRipple
                    onPress={() => setSelectedStudentForDetail(student)}
                    style={styles.rowClickableArea}
                  >
                    <View style={styles.rowInner}>
                      {/* Serial number */}
                      <Text
                        variant="bodyMedium"
                        style={[
                          styles.colSr,
                          { color: isDragging ? colors.onPrimaryContainer : colors.onSurfaceVariant },
                        ]}
                      >
                        {index + 1}
                      </Text>

                      {/* Name: bold, flex, single-line ellipsis */}
                      <Text
                        variant="bodyMedium"
                        style={[
                          styles.colName,
                          {
                            color: isDragging ? colors.onPrimaryContainer : colors.onSurface,
                            fontWeight: '700',
                          },
                        ]}
                        numberOfLines={1}
                      >
                        {student.name}
                      </Text>

                      {/* Reg no: 96 dp */}
                      <Text
                        variant="bodySmall"
                        style={[
                          styles.colReg,
                          { color: isDragging ? colors.onPrimaryContainer : colors.onSurfaceVariant },
                        ]}
                        numberOfLines={1}
                      >
                        {student.regNo}
                      </Text>

                      {/* Sec {X}: 52 dp */}
                      <Text
                        variant="bodySmall"
                        style={[
                          styles.colSec,
                          {
                            color: isDragging ? colors.onPrimaryContainer : colors.onSurfaceVariant,
                            fontWeight: '600',
                          },
                        ]}
                      >
                        {`Sec ${student.section}`}
                      </Text>
                    </View>
                  </TouchableRipple>
                </Animated.View>
              );
            })}
          </Card>
        </ScrollView>
      )}

      {/* Add / Edit Student Modal */}
      <AddEditStudentModal
        visible={isAddModalOpen}
        studentToEdit={studentToEdit}
        existingStudents={students}
        onSave={async (saved) => {
          if (studentToEdit) {
            await updateStudent(saved);
          } else {
            await addStudent(saved);
          }
        }}
        onCancel={() => {
          setIsAddModalOpen(false);
          setStudentToEdit(null);
        }}
      />

      {/* Student Detail Modal (With Edit and Delete actions enabled) */}
      <StudentDetailModal
        visible={selectedStudentForDetail !== null}
        student={selectedStudentForDetail}
        showEditDelete={true}
        onEdit={handleEditFromDetail}
        onDelete={async (id) => {
          await deleteStudent(id);
        }}
        onClose={() => setSelectedStudentForDetail(null)}
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
    paddingBottom: 90,
  },
  listCard: {
    borderRadius: 16,
    overflow: 'hidden',
  },
  rowContainer: {
    height: ROW_HEIGHT,
    flexDirection: 'row',
    alignItems: 'center',
    borderBottomWidth: StyleSheet.hairlineWidth,
    paddingHorizontal: 4,
  },
  dragHandle: {
    width: 40,
    height: 40,
    alignItems: 'center',
    justifyContent: 'center',
  },
  rowClickableArea: {
    flex: 1,
    height: '100%',
  },
  rowInner: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    paddingRight: 8,
  },
  colSr: {
    width: 36,
    textAlign: 'center',
  },
  colName: {
    flex: 1,
    paddingHorizontal: 6,
  },
  colReg: {
    width: 96,
    paddingHorizontal: 4,
  },
  colSec: {
    width: 52,
    textAlign: 'right',
  },
});

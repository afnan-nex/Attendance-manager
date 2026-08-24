import { useState, useEffect } from 'react';
import { dbService } from './database';
import { ClassEntity, StudentEntity, AttendanceRecord, AppSettings } from '../types';

export function useDatabase() {
  const [classes, setClasses] = useState<ClassEntity[]>(() => dbService.getClasses());
  const [students, setStudents] = useState<StudentEntity[]>(() => dbService.getStudents());
  const [attendance, setAttendance] = useState<AttendanceRecord[]>(() => dbService.getAttendance());
  const [settings, setSettings] = useState<AppSettings>(() => dbService.getSettings());

  useEffect(() => {
    const update = () => {
      setClasses(dbService.getClasses());
      setStudents(dbService.getStudents());
      setAttendance(dbService.getAttendance());
      setSettings(dbService.getSettings());
    };

    update();
    const unsubscribe = dbService.subscribe(update);
    return unsubscribe;
  }, []);

  return {
    classes,
    students,
    attendance,
    settings,
    addClass: (c: ClassEntity) => dbService.addClass(c),
    updateClass: (c: ClassEntity) => dbService.updateClass(c),
    toggleClassHidden: (id: string) => dbService.toggleClassHidden(id),
    deleteClass: (id: string) => dbService.deleteClass(id),
    upsertClasses: (cs: ClassEntity[]) => dbService.upsertClasses(cs),

    addStudent: (s: StudentEntity) => dbService.addStudent(s),
    updateStudent: (s: StudentEntity) => dbService.updateStudent(s),
    deleteStudent: (id: string) => dbService.deleteStudent(id),
    reorderStudents: (ss: StudentEntity[]) => dbService.reorderStudents(ss),
    upsertStudents: (ss: StudentEntity[]) => dbService.upsertStudents(ss),

    toggleAttendance: (classId: string, studentId: string, date: string, isPresent: boolean) =>
      dbService.toggleAttendance(classId, studentId, date, isPresent),
    getAttendanceForClassAndDate: (classId: string, date: string) =>
      dbService.getAttendanceForClassAndDate(classId, date),

    updateSettings: (s: Partial<AppSettings>) => dbService.updateSettings(s),
  };
}

package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow

class StudentRepository(
    private val userProfileDao: UserProfileDao,
    private val subjectDao: SubjectDao,
    private val attendanceLogDao: AttendanceLogDao,
    private val semesterDao: SemesterDao,
    private val courseDao: CourseDao
) {
    // User Profile
    fun getUserProfile(email: String): Flow<UserProfile?> = userProfileDao.getUserProfile(email)
    suspend fun saveUserProfile(profile: UserProfile) = userProfileDao.insertUserProfile(profile)

    // Subjects
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()
    fun getSubjectById(id: Int): Flow<Subject?> = subjectDao.getSubjectById(id)
    suspend fun addSubject(subject: Subject): Long = subjectDao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) {
        attendanceLogDao.deleteLogsForSubject(subject.id)
        subjectDao.deleteSubject(subject)
    }

    // Attendance Logs
    val allLogs: Flow<List<AttendanceLog>> = attendanceLogDao.getAllLogs()
    fun getLogsForSubject(subjectId: Int): Flow<List<AttendanceLog>> = attendanceLogDao.getLogsForSubject(subjectId)
    suspend fun logAttendance(log: AttendanceLog) = attendanceLogDao.insertLog(log)
    suspend fun deleteLog(log: AttendanceLog) = attendanceLogDao.deleteLog(log)

    // Semesters
    val allSemesters: Flow<List<Semester>> = semesterDao.getAllSemesters()
    suspend fun addSemester(semester: Semester) = semesterDao.insertSemester(semester)
    suspend fun deleteSemester(semester: Semester) {
        courseDao.deleteCoursesForSemester(semester.id)
        semesterDao.deleteSemester(semester)
    }

    // Courses
    fun getCoursesForSemester(semesterId: Int): Flow<List<Course>> = courseDao.getCoursesForSemester(semesterId)
    suspend fun addCourse(course: Course) = courseDao.insertCourse(course)
    suspend fun deleteCourse(course: Course) = courseDao.deleteCourse(course)
}

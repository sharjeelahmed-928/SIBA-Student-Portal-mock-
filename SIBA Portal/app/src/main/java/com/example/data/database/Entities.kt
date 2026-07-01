package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val email: String,
    val name: String = "",
    val studentId: String = "",
    val rollNumber: String = "",
    val department: String = "",
    val semester: String = "",
    val batch: String = "",
    val university: String = "Sukkur IBA University",
    val phone: String = "",
    val bio: String = "",
    val photoUriString: String = "",
    val githubUrl: String = "",
    val linkedinUrl: String = "",
    val portfolioUrl: String = "",
    val degree: String = "BS",
    val role: String = "Student",
    val creationDate: String = "",
    val lastLogin: String = ""
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val totalClasses: Int = 0,
    val attendedClasses: Int = 0,
    val targetPercentage: Double = 75.0
)

@Entity(tableName = "attendance_logs")
data class AttendanceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val dateMillis: Long = System.currentTimeMillis(),
    val status: String // "Present", "Absent", "Late"
)

@Entity(tableName = "semesters")
data class Semester(
    @PrimaryKey val id: Int, // e.g., 1, 2, 3...
    val name: String // e.g., "Semester 1", "Semester 2"
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val semesterId: Int,
    val name: String,
    val creditHours: Int,
    val grade: String, // "A", "B+", etc.
    val gradePoints: Double // 4.0, 3.5, etc.
)

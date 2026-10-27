package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameAr: String,
    val nameFr: String,
    val code: String,
    val colorHex: String = "#8066B5",
    val orderIndex: Int = 0
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val description: String = "",
    val status: Int = 0 // 0: Not started, 1: Learning, 2: Understood, 3: Needs review
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val subjectId: Long = 0,
    val deadlineTimestamp: Long = 0L,
    val priority: Int = 1, // 0: Low, 1: Medium, 2: High
    val isCompleted: Boolean = false,
    val type: Int = 0 // 0: Homework, 1: Assignment, 2: Prep, 3: Revision
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectId: Long = 0,
    val examTimestamp: Long = 0L,
    val notes: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val subjectId: Long = 0,
    val tags: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val updatedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "borrowed_books")
data class BorrowedBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectId: Long = 0,
    val borrowedDateTimestamp: Long = System.currentTimeMillis(),
    val returnDateTimestamp: Long = System.currentTimeMillis() + 14L * 24 * 3600 * 1000,
    val notes: String = "",
    val isReturned: Boolean = false
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val answer: String,
    val subjectId: Long = 0,
    val tags: String = "",
    val intervalDays: Int = 1,
    val nextReviewTimestamp: Long = System.currentTimeMillis(),
    val reviewCount: Int = 0,
    val lastRating: String = "" // "forgot", "hard", "easy"
)

@Entity(tableName = "formulas")
data class FormulaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mathExpression: String,
    val explanation: String = "",
    val example: String = "",
    val subjectId: Long = 0,
    val tags: String = ""
)

@Entity(tableName = "mistakes")
data class MistakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "cat_calc_error",
    val lessonLearned: String = "",
    val subjectId: Long = 0,
    val isReviewed: Boolean = false,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationMinutes: Int = 25,
    val sessionType: String = "focus", // "focus", "short_break", "long_break"
    val timestamp: Long = System.currentTimeMillis()
)

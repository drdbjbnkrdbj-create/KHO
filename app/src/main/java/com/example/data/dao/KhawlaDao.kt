package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KhawlaDao {

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC, id ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    // Chapters
    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY id ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    // Lessons
    @Query("SELECT * FROM lessons WHERE chapterId = :chapterId ORDER BY id ASC")
    fun getLessonsForChapter(chapterId: Long): Flow<List<LessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity): Long

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: LessonEntity)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, deadlineTimestamp ASC, id DESC")
    fun getAllTasks(): Flow<List<TaskItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItemEntity): Long

    @Update
    suspend fun updateTask(task: TaskItemEntity)

    @Delete
    suspend fun deleteTask(task: TaskItemEntity)

    // Exams
    @Query("SELECT * FROM exams ORDER BY examTimestamp ASC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    // Notes
    @Query("SELECT * FROM notes ORDER BY updatedAtTimestamp DESC")
    fun getAllNotes(): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity): Long

    @Update
    suspend fun updateNote(note: StudyNoteEntity)

    @Delete
    suspend fun deleteNote(note: StudyNoteEntity)

    // Borrowed Books
    @Query("SELECT * FROM borrowed_books ORDER BY isReturned ASC, returnDateTimestamp ASC")
    fun getAllBooks(): Flow<List<BorrowedBookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BorrowedBookEntity): Long

    @Update
    suspend fun updateBook(book: BorrowedBookEntity)

    @Delete
    suspend fun deleteBook(book: BorrowedBookEntity)

    // Flashcards
    @Query("SELECT * FROM flashcards ORDER BY nextReviewTimestamp ASC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)

    // Formulas
    @Query("SELECT * FROM formulas ORDER BY id DESC")
    fun getAllFormulas(): Flow<List<FormulaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormula(formula: FormulaEntity): Long

    @Update
    suspend fun updateFormula(formula: FormulaEntity)

    @Delete
    suspend fun deleteFormula(formula: FormulaEntity)

    // Mistakes
    @Query("SELECT * FROM mistakes ORDER BY isReviewed ASC, createdAtTimestamp DESC")
    fun getAllMistakes(): Flow<List<MistakeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeEntity): Long

    @Update
    suspend fun updateMistake(mistake: MistakeEntity)

    @Delete
    suspend fun deleteMistake(mistake: MistakeEntity)

    // Focus Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    // Bulk Delete for Reset
    @Query("DELETE FROM subjects")
    suspend fun clearSubjects()
    @Query("DELETE FROM chapters")
    suspend fun clearChapters()
    @Query("DELETE FROM lessons")
    suspend fun clearLessons()
    @Query("DELETE FROM tasks")
    suspend fun clearTasks()
    @Query("DELETE FROM exams")
    suspend fun clearExams()
    @Query("DELETE FROM notes")
    suspend fun clearNotes()
    @Query("DELETE FROM borrowed_books")
    suspend fun clearBooks()
    @Query("DELETE FROM flashcards")
    suspend fun clearFlashcards()
    @Query("DELETE FROM formulas")
    suspend fun clearFormulas()
    @Query("DELETE FROM mistakes")
    suspend fun clearMistakes()
    @Query("DELETE FROM focus_sessions")
    suspend fun clearFocusSessions()
}

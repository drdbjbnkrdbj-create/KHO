package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.KhawlaDao
import com.example.data.model.*

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        LessonEntity::class,
        TaskItemEntity::class,
        ExamEntity::class,
        StudyNoteEntity::class,
        BorrowedBookEntity::class,
        FlashcardEntity::class,
        FormulaEntity::class,
        MistakeEntity::class,
        FocusSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun khawlaDao(): KhawlaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "khawla_study.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

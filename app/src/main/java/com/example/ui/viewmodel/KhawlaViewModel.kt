package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.KhawlaRepository
import com.example.data.PreferencesManager
import com.example.data.model.*
import com.example.localization.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RescuePlan(
    val title: String,
    val taskTitle: String,
    val reason: String,
    val durationMinutes: Int,
    val omarAdvice: String
)

class KhawlaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repo = KhawlaRepository(db.khawlaDao())
    val prefs = PreferencesManager(application)

    // Data streams
    val subjects: StateFlow<List<SubjectEntity>> = repo.subjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chapters: StateFlow<List<ChapterEntity>> = repo.chapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskItemEntity>> = repo.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<ExamEntity>> = repo.exams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<StudyNoteEntity>> = repo.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val books: StateFlow<List<BorrowedBookEntity>> = repo.books
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcards: StateFlow<List<FlashcardEntity>> = repo.flashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val formulas: StateFlow<List<FormulaEntity>> = repo.formulas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mistakes: StateFlow<List<MistakeEntity>> = repo.mistakes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSessionEntity>> = repo.focusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Preferences
    val language: StateFlow<AppLanguage> = prefs.language
    val theme: StateFlow<String> = prefs.theme
    val isOmarVisible: StateFlow<Boolean> = prefs.isOmarVisible
    val isOnboardingCompleted: StateFlow<Boolean> = prefs.isOnboardingCompleted
    val soundVibrateEnabled: StateFlow<Boolean> = prefs.soundVibrateEnabled
    val focusDurationMinutes: StateFlow<Int> = prefs.focusDurationMinutes

    // Navigation and UI state
    // 0: Home, 1: Subjects, 2: Tasks, 3: Review, 4: Library, 5: FocusTimer, 6: Settings
    private val _currentNavDestination = MutableStateFlow(0)
    val currentNavDestination: StateFlow<Int> = _currentNavDestination.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId: StateFlow<Long?> = _selectedSubjectId.asStateFlow()

    private val _isQuickAddOpen = MutableStateFlow(false)
    val isQuickAddOpen: StateFlow<Boolean> = _isQuickAddOpen.asStateFlow()

    private val _isOmarHelpOpen = MutableStateFlow(false)
    val isOmarHelpOpen: StateFlow<Boolean> = _isOmarHelpOpen.asStateFlow()

    private val _isRescueDialogOpen = MutableStateFlow(false)
    val isRescueDialogOpen: StateFlow<Boolean> = _isRescueDialogOpen.asStateFlow()

    private val _rescuePlan = MutableStateFlow<RescuePlan?>(null)
    val rescuePlan: StateFlow<RescuePlan?> = _rescuePlan.asStateFlow()

    private val _activeMoodMode = MutableStateFlow<String?>(null) // "organized", "busy", "unmotivated"
    val activeMoodMode: StateFlow<String?> = _activeMoodMode.asStateFlow()

    // Global Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Focus Timer State
    private val _timerSecondsRemaining = MutableStateFlow(25 * 60)
    val timerSecondsRemaining: StateFlow<Int> = _timerSecondsRemaining.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _isBreakMode = MutableStateFlow(false)
    val isBreakMode: StateFlow<Boolean> = _isBreakMode.asStateFlow()

    private var timerJob: Job? = null

    // Flashcard Study State
    private val _currentStudyCardIndex = MutableStateFlow(0)
    val currentStudyCardIndex: StateFlow<Int> = _currentStudyCardIndex.asStateFlow()

    private val _isAnswerRevealed = MutableStateFlow(false)
    val isAnswerRevealed: StateFlow<Boolean> = _isAnswerRevealed.asStateFlow()

    init {
        viewModelScope.launch {
            repo.checkAndSeedInitialData()
        }
    }

    // Navigation methods
    fun navigateTo(destination: Int) {
        _currentNavDestination.value = destination
        if (destination != 1) {
            _selectedSubjectId.value = null
        }
    }

    fun selectSubject(subjectId: Long?) {
        _selectedSubjectId.value = subjectId
    }

    fun setQuickAddOpen(open: Boolean) {
        _isQuickAddOpen.value = open
    }

    fun setOmarHelpOpen(open: Boolean) {
        _isOmarHelpOpen.value = open
    }

    fun setRescueDialogOpen(open: Boolean) {
        _isRescueDialogOpen.value = open
        if (!open) {
            _rescuePlan.value = null
        }
    }

    fun setMoodMode(mode: String?) {
        _activeMoodMode.value = if (_activeMoodMode.value == mode) null else mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Rescue Mode Deterministic Logic
    fun generateRescuePlan(availableMinutes: Int, energyLevel: String, chosenSubjectId: Long?) {
        val currentTasks = tasks.value.filter { !it.isCompleted }
        val currentSubjects = subjects.value

        val filteredTasks = if (chosenSubjectId != null && chosenSubjectId > 0) {
            currentTasks.filter { it.subjectId == chosenSubjectId }
        } else {
            currentTasks
        }

        val targetTask = when (energyLevel) {
            "low" -> {
                // Pick lowest priority or prep/revision task
                filteredTasks.minByOrNull { it.priority } ?: currentTasks.firstOrNull()
            }
            "medium" -> {
                // Pick task with closest deadline
                filteredTasks.filter { it.deadlineTimestamp > 0 }
                    .minByOrNull { it.deadlineTimestamp }
                    ?: filteredTasks.firstOrNull() ?: currentTasks.firstOrNull()
            }
            else -> {
                // High energy: tackle highest priority
                filteredTasks.maxByOrNull { it.priority } ?: currentTasks.firstOrNull()
            }
        }

        val taskTitle = targetTask?.title ?: "مراجعة سريعة لبطاقات اليوم"
        val subjectName = currentSubjects.find { it.id == targetTask?.subjectId }?.let {
            if (language.value == AppLanguage.ARABIC) it.nameAr else it.nameFr
        } ?: "MPSI"

        val advice = when (energyLevel) {
            "low" -> if (language.value == AppLanguage.ARABIC)
                "لا تضغطي على نفسك أبداً. افتحي التمرين واقرئي المعطيات فقط لمدة $availableMinutes دقائق، هذا يكفي كبداية!"
            else
                "Ne te mets aucune pression. Lis simplement l'énoncé pendant $availableMinutes minutes, c'est déjà un formidable premier pas !"
            "medium" -> if (language.value == AppLanguage.ARABIC)
                "ممتاز! ركزي بهدوء على حل سؤال واحد فقط من هذه المهمة ($subjectName)."
            else
                "Super ! Concentre-toi calmement sur une seule question de cette tâche ($subjectName)."
            else -> if (language.value == AppLanguage.ARABIC)
                "طاقتك رائعة اليوم! أعدّي ورقة بيضاء وقلم وابدئي فوراً، ستنهين الكثير في هذه الجلسة!"
            else
                "Belle énergie aujourd'hui ! Prends une feuille blanche et lance-toi, tu vas faire de grands progrès !"
        }

        _rescuePlan.value = RescuePlan(
            title = if (language.value == AppLanguage.ARABIC) "خطوة إنقاذ لمادة $subjectName" else "Plan de sauvetage : $subjectName",
            taskTitle = taskTitle,
            reason = if (language.value == AppLanguage.ARABIC) "مهمة تناسب وقتك ($availableMinutes دقيقة) ومستوى طاقتك الحالي." else "Adapté à ton temps ($availableMinutes min) et ton énergie.",
            durationMinutes = availableMinutes,
            omarAdvice = advice
        )
    }

    // CRUD - Tasks
    fun toggleTaskCompleted(task: TaskItemEntity) {
        viewModelScope.launch {
            repo.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun addTask(title: String, desc: String, subjectId: Long, deadlineMs: Long, priority: Int, type: Int) {
        viewModelScope.launch {
            repo.insertTask(
                TaskItemEntity(
                    title = title,
                    description = desc,
                    subjectId = subjectId,
                    deadlineTimestamp = deadlineMs,
                    priority = priority,
                    type = type
                )
            )
        }
    }

    fun deleteTask(task: TaskItemEntity) {
        viewModelScope.launch {
            repo.deleteTask(task)
        }
    }

    // CRUD - Subjects & Chapters
    fun addSubject(nameAr: String, nameFr: String, code: String, colorHex: String) {
        viewModelScope.launch {
            val order = (subjects.value.maxOfOrNull { it.orderIndex } ?: 0) + 1
            repo.insertSubject(
                SubjectEntity(
                    nameAr = nameAr,
                    nameFr = nameFr,
                    code = code,
                    colorHex = colorHex,
                    orderIndex = order
                )
            )
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repo.deleteSubject(subject)
            if (_selectedSubjectId.value == subject.id) {
                _selectedSubjectId.value = null
            }
        }
    }

    fun addChapter(subjectId: Long, title: String, desc: String, status: Int) {
        viewModelScope.launch {
            repo.insertChapter(
                ChapterEntity(
                    subjectId = subjectId,
                    title = title,
                    description = desc,
                    status = status
                )
            )
        }
    }

    fun updateChapterStatus(chapter: ChapterEntity, newStatus: Int) {
        viewModelScope.launch {
            repo.updateChapter(chapter.copy(status = newStatus))
        }
    }

    fun deleteChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repo.deleteChapter(chapter)
        }
    }

    // Flashcards & Spaced Repetition
    fun addFlashcard(question: String, answer: String, subjectId: Long, tags: String) {
        viewModelScope.launch {
            repo.insertFlashcard(
                FlashcardEntity(
                    question = question,
                    answer = answer,
                    subjectId = subjectId,
                    tags = tags
                )
            )
        }
    }

    fun revealFlashcardAnswer() {
        _isAnswerRevealed.value = true
    }

    fun rateFlashcard(card: FlashcardEntity, rating: String) {
        viewModelScope.launch {
            repo.reviewFlashcard(card, rating)
            _isAnswerRevealed.value = false
            val dueCards = flashcards.value.filter { it.nextReviewTimestamp <= System.currentTimeMillis() }
            if (_currentStudyCardIndex.value < dueCards.size - 1) {
                _currentStudyCardIndex.value += 1
            } else {
                _currentStudyCardIndex.value = 0
            }
        }
    }

    fun deleteFlashcard(card: FlashcardEntity) {
        viewModelScope.launch {
            repo.deleteFlashcard(card)
        }
    }

    // Formulas & Mistakes
    fun addFormula(name: String, expr: String, explanation: String, example: String, subjectId: Long, tags: String) {
        viewModelScope.launch {
            repo.insertFormula(
                FormulaEntity(
                    name = name,
                    mathExpression = expr,
                    explanation = explanation,
                    example = example,
                    subjectId = subjectId,
                    tags = tags
                )
            )
        }
    }

    fun convertFormulaToFlashcard(formula: FormulaEntity) {
        viewModelScope.launch {
            repo.insertFlashcard(
                FlashcardEntity(
                    question = "Formule / Théorème : ${formula.name}",
                    answer = "${formula.mathExpression}\n\n${formula.explanation}\nEx: ${formula.example}",
                    subjectId = formula.subjectId,
                    tags = formula.tags
                )
            )
        }
    }

    fun deleteFormula(formula: FormulaEntity) {
        viewModelScope.launch {
            repo.deleteFormula(formula)
        }
    }

    fun addMistake(title: String, desc: String, category: String, lesson: String, subjectId: Long) {
        viewModelScope.launch {
            repo.insertMistake(
                MistakeEntity(
                    title = title,
                    description = desc,
                    category = category,
                    lessonLearned = lesson,
                    subjectId = subjectId
                )
            )
        }
    }

    fun toggleMistakeReviewed(mistake: MistakeEntity) {
        viewModelScope.launch {
            repo.updateMistake(mistake.copy(isReviewed = !mistake.isReviewed))
        }
    }

    fun deleteMistake(mistake: MistakeEntity) {
        viewModelScope.launch {
            repo.deleteMistake(mistake)
        }
    }

    // Notes
    fun addNote(title: String, content: String, subjectId: Long, tags: String) {
        viewModelScope.launch {
            repo.insertNote(
                StudyNoteEntity(
                    title = title,
                    content = content,
                    subjectId = subjectId,
                    tags = tags
                )
            )
        }
    }

    fun deleteNote(note: StudyNoteEntity) {
        viewModelScope.launch {
            repo.deleteNote(note)
        }
    }

    // Borrowed Books
    fun addBook(title: String, subjectId: Long, returnDateMs: Long, notes: String) {
        viewModelScope.launch {
            repo.insertBook(
                BorrowedBookEntity(
                    title = title,
                    subjectId = subjectId,
                    returnDateTimestamp = returnDateMs,
                    notes = notes
                )
            )
        }
    }

    fun toggleBookReturned(book: BorrowedBookEntity) {
        viewModelScope.launch {
            repo.updateBook(book.copy(isReturned = !book.isReturned))
        }
    }

    fun deleteBook(book: BorrowedBookEntity) {
        viewModelScope.launch {
            repo.deleteBook(book)
        }
    }

    // Exams
    fun addExam(title: String, subjectId: Long, examTimestamp: Long, notes: String) {
        viewModelScope.launch {
            repo.insertExam(
                ExamEntity(
                    title = title,
                    subjectId = subjectId,
                    examTimestamp = examTimestamp,
                    notes = notes
                )
            )
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            repo.deleteExam(exam)
        }
    }

    // Focus Timer Engine
    fun toggleTimer() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerSecondsRemaining.value > 0 && _isTimerRunning.value) {
                delay(1000L)
                _timerSecondsRemaining.value -= 1
            }
            if (_timerSecondsRemaining.value <= 0) {
                // Session completed
                val wasBreak = _isBreakMode.value
                val duration = if (wasBreak) 5 else focusDurationMinutes.value
                repo.logFocusSession(duration, if (wasBreak) "short_break" else "focus")

                // Switch modes
                _isBreakMode.value = !wasBreak
                _timerSecondsRemaining.value = if (_isBreakMode.value) 5 * 60 else focusDurationMinutes.value * 60
                _isTimerRunning.value = false
            }
        }
    }

    private fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _isBreakMode.value = false
        _timerSecondsRemaining.value = focusDurationMinutes.value * 60
    }

    // Backup & Reset
    suspend fun exportBackupJson(): String = repo.exportBackupJson()
    suspend fun importBackupJson(json: String): Boolean = repo.importBackupJson(json)
    fun resetToDefaults() {
        viewModelScope.launch {
            repo.resetAllData()
        }
    }
}

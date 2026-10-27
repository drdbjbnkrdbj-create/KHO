package com.example.data

import com.example.data.dao.KhawlaDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class KhawlaRepository(private val dao: KhawlaDao) {

    val subjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val chapters: Flow<List<ChapterEntity>> = dao.getAllChapters()
    val tasks: Flow<List<TaskItemEntity>> = dao.getAllTasks()
    val exams: Flow<List<ExamEntity>> = dao.getAllExams()
    val notes: Flow<List<StudyNoteEntity>> = dao.getAllNotes()
    val books: Flow<List<BorrowedBookEntity>> = dao.getAllBooks()
    val flashcards: Flow<List<FlashcardEntity>> = dao.getAllFlashcards()
    val formulas: Flow<List<FormulaEntity>> = dao.getAllFormulas()
    val mistakes: Flow<List<MistakeEntity>> = dao.getAllMistakes()
    val focusSessions: Flow<List<FocusSessionEntity>> = dao.getAllFocusSessions()

    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>> =
        dao.getChaptersForSubject(subjectId)

    fun getLessonsForChapter(chapterId: Long): Flow<List<LessonEntity>> =
        dao.getLessonsForChapter(chapterId)

    suspend fun insertSubject(subject: SubjectEntity) = dao.insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)

    suspend fun insertChapter(chapter: ChapterEntity) = dao.insertChapter(chapter)
    suspend fun updateChapter(chapter: ChapterEntity) = dao.updateChapter(chapter)
    suspend fun deleteChapter(chapter: ChapterEntity) = dao.deleteChapter(chapter)

    suspend fun insertLesson(lesson: LessonEntity) = dao.insertLesson(lesson)
    suspend fun updateLesson(lesson: LessonEntity) = dao.updateLesson(lesson)
    suspend fun deleteLesson(lesson: LessonEntity) = dao.deleteLesson(lesson)

    suspend fun insertTask(task: TaskItemEntity) = dao.insertTask(task)
    suspend fun updateTask(task: TaskItemEntity) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskItemEntity) = dao.deleteTask(task)

    suspend fun insertExam(exam: ExamEntity) = dao.insertExam(exam)
    suspend fun updateExam(exam: ExamEntity) = dao.updateExam(exam)
    suspend fun deleteExam(exam: ExamEntity) = dao.deleteExam(exam)

    suspend fun insertNote(note: StudyNoteEntity) = dao.insertNote(note)
    suspend fun updateNote(note: StudyNoteEntity) = dao.updateNote(note)
    suspend fun deleteNote(note: StudyNoteEntity) = dao.deleteNote(note)

    suspend fun insertBook(book: BorrowedBookEntity) = dao.insertBook(book)
    suspend fun updateBook(book: BorrowedBookEntity) = dao.updateBook(book)
    suspend fun deleteBook(book: BorrowedBookEntity) = dao.deleteBook(book)

    suspend fun insertFlashcard(flashcard: FlashcardEntity) = dao.insertFlashcard(flashcard)
    suspend fun updateFlashcard(flashcard: FlashcardEntity) = dao.updateFlashcard(flashcard)
    suspend fun deleteFlashcard(flashcard: FlashcardEntity) = dao.deleteFlashcard(flashcard)

    suspend fun reviewFlashcard(card: FlashcardEntity, rating: String) {
        val now = System.currentTimeMillis()
        val (nextInterval, nextTimestamp) = when (rating) {
            "forgot" -> Pair(0, now + 10 * 60 * 1000) // in 10 minutes
            "hard" -> {
                val interval = maxOf(1, card.intervalDays)
                Pair(interval, now + interval * 24L * 3600 * 1000)
            }
            "easy" -> {
                val interval = if (card.intervalDays <= 1) 3 else (card.intervalDays * 2)
                Pair(interval, now + interval * 24L * 3600 * 1000)
            }
            else -> Pair(1, now + 24L * 3600 * 1000)
        }

        val updated = card.copy(
            intervalDays = nextInterval,
            nextReviewTimestamp = nextTimestamp,
            reviewCount = card.reviewCount + 1,
            lastRating = rating
        )
        dao.updateFlashcard(updated)
    }

    suspend fun insertFormula(formula: FormulaEntity) = dao.insertFormula(formula)
    suspend fun updateFormula(formula: FormulaEntity) = dao.updateFormula(formula)
    suspend fun deleteFormula(formula: FormulaEntity) = dao.deleteFormula(formula)

    suspend fun insertMistake(mistake: MistakeEntity) = dao.insertMistake(mistake)
    suspend fun updateMistake(mistake: MistakeEntity) = dao.updateMistake(mistake)
    suspend fun deleteMistake(mistake: MistakeEntity) = dao.deleteMistake(mistake)

    suspend fun logFocusSession(durationMinutes: Int, sessionType: String) {
        dao.insertFocusSession(
            FocusSessionEntity(
                durationMinutes = durationMinutes,
                sessionType = sessionType
            )
        )
    }

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val currentSubjects = dao.getAllSubjects().first()
        if (currentSubjects.isEmpty()) {
            seedMpsiData()
        }
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        dao.clearSubjects()
        dao.clearChapters()
        dao.clearLessons()
        dao.clearTasks()
        dao.clearExams()
        dao.clearNotes()
        dao.clearBooks()
        dao.clearFlashcards()
        dao.clearFormulas()
        dao.clearMistakes()
        dao.clearFocusSessions()
        seedMpsiData()
    }

    private suspend fun seedMpsiData() {
        // Subjects
        val mathId = dao.insertSubject(
            SubjectEntity(
                nameAr = "الرياضيات",
                nameFr = "Mathématiques",
                code = "MPSI-MATH",
                colorHex = "#8066B5",
                orderIndex = 1
            )
        )
        val physId = dao.insertSubject(
            SubjectEntity(
                nameAr = "الفيزياء",
                nameFr = "Physique",
                code = "MPSI-PHYS",
                colorHex = "#5E81AC",
                orderIndex = 2
            )
        )
        val chemId = dao.insertSubject(
            SubjectEntity(
                nameAr = "الكيمياء",
                nameFr = "Chimie",
                code = "MPSI-CHIM",
                colorHex = "#E06C75",
                orderIndex = 3
            )
        )
        val infoId = dao.insertSubject(
            SubjectEntity(
                nameAr = "الإعلاميات",
                nameFr = "Informatique",
                code = "MPSI-INFO",
                colorHex = "#4CAE75",
                orderIndex = 4
            )
        )
        val siId = dao.insertSubject(
            SubjectEntity(
                nameAr = "علوم المهندس",
                nameFr = "Sciences de l'Ingénieur",
                code = "MPSI-SII",
                colorHex = "#D08770",
                orderIndex = 5
            )
        )

        // Chapters & Lessons for Maths
        val ch1 = dao.insertChapter(
            ChapterEntity(
                subjectId = mathId,
                title = "Dérivation et Développements Limités (DL)",
                description = "Formule de Taylor-Young, Développements asymptotiques",
                status = 1 // Learning
            )
        )
        dao.insertLesson(LessonEntity(chapterId = ch1, title = "Théorème de Rolle et TAF", isCompleted = true))
        dao.insertLesson(LessonEntity(chapterId = ch1, title = "Formules de Taylor", isCompleted = true))
        dao.insertLesson(LessonEntity(chapterId = ch1, title = "Opérations sur les DL usuels", isCompleted = false))

        val ch2 = dao.insertChapter(
            ChapterEntity(
                subjectId = mathId,
                title = "Espaces Vectoriels et Applications Linéaires",
                description = "Bases, dimension finie, théorème du rang",
                status = 2 // Understood
            )
        )
        dao.insertLesson(LessonEntity(chapterId = ch2, title = "Sous-espaces vectoriels et familles libres", isCompleted = true))
        dao.insertLesson(LessonEntity(chapterId = ch2, title = "Théorème du rang : dim(E) = dim(Ker) + dim(Im)", isCompleted = true))

        // Chapters for Physics
        val chPhys1 = dao.insertChapter(
            ChapterEntity(
                subjectId = physId,
                title = "Mécanique du point et oscillateurs",
                description = "Lois de Newton, énergie mécanique, frottements fluides",
                status = 1
            )
        )
        dao.insertLesson(LessonEntity(chapterId = chPhys1, title = "Principe Fondamental de la Dynamique (PFD)", isCompleted = true))
        dao.insertLesson(LessonEntity(chapterId = chPhys1, title = "Oscillateur harmonique et amorti", isCompleted = false))

        val chPhys2 = dao.insertChapter(
            ChapterEntity(
                subjectId = physId,
                title = "Électrocinétique : Régimes transitoires",
                description = "Circuits RC, RL, RLC série et résonance",
                status = 3 // Needs review
            )
        )
        dao.insertLesson(LessonEntity(chapterId = chPhys2, title = "Équations différentielles en régime libre", isCompleted = true))

        // Tasks
        val now = System.currentTimeMillis()
        val dayMs = 24L * 3600 * 1000
        dao.insertTask(
            TaskItemEntity(
                title = "Résoudre TD Développements Limités (Ex 4 et 7)",
                description = "Attention au terme d'erreur o(x^3)",
                subjectId = mathId,
                deadlineTimestamp = now + dayMs,
                priority = 2,
                type = 1
            )
        )
        dao.insertTask(
            TaskItemEntity(
                title = "Rédiger le Devoir Maison (DM) de Mécanique",
                description = "Mouvement d'une particule chargée dans un champ B",
                subjectId = physId,
                deadlineTimestamp = now + 2 * dayMs,
                priority = 2,
                type = 0
            )
        )
        dao.insertTask(
            TaskItemEntity(
                title = "Fiche résumé : Complexité des algorithmes de tri",
                description = "Tri fusion vs Tri rapide (QuickSort)",
                subjectId = infoId,
                deadlineTimestamp = now + 4 * dayMs,
                priority = 1,
                type = 3
            )
        )

        // Exams
        dao.insertExam(
            ExamEntity(
                title = "Concours Blanc #1 : Mathématiques I",
                subjectId = mathId,
                examTimestamp = now + 14 * dayMs,
                notes = "Programme : Analyse réelle, Suites, Fonctions dérivables, DL"
            )
        )
        dao.insertExam(
            ExamEntity(
                title = "Colle de Physique : Oscillateurs & RLC",
                subjectId = physId,
                examTimestamp = now + 3 * dayMs,
                notes = "Questions de cours : Facteur de qualité Q et résonance en intensité"
            )
        )

        // Formulas
        val f1 = dao.insertFormula(
            FormulaEntity(
                name = "Dérivée d'une puissance x^n",
                mathExpression = "d/dx(x^n) = n * x^(n-1)",
                explanation = "Valable pour tout n réel non nul, sur l'intervalle de définition approprié.",
                example = "f(x) = x^3 => f'(x) = 3x^2",
                subjectId = mathId,
                tags = "Dérivée, Analyse"
            )
        )
        dao.insertFormula(
            FormulaEntity(
                name = "Théorème du Rang (Dimension finie)",
                mathExpression = "dim(E) = dim(Ker(u)) + dim(Im(u))",
                explanation = "Pour tout endomorphisme ou application linéaire u d'un espace vectoriel E de dimension finie vers F.",
                example = "Si dim(E) = 3 et dim(Ker(u)) = 1, alors rg(u) = 2.",
                subjectId = mathId,
                tags = "Algèbre linéaire"
            )
        )
        dao.insertFormula(
            FormulaEntity(
                name = "Principe Fondamental de la Dynamique (PFD)",
                mathExpression = "Σ F_ext = m * a",
                explanation = "Dans un référentiel galiléen, la somme vectorielle des forces extérieures est égale au produit de la masse par l'accélération.",
                example = "Chute libre : m*g = m*a => a = g",
                subjectId = physId,
                tags = "Mécanique"
            )
        )

        // Flashcards
        dao.insertFlashcard(
            FlashcardEntity(
                question = "Quelle est la formule de Taylor-Young à l'ordre n au voisinage de 0 ?",
                answer = "f(x) = Σ [f^(k)(0)/k! * x^k] + o(x^n)",
                subjectId = mathId,
                intervalDays = 1,
                nextReviewTimestamp = now - 1000 // Ready for review!
            )
        )
        dao.insertFlashcard(
            FlashcardEntity(
                question = "Quelle est la pulsation propre ω0 d'un circuit RLC série ?",
                answer = "ω0 = 1 / √(L*C)",
                subjectId = physId,
                intervalDays = 1,
                nextReviewTimestamp = now - 2000 // Ready for review!
            )
        )
        dao.insertFlashcard(
            FlashcardEntity(
                question = "Quelle est la complexité temporelle pire cas du tri fusion (Merge Sort) ?",
                answer = "O(n * log(n))",
                subjectId = infoId,
                intervalDays = 2,
                nextReviewTimestamp = now + dayMs
            )
        )

        // Mistakes Log
        dao.insertMistake(
            MistakeEntity(
                title = "Oubli de tester l'hypothèse de continuité avant Rolle",
                description = "Dans l'exercice 3 du TD2, j'ai appliqué le théorème de Rolle directement sans mentionner que f était continue sur [a,b] et dérivable sur ]a,b[.",
                category = "cat_forgot_formula",
                lessonLearned = "Toujours écrire explicitement les 3 hypothèses : f continue sur [a,b], dérivable sur ]a,b[, et f(a) = f(b).",
                subjectId = mathId
            )
        )
        dao.insertMistake(
            MistakeEntity(
                title = "Erreur de signe lors de la projection des forces",
                description = "Projection de la force de frottement fluide f = -h*v sur l'axe ascendant Oz.",
                category = "cat_calc_error",
                lessonLearned = "Bien tracer le vecteur vitesse et orienter le repère avant de projeter.",
                subjectId = physId
            )
        )

        // Study Notes
        dao.insertNote(
            StudyNoteEntity(
                title = "Fiche Méthode : Calcul des limites indéterminées par DL",
                content = "1. Vérifier si c'est une forme indéterminée 0/0 ou inf/inf.\n2. Effectuer un DL du numérateur et du dénominateur au même ordre pertinent.\n3. Simplifier le terme de plus bas degré non nul.\n4. Conclure proprement.",
                subjectId = mathId,
                tags = "Analyse, Limites"
            )
        )
        dao.insertNote(
            StudyNoteEntity(
                title = "Conseils colleurs de Physique",
                content = "Faire attention aux unités et à l'homogénéité des formules ! Toujours faire une analyse dimensionnelle rapide.",
                subjectId = physId,
                tags = "Colles, Astuces"
            )
        )

        // Borrowed Books
        dao.insertBook(
            BorrowedBookEntity(
                title = "Tout-en-un Mathématiques MPSI (Dunod)",
                subjectId = mathId,
                borrowedDateTimestamp = now - 5 * dayMs,
                returnDateTimestamp = now + 9 * dayMs,
                notes = "Emprunté à la bibliothèque de la prépa - Excellents exercices corrigés",
                isReturned = false
            )
        )
        dao.insertBook(
            BorrowedBookEntity(
                title = "Précis de Physique MPSI - Mécanique (Bréal)",
                subjectId = physId,
                borrowedDateTimestamp = now - 20 * dayMs,
                returnDateTimestamp = now - 2 * dayMs,
                notes = "Date dépassée - À rendre d'urgence",
                isReturned = false
            )
        )
    }

    // Export all data as JSON String
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Khawla")
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val subList = dao.getAllSubjects().first()
        val subArr = JSONArray()
        subList.forEach { s ->
            val o = JSONObject()
            o.put("id", s.id)
            o.put("nameAr", s.nameAr)
            o.put("nameFr", s.nameFr)
            o.put("code", s.code)
            o.put("colorHex", s.colorHex)
            subArr.put(o)
        }
        root.put("subjects", subArr)

        val taskList = dao.getAllTasks().first()
        val taskArr = JSONArray()
        taskList.forEach { t ->
            val o = JSONObject()
            o.put("title", t.title)
            o.put("desc", t.description)
            o.put("subjectId", t.subjectId)
            o.put("deadline", t.deadlineTimestamp)
            o.put("priority", t.priority)
            o.put("completed", t.isCompleted)
            o.put("type", t.type)
            taskArr.put(o)
        }
        root.put("tasks", taskArr)

        val noteList = dao.getAllNotes().first()
        val noteArr = JSONArray()
        noteList.forEach { n ->
            val o = JSONObject()
            o.put("title", n.title)
            o.put("content", n.content)
            o.put("subjectId", n.subjectId)
            o.put("tags", n.tags)
            noteArr.put(o)
        }
        root.put("notes", noteArr)

        val cardList = dao.getAllFlashcards().first()
        val cardArr = JSONArray()
        cardList.forEach { c ->
            val o = JSONObject()
            o.put("question", c.question)
            o.put("answer", c.answer)
            o.put("subjectId", c.subjectId)
            o.put("intervalDays", c.intervalDays)
            o.put("nextReview", c.nextReviewTimestamp)
            cardArr.put(o)
        }
        root.put("flashcards", cardArr)

        val formList = dao.getAllFormulas().first()
        val formArr = JSONArray()
        formList.forEach { f ->
            val o = JSONObject()
            o.put("name", f.name)
            o.put("expr", f.mathExpression)
            o.put("explanation", f.explanation)
            o.put("example", f.example)
            o.put("subjectId", f.subjectId)
            formArr.put(o)
        }
        root.put("formulas", formArr)

        val bookList = dao.getAllBooks().first()
        val bookArr = JSONArray()
        bookList.forEach { b ->
            val o = JSONObject()
            o.put("title", b.title)
            o.put("subjectId", b.subjectId)
            o.put("borrowedDate", b.borrowedDateTimestamp)
            o.put("returnDate", b.returnDateTimestamp)
            o.put("isReturned", b.isReturned)
            bookArr.put(o)
        }
        root.put("books", bookArr)

        root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("app") || root.getString("app") != "Khawla") {
                return@withContext false
            }

            if (root.has("tasks")) {
                val tasks = root.getJSONArray("tasks")
                for (i in 0 until tasks.length()) {
                    val o = tasks.getJSONObject(i)
                    dao.insertTask(
                        TaskItemEntity(
                            title = o.getString("title"),
                            description = o.optString("desc", ""),
                            subjectId = o.optLong("subjectId", 0),
                            deadlineTimestamp = o.optLong("deadline", System.currentTimeMillis()),
                            priority = o.optInt("priority", 1),
                            isCompleted = o.optBoolean("completed", false),
                            type = o.optInt("type", 0)
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

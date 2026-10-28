package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.localization.AppLanguage
import com.example.localization.S
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import com.example.ui.viewmodel.KhawlaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: KhawlaViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val books by viewModel.books.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val formulas by viewModel.formulas.collectAsState()
    val mistakes by viewModel.mistakes.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Notes, 1: Books, 2: Pre-Exam
    var isAddNoteOpen by remember { mutableStateOf(false) }
    var isAddBookOpen by remember { mutableStateOf(false) }
    var isAddExamOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("library_search_input"),
            placeholder = { Text(S.get("search", language)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        // Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(S.get("tab_notes", language), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(S.get("tab_books", language), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(S.get("tab_pre_exam", language), fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            0 -> NotesTabContent(
                notes = if (searchQuery.isBlank()) notes else notes.filter {
                    it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true) || it.tags.contains(searchQuery, true)
                },
                subjects = subjects,
                language = language,
                onDelete = { viewModel.deleteNote(it) },
                onOpenAdd = { isAddNoteOpen = true }
            )
            1 -> BooksTabContent(
                books = if (searchQuery.isBlank()) books else books.filter {
                    it.title.contains(searchQuery, true) || it.notes.contains(searchQuery, true)
                },
                subjects = subjects,
                language = language,
                onToggleReturned = { viewModel.toggleBookReturned(it) },
                onDelete = { viewModel.deleteBook(it) },
                onOpenAdd = { isAddBookOpen = true }
            )
            2 -> PreExamTabContent(
                exams = exams,
                subjects = subjects,
                chapters = chapters,
                formulas = formulas,
                mistakes = mistakes,
                flashcards = flashcards,
                language = language,
                onOpenAddExam = { isAddExamOpen = true },
                onDeleteExam = { viewModel.deleteExam(it) }
            )
        }
    }

    if (isAddNoteOpen) {
        AddNoteDialog(
            language = language,
            subjects = subjects,
            onDismiss = { isAddNoteOpen = false },
            onAdd = { title, content, subId, tags ->
                viewModel.addNote(title, content, subId, tags)
                isAddNoteOpen = false
            }
        )
    }

    if (isAddBookOpen) {
        AddBookDialog(
            language = language,
            subjects = subjects,
            onDismiss = { isAddBookOpen = false },
            onAdd = { title, subId, returnDate, notesStr ->
                viewModel.addBook(title, subId, returnDate, notesStr)
                isAddBookOpen = false
            }
        )
    }

    if (isAddExamOpen) {
        AddExamDialog(
            language = language,
            subjects = subjects,
            onDismiss = { isAddExamOpen = false },
            onAdd = { title, subId, examTimestamp, notesStr ->
                viewModel.addExam(title, subId, examTimestamp, notesStr)
                isAddExamOpen = false
            }
        )
    }
}

@Composable
fun NotesTabContent(
    notes: List<StudyNoteEntity>,
    subjects: List<SubjectEntity>,
    language: AppLanguage,
    onDelete: (StudyNoteEntity) -> Unit,
    onOpenAdd: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${S.get("tab_notes", language)} (${notes.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                    onClick = onOpenAdd,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = PalePink)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = S.get("add_note", language), color = DeepLavender)
                }
            }
        }

        if (notes.isEmpty()) {
            item {
                EmptyStateView(icon = Icons.Default.Description, message = S.get("no_data", language))
            }
        } else {
            items(notes, key = { it.id }) { note ->
                val sub = subjects.find { it.id == note.subjectId }
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onDelete(note) }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = MutedText, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (sub != null) {
                            val name = if (language == AppLanguage.ARABIC) sub.nameAr else sub.nameFr
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (note.tags.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = SoftCardBg) {
                                Text(
                                    text = "#${note.tags}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BooksTabContent(
    books: List<BorrowedBookEntity>,
    subjects: List<SubjectEntity>,
    language: AppLanguage,
    onToggleReturned: (BorrowedBookEntity) -> Unit,
    onDelete: (BorrowedBookEntity) -> Unit,
    onOpenAdd: () -> Unit
) {
    val dateLocale = if (language == AppLanguage.ARABIC) Locale("ar") else Locale.FRENCH
    val dateFormat = SimpleDateFormat("dd MMM yyyy", dateLocale)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${S.get("tab_books", language)} (${books.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                    onClick = onOpenAdd,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = PalePink)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = S.get("add_book", language), color = DeepLavender)
                }
            }
        }

        if (books.isEmpty()) {
            item {
                EmptyStateView(icon = Icons.Default.MenuBook, message = S.get("no_data", language))
            }
        } else {
            items(books, key = { it.id }) { book ->
                val sub = subjects.find { it.id == book.subjectId }
                val returnStr = dateFormat.format(Date(book.returnDateTimestamp))
                val isOverdue = !book.isReturned && book.returnDateTimestamp < System.currentTimeMillis()

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (book.isReturned) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onDelete(book) }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = MutedText, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (sub != null) {
                            val name = if (language == AppLanguage.ARABIC) sub.nameAr else sub.nameFr
                            Text(text = name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }

                        if (book.notes.isNotBlank()) {
                            Text(text = book.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isOverdue) GentleError else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${S.get("book_return_date", language)}: $returnStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverdue) GentleError else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            FilterChip(
                                selected = book.isReturned,
                                onClick = { onToggleReturned(book) },
                                label = {
                                    Text(if (book.isReturned) S.get("book_returned", language) else S.get("mark_returned", language))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (book.isReturned) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreExamTabContent(
    exams: List<ExamEntity>,
    subjects: List<SubjectEntity>,
    chapters: List<ChapterEntity>,
    formulas: List<FormulaEntity>,
    mistakes: List<MistakeEntity>,
    flashcards: List<FlashcardEntity>,
    language: AppLanguage,
    onOpenAddExam: () -> Unit,
    onDeleteExam: (ExamEntity) -> Unit
) {
    var selectedExamId by remember { mutableStateOf(exams.firstOrNull()?.id) }
    val selectedExam = exams.find { it.id == selectedExamId } ?: exams.firstOrNull()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = S.get("pre_exam_select", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                    onClick = onOpenAddExam,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = PalePink)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Examen", color = DeepLavender)
                }
            }
        }

        // Exam selector row
        if (exams.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(exams) { ex ->
                        val isSel = ex.id == (selectedExam?.id ?: 0L)
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedExamId = ex.id },
                            label = { Text(ex.title, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PalePink,
                                selectedLabelColor = DeepLavender
                            )
                        )
                    }
                }
            }
        }

        if (selectedExam == null) {
            item {
                EmptyStateView(icon = Icons.Default.EventNote, message = S.get("no_data", language))
            }
        } else {
            val examSubject = subjects.find { it.id == selectedExam.subjectId }
            val relevantChapters = chapters.filter { it.subjectId == selectedExam.subjectId }
            val relevantFormulas = formulas.filter { it.subjectId == selectedExam.subjectId }
            val relevantMistakes = mistakes.filter { it.subjectId == selectedExam.subjectId }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PalePink.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = selectedExam.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepLavender
                        )
                        if (selectedExam.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedExam.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MainText
                            )
                        }
                    }
                }
            }

            // Checklist of Chapters
            item {
                Text(
                    text = "${S.get("chapters_count", language)} (${relevantChapters.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(relevantChapters, key = { "chapter-${it.id}" }) { ch ->
                val (stText, stCol) = when (ch.status) {
                    2 -> Pair(S.get("status_understood", language), GentleSuccess)
                    1 -> Pair(S.get("status_learning", language), GentleWarning)
                    else -> Pair(S.get("status_needs_review", language), GentleError)
                }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = ch.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(8.dp), color = stCol.copy(alpha = 0.2f)) {
                            Text(
                                text = stText,
                                style = MaterialTheme.typography.labelSmall,
                                color = stCol,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Formulas checklist
            if (relevantFormulas.isNotEmpty()) {
                item {
                    Text(
                        text = "${S.get("tab_formulas", language)} (${relevantFormulas.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(relevantFormulas, key = { "formula-${it.id}" }) { f ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = f.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = f.mathExpression, style = MaterialTheme.typography.bodySmall, color = DeepLavender)
                        }
                    }
                }
            }

            // Mistakes to avoid
            if (relevantMistakes.isNotEmpty()) {
                item {
                    Text(
                        text = "${S.get("tab_mistakes", language)} (${relevantMistakes.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(relevantMistakes, key = { "mistake-${it.id}" }) { m ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = m.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = m.lessonLearned, style = MaterialTheme.typography.bodySmall, color = GentleSuccess)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddNoteDialog(
    language: AppLanguage,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onAdd: (title: String, content: String, subId: Long, tags: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = S.get("add_note", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(S.get("note_title", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(S.get("note_content", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text(S.get("note_tags", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(S.get("cancel", language)) }
                    Button(
                        onClick = { if (title.isNotBlank()) onAdd(title, content, selectedSubjectId, tags) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(S.get("save", language))
                    }
                }
            }
        }
    }
}

@Composable
fun AddBookDialog(
    language: AppLanguage,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onAdd: (title: String, subId: Long, returnDate: Long, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notesStr by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = S.get("add_book", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(S.get("book_title", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesStr,
                    onValueChange = { notesStr = it },
                    label = { Text(S.get("task_desc", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(S.get("cancel", language)) }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(
                                    title,
                                    selectedSubjectId,
                                    System.currentTimeMillis() + 14L * 24 * 3600 * 1000,
                                    notesStr
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(S.get("save", language))
                    }
                }
            }
        }
    }
}

@Composable
fun AddExamDialog(
    language: AppLanguage,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onAdd: (title: String, subId: Long, examTimestamp: Long, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notesStr by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }
    var daysFromNow by remember { mutableStateOf(14) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Examen / Concours", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de l'épreuve") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesStr,
                    onValueChange = { notesStr = it },
                    label = { Text("Remarques / Programme") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(S.get("cancel", language)) }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(
                                    title,
                                    selectedSubjectId,
                                    System.currentTimeMillis() + daysFromNow * 24L * 3600 * 1000,
                                    notesStr
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(S.get("save", language))
                    }
                }
            }
        }
    }
}

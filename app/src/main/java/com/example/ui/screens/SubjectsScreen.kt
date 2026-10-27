package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.KhawlaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    viewModel: KhawlaViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isAddSubjectDialogOpen by remember { mutableStateOf(false) }
    var isAddChapterDialogOpen by remember { mutableStateOf(false) }

    val selectedSubject = subjects.find { it.id == selectedSubjectId }

    if (selectedSubject != null) {
        // Detailed Chapter View for selected subject
        SubjectDetailView(
            subject = selectedSubject,
            chapters = chapters.filter { it.subjectId == selectedSubject.id },
            language = language,
            onBack = { viewModel.selectSubject(null) },
            onUpdateChapterStatus = { ch, status -> viewModel.updateChapterStatus(ch, status) },
            onDeleteChapter = { ch -> viewModel.deleteChapter(ch) },
            onOpenAddChapter = { isAddChapterDialogOpen = true },
            onDeleteSubject = {
                viewModel.deleteSubject(selectedSubject)
            }
        )

        if (isAddChapterDialogOpen) {
            AddChapterDialog(
                language = language,
                onDismiss = { isAddChapterDialogOpen = false },
                onAdd = { title, desc, status ->
                    viewModel.addChapter(selectedSubject.id, title, desc, status)
                    isAddChapterDialogOpen = false
                }
            )
        }
    } else {
        // List of all Subjects
        val filteredSubjects = if (searchQuery.isBlank()) {
            subjects
        } else {
            subjects.filter {
                it.nameAr.contains(searchQuery, ignoreCase = true) ||
                it.nameFr.contains(searchQuery, ignoreCase = true) ||
                it.code.contains(searchQuery, ignoreCase = true)
            }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
        ) {
            // Search Bar & Add Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_subjects_input"),
                        placeholder = { Text(S.get("search_subjects_hint", language)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    FilledIconButton(
                        onClick = { isAddSubjectDialogOpen = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("add_subject_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = S.get("add_subject", language))
                    }
                }
            }

            item {
                SectionHeader(title = S.get("all_subjects", language))
            }

            if (filteredSubjects.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.School,
                        message = S.get("no_data", language)
                    )
                }
            } else {
                items(filteredSubjects, key = { it.id }) { subject ->
                    val subjectChapters = chapters.filter { it.subjectId == subject.id }
                    val understoodCount = subjectChapters.count { it.status == 2 }
                    val progress = if (subjectChapters.isNotEmpty()) understoodCount.toFloat() / subjectChapters.size else 0f

                    SubjectCard(
                        subject = subject,
                        chaptersCount = subjectChapters.size,
                        understoodCount = understoodCount,
                        progress = progress,
                        language = language,
                        onClick = { viewModel.selectSubject(subject.id) }
                    )
                }
            }
        }

        if (isAddSubjectDialogOpen) {
            AddSubjectDialog(
                language = language,
                onDismiss = { isAddSubjectDialogOpen = false },
                onAdd = { nameAr, nameFr, code, colorHex ->
                    viewModel.addSubject(nameAr, nameFr, code, colorHex)
                    isAddSubjectDialogOpen = false
                }
            )
        }
    }
}

@Composable
fun SubjectCard(
    subject: SubjectEntity,
    chaptersCount: Int,
    understoodCount: Int,
    progress: Float,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val subColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (e: Exception) {
        DeepLavender
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(subColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = subColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (language == AppLanguage.ARABIC) subject.nameAr else subject.nameFr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subject.code,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$understoodCount/$chaptersCount ${S.get("status_understood", language)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = subColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = subColor,
                trackColor = subColor.copy(alpha = 0.18f)
            )
        }
    }
}

@Composable
fun SubjectDetailView(
    subject: SubjectEntity,
    chapters: List<ChapterEntity>,
    language: AppLanguage,
    onBack: () -> Unit,
    onUpdateChapterStatus: (ChapterEntity, Int) -> Unit,
    onDeleteChapter: (ChapterEntity) -> Unit,
    onOpenAddChapter: () -> Unit,
    onDeleteSubject: () -> Unit
) {
    val subColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (e: Exception) {
        DeepLavender
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = if (language == AppLanguage.ARABIC) subject.nameAr else subject.nameFr,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Subject",
                        tint = GentleError
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = subColor.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = subject.code,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = subColor
                        )
                        Text(
                            text = "${chapters.size} ${S.get("chapters_count", language)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = onOpenAddChapter,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = subColor)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = S.get("add_chapter", language), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item {
            SectionHeader(title = S.get("chapters_count", language))
        }

        if (chapters.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.MenuBook,
                    message = S.get("no_data", language)
                )
            }
        } else {
            items(chapters, key = { it.id }) { chapter ->
                ChapterRowCard(
                    chapter = chapter,
                    language = language,
                    onStatusChange = { newStatus -> onUpdateChapterStatus(chapter, newStatus) },
                    onDelete = { onDeleteChapter(chapter) }
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(S.get("delete_subject", language)) },
            text = { Text(S.get("confirm_delete", language)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteSubject()
                    }
                ) {
                    Text(S.get("delete", language), color = GentleError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(S.get("cancel", language))
                }
            }
        )
    }
}

@Composable
fun ChapterRowCard(
    chapter: ChapterEntity,
    language: AppLanguage,
    onStatusChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val (statusLabelKey, statusColor) = when (chapter.status) {
        1 -> Pair("status_learning", GentleWarning)
        2 -> Pair("status_understood", GentleSuccess)
        3 -> Pair("status_needs_review", GentleError)
        else -> Pair("status_not_started", MutedText)
    }

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
                    text = chapter.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Chapter",
                        tint = MutedText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (chapter.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chapter.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    0 to "status_not_started",
                    1 to "status_learning",
                    2 to "status_understood",
                    3 to "status_needs_review"
                ).forEach { (st, labelKey) ->
                    val isCur = chapter.status == st
                    val pillColor = when (st) {
                        1 -> GentleWarning
                        2 -> GentleSuccess
                        3 -> GentleError
                        else -> MutedText
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCur) pillColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isCur) androidx.compose.foundation.BorderStroke(1.dp, pillColor) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onStatusChange(st) }
                    ) {
                        Text(
                            text = S.get(labelKey, language),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isCur) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCur) pillColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddSubjectDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onAdd: (nameAr: String, nameFr: String, code: String, colorHex: String) -> Unit
) {
    var nameAr by remember { mutableStateOf("") }
    var nameFr by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#8066B5") }

    val presetColors = listOf("#8066B5", "#5E81AC", "#4CAE75", "#E06C75", "#D08770", "#B48EAD")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = S.get("add_subject", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("الاسم بالعربية") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nameFr,
                    onValueChange = { nameFr = it },
                    label = { Text("Nom en Français") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(S.get("subject_code", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Couleur / اللون",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { hex ->
                        val col = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 0.dp,
                                    color = if (selectedColor == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(S.get("cancel", language))
                    }
                    Button(
                        onClick = {
                            if (nameAr.isNotBlank() || nameFr.isNotBlank()) {
                                onAdd(
                                    nameAr.ifBlank { nameFr },
                                    nameFr.ifBlank { nameAr },
                                    code.ifBlank { "MPSI" },
                                    selectedColor
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
fun AddChapterDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onAdd: (title: String, desc: String, status: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = S.get("add_chapter", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(S.get("chapter_title", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(S.get("chapter_desc", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(S.get("cancel", language))
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(title, desc, status)
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

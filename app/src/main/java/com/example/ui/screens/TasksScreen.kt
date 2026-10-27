package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SubjectEntity
import com.example.data.model.TaskItemEntity
import com.example.localization.AppLanguage
import com.example.localization.S
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.KhawlaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(
    viewModel: KhawlaViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var selectedFilter by remember { mutableStateOf("all") }
    var isAddTaskDialogOpen by remember { mutableStateOf(false) }

    val filteredTasks = tasks.filter {
        when (selectedFilter) {
            "pending" -> !it.isCompleted
            "completed" -> it.isCompleted
            "urgent" -> !it.isCompleted && it.priority == 2
            else -> true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val filters = listOf(
                        "all" to "filter_all",
                        "pending" to "filter_pending",
                        "urgent" to "filter_urgent",
                        "completed" to "filter_completed"
                    )
                    items(filters) { (fKey, labelKey) ->
                        FilterChip(
                            selected = selectedFilter == fKey,
                            onClick = { selectedFilter = fKey },
                            label = { Text(S.get(labelKey, language), style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PalePink,
                                selectedLabelColor = DeepLavender
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = { isAddTaskDialogOpen = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("add_task_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = S.get("add_task", language))
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Checklist,
                    message = S.get("no_data", language)
                )
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                val subject = subjects.find { it.id == task.subjectId }
                TaskDetailRowCard(
                    task = task,
                    subject = subject,
                    language = language,
                    onToggle = { viewModel.toggleTaskCompleted(task) },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
        }
    }

    if (isAddTaskDialogOpen) {
        AddTaskDialog(
            language = language,
            subjects = subjects,
            onDismiss = { isAddTaskDialogOpen = false },
            onAdd = { title, desc, subjectId, deadline, priority, type ->
                viewModel.addTask(title, desc, subjectId, deadline, priority, type)
                isAddTaskDialogOpen = false
            }
        )
    }
}

@Composable
fun TaskDetailRowCard(
    task: TaskItemEntity,
    subject: SubjectEntity?,
    language: AppLanguage,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateLocale = if (language == AppLanguage.ARABIC) Locale("ar") else Locale.FRENCH
    val dateFormat = SimpleDateFormat("dd MMM", dateLocale)
    val deadlineStr = if (task.deadlineTimestamp > 0) dateFormat.format(Date(task.deadlineTimestamp)) else null

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = GentleSuccess)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (subject != null) {
                        val subName = if (language == AppLanguage.ARABIC) subject.nameAr else subject.nameFr
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PalePink.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = subName,
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepLavender,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (deadlineStr != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = deadlineStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MutedText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    language: AppLanguage,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onAdd: (title: String, desc: String, subjectId: Long, deadline: Long, priority: Int, type: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }
    var priority by remember { mutableStateOf(1) }
    var type by remember { mutableStateOf(0) }

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
                    text = S.get("add_task", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(S.get("task_title", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(S.get("task_desc", language)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (subjects.isNotEmpty()) {
                    Text(text = S.get("task_subject", language), style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subjects) { sub ->
                            val isSel = sub.id == selectedSubjectId
                            val name = if (language == AppLanguage.ARABIC) sub.nameAr else sub.nameFr
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedSubjectId = sub.id },
                                label = { Text(name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PalePink,
                                    selectedLabelColor = DeepLavender
                                )
                            )
                        }
                    }
                }

                Text(text = S.get("task_priority", language), style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        0 to "priority_low",
                        1 to "priority_medium",
                        2 to "priority_high"
                    ).forEach { (p, labelKey) ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(S.get(labelKey, language)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (p == 2) GentleWarning.copy(alpha = 0.2f) else PalePink
                            )
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
                            if (title.isNotBlank()) {
                                onAdd(
                                    title,
                                    desc,
                                    selectedSubjectId,
                                    System.currentTimeMillis() + 24L * 3600 * 1000,
                                    priority,
                                    type
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

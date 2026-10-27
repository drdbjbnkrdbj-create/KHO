package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.*
import com.example.localization.AppLanguage
import com.example.localization.S
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.KhawlaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: KhawlaViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val focusSessions by viewModel.focusSessions.collectAsState()
    val activeMoodMode by viewModel.activeMoodMode.collectAsState()
    val isRescueDialogOpen by viewModel.isRescueDialogOpen.collectAsState()
    val rescuePlan by viewModel.rescuePlan.collectAsState()

    val pendingTasks = tasks.filter { !it.isCompleted }
    val completedTasksCount = tasks.count { it.isCompleted }
    val understoodChaptersCount = chapters.count { it.status == 2 }
    val dueFlashcards = flashcards.filter { it.nextReviewTimestamp <= System.currentTimeMillis() }

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingKey = when {
        hour < 12 -> "greeting_morning"
        hour < 18 -> "greeting_afternoon"
        else -> "greeting_evening"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // Greeting & Omar Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = S.get(greetingKey, language),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = S.get("quote_of_the_day", language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 20.sp
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.img_omar_sticker_welcome),
                        contentDescription = "Omar",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }

        // 3 Quick Mood Modes
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = S.get("quick_modes_title", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoodModeCard(
                        title = S.get("mode_organized", language),
                        icon = Icons.Default.AutoAwesome,
                        isSelected = activeMoodMode == "organized",
                        color = DeepLavender,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setMoodMode("organized") }
                    )
                    MoodModeCard(
                        title = S.get("mode_busy", language),
                        icon = Icons.Default.Bolt,
                        isSelected = activeMoodMode == "busy",
                        color = GentleWarning,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setMoodMode("busy") }
                    )
                    MoodModeCard(
                        title = S.get("mode_unmotivated", language),
                        icon = Icons.Default.Spa,
                        isSelected = activeMoodMode == "unmotivated",
                        color = GentleSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setMoodMode("unmotivated") }
                    )
                }
            }
        }

        // Active Mood Mode banner if chosen
        if (activeMoodMode != null) {
            item {
                MoodBanner(
                    mode = activeMoodMode!!,
                    language = language,
                    onOpenRescue = { viewModel.setRescueDialogOpen(true) },
                    onStartFocus = { viewModel.navigateTo(5) }
                )
            }
        }

        // Rescue Mode Action Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PalePink),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setRescueDialogOpen(true) }
                    .testTag("rescue_mode_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_omar_sticker_encourage),
                        contentDescription = "Omar encourage",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = S.get("rescue_button", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepLavender
                        )
                        Text(
                            text = S.get("rescue_desc", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MainText
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = DeepLavender
                    )
                }
            }
        }

        // Progress Summary (4 tiles)
        item {
            Column {
                SectionHeader(title = S.get("progress_summary", language))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProgressTile(
                        count = completedTasksCount.toString(),
                        label = S.get("tasks_done", language),
                        icon = Icons.Default.CheckCircle,
                        color = GentleSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    ProgressTile(
                        count = dueFlashcards.size.toString(),
                        label = S.get("cards_due_today", language),
                        icon = Icons.Default.Psychology,
                        color = DeepLavender,
                        modifier = Modifier.weight(1f)
                    )
                    ProgressTile(
                        count = focusSessions.size.toString(),
                        label = S.get("focus_sessions", language),
                        icon = Icons.Default.Timer,
                        color = GentleWarning,
                        modifier = Modifier.weight(1f)
                    )
                    ProgressTile(
                        count = understoodChaptersCount.toString(),
                        label = S.get("chapters_understood", language),
                        icon = Icons.Default.MenuBook,
                        color = GentleInfo,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Today's Priorities (filtered based on active mood mode)
        val displayedTasks = when (activeMoodMode) {
            "busy" -> pendingTasks.filter { it.priority == 2 }.take(3)
            "unmotivated" -> pendingTasks.sortedBy { it.priority }.take(1)
            else -> pendingTasks.take(4)
        }

        item {
            SectionHeader(
                title = S.get("today_priorities", language),
                actionText = S.get("nav_tasks", language),
                onActionClick = { viewModel.navigateTo(2) }
            )
        }

        if (displayedTasks.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.CheckCircleOutline,
                    message = S.get("cards_empty", language)
                )
            }
        } else {
            items(displayedTasks, key = { it.id }) { task ->
                val subject = subjects.find { it.id == task.subjectId }
                TaskItemRow(
                    task = task,
                    subject = subject,
                    language = language,
                    onToggle = { viewModel.toggleTaskCompleted(task) }
                )
            }
        }

        // Upcoming Exams & Deadlines
        val upcomingExams = exams.take(2)
        if (upcomingExams.isNotEmpty()) {
            item {
                SectionHeader(
                    title = S.get("upcoming_exams", language),
                    actionText = S.get("nav_library", language),
                    onActionClick = { viewModel.navigateTo(4) }
                )
            }
            items(upcomingExams, key = { it.id }) { exam ->
                val subject = subjects.find { it.id == exam.subjectId }
                ExamCardRow(exam = exam, subject = subject, language = language)
            }
        }
    }

    // Rescue Dialog Modal
    if (isRescueDialogOpen) {
        RescueModeModal(
            viewModel = viewModel,
            language = language,
            plan = rescuePlan,
            onDismiss = { viewModel.setRescueDialogOpen(false) }
        )
    }
}

@Composable
fun MoodModeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MoodBanner(
    mode: String,
    language: AppLanguage,
    onOpenRescue: () -> Unit,
    onStartFocus: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val (msg, buttonText, action) = when (mode) {
                "busy" -> Triple(
                    if (language == AppLanguage.ARABIC) "قائمة مهامك الآن مختصرة على الأولويات العاجلة فقط لتخفيف العبء."
                    else "Ta liste est filtrée sur les urgences absolues pour ne pas te surcharger.",
                    S.get("rescue_button", language),
                    onOpenRescue
                )
                "unmotivated" -> Triple(
                    if (language == AppLanguage.ARABIC) "لا بأس يا خولة، خطوة واحدة دقيقة أو قراءة ورقة تكفي. لا تجبري نفسك على المستحيل."
                    else "C'est normal Khawla. Faisons une micro-session de 5 min ou lis une seule page sans pression.",
                    S.get("focus_title", language),
                    onStartFocus
                )
                else -> Triple(
                    if (language == AppLanguage.ARABIC) "أنتِ في أفضل حالاتك! حددي 3 مهام أساسية وابدئي بها."
                    else "Tu es au top ! Sélectionne tes 3 tâches clés et lance ta journée.",
                    S.get("focus_title", language),
                    onStartFocus
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = action,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProgressTile(
    count: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TaskItemRow(
    task: TaskItemEntity,
    subject: SubjectEntity?,
    language: AppLanguage,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
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
                if (subject != null) {
                    val subName = if (language == AppLanguage.ARABIC) subject.nameAr else subject.nameFr
                    Text(
                        text = subName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (task.priority == 2) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GentleWarning.copy(alpha = 0.2f),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = S.get("filter_urgent", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = GentleWarning,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ExamCardRow(
    exam: ExamEntity,
    subject: SubjectEntity?,
    language: AppLanguage
) {
    val dateLocale = if (language == AppLanguage.ARABIC) Locale("ar") else Locale.FRENCH
    val dateFormat = SimpleDateFormat("dd MMM yyyy", dateLocale)
    val examDateStr = remember(exam.examTimestamp) { dateFormat.format(Date(exam.examTimestamp)) }
    val daysLeft = ((exam.examTimestamp - System.currentTimeMillis()) / (24L * 3600 * 1000)).coerceAtLeast(0)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subject != null) {
                    val name = if (language == AppLanguage.ARABIC) subject.nameAr else subject.nameFr
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = examDateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PalePink
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$daysLeft",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepLavender
                    )
                    Text(
                        text = if (language == AppLanguage.ARABIC) "يوم" else "j",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepLavender
                    )
                }
            }
        }
    }
}

@Composable
fun RescueModeModal(
    viewModel: KhawlaViewModel,
    language: AppLanguage,
    plan: com.example.ui.viewmodel.RescuePlan?,
    onDismiss: () -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(15) }
    var selectedEnergy by remember { mutableStateOf("medium") } // low, medium, high
    val subjects by viewModel.subjects.collectAsState()
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_omar_sticker_heart),
                            contentDescription = "Omar",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                        Text(
                            text = S.get("rescue_title", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepLavender
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (plan == null) {
                    // Step 1: Input questions
                    Text(
                        text = S.get("rescue_desc", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = S.get("rescue_time_question", language),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 15, 30, 60).forEach { mins ->
                            FilterChip(
                                selected = selectedMinutes == mins,
                                onClick = { selectedMinutes = mins },
                                label = { Text("$mins min") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PalePink,
                                    selectedLabelColor = DeepLavender
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = S.get("rescue_energy_question", language),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("low" to "energy_low", "medium" to "energy_medium", "high" to "energy_high")
                            .forEach { (lvl, labelKey) ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedEnergy == lvl) PalePink else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedEnergy = lvl }
                                        .padding(2.dp)
                                ) {
                                    Text(
                                        text = S.get(labelKey, language),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp),
                                        color = if (selectedEnergy == lvl) DeepLavender else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.generateRescuePlan(selectedMinutes, selectedEnergy, selectedSubjectId)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepLavender),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = S.get("start_now", language),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Step 2: Generated Rescue Plan
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PalePink.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = plan.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepLavender
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = plan.taskTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MainText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = plan.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftCardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = DeepLavender,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = plan.omarAdvice,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MainText,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onDismiss()
                            viewModel.navigateTo(5) // Navigate to Focus Timer!
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepLavender),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${S.get("timer_start", language)} (${plan.durationMinutes} min)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
